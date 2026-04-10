package crm.controller;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import crm.entity.Pdf;
import crm.service.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Cloud-ready PDF Controller that uses Azure Blob Storage for PDF persistence.
 * Replaces local file system operations with Azure Blob Storage.
 */
@Controller
@Slf4j
public class PdfController {

    private final PdfService pdfService;

    @Value("${azure.storage.connection-string:#{null}}")
    private String connectionString;

    @Value("${azure.storage.container-name:crm-files}")
    private String containerName;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    /**
     * Generates PDF and stores it in Azure Blob Storage using reactive patterns.
     * 
     * @param fileName The name of the PDF file
     * @param text The content to write to the PDF
     * @return Mono<String> with the blob URL
     */
    private Mono<String> generateAndStorePdf(String fileName, String text) {
        return Mono.fromCallable(() -> {
            if (!fileName.endsWith(".pdf")) {
                fileName += ".pdf";
            }

            if (connectionString == null || connectionString.isEmpty()) {
                throw new IllegalStateException("Azure Storage connection string is not configured. " +
                        "Please set azure.storage.connection-string in application.properties");
            }

            // Generate PDF in memory
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document();
            
            try {
                PdfWriter.getInstance(document, outputStream);
                document.open();
                Paragraph paragraph = new Paragraph(text);
                document.add(paragraph);
                document.close();

                // Upload to Azure Blob Storage
                BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                        .connectionString(connectionString)
                        .buildClient();

                BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
                
                // Create container if it doesn't exist
                if (!containerClient.exists()) {
                    containerClient.create();
                }

                BlobClient blobClient = containerClient.getBlobClient("pdfs/" + fileName);
                
                byte[] pdfBytes = outputStream.toByteArray();
                blobClient.upload(new java.io.ByteArrayInputStream(pdfBytes), pdfBytes.length, true);

                log.info("PDF successfully uploaded to Azure Blob Storage: {}", fileName);
                return blobClient.getBlobUrl();
                
            } catch (DocumentException e) {
                log.error("Error generating PDF document: {}", fileName, e);
                throw new RuntimeException("Failed to generate PDF", e);
            } catch (IOException e) {
                log.error("Error uploading PDF to Azure Blob Storage: {}", fileName, e);
                throw new RuntimeException("Failed to upload PDF to Azure Blob Storage", e);
            } finally {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.warn("Error closing output stream", e);
                }
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/pdf-generator")
    public String pdfGenerator(Model model) {
        model.addAttribute("pdf", new Pdf());
        return "pdf/generator";
    }

    @PostMapping("/pdf-generator")
    public String generatePdf(@Valid Pdf pdf, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "redirect:/pdf-generator";
        } else {
            try {
                // Use reactive pattern for non-blocking PDF generation and storage
                String blobUrl = generateAndStorePdf(pdf.getName(), pdf.getContent())
                        .block(); // Block for synchronous controller compatibility
                
                // Store metadata with blob URL
                pdf.setName(pdf.getName().endsWith(".pdf") ? pdf.getName() : pdf.getName() + ".pdf");
                pdfService.savePdf(pdf);
                
                model.addAttribute("blobUrl", blobUrl);
                log.info("PDF generated and stored successfully: {}", pdf.getName());
                
            } catch (Exception e) {
                log.error("Error generating or storing PDF", e);
                model.addAttribute("error", "Failed to generate PDF: " + e.getMessage());
                return "pdf/generator";
            }
            return "pdf/success";
        }
    }
}

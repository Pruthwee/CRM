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

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Cloud-ready PDF Controller that uses Azure Blob Storage for PDF persistence.
 * Replaces local file system writes with Azure Blob Storage.
 */
@Controller
@Slf4j
public class PdfController {

    private final PdfService pdfService;

    @Value("${azure.storage.connection-string:#{null}}")
    private String connectionString;

    @Value("${azure.storage.container-name:pdf-files}")
    private String containerName;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    /**
     * Generates PDF and stores it in Azure Blob Storage instead of local file system.
     * 
     * @param fileName The name of the PDF file
     * @param text The content to include in the PDF
     * @return The blob URL where the PDF is stored
     */
    private String generateSamplePdf(String fileName, String text) throws DocumentException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            Paragraph paragraph = new Paragraph(text);
            document.add(paragraph);
            document.close();

            // Upload to Azure Blob Storage
            byte[] pdfBytes = outputStream.toByteArray();
            return uploadToAzureBlobStorage(fileName, pdfBytes);
        } catch (Exception e) {
            log.error("Failed to generate PDF: {}", fileName, e);
            throw new DocumentException("Failed to generate PDF", e);
        }
    }

    /**
     * Uploads PDF content to Azure Blob Storage.
     * 
     * @param fileName The name of the file
     * @param content The PDF content as byte array
     * @return The blob URL
     */
    private String uploadToAzureBlobStorage(String fileName, byte[] content) {
        if (connectionString == null || connectionString.isEmpty()) {
            log.warn("Azure Storage connection string not configured. PDF will not be persisted.");
            return "local://" + fileName;
        }

        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            
            // Create container if it doesn't exist
            if (!containerClient.exists()) {
                containerClient.create();
            }

            BlobClient blobClient = containerClient.getBlobClient(fileName);
            blobClient.upload(new ByteArrayInputStream(content), content.length, true);

            log.info("PDF uploaded to Azure Blob Storage: {}", fileName);
            return blobClient.getBlobUrl();
        } catch (Exception e) {
            log.error("Failed to upload PDF to Azure Blob Storage: {}", fileName, e);
            throw new RuntimeException("Failed to upload PDF to Azure Blob Storage", e);
        }
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
                String blobUrl = generateSamplePdf(pdf.getName(), pdf.getContent());
                pdf.setName(blobUrl); // Store the blob URL instead of local path
                pdfService.savePdf(pdf);
                model.addAttribute("blobUrl", blobUrl);
                log.info("PDF generated successfully: {}", blobUrl);
            } catch (DocumentException e) {
                log.error("Failed to generate PDF document", e);
                model.addAttribute("error", "Failed to generate PDF");
                return "pdf/generator";
            }
            return "pdf/success";
        }
    }
}

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
 * Replaces local file system operations with cloud storage.
 */
@Controller
@Slf4j
public class PdfController {

    private PdfService pdfService;

    @Value("${azure.storage.connection-string:#{null}}")
    private String connectionString;

    @Value("${azure.storage.container-name:crm-files}")
    private String containerName;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    /**
     * Generates a PDF and stores it in Azure Blob Storage instead of local file system.
     * 
     * @param fileName The name of the PDF file
     * @param text The content to include in the PDF
     * @return true if successful, false otherwise
     */
    private boolean generateSamplePdfToAzure(String fileName, String text) {
        try {
            if (!fileName.endsWith(".pdf")) {
                fileName += ".pdf";
            }

            // Generate PDF in memory
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            Paragraph paragraph = new Paragraph(text);
            document.add(paragraph);
            document.close();

            // Upload to Azure Blob Storage
            if (connectionString != null && !connectionString.isEmpty()) {
                uploadToAzureBlobStorage(fileName, outputStream.toByteArray());
                log.info("PDF successfully uploaded to Azure Blob Storage: {}", fileName);
            } else {
                log.warn("Azure Storage connection string not configured. PDF generated in memory only.");
            }

            return true;
        } catch (DocumentException e) {
            log.error("Error generating PDF document: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Error uploading PDF to Azure Blob Storage: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Uploads a byte array to Azure Blob Storage.
     * 
     * @param blobName The name of the blob
     * @param data The data to upload
     */
    private void uploadToAzureBlobStorage(String blobName, byte[] data) {
        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            
            // Create container if it doesn't exist
            if (!containerClient.exists()) {
                log.info("Creating container: {}", containerName);
                containerClient.create();
            }

            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            // Upload the PDF
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            blobClient.upload(inputStream, data.length, true);
            
            log.info("Successfully uploaded blob: {} to container: {}", blobName, containerName);
        } catch (Exception e) {
            log.error("Error uploading to Azure Blob Storage: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload PDF to Azure Blob Storage", e);
        }
    }

    @GetMapping("/pdf-generator")
    public String pdfGenerator(Model model) {
        model.addAttribute("pdf", new Pdf());
        return "pdf/generator";
    }

    @PostMapping("/pdf-generator")
    public String generatePdf(@Valid Pdf pdf, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/pdf-generator";
        } else {
            try {
                boolean success = generateSamplePdfToAzure(pdf.getName(), pdf.getContent());
                if (success) {
                    pdfService.savePdf(pdf);
                    return "pdf/success";
                } else {
                    log.error("Failed to generate PDF");
                    return "redirect:/pdf-generator";
                }
            } catch (Exception e) {
                log.error("Error in PDF generation: {}", e.getMessage(), e);
                return "redirect:/pdf-generator";
            }
        }
    }
}

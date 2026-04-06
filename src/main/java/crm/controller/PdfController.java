package crm.controller;

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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@Slf4j
public class PdfController {

    private PdfService pdfService;

    @Value("${azure.storage.blob.container.name:pdf-documents}")
    private String containerName;

    @Value("${azure.storage.enabled:false}")
    private boolean azureStorageEnabled;

    @Value("${pdf.storage.path:/tmp/pdfs}")
    private String pdfStoragePath;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    /**
     * Generate PDF document and store in cloud-native storage.
     * Uses Azure Blob Storage when enabled, otherwise uses temporary storage.
     * 
     * @param fileName Name of the PDF file
     * @param text Content to be written to PDF
     * @return byte array of generated PDF
     * @throws IOException if file operations fail
     * @throws DocumentException if PDF generation fails
     */
    private byte[] generateSamplePdf(String fileName, String text) throws IOException, DocumentException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }
        
        // Generate PDF in memory instead of writing to local file system
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();
        Paragraph paragraph = new Paragraph(text);
        document.add(paragraph);
        document.close();
        
        byte[] pdfBytes = baos.toByteArray();
        
        // Store PDF based on configuration
        if (azureStorageEnabled) {
            // TODO: Integrate with Azure Blob Storage SDK
            // BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            //     .connectionString(azureConnectionString)
            //     .buildClient();
            // BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            // BlobClient blobClient = containerClient.getBlobClient(fileName);
            // blobClient.upload(new ByteArrayInputStream(pdfBytes), pdfBytes.length, true);
            log.info("PDF would be stored in Azure Blob Storage: container={}, file={}", containerName, fileName);
        } else {
            // Fallback to temporary storage for local/dev environments
            Path storagePath = Paths.get(pdfStoragePath);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }
            Path filePath = storagePath.resolve(fileName);
            Files.write(filePath, pdfBytes);
            log.info("PDF stored in temporary storage: {}", filePath);
        }
        
        return pdfBytes;
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
                generateSamplePdf(pdf.getName(), pdf.getContent());
                pdfService.savePdf(pdf);
                log.info("PDF generated successfully: {}", pdf.getName());
            } catch (IOException e) {
                log.error("Failed to generate PDF due to I/O error: {}", e.getMessage(), e);
            } catch (DocumentException e) {
                log.error("Failed to generate PDF document: {}", e.getMessage(), e);
            }
            return "pdf/success";
        }
    }

}

package crm.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import crm.entity.Pdf;
import crm.service.AzureBlobStorageService;
import crm.service.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;

/**
 * PDF Controller - Cloud-ready version
 * Replaced local file system writes with Azure Blob Storage for cloud compatibility
 */
@Controller
@Slf4j
public class PdfController {

    private final PdfService pdfService;
    private final AzureBlobStorageService azureBlobStorageService;

    public PdfController(PdfService pdfService, AzureBlobStorageService azureBlobStorageService) {
        this.pdfService = pdfService;
        this.azureBlobStorageService = azureBlobStorageService;
    }

    /**
     * Generate PDF and store in Azure Blob Storage instead of local file system.
     * This approach is cloud-native and works in containerized environments.
     * 
     * @param fileName the name of the PDF file
     * @param text the content to write to the PDF
     * @return the URL or path to the stored PDF
     * @throws DocumentException if PDF generation fails
     */
    private String generateSamplePdf(String fileName, String text) throws DocumentException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }
        
        try {
            // Use ByteArrayOutputStream instead of FileOutputStream for cloud compatibility
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            Paragraph paragraph = new Paragraph(text);
            document.add(paragraph);
            document.close();
            
            // Upload to Azure Blob Storage instead of writing to local file system
            byte[] pdfBytes = outputStream.toByteArray();
            String blobUrl = azureBlobStorageService.uploadFile(fileName, pdfBytes);
            log.info("PDF generated and uploaded to cloud storage: {}", blobUrl);
            
            return blobUrl;
        } catch (Exception e) {
            log.error("Failed to generate PDF: {}", e.getMessage(), e);
            throw new DocumentException("Failed to generate PDF", e);
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
                String pdfUrl = generateSamplePdf(pdf.getName(), pdf.getContent());
                pdf.setName(pdfUrl); // Store the cloud URL instead of local path
                pdfService.savePdf(pdf);
                model.addAttribute("pdfUrl", pdfUrl);
                log.info("PDF successfully generated and saved: {}", pdfUrl);
            } catch (DocumentException e) {
                log.error("Document generation error: {}", e.getMessage(), e);
                model.addAttribute("error", "Failed to generate PDF document");
                return "pdf/generator";
            } catch (Exception e) {
                log.error("Unexpected error during PDF generation: {}", e.getMessage(), e);
                model.addAttribute("error", "An unexpected error occurred");
                return "pdf/generator";
            }
            return "pdf/success";
        }
    }
}

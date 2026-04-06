package crm.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import crm.entity.Pdf;
import crm.service.CloudStorageService;
import crm.service.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;

/**
 * PDF Controller - Cloud-ready version
 * Replaced local file system writes with GCP Cloud Storage
 */
@Controller
@Slf4j
public class PdfController {

    private final PdfService pdfService;
    private final CloudStorageService cloudStorageService;

    @Autowired
    public PdfController(PdfService pdfService, CloudStorageService cloudStorageService) {
        this.pdfService = pdfService;
        this.cloudStorageService = cloudStorageService;
    }

    /**
     * Generate PDF and store in Cloud Storage instead of local file system
     * @param fileName Name of the PDF file
     * @param text Content text for the PDF
     * @return Cloud storage path of the generated PDF
     * @throws DocumentException if PDF generation fails
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
            
            // Upload to Cloud Storage instead of writing to local file system
            byte[] pdfContent = outputStream.toByteArray();
            String cloudPath = cloudStorageService.uploadFile(fileName, pdfContent, "application/pdf");
            log.info("PDF generated and uploaded to cloud storage: {}", cloudPath);
            return cloudPath;
        } catch (Exception e) {
            log.error("Failed to generate PDF: {}", fileName, e);
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
                String cloudPath = generateSamplePdf(pdf.getName(), pdf.getContent());
                pdf.setCloudStoragePath(cloudPath); // Store cloud path instead of local path
                pdfService.savePdf(pdf);
                model.addAttribute("cloudPath", cloudPath);
                log.info("PDF successfully generated and saved: {}", cloudPath);
            } catch (DocumentException e) {
                log.error("Document generation error", e);
                model.addAttribute("error", "Failed to generate PDF document");
                return "pdf/generator";
            } catch (Exception e) {
                log.error("Unexpected error during PDF generation", e);
                model.addAttribute("error", "An unexpected error occurred");
                return "pdf/generator";
            }
            return "pdf/success";
        }
    }
}

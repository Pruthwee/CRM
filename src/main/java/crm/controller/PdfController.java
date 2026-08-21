package crm.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import crm.entity.Pdf;
import crm.service.PdfService;
import crm.service.S3StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@Slf4j
public class PdfController {

    private PdfService pdfService;
    private S3StorageService s3StorageService;

    public PdfController(PdfService pdfService, S3StorageService s3StorageService) {
        this.pdfService = pdfService;
        this.s3StorageService = s3StorageService;
    }

    /**
     * Generate PDF and upload to S3 instead of writing to local file system.
     * This ensures data durability and availability in cloud environments.
     *
     * @param fileName The name of the PDF file
     * @param text     The content to include in the PDF
     * @return The S3 URL where the PDF was uploaded
     */
    private String generateAndUploadPdf(String fileName, String text) throws DocumentException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }
        
        // Generate PDF in memory using ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            Paragraph paragraph = new Paragraph(text);
            document.add(paragraph);
            document.close();
            
            // Upload to S3 with timestamp to ensure uniqueness
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String s3Key = String.format("pdfs/%s-%s", timestamp, fileName);
            
            byte[] pdfBytes = outputStream.toByteArray();
            String s3Url = s3StorageService.uploadFile(s3Key, pdfBytes, "application/pdf");
            
            log.info("PDF generated and uploaded to S3: {}", s3Url);
            return s3Url;
        } finally {
            try {
                outputStream.close();
            } catch (Exception e) {
                log.warn("Failed to close output stream: {}", e.getMessage());
            }
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
                // Generate PDF and upload to S3
                String s3Url = generateAndUploadPdf(pdf.getName(), pdf.getContent());
                
                // Store the S3 URL in the PDF entity for reference
                pdf.setContent(s3Url);
                pdfService.savePdf(pdf);
                
                // Add success message with S3 location
                model.addAttribute("s3Url", s3Url);
                log.info("PDF successfully generated and stored in S3: {}", s3Url);
            } catch (DocumentException e) {
                log.error("Failed to generate PDF document: {}", e.getMessage(), e);
                model.addAttribute("error", "Failed to generate PDF document");
                return "pdf/generator";
            } catch (Exception e) {
                log.error("Failed to upload PDF to S3: {}", e.getMessage(), e);
                model.addAttribute("error", "Failed to upload PDF to cloud storage");
                return "pdf/generator";
            }
            return "pdf/success";
        }
    }

}

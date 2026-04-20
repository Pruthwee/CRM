package crm.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import crm.entity.Pdf;
import crm.service.PdfService;
import crm.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller
@Slf4j
public class PdfController {

    private PdfService pdfService;
    private S3Service s3Service;

    public PdfController(PdfService pdfService, S3Service s3Service) {
        this.pdfService = pdfService;
        this.s3Service = s3Service;
    }

    /**
     * Generate PDF and upload to S3 instead of writing to local file system.
     * This ensures data durability and availability in cloud environments.
     *
     * @param fileName The name of the PDF file
     * @param text The content to be written in the PDF
     * @return The S3 URL of the uploaded PDF
     * @throws DocumentException if PDF generation fails
     * @throws IOException if S3 upload fails
     */
    private String generateSamplePdf(String fileName, String text) throws DocumentException, IOException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }
        
        // Use ByteArrayOutputStream instead of FileOutputStream to avoid local file system writes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        Paragraph paragraph = new Paragraph(text);
        document.add(paragraph);
        document.close();
        
        // Upload the PDF to S3 for durable storage
        byte[] pdfBytes = outputStream.toByteArray();
        String s3Url = s3Service.uploadFile(fileName, pdfBytes, "application/pdf");
        
        log.info("PDF generated and uploaded to S3: {}", s3Url);
        return s3Url;
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
                String s3Url = generateSamplePdf(pdf.getName(), pdf.getContent());
                pdfService.savePdf(pdf);
                model.addAttribute("s3Url", s3Url);
                model.addAttribute("message", "PDF generated successfully and stored in S3");
                log.info("PDF generated successfully: {} -> {}", pdf.getName(), s3Url);
            } catch (DocumentException e) {
                log.error("Failed to generate PDF document: {}", e.getMessage(), e);
                model.addAttribute("error", "Failed to generate PDF document");
                return "pdf/generator";
            } catch (IOException e) {
                log.error("Failed to upload PDF to S3: {}", e.getMessage(), e);
                model.addAttribute("error", "Failed to upload PDF to cloud storage");
                return "pdf/generator";
            }
            return "pdf/success";
        }
    }

}

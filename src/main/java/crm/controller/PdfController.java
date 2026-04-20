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
     * This ensures data durability in cloud/containerized environments.
     */
    private String generateSamplePdfToS3(String fileName, String text) throws DocumentException, IOException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }
        
        // Create PDF in memory using ByteArrayOutputStream
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            PdfWriter.getInstance(document, baos);
            document.open();
            Paragraph paragraph = new Paragraph(text);
            document.add(paragraph);
            document.close();
            
            // Upload to S3 instead of writing to local file system
            byte[] pdfBytes = baos.toByteArray();
            String s3Key = "pdfs/" + fileName;
            String s3Url = s3Service.uploadFile(s3Key, pdfBytes, "application/pdf");
            
            log.info("PDF generated and uploaded to S3: {}", s3Url);
            return s3Url;
        } finally {
            baos.close();
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
                String s3Url = generateSamplePdfToS3(pdf.getName(), pdf.getContent());
                log.info("PDF successfully stored in S3 at: {}", s3Url);
                pdfService.savePdf(pdf);
            } catch (DocumentException e) {
                log.error("Error creating PDF document: {}", e.getMessage(), e);
            } catch (IOException e) {
                log.error("Error uploading PDF to S3: {}", e.getMessage(), e);
            }
            return "pdf/success";
        }
    }

}

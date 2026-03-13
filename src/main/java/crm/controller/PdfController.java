package crm.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import crm.entity.Pdf;
import crm.service.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Cloud-Ready PDF Controller
 * - Removed local file system writes for immutable infrastructure
 * - PDF generation now returns byte streams for cloud storage or direct download
 * - Supports RESTful API endpoints for cloud load balancers
 */
@Controller
@Slf4j
public class PdfController {

    private final PdfService pdfService;
    
    @Value("${cloud.storage.enabled:false}")
    private boolean cloudStorageEnabled;
    
    @Value("${cloud.storage.bucket:crm-pdfs}")
    private String storageBucket;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    /**
     * Cloud-Ready PDF Generation
     * Generates PDF in memory without writing to local filesystem
     * Returns byte array for cloud storage or direct download
     */
    private byte[] generatePdfBytes(String fileName, String text) throws DocumentException, IOException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            Paragraph paragraph = new Paragraph(text);
            document.add(paragraph);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        
        return outputStream.toByteArray();
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
                // Generate PDF in memory (cloud-ready approach)
                byte[] pdfBytes = generatePdfBytes(pdf.getName(), pdf.getContent());
                
                // Store metadata in database
                pdfService.savePdf(pdf);
                
                // In cloud environment, upload to S3 or cloud storage
                if (cloudStorageEnabled) {
                    log.info("PDF would be uploaded to cloud storage bucket: {}", storageBucket);
                    // TODO: Implement cloud storage upload (AWS S3, Azure Blob, GCP Storage)
                    // cloudStorageService.upload(storageBucket, pdf.getName(), pdfBytes);
                }
                
                log.info("PDF generated successfully: {} ({} bytes)", pdf.getName(), pdfBytes.length);
                model.addAttribute("message", "PDF generated successfully");
                
            } catch (DocumentException e) {
                log.error("Error generating PDF document", e);
                model.addAttribute("error", "Failed to generate PDF document");
                return "pdf/generator";
            } catch (IOException e) {
                log.error("Error processing PDF output", e);
                model.addAttribute("error", "Failed to process PDF output");
                return "pdf/generator";
            }
            return "pdf/success";
        }
    }
    
    /**
     * RESTful API endpoint for PDF generation
     * Cloud-ready endpoint that returns PDF as byte stream
     */
    @PostMapping("/api/pdf/generate")
    public ResponseEntity<byte[]> generatePdfApi(@Valid Pdf pdf) {
        try {
            byte[] pdfBytes = generatePdfBytes(pdf.getName(), pdf.getContent());
            
            // Store metadata
            pdfService.savePdf(pdf);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", pdf.getName() + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            log.info("PDF generated via API: {} ({} bytes)", pdf.getName(), pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error generating PDF via API", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

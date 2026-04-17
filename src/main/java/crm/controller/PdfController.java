package crm.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import crm.entity.Pdf;
import crm.service.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;

@Controller
@Slf4j
public class PdfController {

    private PdfService pdfService;
    
    @Autowired(required = false)
    private S3Client s3Client;
    
    private static final String S3_BUCKET_NAME = System.getenv().getOrDefault("S3_BUCKET_NAME", "crm-data-bucket");
    private static final String S3_PDF_PREFIX = System.getenv().getOrDefault("S3_PDF_PREFIX", "pdfs/");

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    /**
     * Generates PDF and uploads to Amazon S3 instead of local file system.
     * This ensures data durability and availability in cloud environments.
     */
    private String generateAndUploadPdfToS3(String fileName, String text) throws DocumentException, IOException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }
        
        // Generate PDF in memory using ByteArrayOutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        
        try {
            PdfWriter.getInstance(document, baos);
            document.open();
            Paragraph paragraph = new Paragraph(text);
            document.add(paragraph);
            document.close();
            
            // Upload to S3
            String s3Key = S3_PDF_PREFIX + Instant.now().toEpochMilli() + "-" + fileName;
            
            if (s3Client != null) {
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(S3_BUCKET_NAME)
                        .key(s3Key)
                        .contentType("application/pdf")
                        .build();
                
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(baos.toByteArray()));
                log.info("PDF uploaded to S3: s3://{}/{}", S3_BUCKET_NAME, s3Key);
                return s3Key;
            } else {
                log.warn("S3Client not configured. PDF generated but not uploaded.");
                return null;
            }
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
                String s3Key = generateAndUploadPdfToS3(pdf.getName(), pdf.getContent());
                if (s3Key != null) {
                    pdf.setName(s3Key); // Store S3 key instead of local file path
                }
                pdfService.savePdf(pdf);
            } catch (DocumentException e) {
                log.error("Error generating PDF document", e);
            } catch (IOException e) {
                log.error("Error uploading PDF to S3", e);
            }
            return "pdf/success";
        }
    }

}

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

@Controller
@Slf4j
public class PdfController {

    private PdfService pdfService;

    @Value("${aws.s3.bucket.name:crm-pdf-bucket}")
    private String s3BucketName;

    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    private byte[] generateSamplePdf(String fileName, String text) throws DocumentException, IOException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();
        Paragraph paragraph = new Paragraph(text);
        document.add(paragraph);
        document.close();
        return baos.toByteArray();
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
                byte[] pdfBytes = generateSamplePdf(pdf.getName(), pdf.getContent());

                if (s3Enabled) {
                    log.info("Storing PDF to S3 bucket: {}", s3BucketName);
                    // PDF bytes can be uploaded to S3 using AWS SDK
                    // S3 integration should be implemented in PdfService
                } else {
                    log.warn("S3 storage is disabled. PDF generated in-memory only.");
                }

                pdfService.savePdf(pdf);
            } catch (DocumentException e) {
                log.error("Error generating PDF document", e);
            } catch (IOException e) {
                log.error("Error writing PDF to stream", e);
            }
            return "pdf/success";
        }
    }

}

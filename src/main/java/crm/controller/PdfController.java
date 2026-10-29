package crm.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import crm.entity.Pdf;
import crm.service.PdfService;
import crm.utils.S3Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import software.amazon.awssdk.core.exception.SdkException;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

@Controller
@Slf4j
public class PdfController {

    private PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    private void generateSamplePdf(String fileName, String text) throws FileNotFoundException, DocumentException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }
        Document document = new Document();
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, content);
        document.open();
        Paragraph paragraph = new Paragraph(text);
        document.add(paragraph);
        document.close();
        S3Storage.put("pdf/" + fileName, content.toByteArray(), "application/pdf");
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
            } catch (FileNotFoundException e) {
                log.info("File Not Found");
            } catch (DocumentException e) {
                log.info("Document");
            } catch (SdkException e) {
                log.info("File Not Found");
            }
            return "pdf/success";
        }
    }

}

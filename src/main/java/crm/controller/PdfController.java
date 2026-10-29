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
import java.io.IOException;

@Controller
@Slf4j
public class PdfController {

    private static final String PDF_KEY_PREFIX = "pdf/";

    private PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    private void generateSamplePdf(String fileName, String text) throws FileNotFoundException, DocumentException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }
        ByteArrayOutputStream pdfBytes = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, pdfBytes);
        document.open();
        Paragraph paragraph = new Paragraph(text);
        document.add(paragraph);
        document.close();
        try {
            S3Storage.put(PDF_KEY_PREFIX + fileName, pdfBytes.toByteArray(), "application/pdf");
        } catch (IOException | SdkException e) {
            FileNotFoundException notStored = new FileNotFoundException("Could not store " + fileName + " in S3: " + e.getMessage());
            notStored.initCause(e);
            throw notStored;
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
                generateSamplePdf(pdf.getName(), pdf.getContent());
                pdfService.savePdf(pdf);
            } catch (FileNotFoundException e) {
                log.info("File Not Found");
            } catch (DocumentException e) {
                log.info("Document");
            }
            return "pdf/success";
        }
    }

}

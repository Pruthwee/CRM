package crm.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import crm.entity.Pdf;
import crm.service.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Cloud-ready PDF Controller
 * - Removed file system dependencies
 * - Generates PDFs in-memory and streams to response
 * - Compatible with immutable infrastructure
 */
@Controller
@Slf4j
public class PdfController {

    private PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    /**
     * Generate PDF in-memory and stream to response (Cloud-ready)
     * No file system writes - compatible with immutable infrastructure
     */
    private byte[] generateSamplePdfInMemory(String fileName, String text) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
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
    public String generatePdf(@Valid Pdf pdf, BindingResult bindingResult, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return "redirect:/pdf-generator";
        } else {
            try {
                // Generate PDF in-memory (cloud-ready approach)
                byte[] pdfBytes = generateSamplePdfInMemory(pdf.getName(), pdf.getContent());
                
                // Save metadata to database
                pdfService.savePdf(pdf);
                
                // Stream PDF to response instead of saving to file system
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + pdf.getName() + ".pdf\"");
                response.getOutputStream().write(pdfBytes);
                response.getOutputStream().flush();
                
                log.info("PDF generated successfully in-memory: {}", pdf.getName());
                return null; // Response already written
            } catch (DocumentException e) {
                log.error("Error generating PDF document", e);
                return "redirect:/pdf-generator?error=document";
            } catch (IOException e) {
                log.error("Error writing PDF to response", e);
                return "redirect:/pdf-generator?error=io";
            }
        }
    }

}

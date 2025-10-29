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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Controller
@Slf4j
public class PdfController {

    private final PdfService pdfService;
    private final ResourceLoader resourceLoader;

    @Value("${app.pdf.storage.path:${java.io.tmpdir}/crm-pdfs}")
    private String pdfStoragePath;

    public PdfController(PdfService pdfService, ResourceLoader resourceLoader) {
        this.pdfService = pdfService;
        this.resourceLoader = resourceLoader;
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

        // Save to cloud-compatible storage path if configured
        try {
            Path storagePath = Paths.get(pdfStoragePath);
            Files.createDirectories(storagePath);
            Path filePath = storagePath.resolve(fileName);
            Files.write(filePath, baos.toByteArray());
            log.info("PDF saved to: {}", filePath.toString());
        } catch (IOException e) {
            log.warn("Failed to save PDF to storage path: {}", e.getMessage());
        }

        return baos.toByteArray();
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
                byte[] pdfBytes = generateSamplePdf(pdf.getName(), pdf.getContent());
                pdf.setFileSize((long) pdfBytes.length);
                pdfService.savePdf(pdf);
                model.addAttribute("fileName", pdf.getName());
                log.info("Successfully generated PDF: {} (size: {} bytes)", pdf.getName(), pdfBytes.length);
            } catch (DocumentException e) {
                log.error("Error generating PDF document: {}", e.getMessage(), e);
                model.addAttribute("error", "Failed to generate PDF document");
                return "pdf/generator";
            } catch (IOException e) {
                log.error("IO error during PDF generation: {}", e.getMessage(), e);
                model.addAttribute("error", "Failed to save PDF file");
                return "pdf/generator";
            }
            return "pdf/success";
        }
    }

    @GetMapping("/pdf/download/{fileName}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(pdfStoragePath).resolve(fileName);
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] pdfBytes = Files.readAllBytes(filePath);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (IOException e) {
            log.error("Error reading PDF file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

}

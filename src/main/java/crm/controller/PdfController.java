package crm.controller;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
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

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * Cloud-ready PDF Controller that uses Google Cloud Storage for PDF persistence.
 * Implements asynchronous operations for better cloud performance.
 */
@Controller
@Slf4j
public class PdfController {

    private final PdfService pdfService;
    private final Storage storage;
    private final String gcsBucketName;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
        this.storage = StorageOptions.getDefaultInstance().getService();
        this.gcsBucketName = System.getenv().getOrDefault("GCS_BUCKET_NAME", "crm-data-bucket");
    }

    /**
     * Generate PDF and store in Google Cloud Storage asynchronously
     * @param fileName Name of the PDF file
     * @param text Content of the PDF
     * @return CompletableFuture with GCS blob name
     */
    private CompletableFuture<String> generateAndStorePdfAsync(String fileName, String text) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!fileName.endsWith(".pdf")) {
                    fileName += ".pdf";
                }
                
                // Generate PDF in memory
                Document document = new Document();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                PdfWriter.getInstance(document, outputStream);
                document.open();
                Paragraph paragraph = new Paragraph(text);
                document.add(paragraph);
                document.close();
                
                // Upload to GCS with timestamp to ensure uniqueness
                String blobName = "pdfs/" + Instant.now().toEpochMilli() + "_" + fileName;
                BlobId blobId = BlobId.of(gcsBucketName, blobName);
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                        .setContentType("application/pdf")
                        .build();
                
                storage.create(blobInfo, outputStream.toByteArray());
                log.info("PDF successfully uploaded to GCS: {}/{}", gcsBucketName, blobName);
                
                return blobName;
            } catch (DocumentException e) {
                log.error("Error generating PDF document", e);
                throw new RuntimeException("Failed to generate PDF", e);
            } catch (Exception e) {
                log.error("Error uploading PDF to GCS", e);
                throw new RuntimeException("Failed to upload PDF to GCS", e);
            }
        });
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
                // Asynchronously generate and store PDF in GCS
                CompletableFuture<String> futureBlob = generateAndStorePdfAsync(pdf.getName(), pdf.getContent());
                
                // Wait for completion (with timeout handling)
                String blobName = futureBlob.get(30, java.util.concurrent.TimeUnit.SECONDS);
                
                // Store blob reference in database
                pdf.setName(blobName);
                pdfService.savePdf(pdf);
                
                model.addAttribute("gcsPath", blobName);
                log.info("PDF generation completed successfully: {}", blobName);
                
                return "pdf/success";
            } catch (java.util.concurrent.TimeoutException e) {
                log.error("PDF generation timed out", e);
                model.addAttribute("error", "PDF generation timed out");
                return "pdf/generator";
            } catch (Exception e) {
                log.error("Error during PDF generation and storage", e);
                model.addAttribute("error", "Failed to generate PDF");
                return "pdf/generator";
            }
        }
    }
}

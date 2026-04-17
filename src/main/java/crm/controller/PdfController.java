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

/**
 * Cloud-ready PDF Controller that stores PDFs in Google Cloud Storage
 * instead of local file system.
 */
@Controller
@Slf4j
public class PdfController {

    private static final String GCS_BUCKET_NAME = System.getenv().getOrDefault("GCS_BUCKET_NAME", "crm-data-bucket");
    private static final String PDF_FOLDER = "pdfs/";
    
    private PdfService pdfService;
    private Storage storage;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
        try {
            this.storage = StorageOptions.getDefaultInstance().getService();
        } catch (Exception e) {
            log.error("Failed to initialize Google Cloud Storage client", e);
        }
    }

    /**
     * Generates PDF and stores it in Google Cloud Storage instead of local file system.
     * 
     * @param fileName Name of the PDF file
     * @param text Content to be written in the PDF
     * @return GCS blob name/path
     */
    private String generateSamplePdfToGCS(String fileName, String text) throws DocumentException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            Paragraph paragraph = new Paragraph(text);
            document.add(paragraph);
            document.close();
            
            // Upload to Google Cloud Storage
            String blobName = PDF_FOLDER + fileName;
            BlobId blobId = BlobId.of(GCS_BUCKET_NAME, blobName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType("application/pdf")
                    .build();
            
            if (storage != null) {
                storage.create(blobInfo, outputStream.toByteArray());
                log.info("PDF successfully uploaded to GCS: {}/{}", GCS_BUCKET_NAME, blobName);
                return blobName;
            } else {
                log.error("Google Cloud Storage client not initialized");
                throw new RuntimeException("Cloud storage not available");
            }
        } catch (Exception e) {
            log.error("Error generating and uploading PDF to GCS", e);
            throw new DocumentException("Failed to generate PDF", e);
        }
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
                String gcsPath = generateSamplePdfToGCS(pdf.getName(), pdf.getContent());
                
                // Store GCS path in the entity instead of local file path
                pdf.setName(gcsPath);
                pdfService.savePdf(pdf);
                
                model.addAttribute("gcsPath", gcsPath);
                log.info("PDF generated and saved successfully: {}", gcsPath);
            } catch (DocumentException e) {
                log.error("Error generating PDF document", e);
                model.addAttribute("error", "Failed to generate PDF");
                return "pdf/generator";
            } catch (Exception e) {
                log.error("Unexpected error during PDF generation", e);
                model.addAttribute("error", "An unexpected error occurred");
                return "pdf/generator";
            }
            return "pdf/success";
        }
    }

}

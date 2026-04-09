package crm.controller;

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
import java.time.Instant;

/**
 * Cloud-ready PDF Controller that stores PDFs in Google Cloud Storage
 * instead of local filesystem.
 */
@Controller
    
    private final Storage storage;
    private String bucketName;

    @Value("${gcs.project.id:${GCP_PROJECT_ID:}}")
    private String projectId;
    public PdfController(PdfService pdfService, Storage storage) {
        this.storage = storage;
    /**
     * Generates PDF and stores it in Google Cloud Storage.
     * Returns the GCS URI for the stored PDF.
     */
    private String generateAndStorePdfInGCS(String fileName, String text) throws DocumentException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }

        try {
            // Generate PDF in memory
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    .getService();

            String blobName = pdfFolder + "/" + Instant.now().toEpochMilli() + "_" + fileName;
            BlobId blobId = BlobId.of(bucketName, blobName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType("application/pdf")
                    .build();

            storage.create(blobInfo, baos.toByteArray());

            String gcsUri = String.format("gs://%s/%s", bucketName, blobName);
            log.info("PDF successfully stored in GCS: {}", gcsUri);
            return gcsUri;

        } catch (Exception e) {
            log.error("Error generating and storing PDF in GCS: fileName={}", fileName, e);
            throw new DocumentException("Failed to store PDF in cloud storage", e);
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
                String gcsUri = generateAndStorePdfInGCS(pdf.getName(), pdf.getContent());
                pdf.setName(gcsUri); // Store GCS URI instead of local path
                pdfService.savePdf(pdf);
                model.addAttribute("gcsUri", gcsUri);
                log.info("PDF generated and saved successfully: {}", gcsUri);
            } catch (DocumentException e) {
                log.error("Error generating PDF document", e);
                model.addAttribute("error", "Failed to generate PDF: " + e.getMessage());
                return "pdf/generator";
            }
            return "pdf/success";
        }
    }
}

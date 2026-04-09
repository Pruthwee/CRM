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
import org.springframework.beans.factory.annotation.Value;
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

    private PdfService pdfService;

    @Value("${gcs.bucket.name:${GCS_BUCKET_NAME:default-bucket}}")
    private String bucketName;

    @Value("${gcs.pdf.folder:${GCS_PDF_FOLDER:pdfs}}")
    private String pdfFolder;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    /**
     * Generates PDF and stores it in Google Cloud Storage.
     * 
     * @param fileName The name of the PDF file
     * @param text The content to include in the PDF
     * @return The GCS URI of the stored PDF
     */
    private String generateSamplePdf(String fileName, String text) throws DocumentException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }
        
        try {
            // Create PDF in memory
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();
            Paragraph paragraph = new Paragraph(text);
            document.add(paragraph);
            document.close();
            
            // Upload to Google Cloud Storage
            Storage storage = StorageOptions.getDefaultInstance().getService();
            String blobName = pdfFolder + "/" + fileName;
            BlobId blobId = BlobId.of(bucketName, blobName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType("application/pdf")
                    .build();
            
            storage.create(blobInfo, baos.toByteArray());
            
            String gcsUri = String.format("gs://%s/%s", bucketName, blobName);
            log.info("PDF successfully uploaded to GCS: {}", gcsUri);
            
            return gcsUri;
        } catch (Exception e) {
            log.error("Error generating and uploading PDF to GCS", e);
            throw new DocumentException("Failed to generate and upload PDF", e);
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
                String gcsUri = generateSamplePdf(pdf.getName(), pdf.getContent());
                pdf.setName(gcsUri); // Store GCS URI instead of local path
                pdfService.savePdf(pdf);
                model.addAttribute("gcsUri", gcsUri);
                log.info("PDF generated and saved successfully: {}", gcsUri);
            } catch (DocumentException e) {
                log.error("Error generating PDF document", e);
                model.addAttribute("error", "Failed to generate PDF");
                return "pdf/generator";
            }
            return "pdf/success";
        }
    }
}

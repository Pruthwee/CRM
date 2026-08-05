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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * PdfController – generates PDF documents and stores them durably in Amazon S3
 * instead of writing to the ephemeral local file system (cr-java-0062 fix).
 *
 * <p>The PDF content is first rendered into an in-memory {@link ByteArrayOutputStream}
 * and then uploaded to the configured S3 bucket via the AWS SDK v2 {@link S3Client}.
 * No local file is ever created, making the operation safe in containerised /
 * serverless cloud environments where the local file system is ephemeral.</p>
 *
 * <p>Required environment variables (already wired via application.properties):</p>
 * <ul>
 *   <li>{@code AWS_S3_BUCKET_NAME} – target S3 bucket name</li>
 *   <li>{@code AWS_REGION}         – AWS region (e.g. {@code us-east-1})</li>
 * </ul>
 */
@Controller
@Slf4j
public class PdfController {

    private final PdfService pdfService;

    /** S3 bucket name resolved from the {@code AWS_S3_BUCKET_NAME} environment variable. */
    @Value("${cloud.aws.s3.bucket}")
    private String s3BucketName;

    /** AWS region resolved from the {@code AWS_REGION} environment variable. */
    @Value("${cloud.aws.region}")
    private String awsRegion;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    /**
     * Generates a PDF document from the supplied text and uploads it to Amazon S3.
     *
     * <p>The PDF is rendered entirely in memory using an {@link ByteArrayOutputStream}
     * (replacing the previous {@code FileOutputStream} that wrote to the local disk).
     * The resulting byte array is then uploaded to S3 under the key
     * {@code pdfs/<fileName>}.</p>
     *
     * @param fileName desired object name (a {@code .pdf} suffix is appended if absent)
     * @param text     body text to embed in the PDF
     * @throws DocumentException if iText encounters an error while building the document
     * @throws IOException       if the in-memory stream cannot be read
     */
    private void generateSamplePdf(String fileName, String text) throws DocumentException, IOException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }

        // --- Render PDF into memory (no local file write) ---
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        Document document = new Document();
        // cr-java-0062 fix: use ByteArrayOutputStream instead of FileOutputStream
        // so that no data is written to the ephemeral local file system.
        PdfWriter.getInstance(document, pdfOutputStream);
        document.open();
        Paragraph paragraph = new Paragraph(text);
        document.add(paragraph);
        document.close();

        // --- Upload the in-memory PDF to Amazon S3 for durable storage ---
        byte[] pdfBytes = pdfOutputStream.toByteArray();
        String s3Key = "pdfs/" + fileName;

        S3Client s3Client = S3Client.builder()
                .region(Region.of(awsRegion))
                .build();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3BucketName)
                .key(s3Key)
                .contentType("application/pdf")
                .contentLength((long) pdfBytes.length)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(pdfBytes));
        log.info("PDF '{}' successfully uploaded to S3 bucket '{}' with key '{}'",
                fileName, s3BucketName, s3Key);
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
            } catch (DocumentException e) {
                log.error("Failed to generate PDF document: {}", e.getMessage(), e);
            } catch (IOException e) {
                log.error("Failed to upload PDF to S3: {}", e.getMessage(), e);
            }
            return "pdf/success";
        }
    }

}

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
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * PDF Controller that generates PDF documents and stores them in Amazon S3
 * instead of writing to the local file system.
 * Replaces FileOutputStream-based local writes with S3 PutObject calls
 * using AWS SDK for Java v2 to ensure durable, cloud-native storage.
 */
@Controller
@Slf4j
public class PdfController {

    private final PdfService pdfService;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name:${AWS_S3_BUCKET_NAME:crm-pdf-storage}}")
    private String bucketName;

    @Value("${aws.s3.pdf-prefix:pdfs/}")
    private String pdfPrefix;

    public PdfController(PdfService pdfService, S3Client s3Client) {
        this.pdfService = pdfService;
        this.s3Client = s3Client;
    }

    /**
     * Generates a PDF document and uploads it to Amazon S3.
     * Replaces local FileOutputStream write (blocker cr-java-0062) with S3 PutObject.
     *
     * @param fileName the name of the PDF file (used as S3 object key)
     * @param text     the text content to include in the PDF
     * @throws DocumentException if PDF generation fails
     * @throws IOException       if byte stream operations fail
     */
    private void generateSamplePdf(String fileName, String text) throws DocumentException, IOException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }
        // Generate PDF content into an in-memory byte array (no local file system dependency)
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, byteArrayOutputStream);
        document.open();
        Paragraph paragraph = new Paragraph(text);
        document.add(paragraph);
        document.close();

        // Upload the generated PDF bytes directly to Amazon S3
        String s3Key = pdfPrefix + fileName;
        byte[] pdfBytes = byteArrayOutputStream.toByteArray();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType("application/pdf")
                .contentLength((long) pdfBytes.length)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(pdfBytes));
        log.info("PDF uploaded to S3: s3://{}/{}", bucketName, s3Key);
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
                log.error("PDF document generation error: {}", e.getMessage());
            } catch (IOException e) {
                log.error("IO error during PDF generation: {}", e.getMessage());
            }
            return "pdf/success";
        }
    }

}

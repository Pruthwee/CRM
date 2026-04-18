package crm.controller;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.WriteResult;
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
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Cloud-ready PDF Controller.
 * Replaces local file writes with Google Cloud Firestore for structured data persistence.
 * PDF content is stored as base64-encoded binary data in Firestore.
 */
@Controller
@Slf4j
public class PdfController {

    private static final String FIRESTORE_COLLECTION = "pdf_documents";
    private final PdfService pdfService;
    private final Firestore firestore;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
        // Initialize Firestore client
        this.firestore = FirestoreOptions.getDefaultInstance().getService();
    }

    /**
     * Generates PDF content and stores it in Google Cloud Firestore.
     * Replaces local file system write operations with cloud-native storage.
     * 
     * @param fileName The name of the PDF document
     * @param text The content to include in the PDF
     * @return Document ID in Firestore
     */
    private String generateAndStorePdf(String fileName, String text) throws DocumentException, IOException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }
        
        // Generate PDF in memory instead of writing to local file system
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        
        try {
            PdfWriter.getInstance(document, baos);
            document.open();
            Paragraph paragraph = new Paragraph(text);
            document.add(paragraph);
            document.close();
            
            // Convert PDF to base64 for storage in Firestore
            byte[] pdfBytes = baos.toByteArray();
            String pdfBase64 = java.util.Base64.getEncoder().encodeToString(pdfBytes);
            
            // Store PDF metadata and content in Firestore
            String documentId = UUID.randomUUID().toString();
            Map<String, Object> pdfData = new HashMap<>();
            pdfData.put("fileName", fileName);
            pdfData.put("content", text);
            pdfData.put("pdfBase64", pdfBase64);
            pdfData.put("createdAt", Instant.now().toString());
            pdfData.put("size", pdfBytes.length);
            
            // Write to Firestore
            DocumentReference docRef = firestore.collection(FIRESTORE_COLLECTION).document(documentId);
            WriteResult result = docRef.set(pdfData).get();
            
            log.info("PDF stored in Firestore: documentId={}, fileName={}, size={} bytes, timestamp={}", 
                     documentId, fileName, pdfBytes.length, result.getUpdateTime());
            
            return documentId;
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to store PDF in Firestore: fileName={}", fileName, e);
            throw new IOException("Failed to store PDF in Firestore", e);
        } finally {
            baos.close();
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
                // Generate PDF and store in Firestore instead of local file system
                String documentId = generateAndStorePdf(pdf.getName(), pdf.getContent());
                
                // Store metadata in application database
                pdf.setFirestoreDocumentId(documentId);
                pdfService.savePdf(pdf);
                
                model.addAttribute("documentId", documentId);
                log.info("PDF generation successful: name={}, firestoreId={}", pdf.getName(), documentId);
                
            } catch (DocumentException e) {
                log.error("PDF document generation error: name={}", pdf.getName(), e);
                model.addAttribute("error", "Failed to generate PDF document");
                return "pdf/generator";
            } catch (IOException e) {
                log.error("Firestore storage error: name={}", pdf.getName(), e);
                model.addAttribute("error", "Failed to store PDF in cloud storage");
                return "pdf/generator";
            }
            return "pdf/success";
        }
    }

}

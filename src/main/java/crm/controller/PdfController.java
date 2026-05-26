import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.itextpdf.text.pdf.PdfWriter;
import crm.entity.Pdf;
import crm.service.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
    private void generateSamplePdf(String fileName, String text) throws FileNotFoundException, DocumentException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }

        // Generate PDF into memory instead of local file system
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        Paragraph paragraph = new Paragraph(text);
        document.add(paragraph);
        document.close();

        // Upload generated PDF to Azure Blob Storage for durable, cloud-native storage
        String connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        String containerName = System.getenv("AZURE_STORAGE_CONTAINER");
        if (connectionString == null || containerName == null) {
            throw new IllegalStateException("Azure Storage configuration missing. Please set AZURE_STORAGE_CONNECTION_STRING and AZURE_STORAGE_CONTAINER environment variables.");
        }

        BlobClient blobClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient()
                .getBlobClient(fileName);

        byte[] pdfBytes = outputStream.toByteArray();
        blobClient.upload(new ByteArrayInputStream(pdfBytes), pdfBytes.length, true);
        }

        // Generate PDF into memory instead of local file system
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        Paragraph paragraph = new Paragraph(text);
        document.add(paragraph);
        document.close();

        // Upload generated PDF to Azure Blob Storage for durable, cloud-native storage
        String connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        String containerName = System.getenv("AZURE_STORAGE_CONTAINER");
        if (connectionString == null || containerName == null) {
            throw new IllegalStateException("Azure Storage configuration missing. Please set AZURE_STORAGE_CONNECTION_STRING and AZURE_STORAGE_CONTAINER environment variables.");
        }

        BlobClient blobClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient()
                .getBlobClient(fileName);

        byte[] pdfBytes = outputStream.toByteArray();
        blobClient.upload(new ByteArrayInputStream(pdfBytes), pdfBytes.length, true);
        }

        // Generate PDF into memory instead of local file system
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        Paragraph paragraph = new Paragraph(text);
        document.add(paragraph);
        document.close();

        // Upload generated PDF to Azure Blob Storage for durable, cloud-native storage
        String connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        String containerName = System.getenv("AZURE_STORAGE_CONTAINER");
        if (connectionString == null || containerName == null) {
            throw new IllegalStateException("Azure Storage configuration missing. Please set AZURE_STORAGE_CONNECTION_STRING and AZURE_STORAGE_CONTAINER environment variables.");
        }

        BlobClient blobClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient()
                .getBlobClient(fileName);

        byte[] pdfBytes = outputStream.toByteArray();
        blobClient.upload(new ByteArrayInputStream(pdfBytes), pdfBytes.length, true);
        }

        // Generate PDF into memory instead of local file system
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        Paragraph paragraph = new Paragraph(text);
        document.add(paragraph);
        document.close();

        // Upload generated PDF to Azure Blob Storage for durable, cloud-native storage
        String connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        String containerName = System.getenv("AZURE_STORAGE_CONTAINER");
        if (connectionString == null || containerName == null) {
            throw new IllegalStateException("Azure Storage configuration missing. Please set AZURE_STORAGE_CONNECTION_STRING and AZURE_STORAGE_CONTAINER environment variables.");
        }

        BlobClient blobClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient()
                .getBlobClient(fileName);

        byte[] pdfBytes = outputStream.toByteArray();
        blobClient.upload(new ByteArrayInputStream(pdfBytes), pdfBytes.length, true);
import java.io.FileOutputStream;

@Controller
@Slf4j
public class PdfController {

    private PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    private void generateSamplePdf(String fileName, String text) throws FileNotFoundException, DocumentException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();
        Paragraph paragraph = new Paragraph(text);
        document.add(paragraph);
        document.close();
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

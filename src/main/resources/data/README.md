# Data Resources Directory

## Purpose
This directory contains data files that are packaged with the application and loaded from the classpath. This approach is cloud-compatible as files are embedded in the application JAR.

## Usage

### Loading Files from Classpath
```java
import crm.utils.ReadDataUtils;

// Load a CSV file from classpath
File csvFile = ReadDataUtils.readFileFromClasspath("data/sample.csv");
```

### File Organization
```
src/main/resources/data/
├── README.md           (this file)
├── sample.csv          (sample CSV data)
├── templates/          (template files)
└── imports/            (import data files)
```

## Cloud Deployment Notes

### Development Environment
- Place test data files in this directory
- Files are loaded from classpath using `ClassPathResource`
- Files are automatically packaged in the JAR during build

### Production Environment
For production, consider these alternatives:

1. **AWS S3 Storage** (Recommended)
   - Upload files to S3 bucket
   - Use `FileStorageService` to download files
   - Configure `FILE_STORAGE_TYPE=s3` environment variable

2. **REST API Upload**
   - Implement file upload endpoints
   - Accept `MultipartFile` in controllers
   - Process files in-memory or save to S3

3. **Database Storage**
   - Store small files as BLOB in database
   - Use for configuration files or templates

## Example: Sample CSV File

Create a file `sample.csv` in this directory:

```csv
id,name,email,phone
1,John Doe,john@example.com,555-0100
2,Jane Smith,jane@example.com,555-0101
3,Bob Johnson,bob@example.com,555-0102
```

## Environment Variables

- `CSV_TEST_FILE`: Path to test CSV file (default: "data/sample.csv")
- `FILE_STORAGE_TYPE`: Storage type - "classpath", "local", or "s3"

## Best Practices

1. **Keep Files Small**: Classpath resources increase JAR size
2. **Use for Static Data**: Configuration, templates, seed data
3. **Large Files**: Use S3 or external storage
4. **Sensitive Data**: Never commit sensitive data to version control
5. **Testing**: Use this directory for test data files

## Migration from Desktop File Chooser

**Before (Desktop - Not Cloud Compatible)**:
```java
File file = ReadDataUtils.ReadFile("Select CSV", null, "CSV Files", "csv");
```

**After (Cloud Compatible)**:
```java
// Option 1: Load from classpath
File file = ReadDataUtils.readFileFromClasspath("data/sample.csv");

// Option 2: Upload via REST API
@PostMapping("/upload")
public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
    // Process uploaded file
}

// Option 3: Load from S3
FileStorageService storage = new FileStorageService();
File file = storage.downloadFile("uploads/sample.csv");
```

## Security Considerations

- Do not store credentials or secrets in this directory
- Use AWS Secrets Manager for sensitive configuration
- Validate and sanitize all uploaded files
- Implement file size limits
- Scan uploaded files for malware

## Additional Resources

- [Spring Boot Resource Loading](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [AWS S3 Best Practices](https://docs.aws.amazon.com/AmazonS3/latest/userguide/best-practices.html)
- [File Upload Security](https://owasp.org/www-community/vulnerabilities/Unrestricted_File_Upload)

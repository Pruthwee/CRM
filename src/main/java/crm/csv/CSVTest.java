        // In cloud environments we avoid local file dialogs and file system access.
        // Instead, read the CSV from Azure Blob Storage. The blob name should be
        // provided via environment variable to keep configuration externalized.
        String blobName = System.getenv("CSV_SOURCE_BLOB_NAME");
        if (blobName == null || blobName.isEmpty()) {
            throw new IllegalStateException("CSV_SOURCE_BLOB_NAME environment variable must be set to the source CSV blob name.");
        }

        List<String[]> data = ReadDataUtils.readCsvFromBlob(blobName);
        for (String[] line : data) {
            if (line.length > 2 && "QUICK SUB".equals(line[1])) {
                System.out.println(line[0] + "\t" + line[1] + "\t" + line[2]);
            }
        }
		System.out.println(data.get(1)[1] + "\t" + data.get(1)[2]);*/
    }

}

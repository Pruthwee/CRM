package crm.controller;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
/**
 * CSV Controller - Cloud-ready REST controller
 * Uses response streaming for efficient CSV generation
 */
@Slf4j

    public CSVController(CustomerService customerService) {
        this.customerService = customerService;
    }

        log.info("Generating CSV for {} customers", customers.size());
        log.debug("CSV generation completed successfully");
        log.info("Generating CSV for customer ID: {}", id);
        log.debug("CSV generation completed for customer ID: {}", id);
//    @GetMapping("/show-import")
//    public String showImportCsvSite() {
//        return "csv/import";
//    }

    /*@GetMapping("/import")
    public String processRequestImportCsv(Model model) {
        File document = ReadDataUtils.ReadFile("Select CSV file", null, "Only CSV Files", "csv");
//        System.out.println(document.getName());

            CSVReader reader;
            List<String[]> data = new ArrayList<>();
            try {
                reader = new CSVReader(new FileReader(document));
                String[] line;
                while ((line = reader.readNext()) != null) {
//                    System.out.println(line[1] + "\t" + line[2]);
                    data.add(line);
//                    if(line[1].equals("QUICK SUB")){
//                        System.out.println(line[0] + "\t" + line[1] + "\t" + line[2]);
//                    }
                }
                model.addAttribute("data", data);
            } catch (IOException e) {
                e.printStackTrace();
            }
		*//*System.out.println(data.get(0)[1] + "\t" + data.get(0)[2]);
		System.out.println(data.get(1)[1] + "\t" + data.get(1)[2]);*//*

        return "csv/show";
    }*/

//    @GetMapping("/show")
//    public String showPageWithCsvImported(@ModelAttribute List<String[]> data) {
//        data.
//        return "csv/show";
//    }

}

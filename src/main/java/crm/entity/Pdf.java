package crm.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PDF Entity - Cloud-ready
 * Added cloudStoragePath field to store GCP Cloud Storage location
 */
@NoArgsConstructor
@AllArgsConstructor
public class Pdf {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    @Size(min = 2)
    
    @Column(name = "cloud_storage_path")
    private String cloudStoragePath;
    
}

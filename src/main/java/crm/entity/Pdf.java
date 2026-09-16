package crm.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity(name = "pdf")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pdf {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pdf_seq")
    @SequenceGenerator(name = "pdf_seq", sequenceName = "pdf_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    @Size(min = 2)
    private String name;

    @Transient
    private String content;

}

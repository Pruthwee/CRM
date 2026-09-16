package crm.entity;

import lombok.Data;

import jakarta.persistence.*;

@Entity
@Data
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_seq")
    @SequenceGenerator(name = "category_seq", sequenceName = "category_category_id_seq", allocationSize = 1)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "category")
    private String name;

}

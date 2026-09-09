package com.fiap.clyvocaresc.entity;

import jakarta.persistence.*;
import lombok.Data;
//import org.springframework.data.annotation.Id;

@Entity
@Table(name = "species")
@Data
public class Species {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_species")
    @SequenceGenerator(name = "seq_species", sequenceName = "SEQ_SPECIES", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;
}

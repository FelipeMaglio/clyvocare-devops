package com.fiap.clyvocaresc.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "prescriptions")
@Data
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_prescriptions")
    @SequenceGenerator(name = "seq_prescriptions", sequenceName = "SEQ_PRESCRIPTIONS", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String dosage;

    @Column(nullable = false, length = 50)
    private String frequency;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(length = 500)
    private String instructions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id", nullable = false)
    private Treatment treatment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_item_id", nullable = false)
    private CatalogItem catalogItem; // deve ter type = MEDICATION
}

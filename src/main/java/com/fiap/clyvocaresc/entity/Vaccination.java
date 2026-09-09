package com.fiap.clyvocaresc.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "vaccinations")
@Data
public class Vaccination {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_vaccinations")
    @SequenceGenerator(name = "seq_vaccinations", sequenceName = "SEQ_VACCINATIONS", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "application_date", nullable = false)
    private LocalDate applicationDate;

    @Column(length = 50)
    private String batch;

    @Column(name = "next_dose_date")
    private LocalDate nextDoseDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_item_id", nullable = false)
    private CatalogItem catalogItem; // deve ter type = VACCINE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinarian_id")
    private Veterinarian veterinarian;
}

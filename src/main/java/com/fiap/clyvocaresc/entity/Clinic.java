package com.fiap.clyvocaresc.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "clinics")
@Data
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_clinics")
    @SequenceGenerator(name = "seq_clinics", sequenceName = "SEQ_CLINICS", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "tax_id", nullable = false, length = 14)
    private String taxId;

    @Column(length = 20)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(length = 300)
    private String address;

    @Column(name = "subscription_plan", length = 20)
    private String subscriptionPlan;

    @Column(name = "subscription_date")
    private LocalDate subscriptionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;
}

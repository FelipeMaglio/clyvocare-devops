package com.fiap.clyvocaresc.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "veterinarians")
@Data
public class Veterinarian {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_veterinarians")
    @SequenceGenerator(name = "seq_veterinarians", sequenceName = "SEQ_VETERINARIANS", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "license_number", nullable = false, length = 20)
    private String licenseNumber;

    @Column(length = 100)
    private String specialty;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;
}

package com.fiap.clyvocaresc.entity;
import com.fiap.clyvocaresc.entity.enums.AppointmentType;
import com.fiap.clyvocaresc.entity.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "appointments")
@Data
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_appointments")
    @SequenceGenerator(name = "seq_appointments", sequenceName = "SEQ_APPOINTMENTS", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AppointmentType type;

    @Column(length = 500)
    private String reason;

    @Column(length = 1000)
    private String diagnosis;

    @Lob
    private String notes;

    @Column(name = "weight_at_visit", precision = 6, scale = 3)
    private BigDecimal weightAtVisit;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AppointmentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinarian_id")
    private Veterinarian veterinarian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;
}

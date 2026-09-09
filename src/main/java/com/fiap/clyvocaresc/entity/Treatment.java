package com.fiap.clyvocaresc.entity;

import com.fiap.clyvocaresc.entity.enums.TreatmentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "treatments")
@Data
public class Treatment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_treatments")
    @SequenceGenerator(name = "seq_treatments", sequenceName = "SEQ_TREATMENTS", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TreatmentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;
}

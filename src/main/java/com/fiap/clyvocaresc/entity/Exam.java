package com.fiap.clyvocaresc.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "exams")
@Data
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_exams")
    @SequenceGenerator(name = "seq_exams", sequenceName = "SEQ_EXAMS", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "exam_type", nullable = false, length = 100)
    private String examType;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "result_date")
    private LocalDate resultDate;

    @Lob
    private String result;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(length = 150)
    private String laboratory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;
}

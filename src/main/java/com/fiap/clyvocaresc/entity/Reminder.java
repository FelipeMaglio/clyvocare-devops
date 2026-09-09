package com.fiap.clyvocaresc.entity;

import com.fiap.clyvocaresc.entity.enums.ReminderType;
import com.fiap.clyvocaresc.entity.enums.ReminderStatus;
import com.fiap.clyvocaresc.entity.enums.ReminderChannel;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "reminders")
@Data
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_reminders")
    @SequenceGenerator(name = "seq_reminders", sequenceName = "SEQ_REMINDERS", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReminderType type;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private ReminderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private ReminderChannel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;
}

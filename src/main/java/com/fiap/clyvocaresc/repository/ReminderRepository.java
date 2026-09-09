package com.fiap.clyvocaresc.repository;

import com.fiap.clyvocaresc.entity.Appointment;
import com.fiap.clyvocaresc.entity.Reminder;
import com.fiap.clyvocaresc.entity.enums.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByOwnerIdAndStatus(Long ownerId, ReminderStatus status);
}
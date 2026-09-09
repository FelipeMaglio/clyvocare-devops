package com.fiap.clyvocaresc.dto.response;
import com.fiap.clyvocaresc.entity.enums.AppointmentStatus;
import com.fiap.clyvocaresc.entity.enums.AppointmentType;

import java.time.LocalDate;
public record AppointmentResponseDTO(

        Long id,
        LocalDate appointmentDate,
        AppointmentType type,
        String reason,
        String diagnosis,
        String notes,
        Double weightAtVisit,
        AppointmentStatus status,
        Long petId,
        String petName,
        String veterinarianName,
        String clinicName
) {}
package com.fiap.clyvocaresc.dto.response;


import com.fiap.clyvocaresc.entity.enums.ReminderChannel;
import com.fiap.clyvocaresc.entity.enums.ReminderStatus;
import com.fiap.clyvocaresc.entity.enums.ReminderType;

import java.time.LocalDate;

public record ReminderResponseDTO(
        Long id,
        ReminderType type,
        LocalDate eventDate,
        String message,
        ReminderStatus status,
        ReminderChannel channel,
        Long petId,
        String petName
) {}
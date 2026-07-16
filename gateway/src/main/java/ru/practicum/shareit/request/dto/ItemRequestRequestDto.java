package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ItemRequestRequestDto {
    @NotBlank(message = "Description must not be empty")
    private String description;
}

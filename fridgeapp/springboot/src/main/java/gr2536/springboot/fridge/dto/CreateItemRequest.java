package gr2536.springboot.fridge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateItemRequest(
    @NotBlank String name,
    @Positive int quantity,
    LocalDate expirationDate 
) {}

package com.example.resourcebooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRequest {

    @NotBlank(message = "Resource name is required")
    private String name;

    private String description;

    private String type;

    private String location;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must not be negative")
    private BigDecimal price;

    private Boolean available;
}

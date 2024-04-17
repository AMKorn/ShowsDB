package com.andreas.showsdb.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShowDto {
    @NotNull(message = "Show name may not be null")
    @Size(max = 255, message = "Show name may not be longer than 255 characters")
    String name;
    @Size(max = 255, message = "Country name may not be longer than 255 characters")
    String country;
}

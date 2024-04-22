package com.andreas.showsdb.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MainCastDto {
    @NotNull(message = "Actor id must not be null")
    Long actorId;
    @NotNull(message = "Show id must not be null")
    Long showId;
    @NotNull(message = "Character must not be null")
    @Size(max = 255, message = "Character name must not be longer than 255 characters")
    String character;
}

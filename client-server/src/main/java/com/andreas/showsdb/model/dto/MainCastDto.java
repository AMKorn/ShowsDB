package com.andreas.showsdb.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MainCastDto implements Serializable {
    @NotNull(message = "Actor id must not be null")
    Long actorId;
    @NotNull(message = "Show id must not be null")
    Long showId;
    @NotNull(message = "Character must not be null")
    @Size(max = 255, message = "Character name must not be longer than 255 characters")
    String character;
}

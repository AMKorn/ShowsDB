package com.andreas.showsdb.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActorOutputDto implements Serializable {
    @NotNull(message = "Actor ID must not be null")
    Long id;
    @Size(max = 255, message = "Actor name must not be longer than 255 characters")
    String name;
    @Size(max = 255, message = "Country must not be longer than 255 characters")
    String country;
    @Past(message = "Actor must have been born")
    LocalDate birthDate;
}

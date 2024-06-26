package com.andreas.showsdb.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.util.Date;

@Value
@Builder
public class ActorInputDto {
    @NotBlank(message = "Actor name must not be null nor blank")
    @Size(max = 255, message = "Actor name must not be longer than 255 characters")
    String name;
    @Size(max = 255, message = "Country must not be longer than 255 characters")
    String country;
    @Past(message = "Actor must have been born")
    Date birthDate;
}

package com.andreas.showsdb.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.util.Date;

@Value
@Builder
public class ActorDtoId {
    @NotNull
    Long id;
    @NotNull
    @Size(max = 255, message = "Actor name may not be longer than 255 characters")
    String name;
    @Size(max = 255, message = "Country may not be longer than 255 characters")
    String country;
    @Past
    Date birthDate;
}

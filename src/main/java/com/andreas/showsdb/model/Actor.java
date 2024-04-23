package com.andreas.showsdb.model;

import com.andreas.showsdb.model.dto.ActorInputDto;
import com.andreas.showsdb.model.dto.ActorOutputDto;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "actor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Actor {
    @OneToMany(mappedBy = "actor")
    Set<MainCast> showsAsMainCast;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String country;
    @Column(name = "birth_date")
    private Date birthDate;

    public static Actor translateFromDto(@Valid ActorInputDto dto) {
        return Actor.builder()
                .name(dto.getName())
                .country(dto.getCountry())
                .country(dto.getCountry())
                .birthDate(dto.getBirthDate())
                .build();
    }

    public static Actor translateFromDto(@Valid ActorOutputDto dto) {
        return Actor.builder()
                .id(dto.getId())
                .name(dto.getName())
                .country(dto.getCountry())
                .country(dto.getCountry())
                .birthDate(dto.getBirthDate())
                .build();
    }

    public ActorOutputDto getInfoDto() {
        return ActorOutputDto.builder()
                .id(id)
                .name(name)
                .country(country)
                .birthDate(birthDate)
                .build();
    }
}

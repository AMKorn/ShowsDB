package com.andreas.showsdb.model;

import com.andreas.showsdb.model.dto.ActorDto;
import com.andreas.showsdb.model.dto.ActorDtoId;
import jakarta.persistence.*;
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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String country;
    @Column(name = "birth_date")
    private Date birthDate;

    @OneToMany(mappedBy = "actor")
    Set<MainCast> showsAsMainCast;

    public static Actor translateDto(ActorDto dto){
        return Actor.builder()
                .name(dto.getName())
                .country(dto.getCountry())
                .country(dto.getCountry())
                .birthDate(dto.getBirthDate())
                .build();
    }

    public static Actor translateDto(ActorDtoId dto){
        return Actor.builder()
                .id(dto.getId())
                .name(dto.getName())
                .country(dto.getCountry())
                .country(dto.getCountry())
                .birthDate(dto.getBirthDate())
                .build();
    }

    public ActorDto dto() {
        return ActorDto.builder()
                .name(name)
                .country(country)
                .birthDate(birthDate)
                .build();
    }

    public ActorDtoId dtoId() {
        return ActorDtoId.builder()
                .id(id)
                .name(name)
                .country(country)
                .birthDate(birthDate)
                .build();
    }
}

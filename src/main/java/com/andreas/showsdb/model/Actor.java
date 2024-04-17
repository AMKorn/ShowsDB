package com.andreas.showsdb.model;

import com.andreas.showsdb.model.dto.ActorInput;
import com.andreas.showsdb.model.dto.ActorInfo;
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

    public static Actor translateFromDto(ActorInput dto){
        return Actor.builder()
                .name(dto.getName())
                .country(dto.getCountry())
                .country(dto.getCountry())
                .birthDate(dto.getBirthDate())
                .build();
    }

    public static Actor translateFromDto(ActorInfo dto){
        return Actor.builder()
                .id(dto.getId())
                .name(dto.getName())
                .country(dto.getCountry())
                .country(dto.getCountry())
                .birthDate(dto.getBirthDate())
                .build();
    }

    public ActorInput dto() {
        return ActorInput.builder()
                .name(name)
                .country(country)
                .birthDate(birthDate)
                .build();
    }

    public ActorInfo dtoId() {
        return ActorInfo.builder()
                .id(id)
                .name(name)
                .country(country)
                .birthDate(birthDate)
                .build();
    }
}

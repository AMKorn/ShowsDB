package com.andreas.showsdb.model;

import com.andreas.showsdb.model.dto.MainCastDto;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "main_cast")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainCast {
    @EmbeddedId
    MainCastKey id;

    @ManyToOne
    @MapsId("actorId")
    @JoinColumn(name = "id_actor")
    Actor actor;

    @ManyToOne
    @MapsId("showId")
    @JoinColumn(name = "id_show")
    Show show;

    String character;

    public void copyInfo(@Valid MainCastDto mainCastDto) {
        this.character = mainCastDto.getCharacter();
    }

    public MainCastDto getInfoDto() {
        return MainCastDto.builder()
                .actorId(actor.getId())
                .showId(show.getId())
                .character(character)
                .build();
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MainCastKey implements Serializable {
        @Column(name = "id_actor")
        Long actorId;

        @Column(name = "id_show")
        Long showId;
    }
}

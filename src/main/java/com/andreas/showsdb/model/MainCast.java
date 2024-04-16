package com.andreas.showsdb.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "main_cast")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MainCast {
    @EmbeddedId
    MainCastKey id;

    @ManyToOne
    @MapsId("actorId")
    @JoinColumn(name = "idActor")
    Actor actor;

    @ManyToOne
    @MapsId("showId")
    @JoinColumn(name = "idShow", insertable = false, updatable = false)
    Show show;

    String character;

    public MainCast(Actor actor, Show show, String character) {
        this.actor = actor;
        this.show = show;
        this.character = character;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MainCastKey implements Serializable {
        @Column(name = "actorId")
        Long actorId;

        @Column(name = "showId")
        Long showId;
    }
}

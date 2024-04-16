package com.andreas.showsdb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
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
    @JsonIgnoreProperties({"showsAsMainCast"})
    Actor actor;

    @ManyToOne
    @MapsId("showId")
    @JoinColumn(name = "id_show")
    @JsonIgnoreProperties({"seasons", "mainCast"})
    Show show;

    String character;

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

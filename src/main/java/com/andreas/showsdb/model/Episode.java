package com.andreas.showsdb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(
        name = "episode",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"season", "episodeNumber"})
        }
)
@Data
@NoArgsConstructor
public class Episode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "season")
    @JsonIgnoreProperties(value = {"episodes"})
    private Season season;
    private Integer episodeNumber;
    private String name;
    private Date relDate;

    public Episode(Season season, Integer episodeNumber, String name, Date relDate) {
        this.season = season;
        this.episodeNumber = episodeNumber;
        this.name = name;
        this.relDate = relDate;
    }
}

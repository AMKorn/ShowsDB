package com.andreas.showsdb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "season",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"show", "seasonNumber"})
        }
)
@Data
@NoArgsConstructor
public class Season {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "show")
    @JsonIgnoreProperties(value = {"seasons"})
    private Show show;
    private Integer seasonNumber;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"season"})
    private List<Episode> episodes;

    public Season(Show show, Integer seasonNumber) {
        this.show = show;
        this.seasonNumber = seasonNumber;
    }
}

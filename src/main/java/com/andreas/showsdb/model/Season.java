package com.andreas.showsdb.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "season",
        uniqueConstraints = @UniqueConstraint(columnNames = {"show", "seasonNumber"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Season {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "show")
    private Show show;
    private Integer seasonNumber;

    public Season(Show show, Integer seasonNumber) {
        this.show = show;
        this.seasonNumber = seasonNumber;
    }
}

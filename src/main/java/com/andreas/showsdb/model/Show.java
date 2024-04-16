package com.andreas.showsdb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "show")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String country;

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"show"})
    private List<Season> seasons;

//    @OneToMany(mappedBy = "show")
//    @JsonIgnoreProperties({"show"})
//    Set<MainCast> mainCast;

    public Show(String name, String country) {
        this.name = name;
        this.country = country;
    }

    public Show(Long id, String name, String country) {
        this.id = id;
        this.name = name;
        this.country = country;
    }
}

package com.andreas.showsdb.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private List<Season> seasons;
    
    public Show(String name, String country) {
        this.name = name;
        this.country = country;
    }
}

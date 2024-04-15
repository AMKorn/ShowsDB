package com.andreas.showsdb.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "actor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Actor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String country;
    private Date birthDate;

    public Actor(String name, String country, Date birthDate) {
        this.name = name;
        this.country = country;
        this.birthDate = birthDate;
    }
}

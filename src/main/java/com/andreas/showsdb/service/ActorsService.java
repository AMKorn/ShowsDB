package com.andreas.showsdb.service;

import com.andreas.showsdb.model.Actor;

import java.util.List;
import java.util.Optional;

public interface ActorsService {

    List<Actor> findAll();

    Optional<Actor> findById(long id);

    Actor save(Actor actor);

    void deleteById(long id);
}

package com.andreas.showsdb.service;

import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.repository.ActorsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ActorsService {
    @Autowired
    private ActorsRepository actorsRepository;

    public List<Actor> findAll() {
        return actorsRepository.findAll();
    }

    public Optional<Actor> findById(long id) {
        return actorsRepository.findById(id);
    }

    public Actor save(Actor actor) {
        return actorsRepository.save(actor);
    }

    public void deleteById(long id) {
        actorsRepository.deleteById(id);
    }
}

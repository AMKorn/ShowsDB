package com.andreas.showsdb.service.jpa;

import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.repository.ActorsRepository;
import com.andreas.showsdb.service.ActorsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Primary
public class ActorsServiceJpa implements ActorsService {
    @Autowired
    private ActorsRepository actorsRepository;

    @Override
    public List<Actor> findAll() {
        return actorsRepository.findAll();
    }

    @Override
    public Optional<Actor> findById(long id) {
        return actorsRepository.findById(id);
    }

    @Override
    public Actor save(Actor actor) {
        return actorsRepository.save(actor);
    }

    @Override
    public void deleteById(long id) {
        actorsRepository.deleteById(id);
    }
}

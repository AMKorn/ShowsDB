package com.andreas.showsdb.service;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.model.dto.ActorInput;
import com.andreas.showsdb.model.dto.ActorInfo;
import com.andreas.showsdb.repository.ActorsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ActorsService {
    @Autowired
    private ActorsRepository actorsRepository;

    public List<ActorInfo> findAll() {
        return actorsRepository.findAll().stream()
                .map(Actor::dtoId)
                .toList();
    }

    public ActorInfo findById(long id) throws NotFoundException {
        return actorsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Actor not found"))
                .dtoId();
    }

    public ActorInfo save(ActorInput actorInput) {
        Actor actor = Actor.translateFromDto(actorInput);
        Actor saved = actorsRepository.save(actor);
        return saved.dtoId();
    }

    public ActorInfo modify(ActorInfo actorInfo) throws NotFoundException {
        Optional<Actor> optionalActor = actorsRepository.findById(actorInfo.getId());
        if (optionalActor.isEmpty()) {
            throw new NotFoundException("Actor not found");
        }

        Actor actor = Actor.translateFromDto(actorInfo);
        Actor saved = actorsRepository.save(actor);
        return saved.dtoId();
    }

    public void deleteById(long id) {
        actorsRepository.deleteById(id);
    }
}

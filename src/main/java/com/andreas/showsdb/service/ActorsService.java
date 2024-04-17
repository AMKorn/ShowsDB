package com.andreas.showsdb.service;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.model.dto.ActorDto;
import com.andreas.showsdb.model.dto.ActorDtoId;
import com.andreas.showsdb.repository.ActorsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ActorsService {
    @Autowired
    private ActorsRepository actorsRepository;

    public List<ActorDtoId> findAll() {
        return actorsRepository.findAll().stream()
                .map(Actor::dtoId)
                .toList();
    }

    public ActorDtoId findById(long id) throws NotFoundException {
        return actorsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Actor not found"))
                .dtoId();
    }

    public ActorDtoId save(ActorDto actorDto) {
        Actor actor = Actor.translateDto(actorDto);
        Actor saved = actorsRepository.save(actor);
        return saved.dtoId();
    }

    public ActorDtoId modify(ActorDtoId actorDtoId) throws NotFoundException {
        Optional<Actor> optionalActor = actorsRepository.findById(actorDtoId.getId());
        if (optionalActor.isEmpty()) {
            throw new NotFoundException("Actor not found");
        }

        Actor actor = Actor.translateDto(actorDtoId);
        Actor saved = actorsRepository.save(actor);
        return saved.dtoId();
    }

    public void deleteById(long id) {
        actorsRepository.deleteById(id);
    }
}

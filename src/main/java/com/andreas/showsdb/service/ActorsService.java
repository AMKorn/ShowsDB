package com.andreas.showsdb.service;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.model.dto.ActorInput;
import com.andreas.showsdb.model.dto.ActorInfo;
import com.andreas.showsdb.repository.ActorsRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ActorsService {
    @Autowired
    private ActorsRepository actorsRepository;

    public List<@Valid ActorInfo> findAll() {
        return actorsRepository.findAll().stream()
                .map(Actor::getInfoDto)
                .toList();
    }

    public @Valid ActorInfo findById(long id) throws NotFoundException {
        return actorsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Actor not found"))
                .getInfoDto();
    }

    public @Valid ActorInfo save(@Valid ActorInput actorInput) {
        Actor actor = Actor.translateFromDto(actorInput);
        Actor saved = actorsRepository.save(actor);
        return saved.getInfoDto();
    }

    public @Valid ActorInfo modify(@Valid ActorInfo actorInfo) throws NotFoundException {
        Optional<Actor> optionalActor = actorsRepository.findById(actorInfo.getId());
        if (optionalActor.isEmpty()) {
            throw new NotFoundException("Actor not found");
        }

        Actor actor = Actor.translateFromDto(actorInfo);
        Actor saved = actorsRepository.save(actor);
        return saved.getInfoDto();
    }

    public void deleteById(long id) {
        actorsRepository.deleteById(id);
    }
}

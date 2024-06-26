package com.andreas.showsdb.service;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.model.dto.ActorInputDto;
import com.andreas.showsdb.model.dto.ActorOutputDto;
import com.andreas.showsdb.repository.ActorsRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActorsService {
    private final ActorsRepository actorsRepository;

    @Cacheable("actors-cache")
    public List<ActorOutputDto> findAll() {
        return actorsRepository.findAll().stream()
                .map(Actor::getInfoDto)
                .toList();
    }

    @Cacheable("actors-cache")
    public ActorOutputDto findById(long id) throws NotFoundException {
        return actorsRepository.findById(id)
                .orElseThrow(NotFoundException::new)
                .getInfoDto();
    }

    @CacheEvict(cacheNames = {"actors-cache"}, allEntries = true)
    public ActorOutputDto save(@Valid ActorInputDto actorInputDto) {
        Actor actor = Actor.translateFromDto(actorInputDto);
        Actor saved = actorsRepository.save(actor);
        return saved.getInfoDto();
    }

    @CacheEvict(cacheNames = {"actors-cache"}, allEntries = true)
    public ActorOutputDto modify(@Valid ActorOutputDto actorOutputDto) throws NotFoundException {
        Optional<Actor> optionalActor = actorsRepository.findById(actorOutputDto.getId());
        if (optionalActor.isEmpty()) {
            throw new NotFoundException();
        }

        Actor actor = Actor.translateFromDto(actorOutputDto);
        Actor saved = actorsRepository.save(actor);
        return saved.getInfoDto();
    }

    @CacheEvict(cacheNames = {"actors-cache"}, allEntries = true)
    public void deleteById(long id) {
        actorsRepository.deleteById(id);
    }

    @CacheEvict(cacheNames = {"actors-cache"}, allEntries = true)
    public void clearCache() {
    }
}

package com.andreas.showsdb.service;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.exception.ShowsDatabaseException;
import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.model.MainCast;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.model.dto.MainCastDto;
import com.andreas.showsdb.repository.ActorsRepository;
import com.andreas.showsdb.repository.MainCastRepository;
import com.andreas.showsdb.repository.ShowsRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MainCastService {
    @Autowired
    private MainCastRepository mainCastRepository;
    @Autowired
    private ActorsRepository actorsRepository;
    @Autowired
    private ShowsRepository showsRepository;

    public List<@Valid MainCastDto> findAll() {
        return mainCastRepository.findAll().stream()
                .map(MainCast::getInfoDto)
                .toList();
    }

    public @Valid MainCastDto save(@Valid MainCastDto mainCastDto) throws ShowsDatabaseException {
        Actor actor = actorsRepository.findById(mainCastDto.getActorId())
                .orElseThrow(() -> new NotFoundException("Actor not found"));
        Show show = showsRepository.findById(mainCastDto.getShowId())
                .orElseThrow(() -> new NotFoundException("Show not found"));

        Optional<MainCast> optionalMainCast = mainCastRepository.findDistinctByActorAndShow(actor, show);
        if (optionalMainCast.isPresent()) {
            throw new ShowsDatabaseException("That actor is already in that show", HttpStatus.CONFLICT);
        }

        MainCast mainCast = MainCast.builder()
                .id(new MainCast.MainCastKey())
                .actor(actor)
                .show(show)
                .character(mainCastDto.getCharacter())
                .build();

        return mainCastRepository.save(mainCast).getInfoDto();
    }

    public @Valid MainCastDto modify(@Valid MainCastDto mainCastDto) throws NotFoundException {
        Actor actor = actorsRepository.findById(mainCastDto.getActorId())
                .orElseThrow(() -> new NotFoundException("Actor not found"));
        Show show = showsRepository.findById(mainCastDto.getShowId())
                .orElseThrow(() -> new NotFoundException("Show not found"));

        MainCast mainCast = mainCastRepository.findDistinctByActorAndShow(actor, show)
                .orElseThrow(() -> new NotFoundException("Main cast not found"));

        mainCast.copyInfo(mainCastDto);

        return mainCastRepository.save(mainCast).getInfoDto();
    }

    public List<@Valid MainCastDto> findByActor(long actorId) throws NotFoundException {
        Actor actor = actorsRepository.findById(actorId)
                .orElseThrow(() -> new NotFoundException("Actor not found"));

        return mainCastRepository.findByActor(actor).stream()
                .map(MainCast::getInfoDto)
                .toList();
    }

    public List<@Valid MainCastDto> findByShow(long showId) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));

        return mainCastRepository.findByShow(show).stream()
                .map(MainCast::getInfoDto)
                .toList();
    }

    public void delete(Long actorId, Long showId) throws NotFoundException {
        Actor actor = actorsRepository.findById(actorId)
                .orElseThrow(() -> new NotFoundException("Actor not found"));
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));

        mainCastRepository.deleteDistinctByActorAndShow(actor, show);
    }
}

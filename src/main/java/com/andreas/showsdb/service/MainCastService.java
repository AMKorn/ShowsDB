package com.andreas.showsdb.service;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.exception.ShowsDatabaseException;
import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.model.MainCast;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.model.dto.ActorInfo;
import com.andreas.showsdb.model.dto.MainCastInfo;
import com.andreas.showsdb.model.dto.ShowInfo;
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

    public List<@Valid MainCastInfo> findAll() {
        return mainCastRepository.findAll().stream()
                .map(MainCast::getInfoDto)
                .toList();
    }

    public @Valid MainCastInfo findByActorAndShow(Long actorId, Long showId) throws NotFoundException {
        Actor actor = actorsRepository.findById(actorId)
                .orElseThrow(() -> new NotFoundException("Actor not found"));
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));

        return mainCastRepository.findDistinctByActorAndShow(actor, show)
                .orElseThrow(() -> new NotFoundException("Main cast not found"))
                .getInfoDto();
    }

    public @Valid MainCastInfo save(@Valid MainCastInfo mainCastInfo) throws ShowsDatabaseException {
        Actor actor = actorsRepository.findById(mainCastInfo.getActorId())
                .orElseThrow(() -> new NotFoundException("Actor not found"));
        Show show = showsRepository.findById(mainCastInfo.getShowId())
                .orElseThrow(() -> new NotFoundException("Show not found"));

        Optional<MainCast> optionalMainCast = mainCastRepository.findDistinctByActorAndShow(actor, show);
        if(optionalMainCast.isPresent()) {
            throw new ShowsDatabaseException("That actor is already in that show", HttpStatus.CONFLICT);
        }

        MainCast mainCast = MainCast.builder()
                .id(new MainCast.MainCastKey())
                .actor(actor)
                .show(show)
                .character(mainCastInfo.getCharacter())
                .build();

        return mainCastRepository.save(mainCast).getInfoDto();
    }

    public @Valid MainCastInfo modify(@Valid MainCastInfo mainCastInfo) throws NotFoundException {
        Actor actor = actorsRepository.findById(mainCastInfo.getActorId())
                .orElseThrow(() -> new NotFoundException("Actor not found"));
        Show show = showsRepository.findById(mainCastInfo.getShowId())
                .orElseThrow(() -> new NotFoundException("Show not found"));

        MainCast mainCast = mainCastRepository.findDistinctByActorAndShow(actor, show)
                .orElseThrow(() -> new NotFoundException("Main cast not found"));

        mainCast.copyInfo(mainCastInfo);

        return mainCastRepository.save(mainCast).getInfoDto();
    }

    public List<@Valid MainCastInfo> findByActor(long actorId) throws NotFoundException {
        Actor actor = actorsRepository.findById(actorId)
                .orElseThrow(() -> new NotFoundException("Actor not found"));

        return mainCastRepository.findByActor(actor).stream()
                .map(MainCast::getInfoDto)
                .toList();
    }

    public List<@Valid MainCastInfo> findByShow(long showId) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));

        return mainCastRepository.findByShow(show).stream()
                .map(MainCast::getInfoDto)
                .toList();
    }
}

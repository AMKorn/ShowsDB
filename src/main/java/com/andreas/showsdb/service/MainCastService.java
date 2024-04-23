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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MainCastService {
    private final MainCastRepository mainCastRepository;
    private final ActorsRepository actorsRepository;
    private final ShowsRepository showsRepository;

    public MainCastService(MainCastRepository mainCastRepository,
                           ActorsRepository actorsRepository,
                           ShowsRepository showsRepository) {
        this.mainCastRepository = mainCastRepository;
        this.actorsRepository = actorsRepository;
        this.showsRepository = showsRepository;
    }

    public List<MainCastDto> findAll() {
        return mainCastRepository.findAll().stream()
                .map(MainCast::getInfoDto)
                .toList();
    }

    public MainCastDto save(@Valid MainCastDto mainCastDto) throws ShowsDatabaseException {
        Actor actor = actorsRepository.findById(mainCastDto.getActorId())
                .orElseThrow(() -> new NotFoundException("Actor not found"));
        Show show = showsRepository.findById(mainCastDto.getShowId())
                .orElseThrow(() -> new NotFoundException("Show not found"));

        Optional<MainCast> optionalMainCast = mainCastRepository.findDistinctByActorIdAndShowId(
                mainCastDto.getActorId(), mainCastDto.getShowId());
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

    public MainCastDto modify(@Valid MainCastDto mainCastDto) throws NotFoundException {

        MainCast mainCast = mainCastRepository.findDistinctByActorIdAndShowId(mainCastDto.getActorId(),
                        mainCastDto.getShowId())
                .orElseThrow(NotFoundException::new);

        mainCast.copyInfo(mainCastDto);

        return mainCastRepository.save(mainCast).getInfoDto();
    }

    public List<MainCastDto> findByActor(long actorId) {
        return mainCastRepository.findByActorId(actorId).stream()
                .map(MainCast::getInfoDto)
                .toList();
    }

    public List<MainCastDto> findByShow(long showId) {
        return mainCastRepository.findByShowId(showId).stream()
                .map(MainCast::getInfoDto)
                .toList();
    }

    public void delete(Long actorId, Long showId) {
        mainCastRepository.deleteDistinctByActorIdAndShowId(actorId, showId);
    }
}

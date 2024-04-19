package com.andreas.showsdb.service;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.model.dto.ShowInfo;
import com.andreas.showsdb.model.dto.ShowInput;
import com.andreas.showsdb.repository.SeasonsRepository;
import com.andreas.showsdb.repository.ShowsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShowsService {

    @Autowired
    private ShowsRepository showsRepository;

    @Autowired
    private SeasonsRepository seasonsRepository;

    @Autowired
    private EpisodesService episodesService;

    public List<ShowInfo> findAll() {
        return showsRepository.findAll().stream()
                .map(Show::getInfoDto)
                .toList();
    }

    public ShowInfo findById(long id) throws NotFoundException {
        return showsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Show not found"))
                .getInfoDto();
    }

    public ShowInfo save(ShowInput showInput) {
        Show show = Show.translateFromDto(showInput);
        return showsRepository.save(show).getInfoDto();
    }

    public ShowInfo modify(ShowInfo showInfo) throws NotFoundException {
        Optional<Show> optionalShow = showsRepository.findById(showInfo.getId());
        if (optionalShow.isEmpty()) {
            throw new NotFoundException("Show not found");
        }

        Show actor = Show.translateFromDto(showInfo);
        Show saved = showsRepository.save(actor);
        return saved.getInfoDto();
    }

    public void deleteById(long id) {
        showsRepository.deleteById(id);
    }
}

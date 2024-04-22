package com.andreas.showsdb.service;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.model.dto.ShowOutputDto;
import com.andreas.showsdb.model.dto.ShowInputDto;
import com.andreas.showsdb.repository.ShowsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShowsService {

    @Autowired
    private ShowsRepository showsRepository;

    public List<ShowOutputDto> findAll() {
        return showsRepository.findAll().stream()
                .map(Show::getInfoDto)
                .toList();
    }

    public ShowOutputDto findById(long id) throws NotFoundException {
        return showsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Show not found"))
                .getInfoDto();
    }

    public ShowOutputDto save(ShowInputDto showInputDto) {
        Show show = Show.translateFromDto(showInputDto);
        try {
            return showsRepository.save(show).getInfoDto();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public ShowOutputDto modify(ShowOutputDto showOutputDto) throws NotFoundException {
        Optional<Show> optionalShow = showsRepository.findById(showOutputDto.getId());
        if (optionalShow.isEmpty()) {
            throw new NotFoundException("Show not found");
        }

        Show show = Show.translateFromDto(showOutputDto);
        Show saved = showsRepository.save(show);
        return saved.getInfoDto();
    }

    public void deleteById(long id) {
        showsRepository.deleteById(id);
    }
}

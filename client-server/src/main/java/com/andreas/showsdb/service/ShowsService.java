package com.andreas.showsdb.service;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.model.dto.ShowInputDto;
import com.andreas.showsdb.model.dto.ShowOutputDto;
import com.andreas.showsdb.repository.ShowsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShowsService {

    private final ShowsRepository showsRepository;


    public List<ShowOutputDto> findAll() {
        return showsRepository.findAll().stream()
                .map(Show::getInfoDto)
                .toList();
    }

    public ShowOutputDto findById(long id) throws NotFoundException {
        return showsRepository.findById(id)
                .orElseThrow(NotFoundException::new)
                .getInfoDto();
    }

    public ShowOutputDto save(ShowInputDto showInputDto) {
        Show show = Show.translateFromDto(showInputDto);
        return showsRepository.save(show).getInfoDto();
    }

    public ShowOutputDto modify(ShowOutputDto showOutputDto) throws NotFoundException {
        Optional<Show> optionalShow = showsRepository.findById(showOutputDto.getId());
        if (optionalShow.isEmpty()) throw new NotFoundException();

        Show show = Show.translateFromDto(showOutputDto);
        Show saved = showsRepository.save(show);
        return saved.getInfoDto();
    }

    public void deleteById(long id) {
        showsRepository.deleteById(id);
    }
}

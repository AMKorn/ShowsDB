package com.andreas.showsdb.service;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.exception.ShowsDatabaseException;
import com.andreas.showsdb.model.Episode;
import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.model.dto.ShowInputDto;
import com.andreas.showsdb.model.dto.ShowOutputDto;
import com.andreas.showsdb.repository.EpisodesRepository;
import com.andreas.showsdb.repository.SeasonsRepository;
import com.andreas.showsdb.repository.ShowsRepository;
import com.andreas.showsdb.statemachine.ShowStateMachine;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.andreas.showsdb.model.Show.State.UNRELEASED;

@Service
@RequiredArgsConstructor
public class ShowsService {

    private final ShowsRepository showsRepository;
    private final SeasonsRepository seasonsRepository;
    private final EpisodesRepository episodesRepository;

    @Cacheable("shows-cache")
    public List<ShowOutputDto> findAll() {
        return showsRepository.findAll().stream()
                .map(Show::getInfoDto)
                .toList();
    }

    @Cacheable("shows-cache")
    public ShowOutputDto findById(long id) throws NotFoundException {
        return showsRepository.findById(id)
                .orElseThrow(NotFoundException::new)
                .getInfoDto();
    }

    @CacheEvict(cacheNames = {"shows-cache"}, allEntries = true)
    public ShowOutputDto save(ShowInputDto showInputDto) {
        Show show = Show.translateFromDto(showInputDto);
        return showsRepository.save(show).getInfoDto();
    }

    @CacheEvict(cacheNames = {"shows-cache"}, allEntries = true)
    public ShowOutputDto modify(ShowOutputDto showOutputDto) throws NotFoundException {
        Optional<Show> optionalShow = showsRepository.findById(showOutputDto.getId());
        if (optionalShow.isEmpty()) throw new NotFoundException();

        Show show = Show.translateFromDto(showOutputDto);
        Show saved = showsRepository.save(show);
        return saved.getInfoDto();
    }

    @CacheEvict(cacheNames = {"shows-cache"}, allEntries = true)
    public void deleteById(long id) {
        showsRepository.deleteById(id);
    }

    @CacheEvict(cacheNames = {"shows-cache"}, allEntries = true)
    public void clearCache() {
    }

    public byte[] getAsCsvFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name,Country,Show").append("\n");
        showsRepository.findAll().stream()
                .map(Show::getInfoDto)
                .forEach(show -> sb.append(show.getName())
                        .append(",")
                        .append(show.getCountry())
                        .append(",")
                        .append(show.getNumberOfSeasons())
                        .append("\n"));
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] getAsXlsFile() throws ShowsDatabaseException {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Shows");

            Row firstRow = sheet.createRow(0);
            firstRow.createCell(0).setCellValue("Name");
            firstRow.createCell(1).setCellValue("Country");
            firstRow.createCell(2).setCellValue("Seasons");


            List<ShowOutputDto> shows = showsRepository.findAll().stream()
                    .map(Show::getInfoDto)
                    .toList();
            for (int i = 0, showsSize = shows.size(); i < showsSize; i++) {
                ShowOutputDto show = shows.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(show.getName());
                row.createCell(1).setCellValue(show.getCountry());
                row.createCell(2).setCellValue(show.getNumberOfSeasons());
            }

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                wb.write(outputStream);
                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            throw new ShowsDatabaseException("Something went wrong when creating the xls file",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ShowOutputDto updateState(Long id) throws ShowsDatabaseException {
        Show show = showsRepository.findById(id).orElseThrow(NotFoundException::new);
        Show.State state = show.getState();
        if (state == null) {
            state = UNRELEASED;
        }

        StateMachine<Show.State, Show.Event> showStateMachine = ShowStateMachine.create(state);
        showStateMachine.startReactively().subscribe();

        Season firstSeason = seasonsRepository.findByShowId(id).getFirst();
        if (firstSeason == null) {
            return show.getInfoDto();
        }

        Episode firstEpisode =
                episodesRepository.findBySeasonShowIdAndSeasonNumber(id, firstSeason.getNumber()).getFirst();
        if (firstEpisode != null
            && firstEpisode.getReleaseDate() != null
            && firstEpisode.getReleaseDate().before(new Date())) {
            ShowStateMachine.sendMachineStateEvent(showStateMachine, Show.Event.AIRS);
        }
        Show.State newState = showStateMachine.getState().getId();
        show.setState(newState);
        return showsRepository.save(show).getInfoDto();
    }
}

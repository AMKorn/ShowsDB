package com.andreas.showsdb.controller;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.dto.EpisodeInputDto;
import com.andreas.showsdb.model.dto.EpisodeOutputDto;
import com.andreas.showsdb.service.EpisodesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/shows/{showId}/seasons/{seasonNumber}/episodes")
public class EpisodesController {
    private final EpisodesService episodesService;

    public EpisodesController(EpisodesService episodesService) {
        this.episodesService = episodesService;
    }

    @Operation(summary = "Create an episode",
            description = """
                    Creates an empty episode whose episode number is the one after the last episode
                    (highest episode number in a season).
                                        
                    Alternatively, can create an episode passed through body, in which case it checks for duplicates
                    (same show, same season, same episode number). If a duplicate is found, the episode is not created
                    but is sent as part of the response body.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Episode created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EpisodeOutputDto.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Show or season not found"
            ),
            @ApiResponse(responseCode = "409",
                    description = "Episode already exists. Returns old episode",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EpisodeOutputDto.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<EpisodeOutputDto> create(@Parameter(description = "Id of the show")
                                                   @PathVariable("showId") long showId,
                                                   @Parameter(description = "Season number")
                                                   @PathVariable("seasonNumber") int seasonNumber,
                                                   @RequestBody(required = false) EpisodeInputDto episodeInputDto)
            throws NotFoundException {
        if (episodeInputDto == null) {
            EpisodeOutputDto savedEpisode = episodesService.createInSeason(showId, seasonNumber);
            return new ResponseEntity<>(savedEpisode, HttpStatus.CREATED);
        }

        // In case no episode number was stated, we put it by default as the next episode in the season.
        Integer episodeNumber = episodeInputDto.getEpisodeNumber();
        if (episodeNumber == null) {
            try {
                episodeNumber = episodesService.findBySeason(showId, seasonNumber).stream()
                        .max(Comparator
                                .comparingLong(EpisodeOutputDto::getShowId)
                                .thenComparingInt(EpisodeOutputDto::getSeasonNumber)
                                .thenComparingInt(EpisodeOutputDto::getEpisodeNumber))
                        .orElseThrow()
                        .getEpisodeNumber() + 1;
            } catch (NoSuchElementException e) {
                episodeNumber = 1;
            }
            EpisodeInputDto oldEpisodeInputDto = episodeInputDto;

            episodeInputDto = EpisodeInputDto.builder()
                    .episodeNumber(episodeNumber)
                    .name(oldEpisodeInputDto.getName())
                    .releaseDate(oldEpisodeInputDto.getReleaseDate())
                    .build();
        }

        try {
            EpisodeOutputDto savedEpisode = episodesService.save(showId, seasonNumber, episodeInputDto);
            return new ResponseEntity<>(savedEpisode, HttpStatus.CREATED);
        } catch (DataIntegrityViolationException e) {
            EpisodeInputDto finalEpisodeInputDto = episodeInputDto;
            Optional<EpisodeOutputDto> optionalEpisode = episodesService.findBySeason(showId, seasonNumber).stream()
                    .filter(ep -> ep.getEpisodeNumber().equals(finalEpisodeInputDto.getEpisodeNumber()))
                    .findFirst();
            return new ResponseEntity<>(optionalEpisode.orElseThrow(), HttpStatus.CONFLICT);
        }
    }

    @Operation(summary = "Find an episode")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Episode found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EpisodeOutputDto.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Show, season or episode not found"
            )
    })
    @GetMapping("/{episodeNumber}")
    public EpisodeOutputDto get(@Parameter(description = "Id of the show")
                                @PathVariable("showId") long showId,
                                @Parameter(description = "Season number")
                                @PathVariable("seasonNumber") int seasonNumber,
                                @Parameter(description = "Episode number")
                                @PathVariable("episodeNumber") int episodeNumber) throws NotFoundException {
        return episodesService.findByShowAndSeasonAndEpisodeNumbers(showId, seasonNumber, episodeNumber);
    }

    @Operation(summary = "Find all episodes from a season")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Episodes found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = EpisodeOutputDto.class)
                            )
                    )
            )
    })
    @GetMapping
    public List<EpisodeOutputDto> getAllFromSeason(@Parameter(description = "Id of the show")
                                                   @PathVariable("showId") long showId,
                                                   @Parameter(description = "Season number")
                                                   @PathVariable("seasonNumber") int seasonNumber) {
        return episodesService.findBySeason(showId, seasonNumber);
    }

    @Operation(summary = "Modify an episode")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Episode modified",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EpisodeOutputDto.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Show, season or episode not found"
            )
    })
    @PutMapping
    public EpisodeOutputDto modify(@Parameter(description = "Id of the show")
                                   @PathVariable("showId") long showId,
                                   @Parameter(description = "Season number")
                                   @PathVariable("seasonNumber") int seasonNumber,
                                   @RequestBody EpisodeInputDto episodeInputDto) throws NotFoundException {
        return episodesService.modify(showId, seasonNumber, episodeInputDto);
    }

    @Operation(summary = "Delete an episode")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Episode deleted"
            )
    })
    @DeleteMapping("/{episodeNumber}")
    public void delete(@Parameter(description = "Id of the show")
                       @PathVariable("showId") long showId,
                       @Parameter(description = "Season number")
                       @PathVariable("seasonNumber") int seasonNumber,
                       @Parameter(description = "Episode number")
                       @PathVariable("episodeNumber") int episodeNumber) {
        episodesService.deleteByShowAndSeasonAndEpisodeNumbers(showId, seasonNumber, episodeNumber);
    }

    @Operation(summary = "Delete all episodes from a season")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Episodes deleted"
            )
    })
    @DeleteMapping
    public void deleteAll(@Parameter(description = "Id of the show")
                          @PathVariable("showId") long showId,
                          @Parameter(description = "Season number")
                          @PathVariable("seasonNumber") int seasonNumber) {
        episodesService.deleteAllBySeason(showId, seasonNumber);
    }
}
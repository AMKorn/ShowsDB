package com.andreas.showsdb.controller;

import com.andreas.showsdb.exception.ExceptionMessage;
import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.dto.EpisodeInfo;
import com.andreas.showsdb.model.dto.EpisodeInput;
import com.andreas.showsdb.service.EpisodesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private EpisodesService episodesService;

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
                            schema = @Schema(implementation = EpisodeInfo.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Show or season not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionMessage.class)
                    )
            ),
            @ApiResponse(responseCode = "409",
                    description = "Episode already exists. Returns old episode",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EpisodeInfo.class)
                    )
            )
    })
    @PostMapping("")
    public ResponseEntity<?> create(@Parameter(description = "Id of the show")
                                    @PathVariable("showId") long showId,
                                    @Parameter(description = "Season number")
                                    @PathVariable("seasonNumber") int seasonNumber,
                                    @RequestBody(required = false) EpisodeInput episodeInput) {
        try {
            if (episodeInput == null) {
                EpisodeInfo savedEpisode = episodesService.createInSeason(showId, seasonNumber);
                return new ResponseEntity<>(savedEpisode, HttpStatus.CREATED);
            }

            // In case no episode number was stated, we put it by default as the next episode in the season.
            Integer episodeNumber = episodeInput.getEpisodeNumber();
            if (episodeNumber == null) {
                try {
                    episodeNumber = episodesService.findBySeason(showId, seasonNumber).stream()
                            .max(Comparator
                                    .comparingLong(EpisodeInfo::getShowId)
                                    .thenComparingInt(EpisodeInfo::getSeasonNumber)
                                    .thenComparingInt(EpisodeInfo::getEpisodeNumber))
                            .orElseThrow()
                            .getEpisodeNumber() + 1;
                } catch (NoSuchElementException e) {
                    episodeNumber = 1;
                }
                EpisodeInput oldEpisodeInput = episodeInput;

                episodeInput = EpisodeInput.builder()
                        .episodeNumber(episodeNumber)
                        .name(oldEpisodeInput.getName())
                        .releaseDate(oldEpisodeInput.getReleaseDate())
                        .build();
            }

            try {
                EpisodeInfo savedEpisode = episodesService.save(showId, seasonNumber, episodeInput);
                return new ResponseEntity<>(savedEpisode, HttpStatus.CREATED);
            } catch (DataIntegrityViolationException e) {
                EpisodeInput finalEpisodeInput = episodeInput;
                Optional<EpisodeInfo> optionalEpisode = episodesService.findBySeason(showId, seasonNumber).stream()
                        .filter(ep -> ep.getEpisodeNumber().equals(finalEpisodeInput.getEpisodeNumber()))
                        .findFirst();
                return new ResponseEntity<>(optionalEpisode.orElseThrow(), HttpStatus.CONFLICT);
            }
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @Operation(summary = "Find an episode")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Episode found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EpisodeInfo.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Show, season or episode not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionMessage.class)
                    )
            )
    })
    @GetMapping("/{episodeNumber}")
    public ResponseEntity<?> get(@Parameter(description = "Id of the show")
                                 @PathVariable("showId") long showId,
                                 @Parameter(description = "Season number")
                                 @PathVariable("seasonNumber") int seasonNumber,
                                 @Parameter(description = "Episode number")
                                 @PathVariable("episodeNumber") int episodeNumber) {
        try {
            EpisodeInfo episodeInfo =
                    episodesService.findByShowAndSeasonAndEpisodeNumbers(showId, seasonNumber, episodeNumber);
            return ResponseEntity.ok(episodeInfo);
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @Operation(summary = "Find all episodes from a season")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Episodes found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = EpisodeInfo.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Show or season not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionMessage.class)
                    )
            )
    })
    @GetMapping("")
    public ResponseEntity<?> getAllFromSeason(@Parameter(description = "Id of the show")
                                              @PathVariable("showId") long showId,
                                              @Parameter(description = "Season number")
                                              @PathVariable("seasonNumber") int seasonNumber) {
        try {
            List<EpisodeInfo> episodesInfo = episodesService.findBySeason(showId, seasonNumber);
            return ResponseEntity.ok(episodesInfo);
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @Operation(summary = "Modify an episode")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Episode modified",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EpisodeInfo.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Show, season or episode not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionMessage.class)
                    )
            )
    })
    @PutMapping("")
    public ResponseEntity<?> modify(@Parameter(description = "Id of the show")
                                    @PathVariable("showId") long showId,
                                    @Parameter(description = "Season number")
                                    @PathVariable("seasonNumber") int seasonNumber,
                                    @RequestBody EpisodeInput episodeInput) {
        try {
            EpisodeInfo modifiedEpisode = episodesService.modify(showId, seasonNumber, episodeInput);
            return ResponseEntity.ok(modifiedEpisode);
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @Operation(summary = "Delete an episode")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Episode deleted"
            ),
            @ApiResponse(responseCode = "404",
                    description = "Show, season or episode not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionMessage.class)
                    )
            )
    })
    @DeleteMapping("/{episodeNumber}")
    public ResponseEntity<?> delete(@Parameter(description = "Id of the show")
                                    @PathVariable("showId") long showId,
                                    @Parameter(description = "Season number")
                                    @PathVariable("seasonNumber") int seasonNumber,
                                    @Parameter(description = "Episode number")
                                    @PathVariable("episodeNumber") int episodeNumber) {
        try {
            episodesService.deleteByShowAndSeasonAndEpisodeNumbers(showId, seasonNumber, episodeNumber);
            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @Operation(summary = "Delete all episodes from a season")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Episodes deleted"
            ),
            @ApiResponse(responseCode = "404",
                    description = "Show or season not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionMessage.class)
                    )
            )
    })
    @DeleteMapping("")
    public ResponseEntity<?> deleteAll(@Parameter(description = "Id of the show")
                                       @PathVariable("showId") long showId,
                                       @Parameter(description = "Season number")
                                       @PathVariable("seasonNumber") int seasonNumber) {
        try {
            episodesService.deleteAllBySeason(showId, seasonNumber);
            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }
}

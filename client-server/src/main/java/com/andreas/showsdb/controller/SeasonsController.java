package com.andreas.showsdb.controller;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.dto.SeasonInputDto;
import com.andreas.showsdb.model.dto.SeasonOutputDto;
import com.andreas.showsdb.model.dto.hateoas.SeasonHypermedia;
import com.andreas.showsdb.service.SeasonsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/shows/{showId}/seasons")
@RequiredArgsConstructor
public class SeasonsController {
    private final SeasonsService seasonsService;

    @Operation(summary = "Find all seasons from a show")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seasons found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = SeasonHypermedia.class))))})
    @GetMapping
    public List<SeasonHypermedia> getAllByShow(@Parameter(description = "Id of the show")
                                               @PathVariable("showId") long showId) {
        return seasonsService.findByShow(showId).stream()
                .map(SeasonsController::createSeasonHypermedia)
                .toList();
    }

    @Operation(summary = "Create a season",
            description = """
                    Creates an empty season whose season number is the one after the last season
                    (highest season number in a show).
                    \s
                    Alternatively, can create a season passed through body, in which case it checks for duplicates
                    (same show, same season number). If a duplicate is found, the season is not created
                    but is sent as part of the response body.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Season created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SeasonHypermedia.class))),
            @ApiResponse(responseCode = "404", description = "Show not found"),
            @ApiResponse(responseCode = "409", description = "Season already exists. Returns old season",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SeasonHypermedia.class)))})
    @PostMapping
    public ResponseEntity<SeasonHypermedia> create(@Parameter(description = "Id of the show")
                                                   @PathVariable("showId") long showId,
                                                   @RequestBody(required = false) SeasonInputDto seasonInputDto)
            throws NotFoundException {
        if (seasonInputDto == null || seasonInputDto.getSeasonNumber() == null) {
            SeasonOutputDto savedSeason = seasonsService.createInShow(showId);
            SeasonHypermedia seasonHypermedia = createSeasonHypermedia(savedSeason);
            return new ResponseEntity<>(seasonHypermedia, HttpStatus.CREATED);
        }

        try {
            SeasonOutputDto savedSeason = seasonsService.save(showId, seasonInputDto);
            SeasonHypermedia seasonHypermedia = createSeasonHypermedia(savedSeason);
            return new ResponseEntity<>(seasonHypermedia, HttpStatus.CREATED);
        } catch (DataIntegrityViolationException e) {
            Optional<SeasonHypermedia> optionalSeason = seasonsService.findByShow(showId).stream()
                    .filter(s -> s.getSeasonNumber().equals(seasonInputDto.getSeasonNumber()))
                    .map(SeasonsController::createSeasonHypermedia)
                    .findFirst();
            return new ResponseEntity<>(optionalSeason.orElseThrow(), HttpStatus.CONFLICT);
        }
    }

    @Operation(summary = "Find a season")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Season found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SeasonHypermedia.class))),
            @ApiResponse(responseCode = "404", description = "Show or season not found")})
    @GetMapping("/{seasonNumber}")
    public SeasonHypermedia get(@Parameter(description = "Id of the show")
                                @PathVariable("showId") long showId,
                                @PathVariable("seasonNumber") int seasonNumber) throws NotFoundException {
        SeasonOutputDto season = seasonsService.findByShowAndNumber(showId, seasonNumber);
        return createSeasonHypermedia(season);
    }

    @Operation(summary = "Delete a season")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Season deleted")})
    @DeleteMapping("/{seasonNumber}")
    public void delete(@Parameter(description = "Id of the show")
                       @PathVariable("showId") long showId,
                       @PathVariable("seasonNumber") Integer seasonNumber) {
        seasonsService.delete(showId, seasonNumber);
    }

    @Operation(summary = "Delete all seasons from a show")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seasons deleted")})
    @DeleteMapping
    public void deleteSeasons(@Parameter(description = "Id of the show")
                              @PathVariable("showId") long showId) {
        seasonsService.deleteByShow(showId);
    }

    private static SeasonHypermedia createSeasonHypermedia(SeasonOutputDto season) {
        SeasonHypermedia seasonHypermedia = new SeasonHypermedia(season);
        Long showId = season.getShowId();
        Integer seasonNumber = season.getSeasonNumber();
        try {
            seasonHypermedia.add(linkTo(methodOn(SeasonsController.class).get(showId, seasonNumber)).withSelfRel());
            seasonHypermedia.add(linkTo(methodOn(ShowsController.class).get(showId)).withRel("Show"));
            seasonHypermedia.add(linkTo(methodOn(EpisodesController.class)
                    .getAllFromSeason(showId, seasonNumber))
                    .withRel("Episodes"));
        } catch (NotFoundException e) {
            // Can't happen
        }
        return seasonHypermedia;
    }
}

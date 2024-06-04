package com.andreas.showsdb.controller;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.dto.ActorInputDto;
import com.andreas.showsdb.model.dto.ActorOutputDto;
import com.andreas.showsdb.model.dto.MainCastDto;
import com.andreas.showsdb.model.dto.hateoas.ActorHypermedia;
import com.andreas.showsdb.model.dto.hateoas.MainCastHypermedia;
import com.andreas.showsdb.service.ActorsService;
import com.andreas.showsdb.service.MainCastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/actors")
@RequiredArgsConstructor
public class ActorsController {

    private final ActorsService actorsService;
    private final MainCastService mainCastService;

    @Operation(summary = "List all actors")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ActorHypermedia.class))))})
    @GetMapping
    public List<ActorHypermedia> getAll() {
        return actorsService.findAll().stream()
                .map(ActorHypermedia::new)
                .peek(actor -> {
                    try {
                        Long actorId = actor.getContent().getId();
                        actor.add(linkTo(methodOn(ActorsController.class).get(actorId)).withSelfRel());
                        actor.add(linkTo(methodOn(ActorsController.class).getShows(actorId)).withRel("Shows"));
                    } catch (NotFoundException ignored) {
                        // Not possible to happen, here to shut up the compiler
                    }
                })
                .toList();
    }

    @Operation(summary = "Find an actor given the specified id by parameter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actor found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ActorHypermedia.class))),
            @ApiResponse(responseCode = "404", description = "Actor not found")})
    @GetMapping("/{actorId}")
    public ActorHypermedia get(@Parameter(description = "Id of the actor to be found")
                               @PathVariable("actorId") long id) throws NotFoundException {
        ActorHypermedia actorHypermedia = new ActorHypermedia(actorsService.findById(id));
        actorHypermedia.add(linkTo(methodOn(ActorsController.class).get(id)).withSelfRel());
        actorHypermedia.add(linkTo(methodOn(ActorsController.class).getShows(id)).withRel("Shows"));
        return actorHypermedia;
    }

    @Operation(summary = "Create an actor passed through body. Does not check for duplicates")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Actor created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ActorHypermedia.class)))})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActorHypermedia create(@RequestBody ActorInputDto actor) {
        ActorOutputDto savedActor = actorsService.save(actor);
        ActorHypermedia actorHypermedia = new ActorHypermedia(savedActor);
        try {
            actorHypermedia.add(linkTo(methodOn(ActorsController.class).get(savedActor.getId())).withSelfRel());
        } catch (NotFoundException ignored) {
            // Not possible to happen
        }
        return actorHypermedia;
    }

    @Operation(summary = "Modify an actor passed through body")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actor found and modified",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ActorOutputDto.class))),
            @ApiResponse(responseCode = "404", description = "Actor not found")})
    @PutMapping
    public ActorHypermedia modify(@RequestBody ActorOutputDto actor) throws NotFoundException {
        ActorOutputDto modifiedActor = actorsService.modify(actor);
        ActorHypermedia actorHypermedia = new ActorHypermedia(modifiedActor);
        try {
            Long id = modifiedActor.getId();
            actorHypermedia.add(linkTo(methodOn(ActorsController.class).get(id)).withSelfRel());
            actorHypermedia.add(linkTo(methodOn(ActorsController.class).getShows(id)).withSelfRel());
        } catch (NotFoundException ignored) {
            // Not possible to happen
        }
        return actorHypermedia;
    }

    @Operation(summary = "Delete an actor given the specified id by parameter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actor deleted")})
    @DeleteMapping("/{actorId}")
    public void delete(@Parameter(description = "Id of the actor to be deleted")
                       @PathVariable("actorId") long id) {
        actorsService.deleteById(id);
    }

    @Operation(summary = "Find all the show ids in which an actor was main cast and the characters they played")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shows and characters found",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = MainCastDto.class)))),
            @ApiResponse(responseCode = "404", description = "Actor not found")})
    @GetMapping("/{actorId}/shows")
    public List<MainCastHypermedia> getShows(@PathVariable("actorId") long id) {
        return mainCastService.findByActor(id).stream()
                .map(MainCastHypermedia::new)
                .peek(mainCast -> {
                    try {
                        mainCast.add(linkTo(methodOn(ShowsController.class).get(mainCast.getContent().getShowId()))
                                .withRel("Show"));
                    } catch (NotFoundException e) {
                        // Cannot happen
                    }

                })
                .toList();
    }
}

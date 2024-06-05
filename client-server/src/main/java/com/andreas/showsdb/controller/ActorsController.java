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
import lombok.SneakyThrows;
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

    @SneakyThrows // This method cannot and will not throw an exception, but the compiler doesn't know that.
    private static ActorHypermedia addLinks(ActorOutputDto actor) {
        ActorHypermedia ah = new ActorHypermedia(actor);
        Long actorId = actor.getId();
        ah.add(linkTo(methodOn(ActorsController.class).get(actorId)).withSelfRel());
        ah.add(linkTo(methodOn(ActorsController.class).getShows(actorId)).withRel("shows"));
        return ah;
    }

    @Operation(summary = "List all actors")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ActorHypermedia.class))))})
    @GetMapping
    public List<ActorHypermedia> getAll() {
        return actorsService.findAll().stream()
                .map(ActorsController::addLinks)
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
        ActorOutputDto actor = actorsService.findById(id);
        return addLinks(actor);
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
        return addLinks(savedActor);
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
        return addLinks(modifiedActor);
    }

    @Operation(summary = "Delete an actor given the specified id by parameter")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Actor deleted")})
    @DeleteMapping("/{actorId}")
    public void delete(@Parameter(description = "Id of the actor to be deleted")
                       @PathVariable("actorId") long id) {
        actorsService.deleteById(id);
    }

    @Operation(summary = "Find all the show ids in which an actor was main cast and the characters they played")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shows and characters found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MainCastDto.class)))),
            @ApiResponse(responseCode = "404", description = "Actor not found")})
    @GetMapping("/{actorId}/shows")
    public List<MainCastHypermedia> getShows(@PathVariable("actorId") long id) {
        return mainCastService.findByActor(id).stream()
                .map(MainCastHypermedia::new)
                .peek(mainCast -> {
                    try {
                        mainCast.add(linkTo(methodOn(ShowsController.class).get(mainCast.getContent().getShowId()))
                                .withRel("show"));
                        mainCast.add(linkTo(methodOn(ActorsController.class).get(mainCast.getContent().getActorId()))
                                .withRel("actor"));
                    } catch (NotFoundException e) {
                        // This line cannot and will not throw an exception, but the compiler doesn't know that.
                    }
                })
                .toList();
    }

    @Operation(summary = "Clear all the cache for actors", description = """
            Should not be necessary, as any modifications to the relevant tables in the database will also clear cache, 
            but it's better to have it than not.""")
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "Cache cleared"))
    @DeleteMapping("/cache")
    public void clearCache() {
        actorsService.clearCache();
    }
}

package com.andreas.showsdb.controller;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.dto.ActorInputDto;
import com.andreas.showsdb.model.dto.ActorOutputDto;
import com.andreas.showsdb.model.dto.MainCastDto;
import com.andreas.showsdb.service.ActorsService;
import com.andreas.showsdb.service.MainCastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/actors")
public class ActorsController {

    private final ActorsService actorsService;

    private final MainCastService mainCastService;

    public ActorsController(ActorsService actorsService, MainCastService mainCastService) {
        this.actorsService = actorsService;
        this.mainCastService = mainCastService;
    }

    @Operation(summary = "List all actors")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ActorOutputDto.class)
                            )
                    )
            )
    })
    @GetMapping
    public List<ActorOutputDto> getAll() {
        return actorsService.findAll();
    }

    @Operation(summary = "Find an actor given the specified id by parameter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Actor found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ActorOutputDto.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Actor not found"
            )
    })
    @GetMapping("/{actorId}")
    public ActorOutputDto get(@Parameter(description = "Id of the actor to be found")
                              @PathVariable("actorId") long id) throws NotFoundException {
        return actorsService.findById(id);
    }

    @Operation(summary = "Create an actor passed through body. Does not check for duplicates")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Actor created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ActorOutputDto.class)
                    )
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActorOutputDto create(@RequestBody ActorInputDto actor) {
        return actorsService.save(actor);
    }

    @Operation(summary = "Modify an actor passed through body")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Actor found and modified",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ActorOutputDto.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Actor not found"
            )
    })
    @PutMapping
    public ActorOutputDto modify(@RequestBody ActorOutputDto actor) throws NotFoundException {
        return actorsService.modify(actor);
    }

    @Operation(summary = "Delete an actor given the specified id by parameter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Actor deleted"
            )
    })
    @DeleteMapping("/{actorId}")
    public void delete(@Parameter(description = "Id of the actor to be deleted")
                       @PathVariable("actorId") long id) {
        actorsService.deleteById(id);
    }

    @Operation(summary = "Find all the show ids in which an actor was main cast and the characters they played")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Shows and characters found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = MainCastDto.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Actor not found"
            )
    })
    @GetMapping("/{actorId}/shows")
    public List<MainCastDto> getShows(@PathVariable("actorId") long id) {
        return mainCastService.findByActor(id);
    }
}
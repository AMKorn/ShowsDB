package com.andreas.showsdb.controller;

import com.andreas.showsdb.exception.ExceptionMessage;
import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.dto.ActorInfo;
import com.andreas.showsdb.model.dto.ActorInput;
import com.andreas.showsdb.model.dto.MainCastInfo;
import com.andreas.showsdb.service.ActorsService;
import com.andreas.showsdb.service.MainCastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/actors")
public class ActorsController {

    @Autowired
    private ActorsService actorsService;

    @Autowired
    private MainCastService mainCastService;

    @Operation(summary = "List all actors")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ActorInfo.class)
                            )
                    )
            )
    })
    @GetMapping("")
    public List<ActorInfo> getAll() {
        return actorsService.findAll();
    }

    @Operation(summary = "Find an actor given the specified id by parameter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Actor found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ActorInfo.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Actor not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionMessage.class)
                    )
            )
    })
    @GetMapping("/{actorId}")
    public ResponseEntity<?> get(@Parameter(description = "Id of the actor to be found")
                                 @PathVariable("actorId") long id) {
        try {
            return ResponseEntity.ok(actorsService.findById(id));
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @Operation(summary = "Creates an actor passed through body. Does not check for duplicates")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Actor created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ActorInfo.class)
                    )
            )
    })
    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody ActorInput actor) {
        ActorInfo saved = actorsService.save(actor);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @Operation(summary = "Modifies an actor passed through body")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Actor found and modified",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ActorInfo.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Actor not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionMessage.class)
                    )
            )
    })
    @PutMapping("")
    public ResponseEntity<?> modify(@RequestBody ActorInfo actor) {
        try {
            return ResponseEntity.ok(actorsService.modify(actor));
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @Operation(summary = "Delete an actor given the specified id by parameter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Actor deleted"
            ),
            @ApiResponse(responseCode = "404",
                    description = "Actor not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionMessage.class)
                    )
            )
    })
    @DeleteMapping("/{actorId}")
    public ResponseEntity<?> delete(@Parameter(description = "Id of the actor to be deleted")
                                    @PathVariable("actorId") long id) {
        try {
            actorsService.findById(id);
        } catch (NotFoundException e) {
            return e.getResponse();
        }

        actorsService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Find all the show ids in which an actor was main cast and the characters they played")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Shows and characters found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = MainCastInfo.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Actor not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionMessage.class)
                    )
            )
    })
    @GetMapping("/{actorId}/shows")
    public ResponseEntity<?> getShows(@PathVariable("actorId") long id) {
        try {
            List<MainCastInfo> showsAsMainCast = mainCastService.findByActor(id);
            return ResponseEntity.ok(showsAsMainCast);
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }
}

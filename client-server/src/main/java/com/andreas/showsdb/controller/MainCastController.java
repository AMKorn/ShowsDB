package com.andreas.showsdb.controller;


import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.exception.ShowsDatabaseException;
import com.andreas.showsdb.model.dto.ActorOutputDto;
import com.andreas.showsdb.model.dto.MainCastDto;
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

@RestController
@RequestMapping("/api/main-cast")
@RequiredArgsConstructor
public class MainCastController {

    private final MainCastService mainCastService;

    @Operation(summary = "List all main casts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = MainCastDto.class)
                            )
                    )
            )
    })
    @GetMapping
    public List<MainCastDto> getAll() {
        return mainCastService.findAll();
    }

    @Operation(summary = "Create a main cast passed through body")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Main cast created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ActorOutputDto.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Actor or show not found"
            ),
            @ApiResponse(responseCode = "409",
                    description = "Combination already present in database"
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MainCastDto create(@RequestBody MainCastDto mainCastDto) throws ShowsDatabaseException {
        return mainCastService.save(mainCastDto);
    }

    @Operation(summary = "Modify a main cast passed through body")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Main cast modified",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ActorOutputDto.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Actor, show or main cast not found"
            )
    })
    @PutMapping
    public MainCastDto modify(@RequestBody MainCastDto mainCastDto) throws NotFoundException {
        return mainCastService.modify(mainCastDto);
    }


    @Operation(summary = "Delete a main cast")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Main cast deleted"
            )
    })
    @DeleteMapping
    public void delete(@Parameter(description = "Id of the actor")
                       @RequestParam("actor") Long actorId,
                       @Parameter(description = "Id of the show")
                       @RequestParam("show") Long showId) {
        mainCastService.delete(actorId, showId);
    }
}

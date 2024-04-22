package com.andreas.showsdb.controller;


import com.andreas.showsdb.exception.ExceptionMessage;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/main-cast")
public class MainCastController {
    private final MainCastService mainCastService;

    public MainCastController(MainCastService mainCastService) {
        this.mainCastService = mainCastService;
    }

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
    @GetMapping("")
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
                    description = "Actor or show not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionMessage.class)
                    )
            )
    })
    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody MainCastDto mainCastDto) {
        try {
            MainCastDto savedMainCast = mainCastService.save(mainCastDto);
            return new ResponseEntity<>(savedMainCast, HttpStatus.CREATED);
        } catch (ShowsDatabaseException e) {
            return e.getResponse();
        }
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
                    description = "Actor, show or main cast not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionMessage.class)
                    )
            )
    })
    @PutMapping("")
    public ResponseEntity<?> modify(@RequestBody MainCastDto mainCastDto) {
        try {
            MainCastDto modifiedMainCast = mainCastService.modify(mainCastDto);
            return ResponseEntity.ok(modifiedMainCast);
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @Operation(summary = "Delete a main cast")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Main cast deleted"
            ),
            @ApiResponse(responseCode = "404",
                    description = "Actor, show or main cast not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionMessage.class)
                    )
            )
    })
    @DeleteMapping("")
    public ResponseEntity<?> delete(@Parameter(description = "Id of the actor")
                                    @RequestParam("actor") Long actorId,
                                    @Parameter(description = "Id of the show")
                                    @RequestParam("show") Long showId) {
        try {
            mainCastService.delete(actorId, showId);
            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }
}

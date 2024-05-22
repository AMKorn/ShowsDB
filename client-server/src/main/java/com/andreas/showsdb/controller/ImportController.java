package com.andreas.showsdb.controller;

import com.andreas.showsdb.batch.BatchOrderListener;
import com.andreas.showsdb.exception.ShowsDatabaseException;
import com.andreas.showsdb.messaging.Messenger;
import com.andreas.showsdb.service.ShowsService;
import com.andreas.showsdb.util.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImportController {
    private final Messenger messenger;
    private final ShowsService showsService;

    @Value("${showsdb.files}")
    private String filePath;

    @Operation(summary = "Upload a file of shows to be exported in batch",
            description = """
                    Upload a csv file, with headers Name, Country and Seasons. The file will be uploaded, used by the
                    batch import system and later deleted. Shows with names already in the database will be ignored, and
                    any seasons up to the number of seasons will be created.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "File uploaded"
            )
    })
    @PostMapping("/imports/shows")
    public void importShows(@RequestPart("file") MultipartFile file) throws ShowsDatabaseException {
        String savedFile = Utils.saveFile(file, filePath);
        messenger.sendBatchOrder(BatchOrderListener.SHOWS, savedFile);
    }

    @Operation(summary = "Download a file in the stated format",
            description = """
                    Download a file in the stated format, which includes the name, country and number of seasons of a
                    show. The default and only supported file type for the time being is csv.
                    """)
    @GetMapping("/imports/shows")
    public ResponseEntity<String> exportShows(@Parameter(description = "File format")
                                              @RequestParam(value = "format", required = false) String mode)
            throws ShowsDatabaseException {
        if (mode == null || mode.equals("csv")) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Shows.csv")
                    .body(showsService.getAsCsvFile());
        }
        throw new ShowsDatabaseException("File format %s not supported".formatted(mode), HttpStatus.BAD_REQUEST);
    }

    @Operation(summary = "Upload a file of episodes to be exported in batch",
            description = """
                    Upload a csv file, with headers Show, Season, Episode and Name. The file will be uploaded, used by
                    the batch import system and then deleted. A batch episode operation CAN NOT create new shows nor
                    seasons, so any episodes of a show whose name was not found or of a season stated that does not
                    exist will be ignored.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "File uploaded"
            )
    })
    @PostMapping("/imports/episodes")
    public void importEpisodes(@RequestPart("file") MultipartFile file) throws ShowsDatabaseException {
        String savedFile = Utils.saveFile(file, filePath);
        messenger.sendBatchOrder(BatchOrderListener.EPISODES, savedFile);
    }
}

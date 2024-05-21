package com.andreas.showsdb.controller;

import com.andreas.showsdb.batch.BatchOrderListener;
import com.andreas.showsdb.exception.ShowsDatabaseException;
import com.andreas.showsdb.messaging.Messenger;
import com.andreas.showsdb.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImportController {
    private final Messenger messenger;
//    private final JobLauncher jobLauncher;
//    private final Job showJob;
//    private final Job episodeJob;

    @Value("${showsdb.files}")
    private String filePath;

//    public ImportController(JobLauncher jobLauncher,
//                            @Qualifier("importShowJob") Job showJob,
//                            @Qualifier("importEpisodeJob") Job episodeJob) {
//        this.jobLauncher = jobLauncher;
//        this.showJob = showJob;
//        this.episodeJob = episodeJob;
//    }

    @PostMapping("/imports/shows")
    public void importShows(@RequestPart("file") MultipartFile file) throws ShowsDatabaseException {
//        JobParameters jobParameters = new JobParametersBuilder()
//                .addJobParameter("resource", new JobParameter<>(file, MultipartFile.class))
//                .toJobParameters();
//        try {
//            jobLauncher.run(showJob, jobParameters);
//        } catch (JobParametersInvalidException e) {
//            throw new BatchProcessingException(HttpStatus.BAD_REQUEST, e.getMessage());
//        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException e) {
//            throw new BatchProcessingException(HttpStatus.CONFLICT, e.getMessage());
//        }
        String savedFile = Utils.saveFile(file, filePath);
        messenger.sendBatchOrder(BatchOrderListener.SHOWS, savedFile);
    }

    @PostMapping("/imports/episodes")
    public void importEpisodes(@RequestPart("file") MultipartFile file) throws ShowsDatabaseException {
        String savedFile = Utils.saveFile(file, filePath);
        messenger.sendBatchOrder(BatchOrderListener.EPISODES, savedFile);
    }
}

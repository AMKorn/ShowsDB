package com.andreas.showsdb.batch;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.repository.SeasonsRepository;
import com.andreas.showsdb.repository.ShowsRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class EpisodesBatchConfig {
    @Value("${file.input.episodes}")
    private String episodesInputFile;

    private static final Logger logger = LoggerFactory.getLogger(EpisodesBatchConfig.class);

    @Bean
    public FlatFileItemReader<EpisodeBatchInput> episodesReader() {
        String[] names = {"Show", "Season", "Episode", "Name"};
        BeanWrapperFieldSetMapper<EpisodeBatchInput> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(EpisodeBatchInput.class);
        return new FlatFileItemReaderBuilder<EpisodeBatchInput>().name("episodesReader")
                .resource(new ClassPathResource(episodesInputFile))
                .delimited()
                .names(names)
                .fieldSetMapper(mapper)
                .linesToSkip(1)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<EpisodeBatchInsert> episodeWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<EpisodeBatchInsert>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO `episode`(`season`, `episode_number`, `name`) " +
                     "VALUES (:season, :episodeNumber, :name) " +
                     "ON DUPLICATE KEY UPDATE `name`= :name")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Job importEpisodeJob(JobRepository jobRepository, ImportShowCompletionNotificationListener listener,
                                Step importEpisodeStep1) {
        return new JobBuilder("importEpisodeJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(importEpisodeStep1)
                .end().build();
    }

    @Bean
    public Step importEpisodeStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                   JdbcBatchItemWriter<EpisodeBatchInsert> writer,
                                   ItemProcessor<EpisodeBatchInput, EpisodeBatchInsert> episodeProcessor) {
        return new StepBuilder("importEpisodeStep1", jobRepository)
                .<EpisodeBatchInput, EpisodeBatchInsert>chunk(10, transactionManager)
                .reader(episodesReader())
                .processor(episodeProcessor)
                .writer(writer)
                .build();
    }

    @Bean
    public ItemProcessor<EpisodeBatchInput, EpisodeBatchInsert> episodeProcessor(ShowsRepository showsRepository,
                                                                                 SeasonsRepository seasonsRepository) {
        return episode -> {
            try {
                Long seasonId = seasonsRepository.findFirstByShowNameAndNumber(episode.show, episode.season)
                        .orElseThrow(NotFoundException::new)
                        .getId();
                return EpisodeBatchInsert.builder()
                        .season(seasonId)
                        .episodeNumber(episode.episode)
                        .name(episode.name)
                        .build();
            } catch (NotFoundException e) {
                logger.error("Could not find %s : S%02d".formatted(episode.show, episode.season));
                return null;
            }
        };
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EpisodeBatchInput {
        private String show;
        private Integer season;
        private Integer episode;
        private String name;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EpisodeBatchInsert {
        private Long season;
        private Integer episodeNumber;
        private String name;
    }
}
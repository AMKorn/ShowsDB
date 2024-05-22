package com.andreas.showsdb.batch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class ShowsBatchConfig {
    private static final Logger logger = LoggerFactory.getLogger(ShowsBatchConfig.class);

    @Bean
    public JdbcBatchItemWriter<ShowBatchFormat> showWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<ShowBatchFormat>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO `show`(`name`, `country`) VALUES (:name, :country) " +
                     "ON DUPLICATE KEY UPDATE `name`=:name, `country`=:country")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Job importShowJob(JobRepository jobRepository, JobCompletionNotificationListener listener,
                             Step importShowStep1, Step importShowStep2) {
        return new JobBuilder("importShowJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(importShowStep1)
                .next(importShowStep2)
                .end().build();
    }

    @JobScope
    @Bean
    public Step importShowStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                JdbcBatchItemWriter<ShowBatchFormat> writer,
                                @Value("#{jobParameters['filepath']}") String filepath) {
        logger.info("Filepath: {}", filepath);
        return new StepBuilder("importShowStep1", jobRepository)
                .<ShowBatchFormat, ShowBatchFormat>chunk(10, transactionManager)
                .reader(showsReader(filepath))
                .writer(writer)
                .build();
    }

    @Bean
    public ListUnpackingItemWriter<SeasonBatchFormat> seasonsWriter(DataSource dataSource) {
        JdbcBatchItemWriter<SeasonBatchFormat> delegateWriter = new JdbcBatchItemWriterBuilder<SeasonBatchFormat>()
//                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .itemPreparedStatementSetter((season, ps) -> {
                    ps.setInt(1, season.getSeason());
                    ps.setString(2, season.getShow());
                })
                .sql("INSERT IGNORE INTO `season` (`show`, `season_number`) SELECT s.`id`, ? FROM `show` s " +
                     "WHERE s.`name`=?")
                .dataSource(dataSource)
                .build();
        ListUnpackingItemWriter<SeasonBatchFormat> listUnpackingItemWriter = new ListUnpackingItemWriter<>();
        listUnpackingItemWriter.setDelegate(delegateWriter);
        return listUnpackingItemWriter;
    }

    @JobScope
    @Bean
    public Step importShowStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                ListUnpackingItemWriter<SeasonBatchFormat> writer,
                                @Value("#{jobParameters['filepath']}") String filepath) {
        return new StepBuilder("importShowStep2", jobRepository)
                .<ShowBatchFormat, List<SeasonBatchFormat>>chunk(10, transactionManager)
                .reader(showsReader(filepath))
                .processor(show -> {
                    List<SeasonBatchFormat> seasons = new ArrayList<>();
                    for (int i = 0; i < show.seasons; i++) {
                        SeasonBatchFormat season = SeasonBatchFormat.builder()
                                .show(show.name)
                                .season(i + 1)
                                .build();
                        seasons.add(season);
                    }
                    return seasons;
                })
                .writer(writer)
                .build();
    }

    private FlatFileItemReader<ShowBatchFormat> showsReader(String filepath) {
        BeanWrapperFieldSetMapper<ShowBatchFormat> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(ShowBatchFormat.class);
        return new FlatFileItemReaderBuilder<ShowBatchFormat>().name("showsReader")
                .resource(new FileSystemResource(filepath))
                .delimited()
                .names("Name", "Country", "Seasons")
                .fieldSetMapper(mapper)
                .linesToSkip(1)
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShowBatchFormat {
        private String name;
        private String country;
        private Integer seasons;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeasonBatchFormat {
        private String show;
        private Integer season;
    }
}

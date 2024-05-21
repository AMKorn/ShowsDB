package com.andreas.showsdb.batch;

import com.andreas.showsdb.model.Show;
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

@Configuration
public class ShowsBatchConfig {
    private static final Logger logger = LoggerFactory.getLogger(ShowsBatchConfig.class);

    @Bean
    public JdbcBatchItemWriter<Show> showWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Show>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO `show`(`name`, `country`) VALUES (:name, :country)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Job importShowJob(JobRepository jobRepository, JobCompletionNotificationListener listener,
                             Step importShowStep1) {
        return new JobBuilder("importShowJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(importShowStep1)
                .end().build();
    }

    @JobScope
    @Bean
    public Step importShowStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                JdbcBatchItemWriter<Show> writer,
                                @Value("#{jobParameters['filepath']}") String filepath) {
        logger.info("Filepath: {}", filepath);
        return new StepBuilder("importShowStep1", jobRepository)
                .<Show, Show>chunk(10, transactionManager)
                .reader(showsReader(filepath))
                .processor(processor())
                .writer(writer)
                .build();
    }

    public FlatFileItemReader<Show> showsReader(String filepath) {
        String[] names = {"Name", "Country"};
        BeanWrapperFieldSetMapper<Show> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(Show.class);
        return new FlatFileItemReaderBuilder<Show>().name("showsReader")
                .resource(new FileSystemResource(filepath))
                .delimited()
                .names(names)
                .fieldSetMapper(mapper)
                .linesToSkip(1)
                .build();
    }

    private ShowItemProcessor processor() {
        return new ShowItemProcessor();
    }
}

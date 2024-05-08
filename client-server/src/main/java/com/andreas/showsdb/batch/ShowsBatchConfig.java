package com.andreas.showsdb.batch;

import com.andreas.showsdb.model.Show;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class ShowsBatchConfig {
    @Value("${file.input.shows}")
    private String showsInputFile;

    @Bean
    public FlatFileItemReader<Show> showsReader() {
        String[] names = {"Name", "Country"};
        BeanWrapperFieldSetMapper<Show> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(Show.class);
        return new FlatFileItemReaderBuilder<Show>().name("showsReader")
                .resource(new ClassPathResource(showsInputFile))
                .delimited()
                .names(names)
                .fieldSetMapper(mapper)
                .linesToSkip(1)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Show> showWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Show>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO `show`(`name`, `country`) VALUES (:name, :country)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Job importShowJob(JobRepository jobRepository, ImportShowCompletionNotificationListener listener,
                             Step importShowStep1) {
        return new JobBuilder("importShowJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(importShowStep1)
                .end().build();
    }

    @Bean
    public Step importShowStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                JdbcBatchItemWriter<Show> writer) {
        return new StepBuilder("importShowStep1", jobRepository)
                .<Show, Show>chunk(10, transactionManager)
                .reader(showsReader())
                .processor(processor())
                .writer(writer)
                .build();
    }

    private ShowItemProcessor processor() {
        return new ShowItemProcessor();
    }
}

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
public class BatchConfig {
    @Value("${file.input}")
    private String fileInput;

    @Bean
    public FlatFileItemReader<Show> reader() {
        String[] names = {"Name", "Country"};
        BeanWrapperFieldSetMapper<Show> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(Show.class);
        return new FlatFileItemReaderBuilder<Show>().name("episodesReader")
                .resource(new ClassPathResource(fileInput))
                .delimited()
                .names(names)
                .fieldSetMapper(mapper)
                .linesToSkip(1)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Show> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Show>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO `show`(`name`, `country`) VALUES (:name, :country)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Job importUserJob(JobRepository jobRepository, JobCompletionNotificationListener listener, Step step1) {
        return new JobBuilder("importUserJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end().build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                      JdbcBatchItemWriter<Show> writer) {
        return new StepBuilder("step1", jobRepository)
                .<Show, Show>chunk(10, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer)
                .build();
    }

    private ShowItemProcessor processor() {
        return new ShowItemProcessor();
    }
}

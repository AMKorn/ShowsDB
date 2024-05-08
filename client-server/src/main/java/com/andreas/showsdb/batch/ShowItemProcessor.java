package com.andreas.showsdb.batch;

import com.andreas.showsdb.model.Show;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class ShowItemProcessor implements ItemProcessor<Show, Show> {
    private static final Logger logger = LoggerFactory.getLogger(ShowItemProcessor.class);

    @Override
    public Show process(final Show show) {
        String name = show.getName();
        String country = show.getCountry();

        Show transformedShow = Show.builder()
                .name(name)
                .country(country)
                .build();
        logger.info("Converting ( {} ) into ( {} )", show, transformedShow);

        return transformedShow;
    }
}

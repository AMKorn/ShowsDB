package com.andreas.showsdb.batch;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.util.Assert;

import java.util.List;

@Setter
public class ListUnpackingItemWriter<T> implements ItemWriter<List<T>>, ItemStream, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(ListUnpackingItemWriter.class);
    private ItemWriter<T> delegate;

    @Override
    public void write(final Chunk<? extends List<T>> chunks) throws Exception {
        final Chunk<T> consolidatedList = new Chunk<>();
        for (final List<T> list : chunks) {
            consolidatedList.addAll(list);
        }
        consolidatedList.forEach(ts -> logger.info(ts.toString()));
        try {
            delegate.write(consolidatedList);
        } catch (EmptyResultDataAccessException ignore) {
            // No need to do anything, just keep going
        }
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(delegate, "A delegate must be set.");
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        if (delegate instanceof ItemStream itemStream) {
            itemStream.open(executionContext);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        if (delegate instanceof ItemStream itemStream) {
            itemStream.update(executionContext);
        }
    }

    @Override
    public void close() throws ItemStreamException {
        if (delegate instanceof ItemStream itemStream) {
            itemStream.close();
        }
    }
}

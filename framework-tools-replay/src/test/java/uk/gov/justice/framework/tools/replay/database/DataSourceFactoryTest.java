package uk.gov.justice.framework.tools.replay.database;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Test;

public class DataSourceFactoryTest {

    private final DataSourceFactory dataSourceFactory = new DataSourceFactory();

    @Test
    public void shouldCreateABasicDataSource() throws Exception {

        assertThat(dataSourceFactory.create(), is(instanceOf(BasicDataSource.class)));
    }
}

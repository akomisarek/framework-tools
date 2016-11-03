package uk.gov.justice.framework.replay.database;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class DataSourceFactoryTest {

    private final DataSourceFactory dataSourceFactory = new DataSourceFactory();

    @Test
    public void shouldCreateABasicDataSource() throws Exception {

        assertThat(dataSourceFactory.create(), is(instanceOf(BasicDataSource.class)));
    }
}

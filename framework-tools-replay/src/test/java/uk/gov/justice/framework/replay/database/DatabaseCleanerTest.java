package uk.gov.justice.framework.replay.database;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.framework.replay.Constants.AN_EMPTY_STRING;

import java.sql.SQLException;

import liquibase.exception.LiquibaseException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseCleanerTest {

    @Mock
    private DataSourceFactory dataSourceFactory;

    @Mock
    private LiquibaseFactory liquibaseFactory;

    @InjectMocks
    private DatabaseCleaner databaseCleaner;

    @Test
    public void shouldDropTheDatabaseAndRecreateUsingLiquibase() throws Exception {

        final String contextName = "notification";
        final String expectedUrl = "jdbc:postgresql://localhost:5432/notificationviewstore";
        final String expectedLiquibaseChangeLogXml = "liquibase/notification-view-store-db-changelog.xml";

        final BasicDataSource dataSource = mock(BasicDataSource.class);
        final LiquibaseProxy liquibase = mock(LiquibaseProxy.class);

        when(dataSourceFactory.create()).thenReturn(dataSource);
        when(liquibaseFactory.create(expectedLiquibaseChangeLogXml, dataSource)).thenReturn(liquibase);

        assertThat(liquibaseFactory.create(expectedLiquibaseChangeLogXml, dataSource), is(notNullValue()));

        databaseCleaner.rebuildDatabase(contextName);

        verify(dataSource).setDriverClassName("org.postgresql.Driver");
        verify(dataSource).setUrl(expectedUrl);
        verify(dataSource).setUsername(contextName);
        verify(dataSource).setPassword(contextName);

        verify(liquibase).dropAll();
        verify(liquibase).update(AN_EMPTY_STRING);

        verify(dataSource).close();
    }

    @Test
    public void shouldRethrowALiquibaseExceptionAsARuntimeException() throws Exception {

        final Throwable liquibaseException = new LiquibaseException("Ooops");

        final String contextName = "notification";
        final String expectedLiquibaseChangeLogXml = "liquibase/notification-view-store-db-changelog.xml";

        final BasicDataSource dataSource = mock(BasicDataSource.class);

        when(dataSourceFactory.create()).thenReturn(dataSource);
        when(liquibaseFactory.create(expectedLiquibaseChangeLogXml, dataSource)).thenThrow(liquibaseException);

        try {
            databaseCleaner.rebuildDatabase(contextName);
        } catch (RuntimeException expected) {
            assertThat(expected.getCause(), is(liquibaseException));
            assertThat(expected.getMessage(), is("Failed to run liquibase rebuild"));
        }
    }

    @Test
    public void shouldRethrowAnSQLExceptionExceptionAsARuntimeException() throws Exception {

        final Throwable sqlException = new SQLException("Ooops");

        final String contextName = "notification";
        final String expectedLiquibaseChangeLogXml = "liquibase/notification-view-store-db-changelog.xml";

        final BasicDataSource dataSource = mock(BasicDataSource.class);

        when(dataSourceFactory.create()).thenReturn(dataSource);
        when(liquibaseFactory.create(expectedLiquibaseChangeLogXml, dataSource)).thenThrow(sqlException);

        try {
            databaseCleaner.rebuildDatabase(contextName);
        } catch (RuntimeException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to run liquibase rebuild"));
        }
    }
}

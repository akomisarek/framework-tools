package uk.gov.justice.framework.tools.replay.database;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.sql.Connection;
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

    private static final String NO_CONTEXT_MODE = "";

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
        final String eventBufferChangeLogXml = "liquibase/event-buffer-changelog.xml";

        final BasicDataSource dataSource = mock(BasicDataSource.class);
        final Connection connection = mock(Connection.class);
        final LiquibaseProxy liquibase = mock(LiquibaseProxy.class);
        final Path viewstoreLibraryPath = mock(Path.class);

        when(dataSourceFactory.create()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(liquibaseFactory.create(expectedLiquibaseChangeLogXml, connection, viewstoreLibraryPath)).thenReturn(liquibase);
        when(liquibaseFactory.create(eventBufferChangeLogXml, connection)).thenReturn(liquibase);

        databaseCleaner.rebuildDatabase(contextName, viewstoreLibraryPath);

        verify(dataSource).setDriverClassName("org.postgresql.Driver");
        verify(dataSource).setUrl(expectedUrl);
        verify(dataSource).setUsername(contextName);
        verify(dataSource).setPassword(contextName);

        verify(liquibase).dropAll();
        verify(liquibase, times(2)).update(NO_CONTEXT_MODE);

        verify(dataSource).close();
    }

    @Test
    public void shouldCreateDefaultDatabaseCleaner() throws Exception {
        assertThat(DatabaseCleaner.defaultDatabaseCleaner(), instanceOf(DatabaseCleaner.class));
    }

    @Test
    public void shouldRethrowALiquibaseExceptionAsARuntimeException() throws Exception {

        final Throwable liquibaseException = new LiquibaseException("Ooops");

        final String contextName = "notification";
        final String expectedLiquibaseChangeLogXml = "liquibase/notification-view-store-db-changelog.xml";

        final Path viewstoreLibraryPath = mock(Path.class);
        final BasicDataSource dataSource = mock(BasicDataSource.class);
        final Connection connection = mock(Connection.class);

        when(dataSourceFactory.create()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(liquibaseFactory.create(expectedLiquibaseChangeLogXml, connection, viewstoreLibraryPath)).thenThrow(liquibaseException);

        try {
            databaseCleaner.rebuildDatabase(contextName, viewstoreLibraryPath);
        } catch (RuntimeException expected) {
            assertThat(expected.getCause(), is(liquibaseException));
            assertThat(expected.getMessage(), is("Failed to run liquibase rebuild"));
        }
    }

    @Test
    public void shouldRethrowAnSQLExceptionAsARuntimeException() throws Exception {

        final Throwable sqlException = new SQLException("Ooops");

        final String contextName = "notification";
        final String expectedLiquibaseChangeLogXml = "liquibase/notification-view-store-db-changelog.xml";

        final Path viewstoreLibraryPath = mock(Path.class);
        final BasicDataSource dataSource = mock(BasicDataSource.class);
        final Connection connection = mock(Connection.class);

        when(dataSourceFactory.create()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenThrow(sqlException);

        try {
            databaseCleaner.rebuildDatabase(contextName, viewstoreLibraryPath);
        } catch (RuntimeException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to run liquibase rebuild"));
        }
    }

    @Test
    public void shouldRethrowAnMalformedURLExceptionAsARuntimeException() throws Exception {

        final Throwable malformedURLException = new MalformedURLException("Ooops");

        final String contextName = "notification";
        final String expectedLiquibaseChangeLogXml = "liquibase/notification-view-store-db-changelog.xml";

        final Path viewstoreLibraryPath = mock(Path.class);
        final BasicDataSource dataSource = mock(BasicDataSource.class);
        final Connection connection = mock(Connection.class);

        when(dataSourceFactory.create()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(liquibaseFactory.create(expectedLiquibaseChangeLogXml, connection, viewstoreLibraryPath)).thenThrow(malformedURLException);

        try {
            databaseCleaner.rebuildDatabase(contextName, viewstoreLibraryPath);
        } catch (RuntimeException expected) {
            assertThat(expected.getCause(), is(malformedURLException));
            assertThat(expected.getMessage(), is("Failed to run liquibase rebuild"));
        }
    }
}
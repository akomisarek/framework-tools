package uk.gov.justice.framework.tools.replay.database;

import static java.lang.String.format;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

import liquibase.exception.LiquibaseException;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 * Clean the View Store database and reconstruct using liquibase scripts.  Uses a context viewstore
 * script and the framework event buffer script.
 */
public class DatabaseCleaner {

    private static final String POSTGRES_DRIVER = "org.postgresql.Driver";
    private static final String NO_CONTEXT_MODE = "";
    private static final String VIEWSTORE_DATABASE_URL_TEMPLATE = "jdbc:postgresql://localhost:5432/%sviewstore";
    private static final String VIEWSTORE_CHANGELOG_TEMPLATE = "liquibase/%s-view-store-db-changelog.xml";
    private static final String EVENT_BUFFER_CHANGELOG = "liquibase/event-buffer-changelog.xml";

    private final DataSourceFactory dataSourceFactory;
    private final LiquibaseFactory liquibaseFactory;

    private DatabaseCleaner(final DataSourceFactory dataSourceFactory, final LiquibaseFactory liquibaseFactory) {
        this.dataSourceFactory = dataSourceFactory;
        this.liquibaseFactory = liquibaseFactory;
    }

    /**
     * Create a default instance of the DatabaseCleaner with the required factories.
     *
     * @return instance of a DatabaseCleaner
     */
    public static DatabaseCleaner defaultDatabaseCleaner() {
        return new DatabaseCleaner(new DataSourceFactory(), new LiquibaseFactory());
    }

    /**
     * Rebuild the database of the given context name using the path of the library that contains
     * the liquibase change log.
     *
     * @param contextName      name of the context to clean
     * @param viewstoreLibrary path to the viewstore library that contains the liquibase script
     */
    public void rebuildDatabase(final String contextName, final Path viewstoreLibrary) {

        try (final BasicDataSource dataSource = dataSourceFactory.create()) {

            dataSource.setDriverClassName(POSTGRES_DRIVER);
            dataSource.setUrl(format(VIEWSTORE_DATABASE_URL_TEMPLATE, contextName));
            dataSource.setUsername(contextName);
            dataSource.setPassword(contextName);

            try (final Connection connection = dataSource.getConnection()) {
                recreateContextViewStoreTables(connection, viewstoreLibrary, contextName);
                recreateEventBufferTables(connection);
            }

        } catch (LiquibaseException | SQLException | MalformedURLException e) {
            throw new RuntimeException("Failed to run liquibase rebuild", e);
        }
    }

    private void recreateContextViewStoreTables(final Connection connection,
                                                final Path viewstoreLibrary,
                                                final String contextName) throws LiquibaseException, MalformedURLException {

        final String liquibaseChangeLog = format(VIEWSTORE_CHANGELOG_TEMPLATE, contextName);
        final LiquibaseProxy liquibase = liquibaseFactory.create(liquibaseChangeLog, connection, viewstoreLibrary);

        liquibase.dropAll();
        liquibase.update(NO_CONTEXT_MODE);
    }

    private void recreateEventBufferTables(final Connection connection) throws LiquibaseException {

        final LiquibaseProxy eventBufferLiquibase = liquibaseFactory.create(EVENT_BUFFER_CHANGELOG, connection);
        eventBufferLiquibase.update(NO_CONTEXT_MODE);
    }
}

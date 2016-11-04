package uk.gov.justice.framework.tools.replay.database;

import java.sql.SQLException;

import javax.inject.Inject;

import liquibase.exception.LiquibaseException;
import org.apache.commons.dbcp2.BasicDataSource;

public class DatabaseCleaner {

    private static final String POSTGRES_DRIVER = "org.postgresql.Driver";
    private static final String NO_CONTEXT_MODE = "";

    @Inject
    DataSourceFactory dataSourceFactory;

    @Inject
    LiquibaseFactory liquibaseFactory;

    public void rebuildDatabase(final String contextName) {

        final String url = String.format("jdbc:postgresql://localhost:5432/%sviewstore", contextName);
        final String liquibaseChangeLogXml = String.format("liquibase/%s-view-store-db-changelog.xml", contextName);

        try (final BasicDataSource dataSource = dataSourceFactory.create()) {

            dataSource.setDriverClassName(POSTGRES_DRIVER);
            dataSource.setUrl(url);
            dataSource.setUsername(contextName);
            dataSource.setPassword(contextName);

            final LiquibaseProxy liquibase = liquibaseFactory.create(liquibaseChangeLogXml, dataSource);

            liquibase.dropAll();
            liquibase.update(NO_CONTEXT_MODE);

        } catch (LiquibaseException | SQLException e) {
            throw new RuntimeException("Failed to run liquibase rebuild", e);
        }
    }
}

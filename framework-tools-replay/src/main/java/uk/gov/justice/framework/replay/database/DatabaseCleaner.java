package uk.gov.justice.framework.replay.database;

import static uk.gov.justice.framework.replay.Constants.AN_EMPTY_STRING;

import java.sql.SQLException;

import javax.inject.Inject;

import liquibase.exception.LiquibaseException;
import org.apache.commons.dbcp2.BasicDataSource;

public class DatabaseCleaner {

    private static final String POSTGRES_DRIVER = "org.postgresql.Driver";

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
            liquibase.update(AN_EMPTY_STRING);

        } catch (LiquibaseException | SQLException e) {
            throw new RuntimeException("Failed to run liquibase rebuild", e);
        }
    }
}

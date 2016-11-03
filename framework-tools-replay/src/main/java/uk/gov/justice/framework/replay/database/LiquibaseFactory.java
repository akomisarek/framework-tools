package uk.gov.justice.framework.replay.database;

import java.sql.SQLException;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.dbcp2.BasicDataSource;

public class LiquibaseFactory {

    public LiquibaseProxy create(
            final String liquibaseChangeLogXml,
            final BasicDataSource dataSource) throws LiquibaseException, SQLException {

        final Liquibase liquibase = new Liquibase(
                liquibaseChangeLogXml,
                new ClassLoaderResourceAccessor(),
                new JdbcConnection(dataSource.getConnection()));

        return new LiquibaseProxy(liquibase);
    }
}

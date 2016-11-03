package uk.gov.justice.framework.replay.database;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;

import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.dbcp2.BasicDataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import uk.gov.justice.services.test.utils.persistence.TestJdbcConnectionProvider;

import java.lang.reflect.Field;
import java.sql.Connection;

public class LiquibaseFactoryTest {

    private LiquibaseFactory liquibaseFactory = new LiquibaseFactory();
    private Connection connection;

    @Before
    public void createRealSqlConnectionAsCannotConstructLiquibaseWithAMock() {
        connection = new TestJdbcConnectionProvider().getViewStoreConnection("notification");
    }

    @After
    public void closeTheRealSqlConnection() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void shouldCreateANewLiquibaseObjectWithCorrectDependencies() throws Exception {

        final String liquibaseChangeLogXml = "liquibaseChangeLogXml";
        final BasicDataSource dataSource = mock(BasicDataSource.class);

        when(dataSource.getConnection()).thenReturn(connection);

        final LiquibaseProxy liquibaseProxy = liquibaseFactory.create(liquibaseChangeLogXml, dataSource);

        assertThat(liquibaseProxy, is(notNullValue()));
        assertThat(liquibaseProxy.getLiquibase(), is(instanceOf(Liquibase.class)));
        assertThat(liquibaseProxy.getLiquibase().getChangeLogFile(), is(liquibaseChangeLogXml));
        assertThat(liquibaseProxy.getLiquibase().getResourceAccessor(), is(instanceOf(ClassLoaderResourceAccessor.class)));

        final DatabaseConnection databaseConnection = liquibaseProxy.getLiquibase().getDatabase().getConnection();

        assertThat(connectionInside(databaseConnection), is(connection));
    }

    private Connection connectionInside(final DatabaseConnection databaseConnection) throws Exception {

        final Field field = databaseConnection.getClass().getDeclaredField("con");
        field.setAccessible(true);

        return (Connection) field.get(databaseConnection);
    }
}

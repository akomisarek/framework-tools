package uk.gov.justice.framework.tools.replay;


import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;

public class DatabaseUtils {

    private static final TestProperties TEST_PROPERTIES = new TestProperties("test.properties");


    public static DataSource initEventStoreDb() throws Exception {
        return initDatabase("db.eventstore.url",
                "db.eventstore.userName",
                "db.eventstore.password",
                "liquibase/event-store-db-changelog.xml", "liquibase/snapshot-store-db-changelog.xml");
    }

    public static DataSource initViewStoreDb() throws Exception {
        return initDatabase("db.viewstore.url",
                "db.eventstore.userName",
                "db.eventstore.password",
                "liquibase/viewstore-db-changelog.xml", "liquibase/event-buffer-changelog.xml", "liquibase/snapshot-store-db-changelog.xml");
    }

    private static DataSource initDatabase(final String dbUrlPropertyName,
                                           final String dbUserNamePropertyName,
                                           final String dbPasswordPropertyName,
                                           final String... liquibaseChangeLogXmls) throws Exception {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(TEST_PROPERTIES.value("db.driver"));

        dataSource.setUrl(TEST_PROPERTIES.value(dbUrlPropertyName));
        dataSource.setUsername(TEST_PROPERTIES.value(dbUserNamePropertyName));
        dataSource.setPassword(TEST_PROPERTIES.value(dbPasswordPropertyName));
        boolean dropped = false;
        final JdbcConnection jdbcConnection = new JdbcConnection(dataSource.getConnection());

        for (String liquibaseChangeLogXml : liquibaseChangeLogXmls) {
            Liquibase liquibase = new Liquibase(liquibaseChangeLogXml,
                    new ClassLoaderResourceAccessor(), jdbcConnection);
            if (!dropped) {
                liquibase.dropAll();
                dropped = true;
            }
            liquibase.update("");
        }
        return dataSource;
    }

    public static List<String> viewStoreEvents(DataSource viewStoreDataSource) throws SQLException {
        List<String> viewStoreEvents = new LinkedList<>();
        try (final Connection connection = viewStoreDataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement("SELECT * FROM test")) {
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                viewStoreEvents.add(rs.getString("stream_id"));
            }
            return viewStoreEvents;
        }
    }

    public static void cleanupDataSource(DataSource dataSource, String tableName) throws SQLException {
        final PreparedStatement viewStorePreparedStatement = dataSource.getConnection().prepareStatement(format("delete from %s", tableName));
        viewStorePreparedStatement.executeUpdate();
        viewStorePreparedStatement.getConnection().close();
    }
}

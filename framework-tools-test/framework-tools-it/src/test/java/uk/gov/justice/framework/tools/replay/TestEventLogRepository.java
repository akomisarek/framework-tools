package uk.gov.justice.framework.tools.replay;


import static java.lang.String.format;
import static java.sql.DriverManager.getDriver;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;

import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLogJdbcRepository;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standalone repository class to access event streams. To be used in integration testing
 */
public class TestEventLogRepository extends EventLogJdbcRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(uk.gov.justice.services.test.utils.core.eventsource.TestEventLogRepository.class);
    static final String SQL_FIND_ALL = "SELECT * FROM event_log";

    private final DataSource datasource;

    public TestEventLogRepository(final DataSource datasource) {
        this.datasource = datasource;
        this.logger = LOGGER;
        setField(this, "eventLogInsertionStrategy", new AnsiSQLEventLogInsertionStrategy());
    }

    public TestEventLogRepository(final String url, final String username, final String password, final String driverClassName) {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        this.datasource = dataSource;
        this.logger = LOGGER;

    }

    public TestEventLogRepository(final String contextName) throws SQLException {
        this(jdbcUrlFrom(contextName), contextName, contextName, getDriver(jdbcUrlFrom(contextName)).getClass().getName());
    }

    public static TestEventLogRepository forContext(final String contextName) {
        try {
            return new TestEventLogRepository(contextName);
        } catch (SQLException e) {
            throw new IllegalArgumentException(format("Error instantiating repository for context: %s", contextName), e);
        }
    }

    @Override
    protected DataSource getDataSource() {
        return datasource;
    }

    private static String jdbcUrlFrom(final String contextName) {
        return format("jdbc:postgresql://%s/%seventstore", getHost(), contextName);
    }

}

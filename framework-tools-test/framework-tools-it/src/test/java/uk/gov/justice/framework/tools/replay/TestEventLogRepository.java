package uk.gov.justice.framework.tools.replay;


import static java.lang.String.format;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;

import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLogJdbcRepository;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

/**
 * Standalone repository class to access event streams. To be used in integration testing
 */
public class TestEventLogRepository extends EventLogJdbcRepository {

    private final DataSource datasource;

    public TestEventLogRepository(final DataSource datasource) {
        this.datasource = datasource;
        setField(this, "eventLogInsertionStrategy", new AnsiSQLEventLogInsertionStrategy());
    }

    public TestEventLogRepository(final String url, final String username, final String password, final String driverClassName) {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        this.datasource = dataSource;
    }

    @Override
    protected DataSource getDataSource() {
        return datasource;
    }

    private static String jdbcUrlFrom(final String contextName) {
        return format("jdbc:postgresql://%s/%seventstore", getHost(), contextName);
    }

}

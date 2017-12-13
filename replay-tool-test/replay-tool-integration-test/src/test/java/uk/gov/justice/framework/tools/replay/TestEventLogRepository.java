package uk.gov.justice.framework.tools.replay;


import org.apache.commons.dbcp2.BasicDataSource;
import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLog;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLogJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static uk.gov.justice.framework.tools.replay.DatabaseUtils.initEventStoreDb;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;

/**
 * Standalone repository class to access event streams. To be used in integration testing
 */
public class TestEventLogRepository extends EventLogJdbcRepository {

    private final DataSource datasource;

    public TestEventLogRepository() throws Exception {
        this.datasource = initEventStoreDb();
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


    public List<String> insertEventLogData(UUID streamId) throws SQLException, InvalidSequenceIdException {
        List<String> insertedEvents = new LinkedList<>();
        Long sequenceId = 0L;
        for (int count = 0; count < 5; count++) {
            EventLog eventLog = eventLogFrom("framework.example-test", streamId, ++sequenceId);
            this.insert(eventLog);
            insertedEvents.add(eventLog.getId().toString());
        }
        return insertedEvents;
    }


    private EventLog eventLogFrom(final String eventName, final UUID eventStreamId, final Long sequenceId) {

        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataWithRandomUUID(eventName)
                        .createdAt(ZonedDateTime.now())
                        .withVersion(sequenceId)
                        .withStreamId(eventStreamId).withVersion(1L))
                .withPayloadOf("test", "a string")
                .build();

        final Metadata metadata = jsonEnvelope.metadata();
        final UUID id = metadata.id();

        final UUID streamId = metadata.streamId().orElse(null);
        final String name = metadata.name();
        final String payload = jsonEnvelope.payloadAsJsonObject().toString();
        final ZonedDateTime createdAt = metadata.createdAt().orElse(null);

        return new EventLog(id, streamId, sequenceId, name, metadata.asJsonObject().toString(), payload, createdAt);
    }

}

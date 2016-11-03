package uk.gov.justice.framework.replay.database;

import liquibase.Liquibase;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;

public class LiquibaseProxy {

    private final Liquibase liquibase;

    public LiquibaseProxy(final Liquibase liquibase) {
        this.liquibase = liquibase;
    }

    public void dropAll() throws DatabaseException, LockException {
        liquibase.dropAll();
    }

    public void update(String contexts) throws LiquibaseException {
        liquibase.update(contexts);
    }

    public Liquibase getLiquibase() {
        return liquibase;
    }
}

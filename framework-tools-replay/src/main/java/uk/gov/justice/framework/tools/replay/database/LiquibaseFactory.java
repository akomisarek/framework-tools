package uk.gov.justice.framework.tools.replay.database;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.sql.Connection;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * Factory for creating LiquibaseProxy
 */
public class LiquibaseFactory {

    /**
     * Create LiquibaseProxy with a liquibase script xml resource path and a given database
     * connection.
     *
     * @param liquibaseChangeLog resource path of the liquibase change log to use
     * @param connection         database connection to use for processing
     * @return {@link LiquibaseProxy} that wraps a {@link Liquibase}
     * @throws LiquibaseException thrown if Liquibase fails on creation
     */
    public LiquibaseProxy create(
            final String liquibaseChangeLog,
            final Connection connection) throws LiquibaseException {

        return liquibaseProxyWith(
                liquibaseChangeLog,
                new ClassLoaderResourceAccessor(parentClassloader()),
                new JdbcConnection(connection));
    }

    /**
     * Create LiquibaseProxy with a liquibase script xml resource path that can be loaded from the
     * given library path.
     *
     * @param liquibaseChangeLog resource path of the liquibase change log to use
     * @param connection         database connection to use for processing
     * @param library            path of the jar library where the liquibase change log can be
     *                           found
     * @return {@link LiquibaseProxy} that wraps a {@link Liquibase}
     * @throws LiquibaseException    thrown if Liquibase fails on creation
     * @throws MalformedURLException thrown if the given library path is invalid
     */
    public LiquibaseProxy create(
            final String liquibaseChangeLog,
            final Connection connection,
            final Path library) throws LiquibaseException, MalformedURLException {

        return liquibaseProxyWith(
                liquibaseChangeLog,
                new ClassLoaderResourceAccessor(libraryClassLoader(library)),
                new JdbcConnection(connection));
    }

    private LiquibaseProxy liquibaseProxyWith(
            final String liquibaseChangeLogXml,
            final ClassLoaderResourceAccessor resourceAccessor,
            final JdbcConnection connection) throws LiquibaseException {

        return new LiquibaseProxy(
                new Liquibase(
                        liquibaseChangeLogXml,
                        resourceAccessor,
                        connection));
    }

    private URLClassLoader libraryClassLoader(final Path library) throws MalformedURLException {
        return new URLClassLoader(new URL[]{library.toUri().toURL()}, parentClassloader());
    }

    private ClassLoader parentClassloader() {
        return this.getClass().getClassLoader();
    }
}

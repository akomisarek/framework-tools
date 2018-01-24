# Replay Tool

The replay-tool is a tool to support the re-population of a view_store (projection) that has been implemented within the [microservice_framework](https://github.com/CJSCommonPlatform/microservice_framework)

The tool works by connecting to a chosen event_store (as per configuration) and reads all of the events from each 'active' stream, passing them to the Listener component which converts them into the required read model (view_store).

> Note: The replay-tool is a WildFly Swarm application.

> Note: The replay-tool requires the 'Event Listener' component war for the view_store being repopulated to be provided at runtime.

# How to Build the Replay-Tool

The replay-tool attempts to be a well-behaved Maven project. To install to your local repository for usage:
```bash
mvn clean install
```

The WildFly Swarm application `framework-tools-replay-x.x.x-swarm.jar` will be produced in the `framework-tools-replay/target/` directory.

# Running the Replay Tool

To run the framework-tools-replay WildFly Swarm application you require:
* The built application jar: `framework-tools-replay-3.1.0-SNAPSHOT-swarm.jar`
* A configuration file with datasource of the event_store and view_store: `standalone-ds.xml`
* A war of the 'Event Listener' component that should be used to repopulate your viewstore: `myservice-event-listener-0.0.1-SNAPSHOT.war` 

Additionally to trigger the automatic shutdown hook on completion you need to create a file and provide the fully qualified file as `org.wildfly.swarm.mainProcessFile` System property

```bash
touch processFile
java -jar -Dorg.wildfly.swarm.mainProcessFile=/Fully/Qualified/Path/processFile -Devent.listener.war=myservice-event-listener.war -jar framework-tools-replay-3.1.0-SNAPSHOT-swarm.jar replay -c standalone-ds.xml
```

# Configuration

This will require standard wilfdly datasource configuration in your `standalone-ds.xml` for the event_store to read the events from and view_store to be repopulated.

```xml
<datasources>
    <!-- Event Store -->
    <datasource jndi-name="java:/app/replay-tool/DS.eventstore"
                pool-name="DS.replay-tool.eventstore" enabled="true"
                use-java-context="true">
        <driver>postgres</driver>
        <xa-datasource-property name="ServerName">localhost</xa-datasource-property>
        <xa-datasource-property name="PortNumber">5432</xa-datasource-property>
        <xa-datasource-property name="DatabaseName">myserviceeventstore</xa-datasource-property>
        <xa-datasource-property name="ApplicationName">myservice</xa-datasource-property>
        ...
        <security>
            <user-name>myservice_username</user-name>
            <password>myservice_password</password>
        </security>
    </datasource>
    
    <!-- View Store -->
    <xa-datasource jndi-name="java:/DS.myservice" 
        pool-name="DS.myservice" enabled="true"
        use-java-context="true" spy="true" use-ccm="true" statistics-enabled="true">
            <driver>postgres</driver>
            <xa-datasource-property name="ServerName">localhost</xa-datasource-property>
            <xa-datasource-property name="PortNumber">5432</xa-datasource-property>
            <xa-datasource-property name="DatabaseName">myserviceviewtstore</xa-datasource-property>
            <xa-datasource-property name="ApplicationName">myservice</xa-datasource-property>
            ...
            <security>
                <user-name>myservice_username</user-name>
                <password>myservice_password</password>
            </security>
        </datasource>
</datasources>
```
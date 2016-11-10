#!/usr/bin/env bash
java -jar target/framework-tools-replay-1.0.0-SNAPSHOT-swarm.jar replay -c standalone-ds.xml -n notification -vl notification-viewstore-liquibase-1.0.12-SNAPSHOT.jar -l notification-event-listener-1.0.12-SNAPSHOT.war

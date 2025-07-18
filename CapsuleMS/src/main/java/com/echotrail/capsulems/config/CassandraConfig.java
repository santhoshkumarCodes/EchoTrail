package com.echotrail.capsulems.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(basePackages = "com.echotrail.capsulems.repository")
public class CassandraConfig extends AbstractCassandraConfiguration {

    @Override
    protected String getContactPoints() {
        return "cassandra";
    }

    @Override
    protected int getPort() {
        return 9042;
    }

    @Override
    public String getKeyspaceName() {
        return "echotrail";
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }

    @Override
    protected String getLocalDataCenter() {
        return "datacenter1";
    }
}

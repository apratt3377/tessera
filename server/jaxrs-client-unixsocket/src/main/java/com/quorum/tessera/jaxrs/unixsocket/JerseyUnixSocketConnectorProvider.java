package com.quorum.tessera.jaxrs.unixsocket;

import java.nio.file.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.client.spi.ConnectorProvider;

public class JerseyUnixSocketConnectorProvider implements ConnectorProvider {

    @Override
    public Connector getConnector(Client client, Configuration runtimeConfig) {
        Path unixfile = (Path) runtimeConfig.getProperty("unixfile");
        return new JerseyUnixSocketConnector(unixfile);
    }

}

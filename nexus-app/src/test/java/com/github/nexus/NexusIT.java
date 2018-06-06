package com.github.nexus;

import com.github.nexus.app.Nexus;
import com.github.nexus.service.locator.ServiceLocator;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import javax.json.Json;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import static org.assertj.core.api.Assertions.assertThat;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Ignore;
import org.junit.Test;

public class NexusIT extends JerseyTest {

    public NexusIT() {
    }

    @Override
    protected Application configure() {
        ServiceLocator serviceLocator = ServiceLocator.create();
        return new Nexus(serviceLocator);
    }

    @Test
    public void sendSingleTransactionToSingleParty() {

        String sendRequest = Json.createObjectBuilder()
                .add("from", "bXlwdWJsaWNrZXk=")
                .add("to", Json.createArrayBuilder().add("cmVjaXBpZW50MQ=="))
                .add("payload", "Zm9v").build().toString();

        javax.ws.rs.core.Response response = target()
                .path("/transaction/send")
                .request()
                .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));
        
        
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
        
    }

    /**
     * Quorum sends transaction with public key not in PartyInfo store.
     */
    @Test
    public void sendSingleTransactionToMultipleParties() {
        String sendRequest = Json.createObjectBuilder()
                .add("from", "bXlwdWJsaWNrZXk=")
                .add("to", Json.createArrayBuilder()
                                .add("cmVjaXBpZW50MQ==")
                                .add(Base64.getEncoder().encodeToString("HELLOW".getBytes()))
                )
                .add("payload", "Zm9v").build().toString();

        javax.ws.rs.core.Response response = target()
                .path("/transaction/send")
                .request()
                .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));
        
        
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    /**
     * Quorum sends transaction with public key not in PartyInfo store.
     */
    @Ignore
    @Test
    public void sendUnknownPublicKey() {
        String sendRequest = Json.createObjectBuilder()
                .add("from", "bXlwdWJsaWNrZXk=")
                .add("to", Json.createArrayBuilder()
                                .add(Base64.getEncoder().encodeToString("BOGUS".getBytes()))
                )
                .add("payload", "Zm9v").build().toString();

        javax.ws.rs.core.Response response = target()
                .path("/transaction/send")
                .request()
                .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));
        
        
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Ignore
    @Test
    public void partyInfo() {

        InputStream data = new ByteArrayInputStream("SOMEDATA".getBytes());

        javax.ws.rs.core.Response response = target()
                .path("/partyinfo")
                .request()
                .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void upcheck() {
        javax.ws.rs.core.Response response = target()
                .path("/upcheck")
                .request()
                .get();

        assertThat(response).isNotNull();
        assertThat(response.readEntity(String.class))
                .isEqualTo("I'm up!");
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void requestVersion() {

        javax.ws.rs.core.Response response = target()
                .path("/version")
                .request()
                .get();

        assertThat(response).isNotNull();
        assertThat(response.readEntity(String.class))
                .isEqualTo("No version defined yet!");
        assertThat(response.getStatus()).isEqualTo(200);

    }

}
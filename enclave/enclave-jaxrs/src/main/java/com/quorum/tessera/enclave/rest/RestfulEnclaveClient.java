package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.enclave.EnclaveClient;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.RawTransaction;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.nacl.Nonce;

import javax.json.JsonArray;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RestfulEnclaveClient implements EnclaveClient {

    private final Client client;

    private final URI uri;

    public RestfulEnclaveClient(Client client, URI uri) {
        this.client = Objects.requireNonNull(client);
        this.uri = Objects.requireNonNull(uri);
    }

    @Override
    public PublicKey defaultPublicKey() {

        Response response = client.target(uri)
                .path("default")
                .request()
                .get();

        byte[] data = response.readEntity(byte[].class);

        return PublicKey.from(data);
    }

    @Override
    public Set<PublicKey> getForwardingKeys() {

        Response response = client.target(uri)
                .path("forwarding")
                .request()
                .get();

        JsonArray results = response.readEntity(JsonArray.class);

        return IntStream.range(0, results.size())
                .mapToObj(i -> results.getString(i))
                .map(s -> Base64.getDecoder().decode(s))
                .map(PublicKey::from)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<PublicKey> getPublicKeys() {
        Response response = client.target(uri)
                .path("public")
                .request()
                .get();

        JsonArray results = response.readEntity(JsonArray.class);

        return IntStream.range(0, results.size())
                .mapToObj(i -> results.getString(i))
                .map(s -> Base64.getDecoder().decode(s))
                .map(PublicKey::from)
                .collect(Collectors.toSet());
    }

    @Override
    public EncodedPayload encryptPayload(byte[] message, PublicKey senderPublicKey, List<PublicKey> recipientPublicKeys) {

        EnclavePayload enclavePayload = new EnclavePayload();
        enclavePayload.setData(message);
        enclavePayload.setSenderKey(senderPublicKey.getKeyBytes());
        enclavePayload.setRecipientPublicKeys(recipientPublicKeys.stream()
                .map(PublicKey::getKeyBytes)
                .collect(Collectors.toList()));

        Response response = client.target(uri)
                .path("encrypt")
                .request()
                .post(Entity.json(enclavePayload));

        byte[] result = response.readEntity(byte[].class);

        return PayloadEncoder.create().decode(result);
    }

    @Override
    public EncodedPayload encryptPayload(RawTransaction rawTransaction, List<PublicKey> recipientPublicKeys) {

        EnclaveRawPayload enclaveRawPayload = new EnclaveRawPayload();
        enclaveRawPayload.setNonce(rawTransaction.getNonce().getNonceBytes());
        enclaveRawPayload.setFrom(rawTransaction.getFrom().getKeyBytes());
        enclaveRawPayload.setRecipientPublicKeys(
                recipientPublicKeys.stream()
                        .map(PublicKey::getKeyBytes)
                        .collect(Collectors.toList())
        );
        enclaveRawPayload.setEncryptedPayload(rawTransaction.getEncryptedPayload());
        enclaveRawPayload.setEncryptedKey(rawTransaction.getEncryptedKey());

        Response response = client.target(uri)
                .path("encrypt")
                .path("raw")
                .request()
                .post(Entity.json(enclaveRawPayload));

        byte[] body = response.readEntity(byte[].class);

        return PayloadEncoder.create().decode(body);

    }

    @Override
    public RawTransaction encryptRawPayload(byte[] message, PublicKey sender) {

        EnclavePayload enclavePayload = new EnclavePayload();
        enclavePayload.setData(message);
        enclavePayload.setSenderKey(sender.getKeyBytes());

        Response response = client.target(uri)
                .path("encrypt")
                .path("toraw")
                .request()
                .post(Entity.json(enclavePayload));

        EnclaveRawPayload enclaveRawPayload = response.readEntity(EnclaveRawPayload.class);

        byte[] encryptedPayload = enclaveRawPayload.getEncryptedPayload();
        byte[] encryptedKey = enclaveRawPayload.getEncryptedKey();
        Nonce nonce = new Nonce(enclaveRawPayload.getNonce());
        PublicKey senderKey = PublicKey.from(enclaveRawPayload.getFrom());
        return new RawTransaction(encryptedPayload, encryptedKey, nonce, senderKey);
    }

    @Override
    public byte[] unencryptTransaction(EncodedPayload payload, PublicKey providedKey) {

        EnclaveUnencryptPayload dto = new EnclaveUnencryptPayload();

        byte[] body = PayloadEncoder.create().encode(payload);

        dto.setData(body);
        dto.setProvidedKey(providedKey.getKeyBytes());

        Response response = client.target(uri)
                .path("unencrypt")
                .request()
                .post(Entity.json(dto));

        return response.readEntity(byte[].class);
    }

    @Override
    public byte[] createNewRecipientBox(final EncodedPayload payload, final PublicKey recipientKey) {

        final byte[] body = PayloadEncoder.create().encode(payload);

        final EnclaveUnencryptPayload dto = new EnclaveUnencryptPayload();
        dto.setData(body);
        dto.setProvidedKey(recipientKey.getKeyBytes());

        final Response response = client.target(uri)
                .path("addRecipient")
                .request()
                .post(Entity.json(dto));

        return response.readEntity(byte[].class);
    }

    @Override
    public Status status() {
        
        final Response response = client.target(uri)
                .path("ping")
                .request().get();
        
        if(response.getStatus() == 200) {
            return Status.STARTED;
        }
        return Status.STOPPED;
    }


    
    
}

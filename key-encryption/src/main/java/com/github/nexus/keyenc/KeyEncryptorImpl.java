package com.github.nexus.keyenc;

import com.github.nexus.argon2.Argon2;
import com.github.nexus.argon2.ArgonResult;
import com.github.nexus.config.ArgonOptions;
import com.github.nexus.config.PrivateKey;
import com.github.nexus.config.PrivateKeyType;
import com.github.nexus.nacl.Key;
import com.github.nexus.nacl.NaclFacade;
import com.github.nexus.nacl.Nonce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * An implementation of {@link KeyEncryptor} that uses Argon2
 *
 * The password is hashed using the generated/provided salt to generate a 32 byte hash
 * This hash is then used as the symmetric key to encrypt the private key
 */
public class KeyEncryptorImpl implements KeyEncryptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyEncryptorImpl.class);

    private final Argon2 argon2;

    private final NaclFacade nacl;

    private final Base64.Decoder decoder = Base64.getDecoder();

    private final Base64.Encoder encoder = Base64.getEncoder();

    private final SecureRandom secureRandom = new SecureRandom();

    public KeyEncryptorImpl(final Argon2 argon2, final NaclFacade nacl) {
        this.argon2 = Objects.requireNonNull(argon2);
        this.nacl = Objects.requireNonNull(nacl);
    }

    @Override
    public PrivateKey encryptPrivateKey(final Key privateKey, final String password) {

        LOGGER.info("Encrypting a private key");

        LOGGER.debug("Encrypting private key {} using password {}", privateKey, password);

        final byte[] salt = new byte[KeyEncryptor.SALTLENGTH];
        secureRandom.nextBytes(salt);

        LOGGER.debug("Generated the random salt {}", Arrays.toString(salt));

        final ArgonResult argonResult = argon2.hash(password, salt);

        final Nonce nonce = nacl.randomNonce();
        LOGGER.debug("Generated the random nonce {}", nonce);

        final byte[] encryptedKey = nacl.sealAfterPrecomputation(
            privateKey.getKeyBytes(),
            nonce,
            new Key(argonResult.getHash())
        );

        LOGGER.info("Private key encrypted");
        
        ArgonOptions argonOptions = new ArgonOptions(argonResult.getOptions().getAlgorithm(), 
                argonResult.getOptions().getIterations(), argonResult.getOptions().getMemory(), argonResult.getOptions().getParallelism());
        
        String nonceString = encoder.encodeToString(nonce.getNonceBytes());
        String saltString = encoder.encodeToString(salt);
        String encyptKeyString =  encoder.encodeToString(encryptedKey);
        
        PrivateKey privateKey1 = new PrivateKey(privateKey.toString(), password, PrivateKeyType.LOCKED, nonceString, saltString, encyptKeyString, argonOptions);
        
        return privateKey1;

    }
    
    static com.github.nexus.argon2.ArgonOptions toArgonOptions(ArgonOptions opts) {
        com.github.nexus.argon2.ArgonOptions argonOptions = 
                new com.github.nexus.argon2.ArgonOptions(opts.getAlgorithm(), 
                        opts.getIterations(), opts.getMemory(), opts.getParallelism());
        return argonOptions;
        
    }
    
    @Override
    public Key decryptPrivateKey(final PrivateKey privateKey) {

        LOGGER.info("Decrypting private key");
        LOGGER.debug("Decrypting private key {} using password {}", privateKey.getValue(), privateKey.getPassword());


        final byte[] salt = decoder.decode(privateKey.getAsalt());
        
        
        final ArgonResult argonResult = argon2.hash(toArgonOptions(privateKey.getArgonOptions()), privateKey.getPassword(), salt);

        final byte[] originalKey = nacl.openAfterPrecomputation(
            decoder.decode(privateKey.getSbox()),
            new Nonce(decoder.decode(privateKey.getSnonce())),
            new Key(argonResult.getHash())
        );

        LOGGER.info("Decrypting private key");
        LOGGER.debug("Decrypted private key {}", new Key(originalKey));

        return new Key(originalKey);
    }

}
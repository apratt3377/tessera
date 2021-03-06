package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.SslAuthenticationMode;
import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.config.SslTrustMode;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.util.EnvironmentVariableProviderFactory;
import com.quorum.tessera.config.util.EnvironmentVariables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.validation.ConstraintValidatorContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SslConfigValidatorTest {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    private Path tmpFile;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    private SslConfigValidator validator;

    private EnvironmentVariableProvider envVarProvider;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        doNothing().when(context).disableDefaultConstraintViolation();
        when(builder.addConstraintViolation()).thenReturn(context);
        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
        tmpFile = Paths.get(tmpDir.getRoot().getPath(), "tmpFile");
        Files.createFile(tmpFile);
        validator = new SslConfigValidator();

        envVarProvider = EnvironmentVariableProviderFactory.load().create();
        when(envVarProvider.hasEnv(anyString())).thenReturn(false);
    }

    @Test
    public void testSslConfigNull() {
        SslConfig sslConfig = null;
        assertThat(validator.isValid(sslConfig, context)).isTrue();
    }

    @Test
    public void testSslConfigNotNullButTlsOff() {
        SslConfig sslConfig = new SslConfig();
        sslConfig.setTls(SslAuthenticationMode.OFF);
        assertThat(validator.isValid(sslConfig, context)).isTrue();
    }

    @Test
    public void testTlsAllowKeyStoreGeneration() {
        SslConfig sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, true, null, null, null, null, SslTrustMode.NONE, null, null, null, null, SslTrustMode.NONE, null, null, null, null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isTrue();
    }

    @Test
    public void testKeyStoreConfigInvalid() {
        SslConfig sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, Paths.get("somefile"), "somepassword", null, null, null, Paths.get("somefile"), null, null, null, null, null, null, null, null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, null, null, null, null, Paths.get("somefile"), null, null, null, null, null, null, null, null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, null, null, null, null, null, null, null, null, null, null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, null, Paths.get("somefile"), "password", null, null, null, null, null, null, null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, null, tmpFile, null, null, null, null, null, null, null, null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, false, tmpFile, null, null, null, null, tmpFile, null, null, null, null, null, null, null, null,Paths.get("someFile"),null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, false, tmpFile, null, null, null, null, tmpFile, null, null, null, null, null, null, null, null,Paths.get("someFile"),Paths.get("someFile"),null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, false, tmpFile, null, null, null, null, tmpFile, null, null, null, null, null, null, null, null, tmpFile,Paths.get("someFile"),null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, false, tmpFile, null, null, null, null, tmpFile, null, null, null, null, null, null, null, null, tmpFile, tmpFile, Paths.get("someFile"),null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, false, tmpFile, null, null, null, null, tmpFile, null, null, null, null, null, null, null, null, tmpFile, tmpFile, Paths.get("someFile"),Paths.get("someFile"),null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, false, tmpFile, null, null, null, null, tmpFile, null, null, null, null, null, null, null, null, tmpFile, tmpFile, tmpFile, Paths.get("someFile"),null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

    }

    @Test
    public void noServerKeyStorePasswordInConfigOrEnvVarsThenInvalid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(false);
        sslConfig.setServerKeyStore(tmpFile);

        sslConfig.setServerKeyStorePassword(null);
        when(envVarProvider.hasEnv(anyString())).thenReturn(false);

        final boolean result = validator.isValid(sslConfig, context);

        assertThat(result).isFalse();

        final String msg = "Server keystore configuration not valid. " +
            "Please ensure keystore file exists or keystore password not null, " +
            "otherwise please set keystore generation flag to true to have keystore created";
        verify(context).buildConstraintViolationWithTemplate(msg);
    }

    @Test
    public void serverKeyStorePasswordInConfigOnlyThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(false);
        sslConfig.setServerKeyStore(tmpFile);

        sslConfig.setServerKeyStorePassword("password");
        when(envVarProvider.hasEnv(EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(false);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(false);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Server keystore configuration not valid. " +
            "Please ensure keystore file exists or keystore password not null, " +
            "otherwise please set keystore generation flag to true to have keystore created";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for some reason other than server keystore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void serverKeyStorePasswordInGlobalEnvVarOnlyThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(false);
        sslConfig.setServerKeyStore(tmpFile);

        sslConfig.setServerKeyStorePassword(null);
        when(envVarProvider.hasEnv(EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(true);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(false);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Server keystore configuration not valid. " +
            "Please ensure keystore file exists or keystore password not null, " +
            "otherwise please set keystore generation flag to true to have keystore created";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for reasons other than server keystore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void serverKeyStorePasswordInPrefixedEnvVarOnlyThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(false);
        sslConfig.setServerKeyStore(tmpFile);
        sslConfig.setEnvironmentVariablePrefix("PREFIX");

        sslConfig.setServerKeyStorePassword(null);
        when(envVarProvider.hasEnv(EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(false);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(true);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Server keystore configuration not valid. " +
            "Please ensure keystore file exists or keystore password not null, " +
            "otherwise please set keystore generation flag to true to have keystore created";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for reasons other than server keystore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void serverKeyStorePasswordInConfigAndGlobalEnvVarThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(false);
        sslConfig.setServerKeyStore(tmpFile);

        sslConfig.setServerKeyStorePassword("password");
        when(envVarProvider.hasEnv(EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(true);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(false);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Server keystore configuration not valid. " +
            "Please ensure keystore file exists or keystore password not null, " +
            "otherwise please set keystore generation flag to true to have keystore created";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for reasons other than server keystore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void serverKeyStorePasswordInConfigAndPrefixedEnvVarThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(false);
        sslConfig.setServerKeyStore(tmpFile);
        sslConfig.setEnvironmentVariablePrefix("PREFIX");

        sslConfig.setServerKeyStorePassword("password");
        when(envVarProvider.hasEnv(EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(false);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(true);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Server keystore configuration not valid. " +
            "Please ensure keystore file exists or keystore password not null, " +
            "otherwise please set keystore generation flag to true to have keystore created";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for reasons other than server keystore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void serverKeyStorePasswordInGlobalAndPrefixedEnvVarThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(false);
        sslConfig.setServerKeyStore(tmpFile);
        sslConfig.setEnvironmentVariablePrefix("PREFIX");

        sslConfig.setServerKeyStorePassword(null);
        when(envVarProvider.hasEnv(EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(true);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(true);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Server keystore configuration not valid. " +
            "Please ensure keystore file exists or keystore password not null, " +
            "otherwise please set keystore generation flag to true to have keystore created";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for reasons other than server keystore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void serverKeyStorePasswordInConfigAndGlobalAndPrefixedEnvVarThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(false);
        sslConfig.setServerKeyStore(tmpFile);
        sslConfig.setEnvironmentVariablePrefix("PREFIX");

        sslConfig.setServerKeyStorePassword("password");
        when(envVarProvider.hasEnv(EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(true);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(true);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Server keystore configuration not valid. " +
            "Please ensure keystore file exists or keystore password not null, " +
            "otherwise please set keystore generation flag to true to have keystore created";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for reasons other than server keystore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }


    @Test
    public void noClientKeyStorePasswordInConfigOrEnvVarsThenInvalid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(false);
        sslConfig.setServerKeyStore(tmpFile);
        sslConfig.setServerKeyStorePassword("pwd");

        sslConfig.setClientKeyStore(tmpFile);

        sslConfig.setClientKeyStorePassword(null);
        when(envVarProvider.hasEnv(anyString())).thenReturn(false);

        final boolean result = validator.isValid(sslConfig, context);

        assertThat(result).isFalse();

        final String msg = "Client keystore configuration not valid. " +
            "Please ensure keystore file exists or keystore password not null, " +
            "otherwise please set keystore generation flag to true to have keystore created";
        verify(context).buildConstraintViolationWithTemplate(msg);
    }

    @Test
    public void clientKeyStorePasswordInConfigOnlyThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(false);
        sslConfig.setServerKeyStore(tmpFile);
        sslConfig.setServerKeyStorePassword("password");

        sslConfig.setClientKeyStore(tmpFile);

        sslConfig.setClientKeyStorePassword("password");
        when(envVarProvider.hasEnv(EnvironmentVariables.CLIENT_KEYSTORE_PWD)).thenReturn(false);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD)).thenReturn(false);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Client keystore configuration not valid. " +
            "Please ensure keystore file exists or keystore password not null, " +
            "otherwise please set keystore generation flag to true to have keystore created";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for some reason other than server keystore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void clientKeyStorePasswordInGlobalEnvVarOnlyThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(false);
        sslConfig.setServerKeyStore(tmpFile);
        sslConfig.setServerKeyStorePassword("pwd");

        sslConfig.setClientKeyStore(tmpFile);

        sslConfig.setClientKeyStorePassword(null);
        when(envVarProvider.hasEnv(EnvironmentVariables.CLIENT_KEYSTORE_PWD)).thenReturn(true);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD)).thenReturn(false);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Client keystore configuration not valid. " +
            "Please ensure keystore file exists or keystore password not null, " +
            "otherwise please set keystore generation flag to true to have keystore created";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for reasons other than server keystore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void clientKeyStorePasswordInPrefixedEnvVarOnlyThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(false);
        sslConfig.setServerKeyStore(tmpFile);
        sslConfig.setServerKeyStorePassword("pwd");
        sslConfig.setEnvironmentVariablePrefix("PREFIX");
        sslConfig.setClientKeyStore(tmpFile);

        sslConfig.setClientKeyStorePassword(null);
        when(envVarProvider.hasEnv(EnvironmentVariables.CLIENT_KEYSTORE_PWD)).thenReturn(false);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD)).thenReturn(true);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Client keystore configuration not valid. " +
            "Please ensure keystore file exists or keystore password not null, " +
            "otherwise please set keystore generation flag to true to have keystore created";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for reasons other than server keystore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void clientKeyStorePasswordInConfigAndGlobalEnvVarThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(false);
        sslConfig.setServerKeyStore(tmpFile);
        sslConfig.setServerKeyStorePassword("pwd");

        sslConfig.setClientKeyStore(tmpFile);

        sslConfig.setClientKeyStorePassword("password");
        when(envVarProvider.hasEnv(EnvironmentVariables.CLIENT_KEYSTORE_PWD)).thenReturn(true);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD)).thenReturn(false);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Client keystore configuration not valid. " +
            "Please ensure keystore file exists or keystore password not null, " +
            "otherwise please set keystore generation flag to true to have keystore created";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for reasons other than server keystore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void clientKeyStorePasswordInConfigAndPrefixedEnvVarThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(false);
        sslConfig.setServerKeyStore(tmpFile);
        sslConfig.setServerKeyStorePassword("pwd");
        sslConfig.setEnvironmentVariablePrefix("PREFIX");
        sslConfig.setClientKeyStore(tmpFile);

        sslConfig.setClientKeyStorePassword("password");
        when(envVarProvider.hasEnv(EnvironmentVariables.CLIENT_KEYSTORE_PWD)).thenReturn(false);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD)).thenReturn(true);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Client keystore configuration not valid. " +
            "Please ensure keystore file exists or keystore password not null, " +
            "otherwise please set keystore generation flag to true to have keystore created";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for reasons other than server keystore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void clientKeyStorePasswordInGlobalAndPrefixedEnvVarThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(false);
        sslConfig.setServerKeyStore(tmpFile);
        sslConfig.setServerKeyStorePassword("pwd");
        sslConfig.setEnvironmentVariablePrefix("PREFIX");
        sslConfig.setClientKeyStore(tmpFile);

        sslConfig.setClientKeyStorePassword(null);
        when(envVarProvider.hasEnv(EnvironmentVariables.CLIENT_KEYSTORE_PWD)).thenReturn(true);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD)).thenReturn(true);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Client keystore configuration not valid. " +
            "Please ensure keystore file exists or keystore password not null, " +
            "otherwise please set keystore generation flag to true to have keystore created";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for reasons other than server keystore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void clientKeyStorePasswordInConfigAndGlobalAndPrefixedEnvVarThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(false);
        sslConfig.setServerKeyStore(tmpFile);
        sslConfig.setServerKeyStorePassword("pwd");
        sslConfig.setEnvironmentVariablePrefix("PREFIX");
        sslConfig.setClientKeyStore(tmpFile);

        sslConfig.setClientKeyStorePassword("password");
        when(envVarProvider.hasEnv(EnvironmentVariables.CLIENT_KEYSTORE_PWD)).thenReturn(true);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD)).thenReturn(true);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Client keystore configuration not valid. " +
            "Please ensure keystore file exists or keystore password not null, " +
            "otherwise please set keystore generation flag to true to have keystore created";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for reasons other than server keystore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void testTrustModeNull() {
        SslConfig sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, null, tmpFile, "password", null, null, null, null, null, null, null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, SslTrustMode.CA, tmpFile, "password", null, null, null, null, null, null, null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();
    }

    @Test
    public void testTrustModeWhiteListButKnownHostsFileNotExisted() {
        SslConfig sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, SslTrustMode.WHITELIST, tmpFile, "password", null, null, SslTrustMode.WHITELIST, null, null, null, null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, SslTrustMode.WHITELIST, tmpFile, "password", null, null, SslTrustMode.WHITELIST, Paths.get("somefile"), null, null, null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, SslTrustMode.WHITELIST, tmpFile, "password", null, null, SslTrustMode.WHITELIST, tmpFile, null, null, null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, SslTrustMode.WHITELIST, tmpFile, "password", null, null, SslTrustMode.WHITELIST, tmpFile, Paths.get("some"),null,null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();
    }

    @Test
    public void testTrustModeCAButTrustStoreConfigInvalid() {
        SslConfig sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, SslTrustMode.CA, tmpFile, "password", null, null, SslTrustMode.NONE, null, null, null, null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", tmpFile, null, SslTrustMode.CA, tmpFile, "password", null, null, SslTrustMode.NONE, null, null, null, null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", Paths.get("somefile"), "password", SslTrustMode.CA, tmpFile, "password", null, null, SslTrustMode.NONE, null, null, null, null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", tmpFile, "p", SslTrustMode.CA, tmpFile, "password", null, null, SslTrustMode.CA, null, null, null, null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", tmpFile, null, SslTrustMode.CA, tmpFile, "password", tmpFile, null, SslTrustMode.CA, null, null, null, null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", Paths.get("somefile"), "password", SslTrustMode.CA, tmpFile, "password", Paths.get("somefile"), "p", SslTrustMode.CA, null, null, null, null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();
    }

    @Test
    public void serverCaModeNoTrustStorePasswordInConfigOrEnvVarsThenInvalid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(true);
        sslConfig.setServerTrustMode(SslTrustMode.CA);
        sslConfig.setClientTrustMode(SslTrustMode.CA);
        sslConfig.setServerTrustStore(tmpFile);

        sslConfig.setServerTrustStorePassword(null);
        when(envVarProvider.hasEnv(anyString())).thenReturn(false);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Trust store config not valid. If server trust mode is CA, trust store must exist and not be null";
        verify(context).buildConstraintViolationWithTemplate(msg);

        assertThat(result).isFalse();
    }

    @Test
    public void serverCaModeTrustStorePasswordInConfigOnlyThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(true);
        sslConfig.setServerTrustMode(SslTrustMode.CA);
        sslConfig.setClientTrustMode(SslTrustMode.CA);
        sslConfig.setServerTrustStore(tmpFile);

        sslConfig.setServerTrustStorePassword("password");
        when(envVarProvider.hasEnv(EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(false);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(false);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Trust store config not valid. If server trust mode is CA, trust store must exist and not be null";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for reasons other than server truststore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void serverCaModeTrustStorePasswordInGlobalEnvVarOnlyThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(true);
        sslConfig.setServerTrustMode(SslTrustMode.CA);
        sslConfig.setClientTrustMode(SslTrustMode.CA);
        sslConfig.setServerTrustStore(tmpFile);

        sslConfig.setServerTrustStorePassword(null);
        when(envVarProvider.hasEnv(EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(true);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(false);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Trust store config not valid. If server trust mode is CA, trust store must exist and not be null";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for reasons other than server truststore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void serverCaModeTrustStorePasswordInPrefixedEnvVarOnlyThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(true);
        sslConfig.setServerTrustMode(SslTrustMode.CA);
        sslConfig.setClientTrustMode(SslTrustMode.CA);
        sslConfig.setServerTrustStore(tmpFile);
        sslConfig.setEnvironmentVariablePrefix("PREFIX");

        sslConfig.setServerTrustStorePassword(null);
        when(envVarProvider.hasEnv(EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(false);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(true);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Trust store config not valid. If server trust mode is CA, trust store must exist and not be null";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for reasons other than server truststore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void serverCaModeTrustStorePasswordInConfigAndGlobalEnvVarThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(true);
        sslConfig.setServerTrustMode(SslTrustMode.CA);
        sslConfig.setClientTrustMode(SslTrustMode.CA);
        sslConfig.setServerTrustStore(tmpFile);

        sslConfig.setServerTrustStorePassword("password");
        when(envVarProvider.hasEnv(EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(true);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(false);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Trust store config not valid. If server trust mode is CA, trust store must exist and not be null";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for reasons other than server truststore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void serverCaModeTrustStorePasswordInConfigAndPrefixedEnvVarThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(true);
        sslConfig.setServerTrustMode(SslTrustMode.CA);
        sslConfig.setClientTrustMode(SslTrustMode.CA);
        sslConfig.setServerTrustStore(tmpFile);
        sslConfig.setEnvironmentVariablePrefix("PREFIX");

        sslConfig.setServerTrustStorePassword("password");
        when(envVarProvider.hasEnv(EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(false);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(true);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Trust store config not valid. If server trust mode is CA, trust store must exist and not be null";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for reasons other than server truststore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void serverCaModeTrustStorePasswordInGlobalAndPrefixedEnvVarThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(true);
        sslConfig.setServerTrustMode(SslTrustMode.CA);
        sslConfig.setClientTrustMode(SslTrustMode.CA);
        sslConfig.setServerTrustStore(tmpFile);
        sslConfig.setEnvironmentVariablePrefix("PREFIX");

        sslConfig.setServerTrustStorePassword(null);
        when(envVarProvider.hasEnv(EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(true);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(true);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Trust store config not valid. If server trust mode is CA, trust store must exist and not be null";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for reasons other than server truststore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void serverCaModeTrustStorePasswordInConfigAndGlobalAndPrefixedEnvVarThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(true);
        sslConfig.setServerTrustMode(SslTrustMode.CA);
        sslConfig.setClientTrustMode(SslTrustMode.CA);
        sslConfig.setServerTrustStore(tmpFile);
        sslConfig.setEnvironmentVariablePrefix("PREFIX");

        sslConfig.setServerTrustStorePassword("password");
        when(envVarProvider.hasEnv(EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(true);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(true);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Trust store config not valid. If server trust mode is CA, trust store must exist and not be null";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        // validation then fails for reasons other than server truststore config
        verify(context).buildConstraintViolationWithTemplate(anyString());
        assertThat(result).isFalse();
    }

    @Test
    public void clientCaModeNoTrustStorePasswordInConfigOrEnvVarsThenInvalid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(true);
        sslConfig.setServerTrustMode(SslTrustMode.CA);
        sslConfig.setClientTrustMode(SslTrustMode.CA);
        sslConfig.setServerTrustStore(tmpFile);
        sslConfig.setServerTrustStorePassword("password");
        sslConfig.setClientTrustStore(tmpFile);

        sslConfig.setClientTrustStorePassword(null);
        when(envVarProvider.hasEnv(anyString())).thenReturn(false);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Trust store config not valid. If client trust mode is CA, trust store must exist and not be null";
        verify(context).buildConstraintViolationWithTemplate(msg);

        assertThat(result).isFalse();
    }

    @Test
    public void clientCaModeTrustStorePasswordInConfigOnlyThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(true);
        sslConfig.setServerTrustMode(SslTrustMode.CA);
        sslConfig.setClientTrustMode(SslTrustMode.CA);
        sslConfig.setServerTrustStore(tmpFile);
        sslConfig.setServerTrustStorePassword("password");
        sslConfig.setClientTrustStore(tmpFile);

        sslConfig.setClientTrustStorePassword("password");
        when(envVarProvider.hasEnv(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD)).thenReturn(false);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD)).thenReturn(false);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Trust store config not valid. If client trust mode is CA, trust store must exist and not be null";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        assertThat(result).isTrue();
    }

    @Test
    public void clientCaModeTrustStorePasswordInGlobalEnvVarOnlyThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(true);
        sslConfig.setServerTrustMode(SslTrustMode.CA);
        sslConfig.setClientTrustMode(SslTrustMode.CA);
        sslConfig.setServerTrustStore(tmpFile);
        sslConfig.setServerTrustStorePassword("password");
        sslConfig.setClientTrustStore(tmpFile);

        sslConfig.setClientTrustStorePassword(null);
        when(envVarProvider.hasEnv(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD)).thenReturn(true);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD)).thenReturn(false);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Trust store config not valid. If client trust mode is CA, trust store must exist and not be null";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        assertThat(result).isTrue();
    }

    @Test
    public void clientCaModeTrustStorePasswordInPrefixedEnvVarOnlyThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(true);
        sslConfig.setServerTrustMode(SslTrustMode.CA);
        sslConfig.setClientTrustMode(SslTrustMode.CA);
        sslConfig.setServerTrustStore(tmpFile);
        sslConfig.setServerTrustStorePassword("password");
        sslConfig.setClientTrustStore(tmpFile);
        sslConfig.setEnvironmentVariablePrefix("PREFIX");

        sslConfig.setClientTrustStorePassword(null);
        when(envVarProvider.hasEnv(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD)).thenReturn(false);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD)).thenReturn(true);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Trust store config not valid. If client trust mode is CA, trust store must exist and not be null";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        assertThat(result).isTrue();
    }

    @Test
    public void clientCaModeTrustStorePasswordInConfigAndGlobalEnvVarThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(true);
        sslConfig.setServerTrustMode(SslTrustMode.CA);
        sslConfig.setClientTrustMode(SslTrustMode.CA);
        sslConfig.setServerTrustStore(tmpFile);
        sslConfig.setServerTrustStorePassword("password");
        sslConfig.setClientTrustStore(tmpFile);

        sslConfig.setClientTrustStorePassword("password");
        when(envVarProvider.hasEnv(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD)).thenReturn(true);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD)).thenReturn(false);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Trust store config not valid. If client trust mode is CA, trust store must exist and not be null";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        assertThat(result).isTrue();
    }

    @Test
    public void clientCaModeTrustStorePasswordInConfigAndPrefixedEnvVarThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(true);
        sslConfig.setServerTrustMode(SslTrustMode.CA);
        sslConfig.setClientTrustMode(SslTrustMode.CA);
        sslConfig.setServerTrustStore(tmpFile);
        sslConfig.setServerTrustStorePassword("password");
        sslConfig.setClientTrustStore(tmpFile);
        sslConfig.setEnvironmentVariablePrefix("PREFIX");

        sslConfig.setClientTrustStorePassword("password");
        when(envVarProvider.hasEnv(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD)).thenReturn(false);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD)).thenReturn(true);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Trust store config not valid. If client trust mode is CA, trust store must exist and not be null";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        assertThat(result).isTrue();
    }

    @Test
    public void clientCaModeTrustStorePasswordInGlobalAndPrefixedEnvVarThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(true);
        sslConfig.setServerTrustMode(SslTrustMode.CA);
        sslConfig.setClientTrustMode(SslTrustMode.CA);
        sslConfig.setServerTrustStore(tmpFile);
        sslConfig.setServerTrustStorePassword("password");
        sslConfig.setClientTrustStore(tmpFile);
        sslConfig.setEnvironmentVariablePrefix("PREFIX");

        sslConfig.setClientTrustStorePassword(null);
        when(envVarProvider.hasEnv(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD)).thenReturn(true);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD)).thenReturn(true);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Trust store config not valid. If client trust mode is CA, trust store must exist and not be null";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        assertThat(result).isTrue();
    }

    @Test
    public void clientCaModeTrustStorePasswordInConfigAndGlobalAndPrefixedEnvVarThenValid() {
        final SslConfig sslConfig = new SslConfig();

        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(true);
        sslConfig.setServerTrustMode(SslTrustMode.CA);
        sslConfig.setClientTrustMode(SslTrustMode.CA);
        sslConfig.setServerTrustStore(tmpFile);
        sslConfig.setServerTrustStorePassword("password");
        sslConfig.setClientTrustStore(tmpFile);
        sslConfig.setEnvironmentVariablePrefix("PREFIX");

        sslConfig.setClientTrustStorePassword("password");
        when(envVarProvider.hasEnv(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD)).thenReturn(true);
        when(envVarProvider.hasEnv(sslConfig.getEnvironmentVariablePrefix() + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD)).thenReturn(true);

        final boolean result = validator.isValid(sslConfig, context);

        final String msg = "Trust store config not valid. If client trust mode is CA, trust store must exist and not be null";
        verify(context, never()).buildConstraintViolationWithTemplate(msg);

        assertThat(result).isTrue();
    }

    @Test
    public void testNoKeyStoreFilesButPemFilesProvided() {
        SslConfig sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, false, null, null, null, null, SslTrustMode.CA, null, null, null, null, SslTrustMode.CA, null, null, Arrays.asList(tmpFile), Arrays.asList(tmpFile), tmpFile,tmpFile,tmpFile,tmpFile,null
        );
        assertThat(validator.isValid(sslConfig, context)).isTrue();
    }

    @Test
    public void testValidSsl() {
        SslConfig sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, false, tmpFile, "pw", tmpFile, "pw", SslTrustMode.CA, tmpFile, "pw", tmpFile, "pw", SslTrustMode.CA, tmpFile, tmpFile, Arrays.asList(tmpFile), Arrays.asList(tmpFile), tmpFile,tmpFile,tmpFile,tmpFile,null
        );
        assertThat(validator.isValid(sslConfig, context)).isTrue();
    }
}

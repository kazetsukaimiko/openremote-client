package com.revenerg.client;

import lombok.Getter;
import lombok.extern.jbosslog.JBossLog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

@JBossLog
@Getter
public class ClientConfig {
    private final String address;
    private final String clientId;
    private final String realm;
    private final String username;
    private final String secret;
    private final String mqttUser;
    private final boolean useSSL;
    private final Path tlsCert;

    public ClientConfig(Path propertiesFile) throws IOException {
        Properties p = new Properties();
        p.load(Files.newInputStream(propertiesFile));

        this.address = p.getProperty("address", null);
        this.clientId = p.getProperty("clientId", null);
        this.realm =  p.getProperty("realm", null);
        this.username =  p.getProperty("username", null);
        this.secret =  p.getProperty("secret", null);
        this.mqttUser = realm + ":" + username;
        this.useSSL = address.startsWith("ssl");

        // Find a TLS cert relative to the properties file if needed.
        this.tlsCert = Optional.ofNullable(p.getProperty("tlsCert", null))
            .map(Paths::get)
            .map(path -> path.isAbsolute()
                    ? path
                    : propertiesFile.getParent().relativize(path))
                    .orElse(null);
    }


    public ClientConfig validate() {
        validateAddress();
        validateMqttInfo();
        validateSSL();
        return this;
    }

    private void validateAddress() {
        if (!(address.startsWith("ssl") || address.startsWith("tcp"))) {
            throw new IllegalArgumentException("Address must begin with a protocol, ssl:// or tcp:// (Java Specific).");
        }
    }

    private void validateMqttInfo() {
        if (clientId == null) {
            throw new IllegalArgumentException("clientId must be present.");
        }
        if (realm == null) {
            throw new IllegalArgumentException("realm must be present.");
        }
        if (username == null) {
            throw new IllegalArgumentException("username must be present.");
        }
        if (secret == null) {
            throw new IllegalArgumentException("secret must be present.");
        }
    }

    private void validateSSL() {
        if (useSSL) {
            if (tlsCert != null) {
                if (!Files.exists(tlsCert)) {
                    throw new IllegalStateException("CA File '%s' doesn't exist".formatted(tlsCert));
                }
                if (!Files.exists(tlsCert)) {
                    throw new IllegalStateException("CA File '%s' doesn't exist".formatted(tlsCert));
                }
                if (!Files.isReadable(tlsCert)) {
                    throw new IllegalStateException("CA File '%s' tlsCertnnot be read".formatted(tlsCert));
                }
            }
        }
    }
}

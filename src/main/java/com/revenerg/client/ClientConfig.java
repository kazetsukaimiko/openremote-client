package com.revenerg.client;

import com.revenerg.client.cmd.OpenSSLCommand;
import lombok.Getter;
import lombok.extern.jbosslog.JBossLog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

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
    private final boolean autoProvision;
    private final Path tlsCert;
    private final Path realmCert;
    private final Path realmCertKey;
    
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
        this.autoProvision = Objects.equals("true", p.getProperty("autoprovision", null));

        // Find a TLS cert relative to the properties file if needed.
        this.tlsCert = Optional.ofNullable(p.getProperty("tlsCert", null))
                .map(Paths::get)
                .map(path -> path.isAbsolute()
                        ? path
                        : propertiesFile.getParent().resolve(path))
                .orElse(null);

        // Find a Realm cert relative to the properties file if needed.
        this.realmCert = Optional.ofNullable(p.getProperty("realmCert", null))
            .map(Paths::get)
            .map(path -> path.isAbsolute()
                    ? path
                    : propertiesFile.getParent().resolve(path))
                    .orElse(null);

        // Find a Realm cert key relative to the properties file if needed.
        this.realmCertKey = Optional.ofNullable(p.getProperty("realmCertKey", null))
                .map(Paths::get)
                .map(path -> path.isAbsolute()
                        ? path
                        : propertiesFile.getParent().resolve(path))
                .orElse(null);

    }


    public ClientConfig validate() {
        log.infof("Validating ClientConfig.");
        if (validateAddress() && validateMqttInfo() &&  validateSSL()) {
            log.infof("Valid ClientConfig.");
            return this;
        }
        log.errorf("Could not validate configuration.");
        throw new IllegalStateException("Could not validate config.");
    }

    private boolean validateAddress() {
        if (!(address.startsWith("ssl") || address.startsWith("tcp"))) {
            log.errorf("Address must begin with a protocol, ssl:// or tcp:// (Java Specific).");
            return false;
        }
        return true;
    }

    private boolean validateMqttInfo() {
        if (clientId == null) {
            log.errorf("clientId must be present.");
        }
        if (realm == null) {
            log.errorf("realm must be present.");
        }
        if (username == null) {
            log.errorf("username must be present.");
        }
        if (secret == null) {
            log.errorf("secret must be present.");
        }
        return Stream.of(clientId, realm, username, secret).noneMatch(Objects::isNull);
    }

    private boolean validateSSL() {
        if (useSSL) {
            if (realmCert != null) {
                return OpenSSLCommand.accessibilityCheck("tlsCert", realmCert);
            }
            return false;
        }
        return true;
    }
}

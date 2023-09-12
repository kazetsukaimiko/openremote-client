package com.revenerg.client;

import com.revenerg.client.cmd.GenerateDeviceKeyAndCSR;
import com.revenerg.client.cmd.GenerateDeviceCertSignedByRealmCert;
import com.revenerg.client.cmd.model.DeviceInfo;
import com.revenerg.client.cmd.model.RealmCertInfo;
import com.revenerg.client.cmd.model.DeviceCert;
import com.revenerg.client.cmd.model.DeviceKeyAndCSR;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

@JBossLog
public class ClientTests {

    static final Path PROPERTIES_FILE = Paths.get(System.getProperty("user.home"), "openremote.properties");

    @Test
    public void testConnectingToOpenRemote() throws Exception {

        // Start with a Realm CA Cert and signing key. (ca.pem and ca.key respectively)
        ClientConfig config = new ClientConfig(PROPERTIES_FILE);

        // Create a device CSR + key. We will use the CSR to generate a device-specific PEM file.
        // The class that calls openssl to generate the device CSR + Key.
        DeviceKeyAndCSR csr = GenerateDeviceKeyAndCSR.INSTANCE.apply(new DeviceInfo(
                config.getClientId()));

        // Create a cert specific to the device signed by the Realm CA Cert above.
        DeviceCert deviceCert = GenerateDeviceCertSignedByRealmCert.INSTANCE.apply(new RealmCertInfo(
                config.getClientId(),
                config.getRealmCert(),
                config.getRealmCertKey(),
                csr,
                Duration.ofDays(365 * 20)));

        log.infof("Creating OpenRemoteClient.");
        // Lets connect!
        try (OpenRemoteClient client = new OpenRemoteClient(config, deviceCert)) {
            log.infof("Successfully connected and provisioned.");

            // TODO Send some data here as attributes.

            Thread.sleep(5000);
        }
        log.infof("Disconnected, finish.");
    }

    private void receiveResponse(String topic, MqttMessage mqttMessage) {
        log.infof("Received message on topic '%s':\n%s",
                topic, new String(mqttMessage.getPayload(), StandardCharsets.UTF_8));
    }

}

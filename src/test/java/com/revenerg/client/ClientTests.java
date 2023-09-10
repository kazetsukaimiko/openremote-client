package com.revenerg.client;

import com.revenerg.client.cmd.GenerateKeyAndCSR;
import com.revenerg.client.cmd.GenerateSignedCert;
import com.revenerg.client.cmd.model.CSRInfo;
import com.revenerg.client.cmd.model.CertInfo;
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

    Path PROPERTIES_FILE = Paths.get(System.getProperty("user.home"), "openremote.properties");

    @Test
    public void testConnectingToOpenRemote() throws Exception {

        // Start with a Realm CA Cert and signing key.
        ClientConfig config = new ClientConfig(PROPERTIES_FILE);

        // Create a device CSR + key.
        CSRInfo info = new CSRInfo(config.getClientId());
        DeviceKeyAndCSR csr = GenerateKeyAndCSR.INSTANCE.apply(info);

        // Create a cert for the device signed by the Realm CA Cert above.
        CertInfo certInfo = new CertInfo(config.getClientId(), config.getTlsCert(), config.getTlsCertKey(), csr, Duration.ofDays(365 * 20));
        DeviceCert deviceCert = GenerateSignedCert.INSTANCE.apply(certInfo);

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

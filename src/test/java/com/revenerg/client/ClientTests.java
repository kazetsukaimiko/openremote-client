package com.revenerg.client;

import com.revenerg.util.ssl.CACertSocketFactory;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Properties;

import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1_1;

@JBossLog
public class ClientTests {

    Path PROPERTIES_FILE = Paths.get(System.getProperty("user.home"), "openremote.properties");
    Path TLS_ROOT_CA_FILE = Paths.get(System.getProperty("user.home"), "sheesh", "openremote", "certs", "isrgrootx1.pem");

    @Test
    public void testConnectingToOpenRemote() throws Exception {

        ClientConfig config = new ClientConfig(PROPERTIES_FILE);

        try (OpenRemoteClient client = new OpenRemoteClient(config)) {
            Thread.sleep(5000);
        }




        /*
        log.infof("Creating MqttClient for %s as %s", config.getAddress(), config.getClientId());

        MemoryPersistence persistence = new MemoryPersistence();
        try (MqttClient mqttClient = new MqttClient(config.getAddress(), config.getClientId(), persistence)) {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setMqttVersion(MQTT_VERSION_3_1_1);

            log.infof("Setting username '%s'.", config.getMqttUser());
            options.setUserName(config.getMqttUser());
            options.setPassword(config.getSecret().toCharArray());

            if (config.isUseSSL()) {
                SSLSocketFactory socketFactory =
                        //AllTrustingSocketFactory.INSTANCE.getFactory();
                        new CACertSocketFactory(TLS_ROOT_CA_FILE).getFactory();
                options.setSocketFactory(socketFactory);
            }

            options.setCleanSession(true);
            options.setAutomaticReconnect(true);

            if (mqttClient.isConnected()) {
                log.infof("Connected to MQTT Server.");
            } else {
                log.infof("Connecting.");
                mqttClient.connect(options);
            }

            var responseTopic = OpenRemoteClient.PROVISIONING_RESPONSE_TOPIC.formatted(config.getClientId());
            log.infof("Subscribing to '%s'".formatted(responseTopic));
            mqttClient.subscribe(responseTopic, this::receiveResponse);

            Thread.sleep(5000);

            log.infof("Disconnecting.");
            mqttClient.disconnect();
        }

         */





    }

    private void receiveResponse(String topic, MqttMessage mqttMessage) {
        log.infof("Received message on topic '%s':\n%s",
                topic, new String(mqttMessage.getPayload(), StandardCharsets.UTF_8));
    }

}

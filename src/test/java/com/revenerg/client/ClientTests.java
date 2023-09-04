package com.revenerg.client;

import com.revenerg.util.ssl.AllTrustingSocketFactory;
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
import java.util.Map;
import java.util.Properties;

@JBossLog
public class ClientTests {

    Path PROPERTIES_FILE = Paths.get(System.getProperty("user.home"), "openremote.properties");
    Path CA_FILE = Paths.get(System.getProperty("user.home"), "sheesh", "openremote", "certs", "isrgrootx1.pem");

    @Test
    public void testConnectingToOpenRemote() throws MqttException, NoSuchAlgorithmException, KeyManagementException, IOException, InterruptedException, CertificateException, KeyStoreException {
        Properties p = new Properties();
        p.load(Files.newInputStream(PROPERTIES_FILE));

        String address = p.getProperty("address", null);
        String clientId = p.getProperty("clientId", null);
        String realm =  p.getProperty("realm", null);
        String username =  p.getProperty("username", null);
        String secret =  p.getProperty("secret", null);

        String mqttUser = realm + ":" + username;

        log.infof("Creating MqttClient for %s as %s", address, clientId);

        MemoryPersistence persistence = new MemoryPersistence();
        try (MqttClient mqttClient = new MqttClient(address, clientId, persistence)) {
            MqttConnectOptions options = new MqttConnectOptions();

            log.infof("Setting username '%s'.", mqttUser);
            options.setUserName(mqttUser);
            options.setPassword(secret.toCharArray());

            // SSL.
            SSLSocketFactory socketFactory =
                    //AllTrustingSocketFactory.INSTANCE.getFactory();
                    new CACertSocketFactory(CA_FILE).getFactory();
            
            options.setSocketFactory(socketFactory);

            options.setCleanSession(true);
            //options.setAutomaticReconnect(true);

            if (mqttClient.isConnected()) {
                log.infof("Connected to MQTT Server.");
            } else {
                log.infof("Connecting.");
                mqttClient.connect(options);
            }

            var responseTopic = OpenRemoteClient.PROVISIONING_RESPONSE_TOPIC.formatted(clientId);
            log.infof("Subscribing to '%s'".formatted(responseTopic));
            mqttClient.subscribe(responseTopic, this::receiveResponse);

            Thread.sleep(5000);

            log.infof("Disconnecting.");
            mqttClient.disconnect();
        }


    }

    private void receiveResponse(String topic, MqttMessage mqttMessage) {
        log.infof("Received message on topic '%s':\n%s",
                topic, new String(mqttMessage.getPayload(), StandardCharsets.UTF_8));
    }

}

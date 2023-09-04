package com.revenerg.client;

import com.revenerg.util.ssl.AllTrustingSocketFactory;
import lombok.extern.jbosslog.JBossLog;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@JBossLog
public class ClientTests {


    @Test
    public void testConnectingToOpenRemote() throws MqttException, NoSuchAlgorithmException, KeyManagementException {

        String clientId = "clientId";
        String address = "ssl://104.37.189.62:8883";

        log.infof("Creating MqttClient for %s as %s", address, clientId);

        MemoryPersistence persistence = new MemoryPersistence();
        try (MqttClient mqttClient = new MqttClient(address, clientId, persistence)) {
            MqttConnectOptions options = new MqttConnectOptions();

            // SSL.
            SSLSocketFactory socketFactory = AllTrustingSocketFactory.INSTANCE.getFactory();
            options.setHttpsHostnameVerificationEnabled(false); // TODO Fix this with a CA.
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
            mqttClient.subscribe(responseTopic);

            Thread.sleep(5000);

            log.infof("Disconnecting.");
            mqttClient.disconnect();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

}

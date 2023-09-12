package com.revenerg.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revenerg.client.cmd.model.DeviceCert;
import com.revenerg.client.request.ProvisionRequest;
import com.revenerg.client.response.ErrorProvisionResponse;
import com.revenerg.client.response.ProvisionResponse;
import com.revenerg.client.response.SuccessProvisionResponse;
import com.revenerg.util.ssl.AllTrustingSocketFactory;
import com.revenerg.util.ssl.CACertSocketFactory;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1_1;


@Getter
@JBossLog
public class OpenRemoteClient implements AutoCloseable {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DELIM = "\n---------------------------------------------------------";

    public static final String PROVISIONING_REQUEST_TOPIC = "provisioning/%s/request";
    public static final String PROVISIONING_RESPONSE_TOPIC = "provisioning/%s/response";

    private final ClientConfig config;
    private final MqttClient client;

    public OpenRemoteClient(@NonNull ClientConfig config, DeviceCert deviceCert) throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, MqttException, InterruptedException {
        this.config = config.validate();
        this.client = createClient(config);
        if (deviceCert != null) {
            autoProvision(deviceCert);
        }
    }


    public OpenRemoteClient subscribe(String topic, IMqttMessageListener messageListener) throws MqttException {
        client.subscribe(topic, messageListener);
        return this;
    }

    @Override
    public void close() throws Exception {
        log.infof("Disconnecting.");
        client.disconnect();
    }

    private void autoProvision(@NonNull DeviceCert deviceCert) throws MqttException, InterruptedException, IOException {
        log.infof("Entering Automatic Provisioning.");

        // Setup provisioning listener.
        var responseTopic = OpenRemoteClient.PROVISIONING_RESPONSE_TOPIC.formatted(config.getClientId());
        log.infof("Subscribing to '%s'".formatted(responseTopic));
        final BlockingQueue<ProvisionResponse> queue = new LinkedBlockingQueue<>();
        client.subscribe(responseTopic, (topic, message) -> {
            log.infof("Received provision response.");
            queue.put(OBJECT_MAPPER.readValue(debugMessage(topic, message).getPayload(), ProvisionResponse.class));
                });

        // Send certificate.
        ProvisionRequest provisionRequest = new ProvisionRequest(Files.readString(deviceCert.devicePem()));
        client.publish(OpenRemoteClient.PROVISIONING_REQUEST_TOPIC.formatted(config.getClientId()),
                asMqttMessage(provisionRequest));

        log.infof("Waiting for provisioning response.");
        ProvisionResponse response = queue.poll(5, TimeUnit.SECONDS);
        log.infof("Response poll done. %s", response);


        if (response instanceof ErrorProvisionResponse error) {
            log.errorf("Provisioning error: %s", error.getError());
            throw new IllegalStateException("Error provisioning: %s".formatted(error.getError()));
        } else if (response instanceof SuccessProvisionResponse success) {
            log.infof("Successfully provisioned with realm %s ", success.getRealm());
        } else if (response == null) {
            log.errorf("Provisioning timeout.");
            throw new IllegalStateException("Provisioning timeout.");
        } else {
            log.infof("Unknown condition: %s", response);
        }
    }

    private MqttMessage debugMessage(String topic, MqttMessage message) {
        log.infof("Received message on topic '%s':%s\n%s%s",
                topic, DELIM, new String(message.getPayload(), StandardCharsets.UTF_8), DELIM);
        return message;
    }

    private static MqttMessage asMqttMessage(ProvisionRequest provisionRequest) throws IOException {
        MqttMessage mqttMessage = new MqttMessage();
        try {
            mqttMessage.setPayload(OBJECT_MAPPER.writeValueAsBytes(provisionRequest));
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        }
        mqttMessage.setQos(0);
        mqttMessage.setRetained(false);
        return mqttMessage;
    }

    private static MqttClient createClient(ClientConfig config) throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, MqttException {
        log.infof("Creating MqttClient for %s as %s", config.getAddress(), config.getClientId());
        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient mqttClient = new MqttClient(config.getAddress(), config.getClientId(), persistence);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MQTT_VERSION_3_1_1);

        log.infof("Setting username '%s'.", config.getMqttUser());
        options.setUserName(config.getMqttUser());
        options.setPassword(config.getSecret().toCharArray());
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);

        if (config.isUseSSL()) {
            log.infof("Using TLS.");
            if (config.getTlsCert() != null) {
                log.infof("Using TLS Cert: %s", config.getRealmCert());
                options.setSocketFactory(new CACertSocketFactory(config.getTlsCert()).getFactory());
            } else {
                log.warnf("No TLS Cert provided, trusting connection.");
                options.setSocketFactory(AllTrustingSocketFactory.INSTANCE.getFactory());
            }
        }

        if (mqttClient.isConnected()) {
            log.infof("Connected to MQTT Server.");
        } else {
            log.infof("Connecting.");
            mqttClient.connect(options);
        }

        return mqttClient;
    }
}

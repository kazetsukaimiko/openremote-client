package com.revenerg.client.cmd;

import com.revenerg.client.cmd.model.DeviceCert;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.jbosslog.JBossLog;

import java.nio.file.Files;
import java.nio.file.Path;

@JBossLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BinaryDeviceCert extends OpenSSLCommand<DeviceCert, DeviceCert> {
    public static final BinaryDeviceCert INSTANCE = new BinaryDeviceCert();
    @Override
    public DeviceCert apply(DeviceCert deviceCert) {
        Path deviceDer = tempFile(deviceCert.realmCertInfo().deviceId(), deviceCert.realmCertInfo().deviceId() + ".pem.der");

        log.infof("BinaryDeviceCert Called.\nDevicePEM.DER: %s", deviceDer);

        if (!Files.exists(deviceDer)) {
            executeOpenSSL(
                    "openssl", "base64",
                    "-d",
                    "-in",  deviceCert.devicePem().toString(),
                    "-out", deviceDer.toString());
        }

        log.infof("Binary DeviceCert rendered.\nPEM.DIR:%s", deviceDer);
        return new DeviceCert(deviceCert.realmCertInfo(), deviceDer);
    }
}

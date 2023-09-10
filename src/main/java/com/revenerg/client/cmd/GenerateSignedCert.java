package com.revenerg.client.cmd;

import com.revenerg.client.cmd.model.CertInfo;
import com.revenerg.client.cmd.model.DeviceCert;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.jbosslog.JBossLog;

import java.nio.file.Files;
import java.nio.file.Path;

@JBossLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GenerateSignedCert extends OpenSSLCommand<CertInfo, DeviceCert> {
    public static final GenerateSignedCert INSTANCE = new GenerateSignedCert();
    @Override
    public DeviceCert apply(CertInfo certInfo) {
        if (!certInfo.validate()) {
            log.errorf("CertInfo failed to validate.");
            throw new IllegalStateException("CertInfoValidation");
        }

        Path devicePem = tempFile(certInfo.deviceId(), certInfo.deviceId() + ".pem");

        log.infof("GenerateSignedCert Called.\nDevicePEM: %s", devicePem);

        if (!Files.exists(devicePem)) {
            executeOpenSSL(
                    "openssl", "x509",
                    "-req",
                    "-in", certInfo.deviceKeyAndCSR().deviceCSR().toString(),
                    "-CA", certInfo.ca().toString(),
                    "-CAkey", certInfo.caKey().toString(),
                    "-CAcreateserial",
                    "-out", devicePem.toString(),
                    "-days", String.valueOf(certInfo.expiry().toDays()),
                    "-sha256");
        }

        log.infof("DeviceCert rendered.\nPEM:%s", devicePem);
        return new DeviceCert(certInfo, devicePem);
    }
}

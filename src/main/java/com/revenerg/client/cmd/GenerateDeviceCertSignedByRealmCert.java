package com.revenerg.client.cmd;

import com.revenerg.client.cmd.model.RealmCertInfo;
import com.revenerg.client.cmd.model.DeviceCert;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.jbosslog.JBossLog;

import java.nio.file.Files;
import java.nio.file.Path;

@JBossLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GenerateDeviceCertSignedByRealmCert extends OpenSSLCommand<RealmCertInfo, DeviceCert> {
    public static final GenerateDeviceCertSignedByRealmCert INSTANCE = new GenerateDeviceCertSignedByRealmCert();
    @Override
    public DeviceCert apply(RealmCertInfo realmCertInfo) {
        if (!realmCertInfo.validate()) {
            log.errorf("CertInfo failed to validate.");
            throw new IllegalStateException("CertInfoValidation");
        }

        Path devicePem = tempFile(realmCertInfo.deviceId(), realmCertInfo.deviceId() + ".pem");

        log.infof("GenerateDeviceCert Called.\nDevicePEM: %s", devicePem);

        if (!Files.exists(devicePem)) {
            executeOpenSSL(
                    "openssl", "x509",
                    "-req",
                    "-in", realmCertInfo.deviceKeyAndCSR().deviceCSR().toString(),
                    "-CA", realmCertInfo.ca().toString(),
                    "-CAkey", realmCertInfo.caKey().toString(),
                    "-CAcreateserial",
                    "-out", devicePem.toString(),
                    "-days", String.valueOf(realmCertInfo.expiry().toDays()),
                    "-sha256");
        }

        log.infof("DeviceCert rendered.\nPEM:%s", devicePem);
        return new DeviceCert(realmCertInfo, devicePem);
    }
}

package com.revenerg.client.cmd;

import com.revenerg.client.cmd.model.CSRInfo;
import com.revenerg.client.cmd.model.DeviceKeyAndCSR;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.jbosslog.JBossLog;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generate a Device CSR + Keyfile with OpenSSL.
 */
@JBossLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GenerateKeyAndCSR extends OpenSSLCommand<CSRInfo, DeviceKeyAndCSR> {
    public static final GenerateKeyAndCSR INSTANCE = new GenerateKeyAndCSR();
    @Override
    public DeviceKeyAndCSR apply(CSRInfo deviceInfo) {
        String subject = deviceInfo.subjectString();

        Path deviceKey = tempFile(deviceInfo.deviceId(), deviceInfo.deviceId() + ".key");
        Path deviceCSR = tempFile(deviceInfo.deviceId(), deviceInfo.deviceId() + ".csr");

        log.infof("GenerateCSR Called.\nSubject: %s\nDeviceKey: %s\nDeviceCSR: %s", subject, deviceKey, deviceCSR);

        if (!(Files.exists(deviceKey) && Files.exists(deviceCSR))) {
            executeOpenSSL(
                    "openssl", "req",
                    "-nodes",
                    "--newkey", "rsa:4096",
                    "-keyout", deviceKey.toString(),
                    "-subj", subject,
                    "-out", deviceCSR.toString());
        }

        log.infof("");
        return new DeviceKeyAndCSR(deviceKey, deviceCSR);
    }
}

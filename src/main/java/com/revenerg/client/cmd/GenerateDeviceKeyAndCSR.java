package com.revenerg.client.cmd;

import com.revenerg.client.cmd.model.DeviceInfo;
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
public class GenerateDeviceKeyAndCSR extends OpenSSLCommand<DeviceInfo, DeviceKeyAndCSR> {
    public static final GenerateDeviceKeyAndCSR INSTANCE = new GenerateDeviceKeyAndCSR();
    @Override
    public DeviceKeyAndCSR apply(DeviceInfo deviceInfo) {
        String subject = deviceInfo.subjectString();

        Path deviceKey = tempFile(deviceInfo.deviceId(), deviceInfo.deviceId() + ".key");
        Path deviceCSR = tempFile(deviceInfo.deviceId(), deviceInfo.deviceId() + ".csr");

        log.infof("GenerateCSR Called.\nSubject: %s\nDeviceKey: %s\nDeviceCSR: %s", subject, deviceKey, deviceCSR);

        if (!(Files.exists(deviceKey) && Files.exists(deviceCSR))) {
            executeOpenSSL(
                    "openssl", "req",
                    "-nodes",
                    "--newkey", "rsa:1024",
                    "-keyout", deviceKey.toString(),
                    "-subj", subject,
                    "-out", deviceCSR.toString());
        }

        log.infof("");
        return new DeviceKeyAndCSR(deviceKey, deviceCSR);
    }
}

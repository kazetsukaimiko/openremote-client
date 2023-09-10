package com.revenerg.client.cmd.model;

import com.revenerg.client.cmd.OpenSSLCommand;
import lombok.extern.jbosslog.JBossLog;

import java.nio.file.Path;
import java.time.Duration;

@JBossLog
public record CertInfo(String deviceId, Path ca, Path caKey, DeviceKeyAndCSR deviceKeyAndCSR, Duration expiry) {
    public boolean validate() {
        boolean validated = true;
        if (deviceId == null) {
            log.errorf("deviceId must be present.");
            validated = false;
        }
        validated = validated && OpenSSLCommand.accessibilityCheck("ca", ca);
        validated = validated && OpenSSLCommand.accessibilityCheck("caKey", caKey);
        if (expiry == null) {
            log.errorf("Expiry must be set.");
            validated = false;
        }
        return validated;
    }

}

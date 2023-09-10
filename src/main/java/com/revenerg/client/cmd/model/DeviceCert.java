package com.revenerg.client.cmd.model;

import java.nio.file.Path;

public record DeviceCert(CertInfo certInfo, Path devicePem) {
}

package com.revenerg.client.cmd.model;

import java.nio.file.Path;

public record DeviceCert(RealmCertInfo realmCertInfo, Path devicePem) {
}

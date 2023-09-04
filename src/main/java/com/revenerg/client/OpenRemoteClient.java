package com.revenerg.client;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.jbosslog.JBossLog;

import java.nio.file.Files;
import java.nio.file.Path;


@Getter
@JBossLog
public class OpenRemoteClient {

    public static final String PROVISIONING_REQUEST_TOPIC = "provisioning/%s/request";
    public static final String PROVISIONING_RESPONSE_TOPIC = "provisioning/%s/response";

    private final Path ca;
    private final String address;

    public OpenRemoteClient(@NonNull Path ca, @NonNull String address) {
        if (!Files.exists(ca)) {
            throw new IllegalStateException("CA File '%s' doesn't exist".formatted(ca));
        }
        if (!Files.isReadable(ca)) {
            throw new IllegalStateException("CA File '%s' cannot be read".formatted(ca));
        }
        this.ca = ca;
        this.address = address;
    }

    public OpenRemoteClient connect() {
        return this;
    }

}

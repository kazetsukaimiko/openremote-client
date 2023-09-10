package com.revenerg.client.request;

public record ProvisionRequest(String type, String cert) {
    public ProvisionRequest(String cert) {
        this("x509", cert);
    }


}

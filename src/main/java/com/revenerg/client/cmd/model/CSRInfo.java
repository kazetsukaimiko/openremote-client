package com.revenerg.client.cmd.model;

public record CSRInfo(String deviceId, String countryCode, String cityName) {
    private static final String COUNTRY_TEMPLATE = "/C=%s";
    private static final String CITY_TEMPLATE = "/ST=%s";
    private static final String SUBJECT_TEMPLATE = "/O=OpenRemote/CN=%s";

    public CSRInfo(String deviceId) {
        this(deviceId, null, null);
    }

    /**
     * Returns a subject string for the device CSR.
     * Example:
     * /C=NL/ST=North Brabant/O=OpenRemote/CN=deviceN
     */
    public String subjectString() {
        return
                (countryCode != null ? COUNTRY_TEMPLATE.formatted(countryCode) : "") +
                (cityName != null ? CITY_TEMPLATE.formatted(cityName) : "") +
                SUBJECT_TEMPLATE.formatted(deviceId);
    }
}

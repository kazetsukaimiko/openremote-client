package com.revenerg.util.ssl;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public interface SSLSocketFactoryProvider {
    SSLSocketFactory getFactory() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException;
}

package com.revenerg.util.ssl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;



/**
 * This SSLSocketFactory ignores CA Verification. Should avoid this in a production environment.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AllTrustingSocketFactory implements SSLSocketFactoryProvider {
    public static final AllTrustingSocketFactory INSTANCE = new AllTrustingSocketFactory();

    @Override
    public SSLSocketFactory getFactory() throws NoSuchAlgorithmException, KeyManagementException {
        X509TrustManager mgr = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };

        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(null, new TrustManager[]{mgr}, new SecureRandom());

        return context.getSocketFactory();
    }


}

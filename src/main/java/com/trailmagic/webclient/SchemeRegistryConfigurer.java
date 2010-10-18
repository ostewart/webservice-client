package com.trailmagic.webclient;

import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * Created by: oliver on Date: Dec 18, 2009 Time: 1:37:05 AM
 */
@SuppressWarnings({"UnusedDeclaration"})
@Service
public class SchemeRegistryConfigurer {
    @Autowired
    public SchemeRegistryConfigurer(SchemeRegistry schemeRegistry) {
        try {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            final FileInputStream inputStream = new FileInputStream("/tmp/server.truststore");
            try {
                trustStore.load(inputStream, "foo".toCharArray());
            } finally {
                inputStream.close();
            }
            final SSLSocketFactory sslSocketFactory = new SSLSocketFactory(trustStore);
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

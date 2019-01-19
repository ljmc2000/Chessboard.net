package net.ddns.gingerpi.chessboardnet;
//based on http://matematicainformatica.altervista.org/volley-android-https-self-signed-certificate/

import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SelfSignedFactory{

	public static SSLSocketFactory getSocketFactory(Context mContext, int certId){
		CertificateFactory cf = null;

		try {
			cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = mContext.getResources().openRawResource(certId);
            Certificate ca;
            try {
            	ca = cf.generateCertificate(caInput);
            	Log.e("CERT", "ca=" + ((X509Certificate) ca).getSubjectDN());
            }

            finally {
            	caInput.close();
            }


            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);


            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);


            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            	@Override
				public boolean verify(String hostname, SSLSession session) {
                   	Log.d("#CipherUsed", session.getCipherSuite());
                   	return hostname.compareTo("192.168.0.1")==0; //The Hostname of your server
                   	//return hostname.compareTo("gingerpi.ddns.net")==0; //The Hostname of your server
            	}
            };

            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
            SSLContext context = null;
            context = SSLContext.getInstance("TLS");

            context.init(null, tmf.getTrustManagers(), null);
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

            SSLSocketFactory sf = context.getSocketFactory();


            return sf;
		}

		catch (Exception e) {
			Log.e("#certificate",e.toString());
		}

		return  null;
	}
}

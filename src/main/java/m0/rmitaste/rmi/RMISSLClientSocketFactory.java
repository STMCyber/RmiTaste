package m0.rmitaste.rmi;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.security.cert.X509Certificate;

/**
 * Implements RMIClientSocketFactory interface
 * @see java.rmi.server.RMIClientSocketFactory
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */
public class RMISSLClientSocketFactory implements RMIClientSocketFactory {

    /**
     * Create a client socket connected to the specified host and port. This method creates SSL connection and bypass certificate verification
     *
     * @param host the host name
     * @param port the port number
     * @return a socket connected to the specified host and port.
     * @throws IOException  if an I/O error occurs during socket creation
     */
    public Socket createSocket(String host, int port) throws IOException {
        // Create custom TrustManager and trust all certificates
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        try {
            // Hack certificate verification
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLContext.setDefault(sc);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            // Create custom socket factory
            SSLContext ctx = SSLContext.getInstance("SSL");
            ctx.init(null, trustAllCerts, null);
            SSLSocketFactory ssf = ctx.getSocketFactory();
            SSLSocket socket = (SSLSocket) ssf.createSocket(host, port);
            return socket;
        }catch(Exception ex){
            return null;
        }
    }

    public int hashCode() {
        return getClass().hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }
}

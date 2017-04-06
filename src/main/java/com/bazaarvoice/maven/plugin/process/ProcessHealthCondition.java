package com.bazaarvoice.maven.plugin.process;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Properties;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.net.util.SSLContextUtils;
import org.apache.commons.net.util.TrustManagerUtils;

public class ProcessHealthCondition {
    private static final int SECONDS_BETWEEN_CHECKS = 1;

    private ProcessHealthCondition() {}

    public static void waitSecondsUntilHealthy(HealthcheckUrl healthCheckUrl, int timeoutInSeconds, boolean validateSsl) {
        if (healthCheckUrl == null) {
            // Wait for timeout seconds to let the process come up
            sleep(timeoutInSeconds);
            return;
        }
        final long start = System.currentTimeMillis();
        while ((System.currentTimeMillis() - start) / 1000 < timeoutInSeconds) {
            internalSleep();
            if (is200(healthCheckUrl, validateSsl)) {
                return; // success!!!
            }
            internalSleep();
        }
        throw new RuntimeException("Process was not healthy even after " + timeoutInSeconds + " seconds");
    }

    private static SSLSocketFactory getFactory(boolean validateSsl) {
        try {
            if (validateSsl) {
                return SSLContext.getDefault().getSocketFactory();
            } else {
                return SSLContextUtils.createSSLContext("TLS", null, TrustManagerUtils.getAcceptAllTrustManager())
                    .getSocketFactory();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain SSLSocketFactory", e);
        }
    }

    private static boolean is200(HealthcheckUrl url, boolean validateSsl) {
        try {
            final int code = getResponseCode(url, validateSsl);
            return 200 <= code && code < 300;
        } catch (Exception e) {
            return false;
        }
    }

    private static int getResponseCode(HealthcheckUrl healthcheckUrl, boolean validateSsl) {
        InputStream in = null;
        try {
            URL url = healthcheckUrl.getUrl();
            final HttpURLConnection http = (HttpURLConnection) url.openConnection();

            if (http instanceof HttpsURLConnection) {
                ((HttpsURLConnection) http).setSSLSocketFactory(getFactory(validateSsl));
            }

            http.setRequestMethod("GET");
            setHeaders(healthcheckUrl.getHeaders(), http);
            http.connect();
            in = http.getInputStream();
            return http.getResponseCode();
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeQuietly(in);
        }
    }

    private static void setHeaders(Properties headers, HttpURLConnection http) {
        if(headers != null) {
            for (Object key : headers.keySet()) {
                http.setRequestProperty((String) key, (String) headers.getProperty((String) key));
            }
        }
    }

    private static void internalSleep() {
        sleep(SECONDS_BETWEEN_CHECKS * 1000);
    }

    private static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private static void closeQuietly(Closeable out) {
        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception e) {/**/}
    }

}

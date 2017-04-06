package org.honton.chas.process.exec.maven.plugin;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.net.util.SSLContextUtils;
import org.apache.commons.net.util.TrustManagerUtils;
import org.apache.maven.plugin.logging.Log;

public class ProcessHealthCondition {
  private static final int SECONDS_BETWEEN_CHECKS = 1;

  private final Log log;
  private final HealthCheckUrl healthCheckUrl;
  private final int timeoutInSeconds;
  private final boolean validateSsl;

  public ProcessHealthCondition(Log log, HealthCheckUrl healthCheckUrl, int timeoutInSeconds, boolean validateSsl) {
    this.log = log;
    this.healthCheckUrl = healthCheckUrl;
    this.timeoutInSeconds = timeoutInSeconds;
    this.validateSsl = validateSsl;
  }

  public void waitSecondsUntilHealthy() {
    if (healthCheckUrl.getUrl() == null) {
      // Wait for timeout seconds to let the process come up
      sleep(timeoutInSeconds);
      return;
    }
    final long start = System.currentTimeMillis();
    while ((System.currentTimeMillis() - start) / 1000 < timeoutInSeconds) {
      if (is200()) {
        return; // success!!!
      }
      sleep(SECONDS_BETWEEN_CHECKS);
    }
    throw new RuntimeException(
        "Process was not healthy even after " + timeoutInSeconds + " seconds");
  }

  private SSLSocketFactory getFactory() {
    try {
      if (validateSsl) {
        return SSLContext.getDefault().getSocketFactory();
      } else {
        return SSLContextUtils
            .createSSLContext("TLS", null, TrustManagerUtils.getAcceptAllTrustManager())
            .getSocketFactory();
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to obtain SSLSocketFactory", e);
    }
  }

  private boolean is200() {
    try {
      final int code = getResponseCode();
      return 200 <= code && code < 300;
    } catch (Exception e) {
      return false;
    }
  }

  private int getResponseCode() {
    InputStream in = null;
    try {
      URL url = healthCheckUrl.getUrl();
      log.debug("GET " + url);

      final HttpURLConnection http = (HttpURLConnection) url.openConnection();

      if (http instanceof HttpsURLConnection) {
        ((HttpsURLConnection) http).setSSLSocketFactory(getFactory());
      }

      http.setRequestMethod("GET");
      setHeaders(http);
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

  private void setHeaders(HttpURLConnection http) {
    if (healthCheckUrl.getHeaders() != null) {
      for (Map.Entry entry : healthCheckUrl.getHeaders().entrySet()) {
        log.debug(entry.getKey() + " = " + entry.getValue());
        http.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
      }
    }
  }

  private void sleep(int seconds) {
    try {
      log.debug("waiting for " + seconds + " seconds");
      Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
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

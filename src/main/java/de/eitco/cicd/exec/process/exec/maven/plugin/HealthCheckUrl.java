package de.eitco.cicd.exec.process.exec.maven.plugin;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class HealthCheckUrl {
    private URL url;
    private Properties headers;

    public URL getUrl() {
        return url;
    }

    public Properties getHeaders() {
        return headers;
    }

    public void setUrl(String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    @Override
    public String toString() {
        return "HealthCheckUrl{" + "url=" + url + ", headers=" + headers + '}';
    }
}

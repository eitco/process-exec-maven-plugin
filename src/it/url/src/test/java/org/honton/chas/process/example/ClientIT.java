package org.honton.chas.process.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import org.honton.chas.url.extension.urlhandler.UrlStreamHandlerRegistry;
import org.junit.Assert;
import org.junit.Test;

public class ClientIT {

  @Test
  public void testTcpClient() throws IOException {
    UrlStreamHandlerRegistry.register();

    URLConnection connection = new URL("tcp://127.0.0.1:" + System.getProperty("port")).openConnection();
    Writer writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
    writer.append("line\n").flush();

    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
    Assert.assertEquals("line", reader.readLine());
  }
}


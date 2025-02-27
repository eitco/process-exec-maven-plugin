package de.eitco.cicd.exec.process.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Example server
 */
public class Server {

  private final ServerSocket serverSocket;

  Server(int port) throws IOException {
    serverSocket = new ServerSocket(port);
  }

  private void serve() throws IOException {
    while (true) {
      acceptAndRespond();
    }
  }

  private void acceptAndRespond() throws IOException {
    try(Socket connectionSocket = serverSocket.accept()) {

      String body = "<html><head><title>Test</title></head><body><h1>Test</h1></body></html>";

      int length = body.length();
      InputStream is = connectionSocket.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
      reader.readLine();
      Writer writer = new OutputStreamWriter(connectionSocket.getOutputStream(), StandardCharsets.UTF_8);

      writer.write("HTTP/1.0 200 OK\r\n");
      writer.write("Date: " + LocalDateTime.now() + "\r\n");
      writer.write("Server: Custom Server\r\n");
      writer.write("Content-Type: text/html\r\n");
      writer.write("Content-Length: " + length + "\r\n");
      writer.write("\r\n");
      writer.write(body);
      writer.flush();
      writer.close();
    }
  }

  public static void main(String argv[]) throws Exception {
    new Server(Integer.parseInt(argv[0])).serve();
  }
}

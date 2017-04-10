package org.honton.chas.process.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
      InputStream is = connectionSocket.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
      Writer writer = new OutputStreamWriter(connectionSocket.getOutputStream(), StandardCharsets.UTF_8);
      writer.append(reader.readLine()).close();
    }
  }

  public static void main(String argv[]) throws Exception {
    new Server(Integer.parseInt(argv[0])).serve();
  }
}

package org.honton.chas.process.exec.maven.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.maven.plugin.logging.Log;

public class StdoutRedirector extends Thread {

  private final BufferedReader in;
  private final Log log;
  private final boolean isErr;

  StdoutRedirector(InputStream in, Log log, boolean isErr) {
    this.in = new BufferedReader(new InputStreamReader(in));
    this.log = log;
    this.isErr = isErr;
    setDaemon(true);
    start();
  }

  @Override
  public void run() {
    try {
      while (oneLine()) {
      }
    } catch (IOException ignore) {
    } finally {
      try {
        in.close();
      }
      catch (IOException ignore) {
      }
    }
  }

  private boolean oneLine() throws IOException {
    String line = in.readLine();
    if(line == null) {
      return false;
    }
    if(isErr) {
      log.error(line);
    }
    else {
      log.info(line);
    }
    return true;
  }
}

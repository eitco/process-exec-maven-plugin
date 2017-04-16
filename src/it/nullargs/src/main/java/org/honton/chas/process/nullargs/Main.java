package org.honton.chas.process.nullargs;

import org.junit.Assert;

/**
 * Expect only non-null arguments
 */
public class Main {
  public static void main(String argv[]) throws Exception {
    Assert.assertEquals(1, argv.length);
    Assert.assertEquals("one", argv[0]);
    System.exit(0);
  }
}

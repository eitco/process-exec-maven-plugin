package de.eitco.cicd.exec.process.order;

import java.util.concurrent.TimeUnit;

/**
 * Generate output and wait for shutdown
 */
public class Main {
    public static void main(String argv[]) {
        final String arg = argv[0];

        System.out.println(arg + " started");
        System.err.println(arg + " again");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println(arg + " shutdown hook");
            }
        });

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(300));
        } catch (InterruptedException ignore) {
        }
        System.exit(0);
    }

}

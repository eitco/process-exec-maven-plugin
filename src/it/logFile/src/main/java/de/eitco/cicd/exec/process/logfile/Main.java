package de.eitco.cicd.exec.process.logfile;

import java.util.concurrent.TimeUnit;

/**
 * Generate some characters on out and err.
 */
public class Main {
    public static void main(String argv[]) {
        System.out.println("started");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("shutdown hook");
            }
        });

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(30));
        } catch (InterruptedException ignore) {
        }

        System.exit(0);
    }
}

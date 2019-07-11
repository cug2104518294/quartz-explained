package org.quartz.helpers;

import org.quartz.core.QuartzScheduler;

/**
 * <p>
 * Prints the version of Quartz on stdout.
 * </p>
 */
public class VersionPrinter {

    /**
     * Private constructor because this is a pure utility class.
     */
    private VersionPrinter() {
    }


    public static void main(String[] args) {
        System.out.println("Quartz version: " + QuartzScheduler.getVersionMajor()
                + "." + QuartzScheduler.getVersionMinor() + "."
                + QuartzScheduler.getVersionIteration());
    }
}

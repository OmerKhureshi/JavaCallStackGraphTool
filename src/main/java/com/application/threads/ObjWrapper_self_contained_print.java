package com.application.threads;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.time.Instant;

public class ObjWrapper_self_contained_print {
    // Process id remains same for all threads. Therefore a single static final String constant variable will suffice.
    static final String PROCESS_ID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

    // Separator used in the call trace log.
    static final String SEPARATOR = "|";

    // Unique tag to differentiate Object Wrapper specific statement
    static final String TAG = "ObjWrapper";

    public static void printTraceInfo(Object obj, String eventType, String methodId) {
        String callTraceEntry = PROCESS_ID                // Process ID
                + SEPARATOR + Thread.currentThread().getId() + "(" + Thread.currentThread().getName() + ")"    // Thread Id + Thread Name
                + SEPARATOR + methodId                          // Method Id reserve for methods of this class
                + SEPARATOR + eventType                         // Enter or Exit
                + SEPARATOR + "[]"                              // Arguments for methods. Blank for all methods.
                + SEPARATOR + Instant.now()                     // Time stamp
                + SEPARATOR + System.identityHashCode(obj);     // Unique id for each object

        // ToDo Determine best way to print to console or log path.
        System.out.println(TAG + " : " + callTraceEntry);

        LogWriterUtil.write(callTraceEntry);
    }

    public static void wait(Object obj)throws InterruptedException {
        printTraceInfo(obj, "Enter", "-1 (wait)");
        obj.wait();
        printTraceInfo(obj, "Exit", "-1 (wait)");
    }

    public static void notify(Object obj) {
        printTraceInfo(obj, "Enter", "-2 (notify)");
        obj.notify();
        printTraceInfo(obj, "Exit", "-2 (notify)");
    }

    public static void notifyAll(Object obj) {
        printTraceInfo(obj, "Enter", "-3 (notifyAll)");
        obj.notifyAll();
        printTraceInfo(obj, "Exit", "-3 (notifyAll)");
    }

    public static void setLogFileName(File fileName) {
        LogWriterUtil.initialize(fileName);
    }
}

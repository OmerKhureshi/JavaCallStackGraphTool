package com.ObjectWrapper;

import com.itool.Constants;
import com.itool.Logger;
import com.itool.LoggerFactory;
import com.itool.exception.LoggerException;

import java.lang.reflect.Method;

public class ObjWrapper     {

    public static void wait(Object obj) throws InterruptedException {
        Logger logger = LoggerFactory.getLogger();
        class Local {}
        Method m = Local.class.getEnclosingMethod();
        try {
            logger.logCustom(m, "WAIT-ENTER" + Constants.separator + System.identityHashCode(obj));
            obj.wait();
            logger.logCustom(m, "WAIT-EXIT" + Constants.separator + System.identityHashCode(obj));
        } catch (LoggerException e) {}
    }

    public static void notify(Object obj) {
        Logger logger = LoggerFactory.getLogger();
        class Local {}
        Method m = Local.class.getEnclosingMethod();
        try {
            logger.logCustom(m, "NOTIFY-ENTER" + Constants.separator + System.identityHashCode(obj));
            obj.notify();
            logger.logCustom(m, "NOTIFY-EXIT" + Constants.separator + System.identityHashCode(obj));
            } catch (LoggerException e) {}
    }

    public static void notifyAll(Object obj){
        Logger logger = LoggerFactory.getLogger();
        class Local {}
        Method m = Local.class.getEnclosingMethod();
        try{
            logger.logCustom(m, "NOTIFYALL-ENTER" + Constants.separator + System.identityHashCode(obj));
            obj.notifyAll();
            logger.logCustom(m, "NOTIFYALL-EXIT" + Constants.separator + System.identityHashCode(obj));
        } catch (LoggerException e) {}
    }
}

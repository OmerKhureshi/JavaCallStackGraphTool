package com.application.threads;

import java.time.Instant;

public class ObjWrapperOri {

    Object obj = null;

    ObjWrapperOri(Object obj) {
        System.out.println("New Object wrapper created: Thread: " + Thread.currentThread() + " at time: " + Instant.now());
        this.obj = obj;
    }

    public void waitNew()throws InterruptedException {
        System.out.println("Entering Object::wait: Thread: " + Thread.currentThread() + " at time: " + Instant.now());
        obj.wait();
        System.out.println("Exiting Object::wait: Thread: " + Thread.currentThread() + " at time: " + Instant.now());
    }

    public void notifyNew() {
        System.out.println("Entering Object::notify: Thread: " + Thread.currentThread() + " at time: " + Instant.now());
        obj.notify();
        System.out.println("Exiting Object::notify: Thread: " + Thread.currentThread() + " at time: " + Instant.now());
    }

    public void notifyAllNew() {
        System.out.println("Entering Object::notifyAll: Thread: " + Thread.currentThread() + " at time: " + Instant.now());
        obj.notifyAll();
        System.out.println("Exiting Object::notifyAll: Thread: " + Thread.currentThread() + " at time: " + Instant.now());
    }
}

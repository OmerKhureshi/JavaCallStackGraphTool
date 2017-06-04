package test.tests;

/**
 * Main thread create a new thread, b, and waits while thread b executes a task and notifies the main thread.
 */
class ThreadExampleOne {
    public static void main(String [] args) {
        ThreadB b = new ThreadB();
        b.setName("Thread B");
        b.start();
        synchronized(b) {
            try {
                System.out.println("Waiting for b to complete...");
                ObjWrapper.wait(b);  // Use instead of b.wait();
                b.total *= 2;
            } catch (InterruptedException e) {}
        }
        System.out.println("Total is: " + b.total);
    }
}

/**
 * ThreadB acquires the lock, completes the task and notifies the main thread.
 * Uses the ObjWrapper notify method to notify the main thread.
 */
class ThreadB extends Thread {
    int total  = 0;
    boolean isNotified = false;
    public void run() {
        synchronized(this) {
            try {
                sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(int i = 0; i < 10; i++) {
                total += 1;
            }
            ObjWrapper.notify(this);  // Use instead of b.notify();
        }
    }
}
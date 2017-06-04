package test;

import com.ObjectWrapper.ObjWrapper;

public class ThreadExampleTwo {
    public class Lock {
        int total=0;
    }

    Lock lock = new Lock();
    boolean notified = false;

    public static void main(String[] args) {
        new ThreadExampleTwo().startAll("Thread A", "Thread B", "Thread C");
    }

    public void startAll( String threadOne, String threadTwo, String threadThree) {
        Thread t1 = new Thread(r1);
        t1.setName(threadOne);
        t1.start();

        Thread t2 = new Thread(r1);
        t2.setName(threadTwo);
        t2.start();

        for (int i = 0; i < 1000000000; i++) {
            // Just wait
        }

        Thread t3 = new Thread(r2);
        t3.setName(threadThree);
        t3.start();
    }

    Runnable r1 = new Runnable() {
        @Override
        public void run() {
            synchronized (lock) {
                try {
                    while (!notified) {
                        ObjWrapper.wait(lock);
                    }
                    lock.total *=2;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    Runnable r2 = new Runnable() {
        @Override
        public void run() {
            synchronized(lock) {
                for (int i = 0; i < 1000; i++) {
                    lock.total++;
                }
                ObjWrapper.notifyAll(lock);
                notified = true;
            }
        }
    };
}


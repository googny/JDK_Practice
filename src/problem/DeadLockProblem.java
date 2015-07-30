package problem;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 制造死锁场景，用JPS和Jstack 俩检测Java死锁
 */
public class DeadLockProblem {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hello World!");
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        byte[] i = new byte[0];
        byte[] j = new byte[0];
        final CountDownLatch countDownLatch = new CountDownLatch(2);
        executorService.execute(new DeadThread1(i, j, countDownLatch));
        executorService.execute(new DeadThread2(i, j, countDownLatch));
        countDownLatch.await();
        System.out.println("done !!!");
    }

    public static class DeadThread1 implements Runnable {

        private byte[] i;
        private byte[] j;
        private CountDownLatch countDownLatch;

        public DeadThread1(byte[] i, byte[] j, CountDownLatch countDownLatch) {
            this.i = i;
            this.j = j;
            this.countDownLatch = countDownLatch;
        }


        @Override
        public void run() {
            synchronized (i) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (j) {
                    System.out.println(Thread.currentThread().getName() + "is running!!");
                    countDownLatch.countDown();
                }
            }
        }
    }

    public static class DeadThread2 implements Runnable {

        private byte[] i;
        private byte[] j;
        private CountDownLatch countDownLatch;

        public DeadThread2(byte[] i, byte[] j, CountDownLatch countDownLatch) {
            this.i = i;
            this.j = j;
            this.countDownLatch = countDownLatch;
        }


        @Override
        public void run() {
            synchronized (j) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (i) {
                    System.out.println(Thread.currentThread().getName() + "is running!!");
                    countDownLatch.countDown();
                }
            }
        }
    }
} 
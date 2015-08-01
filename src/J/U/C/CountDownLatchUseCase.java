package J.U.C;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author mti1301
 * @since 2015/8/1.
 */
public class CountDownLatchUseCase {
    public static void main(String[] args) {
        try {
            Driver.driver(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Driver {
    public static void driver(int workerNum) throws InterruptedException {
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch endSignal = new CountDownLatch(workerNum);
        ExecutorService threadPool = Executors.newFixedThreadPool(workerNum);
        for (int i = 0; i < workerNum; ++i) {
            threadPool.execute(new Worker(i, startSignal, endSignal));
        }
        threadPool.shutdown();

        doSomethingToStart();
        startSignal.countDown();
        endSignal.await();
        doSomethingToEnd();
    }

    public static void doSomethingToStart() {
        System.out.println("Driver start ,workers ready to work...");
    }

    public static void doSomethingToEnd() {
        System.out.println("Workers have done all work, Driver ready to end...");
    }
}

class Worker implements Runnable {
    private final int threadId;
    private final CountDownLatch startSignal;
    private final CountDownLatch endSignal;

    public Worker(int threadId, CountDownLatch startSignal, CountDownLatch endSignal) {
        this.threadId = threadId;
        this.startSignal = startSignal;
        this.endSignal = endSignal;
    }

    @Override
    public void run() {
        try {
            //等待Driver操作执行完
            startSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Thread "+threadId +" start to work...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Thread "+threadId +" end to work...");

        //Worker线程执行完毕，计数减一
        endSignal.countDown();
    }
}
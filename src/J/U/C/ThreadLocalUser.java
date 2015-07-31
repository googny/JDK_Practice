package J.U.C;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Googny
 * @since 2015/7/31.
 */
public class ThreadLocalUser {

    private static ExecutorService threadPool = Executors.newFixedThreadPool(2);

    public static void main(String[] args) {
//        testThreadLocalInThread();
        testMultiThreadAccessUniqueIdGenerator();
    }

    public static void testThreadLocalInThread() {
        int cursor = 0;
        threadPool.execute(new Task(0, cursor));
        threadPool.execute(new Task(1, cursor));
        threadPool.shutdown();
    }

    public static void testMultiThreadAccessUniqueIdGenerator() {
        threadPool.execute(new UniqueIdGeneratorAccessor(0));
        threadPool.execute(new UniqueIdGeneratorAccessor(1));
        threadPool.shutdown();
    }


    static class UniqueIdGeneratorAccessor implements Runnable {
        private final int id;

        public UniqueIdGeneratorAccessor(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            int i = 10;
            while (i-- > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread id is " + id + " and UniqueId is "
                        + UniqueThreadIdGenerator.getCurrentThreadIdByAtomicInteger());
            }
        }
    }


    static class Task implements Runnable {
        private final int id;
        private int cursor;
        private ThreadLocal<Integer> localVar = new ThreadLocal<Integer>() {
            @Override
            protected Integer initialValue() {
                return cursor;
            }

            @Override
            public Integer get() {
                return cursor++;
            }
        };

        public Task(int id, int cursor) {
            this.id = id;
            this.cursor = cursor;
        }

        @Override
        public void run() {
            int i = 10;
            while (i-- > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread id is " + id + " and UniqueId is " + localVar.get());
            }
        }
    }

    /**
     * this class is from JDK API @see ThreadLocal
     */
    static class UniqueThreadIdGenerator {
        private static final AtomicInteger uniqueId = new AtomicInteger(0);
        private static final ThreadLocal<Integer> uniqueNum = new ThreadLocal<Integer>() {
            @Override
            protected Integer initialValue() {
                return uniqueId.getAndIncrement();
            }
        };

        public static int getCurrentThreadIdByThreadLocal() {
            return uniqueNum.get();
        }

        public static int getCurrentThreadIdByAtomicInteger() {
            return uniqueId.getAndIncrement();
        }
    }
}


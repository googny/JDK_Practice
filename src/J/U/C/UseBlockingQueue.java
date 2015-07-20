package J.U.C;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 使用阻塞队列，实现遍历一个目录下所有文件，判断文件是否包含某字段。
 * 遍历任务（生产者线程）递归遍历目录，将文件放入阻塞队列。
 * 查找任务（消费者线程）从阻塞队列中拿文件
 *
 * @author mti1301
 * @since 2015/7/15.
 */
public class UseBlockingQueue {
    public static void main(String[] args) {
        BlockingQueue<File> queue = new ArrayBlockingQueue<>(100);
        File file = new File("E:\\tonghaoqi\\Workspaces\\LeetCode_j\\src");
        ExecutorService searchThreadPool = Executors.newFixedThreadPool(10);
        Producer producer = new Producer(file, queue);
        new Thread(producer).start();

        for (int i = 0; i < 10; i++) {
            searchThreadPool.execute(new Customer(queue, "class"));
        }
        searchThreadPool.shutdown();
    }
}

class Producer implements Runnable {
    private File file;
    private BlockingQueue<File> queue;
    public static File DUMMY = new File("");

    public Producer(File file, BlockingQueue<File> queue) {
        this.file = file;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            enumFile(file, queue);
            queue.put(DUMMY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void enumFile(File file, BlockingQueue queue) throws InterruptedException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File subFile : files) {
                if (subFile.isDirectory()) {
                    enumFile(subFile, queue);
                } else {
                    queue.put(subFile);
                }
            }
        } else {
            queue.put(file);
        }
    }
}

class Customer implements Runnable {
    private BlockingQueue queue;
    private String text;

    public Customer(BlockingQueue queue, String text) {
        this.queue = queue;
        this.text = text;
    }

    @Override
    public void run() {
        try {
            while (true) {
                File file = (File) queue.take();
                if (file.equals(Producer.DUMMY)) {
                    queue.put(file);
                    break;
                }
                searchText(file, text);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void searchText(File file, String text) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNo = 0;
            int index;
            do {
                line = reader.readLine();
                ++lineNo;
                if ((index = line.indexOf(text)) > 0) {
                    System.out.println(String.format("\"%s\" in File \"%s\" Line Number \"%d\" Index \"%d\""
                            , text, file.getName(), lineNo, index));
                    return;
                }
            } while (line != null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}




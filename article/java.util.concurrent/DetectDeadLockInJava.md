# Java 检测线程死锁 #
## 什么是死锁？ ##
死锁就是多个线程相互等待彼此占有的锁对象而处于阻塞状态。
## Java 中怎么检测线程的死锁呢？ ##
### jps和jstack 工具检测线程死锁 ###
jps 是JDK提供的查看系统中Java进程的工具，结果是进程号和进程名。  
jstack 是查看Thread Dump结果的，可以看到线程持有的锁，和线程请求的锁。  
### jvisualvm 可视化工具检测线程死锁 ###
jvisualvm.exe 是虚拟机可视化工具，可以用来检测线程死锁，连接到死锁的进程，然后Thread Dump ，可以看到线程持有的锁，和线程请求的锁。
### JConsole 可视化工具检测线程死锁 ###
JConsole 连接到死锁的进程，查看死锁的线程，即可看到死锁发生在哪段代码段。

## 隐藏的线程死锁 ##
隐藏的线程死锁指的是因为JVM缺少对读锁的跟踪，导致JVM不能检测到关于读锁的死锁情况。
    public class Task {
 
       // Object used for FLAT lock
       private final Object sharedObject = new Object();
       // ReentrantReadWriteLock used for WRITE & READ locks
       private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
 
       /**
        *  Execution pattern #1
        */
       public void executeTask1() {
 
             // 1. Attempt to acquire a ReentrantReadWriteLock READ lock
             lock.readLock().lock();
 
             // Wait 2 seconds to simulate some work...
             try { Thread.sleep(2000);}catch (Throwable any) {}
 
             try {              
                    // 2. Attempt to acquire a Flat lock...
                    synchronized (sharedObject) {}
             }
             // Remove the READ lock
             finally {
                    lock.readLock().unlock();
             }           
 
             System.out.println("executeTask1() :: Work Done!");
       }
 
       /**
        *  Execution pattern #2
        */
       public void executeTask2() {
 
             // 1. Attempt to acquire a Flat lock
             synchronized (sharedObject) {                 
 
                    // Wait 2 seconds to simulate some work...
                    try { Thread.sleep(2000);}catch (Throwable any) {}
 
                    // 2. Attempt to acquire a WRITE lock                   
                    lock.writeLock().lock();
 
                    try {
                           // Do nothing
                    }
 
                    // Remove the WRITE lock
                    finally {
                           lock.writeLock().unlock();
                    }
             }
 
             System.out.println("executeTask2() :: Work Done!");
       }
 
       public ReentrantReadWriteLock getReentrantReadWriteLock() {
             return lock;
       }
}
## 在平时编程中应该怎么避免？ ##
**加锁顺序**  
当多个线程需要相同的一些锁，但是按照不同的顺序加锁，死锁就很容易发生。  
如果能确保所有的线程都是按照相同的顺序获得锁，那么死锁就不会发生。  
**加锁时限**  
另外一个可以避免死锁的方法是在尝试获取锁的时候加一个超时时间，这也就意味着在尝试获取锁的过程中若超过了这个时限该线程则放弃对该锁请求。若一个线程没有在给定的时限内成功获得所有需要的锁，则会进行回退并释放所有已经获得的锁，然后等待一段随机的时间再重试。这段随机的等待时间让其它线程有机会尝试获取相同的这些锁，并且让该应用在没有获得锁的时候可以继续运行  
**死锁检测**  
死锁检测是一个更好的死锁预防机制，它主要是针对那些不可能实现按序加锁并且锁超时也不可行的场景。

每当一个线程获得了锁，会在线程和锁相关的数据结构中（map、graph等等）将其记下。除此之外，每当有线程请求锁，也需要记录在这个数据结构中。

当一个线程请求锁失败时，这个线程可以遍历锁的关系图看看是否有死锁发生。例如，线程A请求锁7，但是锁7这个时候被线程B持有，这时线程A就可以检查一下线程B是否已经请求了线程A当前所持有的锁。如果线程B确实有这样的请求，那么就是发生了死锁（线程A拥有锁1，请求锁7；线程B拥有锁7，请求锁1）。  
**那么当检测出死锁时，这些线程该做些什么呢？**  

一个可行的做法是释放所有锁，回退，并且等待一段随机的时间后重试。这个和简单的加锁超时类似，不一样的是只有死锁已经发生了才回退，而不会是因为加锁的请求超时了。虽然有回退和等待，但是如果有大量的线程竞争同一批锁，它们还是会重复地死锁。  


ps：以上知识来源网络，感谢！
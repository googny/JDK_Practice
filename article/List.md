# List 接口以及实现类和相关类源码分析 #
----------
## List接口分析 ##
###接口描述###
用户可以对列表进行随机的读取(get)，插入(add)，删除(remove)，修改(set)，也可批量增加(addAll)，删除(removeAll,retainAll),获取(subList)。  
还有一些判定操作：包含(contains[All]),相等(equals),索引(indexOf,lastIndexOf),大小(size)。  
还有获取元素类型数组的操作：toArray()
###注意事项###
**两种迭代器`Iterator`和`ListIterator`**:  
`Iterator` 正向遍历列表元素的迭代器  
`ListIterator` 支持正向和反向列表元素的迭代器，也支持插入和删除元素，也能从列表中指定位置开始的列表迭代器  
**插入和删除元素**：  
`List`接口的不同实现，在插入和删除上开销不一样，在使用具体实现的时候注意区分。  
**元素限制**：  
某些实现可能包含的元素有限制，某些实现禁止`null`元素，某些查询不合格的元素是否存在可能会抛出异常，而某些可能返回`false`
## ArrayList源码分析 ##
###类描述###
随机访问列表，可clone,可序列化，允许元素为`null`在内的所有元素，非同步类（非线程安全）。  
访问元素是O(1)时间，添加元素是O(n)时间。 size <= capacity。
###注意事项###
- **此实现是非同步的**  
如果多线程同时访问一个`ArrayList`实例，而其中至少一个线程从**结构上修改** 了列表，那么它**必须**保持同步。一般是使用自然封装该列表的对象进行同步操作来完成。或者使用`Collections.synchronizedList（）`方法来获取该实现的同步视图，最好在创建时完成，以防意外对列表进行不同步的访问。  
- **迭代器**  
该实现返回的`iterator`和`listIterator`是 **快速失败**的：再创建迭代器之后，除非通过迭代器自身的`remove`和`add`方法从结构上对列表进行修改，否则在任何时间以任何形式对列表进行修改，迭代器都会抛出`ConcurrentModificationException`。因此面对并发的修改，迭代器很快就会完全失败。
> **结构上的修改**：指的是添加或删除一个或多个元素；显式调整底层数组的大小；仅仅设置元素的值不是结构上的修改。  
> **快速失败**无法得到保证，一般来说，该行为仅用于检测bug。快速失败迭代器会 **尽最大可能**抛出`ConcurrentModificationException`，因此，为提高此类迭代器的正确性而编写一个依赖于此异常的程序是错误的做法。  

- **从源码看迭代器快速失败行为**  
   `ArrayList`类中有`modCount`属性来记录对象修改的次数，迭代器内部类`Itr`中有`expectedModCount`属性，该属性初始化为外部类的`modCount`，客户端调用`ArrayList`对象的`iterator()`方法时，`ArrayList`新建一个`Itr`实例返回。  
   每调用一次外部类中的所有**修改内部数组结构**的方法，`modCount`属性加`1`，这样在多线程环境下，一个线程修改了内部数组的结构，另一个线程使用迭代器遍历数组，将会产生`expectedModCount`和`modCount`不一致的情况，因此会抛出`ConcurrentModificationException`异常。  
   而通过迭代器 **修改内部数组结构**，则不会抛出异常，为什么呢？迭代器也是通过调用外部类的方法来移除数组中的某个元素，在移除元素后，迭代器在方法中将`expectedModCount`更新为`modCount`。  
- **一些源码中学到的其他东西**  
	1. 每次扩充容量都是扩充原来容量的1.5倍。(**源码对边界越界情况检查的非常严格，值得学习**)  
	2. 删除元素（批量删除，个体删除，全部删除），删除完之后将引用设置为`null`，方便GC回收垃圾。  
	3. 序列化和反序列化，先将`size`序列化，再逐个序列化元素。反序列化也一样，先反序列化`size`，再逐个反序列化元素。  

## LinkedList源码分析 ##
###类描述###
接口`List`和`Deque`的实现类，可clone，可序列化，允许`null`元素，除了`List`接口之外，该实现类还在列表的开头和结尾获取，删除，插入元素提供了统一的命名方法，允许这些操作将列表作为**堆栈**、**队列** 和 **双端队列**使用。  
在结尾插入元素的操作：  
`add(e)`、`addAll(Collection)`、`addLast(E)`、`boolean offer(E)`、`boolean offerLast(E)`。  
其他操作参见JDK文档。**一定不要想当然的认为push或pop等操作就是在结尾操作元素的，在使用过程中一定要仔细查阅API**  
该实现类也**不是同步的**，多线程修改也会造成对象状态不一致的情景，同样可以使用`Collections.synchronizedList()`方法得到**同步视图**。
迭代器也是**快速失败**的，对结构上的修改，除非通过迭代器自身的修改，其他任何时间任何方式的修改，迭代器都将抛出`ConcurrentModificationException`异常。
###注意事项###
和`ArrayList`的**注意事项**类似，可以参考ArrayList的注意事项。  
**为什么LinkedList类实现了Deque接口，而ArrayList类却没有呢？**  
LinkedList类内部实现是链表形式，对链表的插入、删除时间复杂度都是O(1)，并且，队列、双端队列和堆栈的操作大部分都是在链表的头部和结尾，插入删除非常方便。  而`ArrayList`类的内部实现是数组，数组是随机访问速度比较快，但是在头部的插入和删除需要挪动整个数组(**或者得时刻记录这头部下标，这样造成的复杂程度和联动反应“比较大”**)，代价略大。

## List同步类（同步视图） ##
在上面两个实现类中我们可以看到，它们都是**非同步的**，所以在多线程环境下是不能使用的（只读必须可以啊），特别是多线程中有修改列表结构的线程，那么出错的概率将会很大。
于是`Collections`类提供了`synchronizedList()`静态方法来讲非同步的List实现类包装成同步类（我更习惯称之为**同步视图**）。  
说到这里不得不提一个类`Collections`。
###`Collections`类描述###
该类完全由在`Collection`上进行操作或返回`Collection`的静态方法组成。它包含在Collection上操作的多态算法（排序、查找等）  
如果为此类的方法提供的`Collection`或类对象为`null`的话，这些方法将抛出`NullPointerException`。  
该类内部提供了几种**包装器**类，例如：不可修改的xxx，同步的xxx，检查类型的xxx（其中xxx为Collection下的接口或实现类）。  
###**针对List的同步视图、不可修改视图和检查类型视图通过源码进行分析**###
- `SynchronizedList`和`SynchronizedRandomAccessList`  
 着重分析`SynchronizedList`，后者是继承了前者，在`subList`操作上不一样（前者的`subList`操作返回的子列表需要包装为`SynchronizedList`而后者需要包装为`SynchronizedRandomAccessList`）。  
 `SynchronizedList`类其实是`List`实现类的同步视图，将实现类组合到自身，并且自身包含一个**对象锁**，每次调用`List`接口的某个操作，都会锁定整个方法，然后将请求**委托**给实现类，以此达到同步的目的。  

> **题外话**：同步视图是对整个方法进行加锁，串化执行，这样的效率显然不是满足高并发的需求，多线程同时竞争一个锁，这和单线程有什么区别。   

- `UnmodifiableList`和  `UnmodifiableRandomAccessList`  
此两个内部类是`List`实现类的不能修改类视图（无论是修改结构还是元素值），调用修改对象的方法将抛出`UnsupportedOperationException`异常。  
- `CheckedList`和`CheckedRandomAccessList`  
这两个内部类是`List`实现类的检查类型视图，构造该内部类的时候，需要传递给构造方法元素类型信息，每个读取方法都返回和类型匹配的元素，每个设置方法都先检查类型是否匹配。  
  
**完**

# Map接口以及HashMap源码分析 #
----  
## Map接口 ##
### Map接口描述 ###
Map 是将键映射到值的对象的集合，一个映射不能包含重复的键，每个键最多只能映射一个值。  

Map接口提供了三种*Collection* 视图，允许以键集合、值集合和键值对集合查看某个映射的内容。映射 *顺序* 定义为迭代器在映射的 *Collection* 视图上返回其元素的顺序。  

某些映射实现可保证其顺序如`TreeMap`；另一些映射实现则不能保证顺序，如`HashMap`类。  

约定所有的Map实现类都应该提供两个构造方法：一个`void`参数，一个带有单个`Map`类型的构造方法。  

如果某实现类中有不支持接口中的某些方法时，这些方法可以抛出`UnsupportedOperationException`。  

某些映射实现类可能对包含的键和值有所限制，例如，某些实现禁止`null`键和值。  
### 注意事项 ###
#### 可变对象设置为映射键 ####
将可变对象设置为映射键时要格外小心，当对象时映射中的某个键时，如果以影响`equals`比较的方式更改了对象的值，则映射的行为将是不确定的。也就是说“**相等的实例应该都具有相等的hashCode**”  
1. 在应用程序执行期间，只要对象的`equals` 方法的比较操作所用到的信息没有被修改，那么对同一对象调用多次，`hashCode` 都必须始终返回同一个整数。  
2. 如果两个对象根据`equals`的比较是相等的，那么调用这两个对象的`hashCode`方法都必须产生同样的整数结果  
3. 如果两个对象根据`equals`的比较不相等，那么`hashCode`方法返回值不一定不相等。（不相等的话，能提高散列表的性能）  
**参见：Effective Java Item 9: 覆盖equals时总要覆盖hashCode**  
#### 三种collection视图 ####
三种视图获取方法如下  

	Set<K> keySet(); //键集合
	Collection<V> values(); //值集合
	Set<Map.Entry<K,V>> entrySet(); //键值对集合

其中，映射的改变会实时反映到视图中，反过来亦然，如果在遍历视图的过程中，映射发生改变（除非是迭代器自身的`remove`操作），迭代的结果是不确定的，除了通过setValue在映射项(Map.Entry)上执行操作之外。视图支持删除元素的操作，并且删除操作会反映到映射中，但是视图不支持添加元素的操作。  

	public static void main(String[] args) {  
		Map<String, String> map = new HashMap<>();
		map.put("1", "1");  
		map.put("2", "2");  
		map.put("3", "3");
		map.put("4", "4");
        map.put("5", "5");  
        map.put("6", "6");  
        map.put("7", "7");  
        Set<String> keys = map.keySet();  
        Iterator<String> keyIter = keys.iterator();  
	    while (keyIter.hasNext()){  
            String key = keyIter.next();  
            if (key.equals("5")){  
                keyIter.remove();  
            }  
        }  
        //        for (String key : keys) {  
        //            if (key.equals("5")) {  
        //                keys.remove(key);  // ConcurrentModificationException
        //            }  
        //        }  
        Set<Map.Entry<String, String>> entries = map.entrySet();  
        for (Map.Entry<String, String> entry : entries) {  
            System.out.println(entry.getKey()+":"+entry.getValue());  
        }  
    }  

## HashMap源码分析 ##
### HashMap类描述 ###
基于哈希表的Map接口实现，可克隆，可拷贝，允许使用`null`值和`null`键，**不保证映射的顺序，并不保证顺序恒定不变**。   
此实现假定哈希函数将元素适当的分布在各桶之间，可为基本操作（get/put）提供稳定性能。
  
> **问题来了** 哈希函数怎么保证元素适当的分布在各桶之间？基本操作的性能如何？  

迭代`collection`视图所需的时间与`HashMap`实例的“容量”（桶的数量）以及大小（键值对数量）成比例。 所以**如果迭代性能很重要，则不要将初始容量设置得太高（或将加载因子设置的太低）**  
> **问题来了** 为什么当迭代性能很重要的时候，不要将容量设置得太高呢？`HashMap`的迭代器是个怎么回事儿？迭代的最高性能不是O（n）吗？难道`HashMap`的迭代性能大于等于O（n）？

`HashMap`的实例有两个参数影响其性能：**初始容量**和**加载因子**。**容量**是`HashMap`中桶的数量，初始容量就是在创建时桶的数量。**加载因子**是哈希表在其容量自动增加之前可以达到多满的一种尺度（多满指的是哈希表中已存在的键值对个数和所有桶的比例）  
 
当哈希表中的键值对数目超出了**加载因子**和**当前容量**的乘积时，则要对哈希表进行*rehash*操作（即重建内部数据结构），从而哈希表将具有大约两倍的桶数。
> **问题来了***rehash* 是个什么鬼？为什么哈希表的桶数要是之前的两倍？

默认**加载因子**(0.75)在时间和空间上寻求一种折中，加载因子过高虽减少了空间开销，但增加了查询成本，在设置初始容量时应该考虑到映射中所需的条目数以及加载因子，尽量减少哈希表*rehash*操作次数。  
如果有很多键值对要存储在`HashMap`中，则相对于按需执行自增来说，使用足够大的初始容量创建它将使得哈希表更有效的存储。  

#### 问题来了--解答区 ####
要解答上面的问题，需要从源码角度入手，窥一眼`HashMap`到底是怎么做到的。  
 1.**哈希函数怎么保证元素适当的分布在各桶之间？基本操作的性能如何？**  
每个对象都有对应的哈希码，作为哈希表的Key对象，key的哈希码通过`key.hashCode()`得到的。当键值对添加到哈希表中，`HashMap`对key对象的哈希码做了进一步的处理。

	public V put(K key, V value) {
        if (key == null)
            return putForNullKey(value); // null key 添加到table[0] 中
        int hash = hash(key); // 对key对象的哈希码做了进一步哈希
        int i = indexFor(hash, table.length); // 根据哈希码和table长度确定在table中的位置
        for (Entry<K,V> e = table[i]; e != null; e = e.next) { // 如果key处理后的哈希码和某键值对的key的哈希码一样，则更新键值对中的值
            Object k;
            if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                V oldValue = e.value;
                e.value = value;
                e.recordAccess(this); // 钩子函数
                return oldValue;
            }
        }

        modCount++; // 修改+1
        addEntry(hash, key, value, i); //不存在，则添加（如果table[i]上不为空，且size大于阈值threshold，则double length 并rehash）
        return null;
    }

着重分析下`hash(key)`函数

	final int hash(Object k) {
        int h = 0;
        if (useAltHashing) {
            if (k instanceof String) {
                return sun.misc.Hashing.stringHash32((String) k);
            }
            h = hashSeed;
        }

        h ^= k.hashCode(); // 获取原生的哈希码

        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12); // 对哈希码做进一步处理
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

为什么不直接使用key对象的哈希码呢？这是因为在根据哈希码和table.length 确定在table中的位置的函数是这样的  

	static int indexFor(int h, int length) {
        return h & (length-1); // 由于length 是2的次方（参见源码的构造函数），所以h&(length - 1) 就等于 h%length
    }

也就是说哈希码h中，只有低位的n位是有效的（n等于length开2的次方），这样冲突的概率就大大增大了，所以要对key对象的哈希码做进一步处理，使得高位的字节也能有效。  
**效率如何？** 我们上面分析了`put`操作的原理，其实`get`操作的原理是类似的，这里就不做分析了，我们可以看到，如果`table`中所有桶都是空的，那么添加元素的效率是O（1），如果`table`中的所有桶都只有一个`entry`，那么获取元素的效率是O(1),正因为`HashMap`中哈希函数能够尽可能的避免冲突，所以基本操作才会有这么高的效率。  
**影响效率**的地方：1. 哈希冲突 2. size超过阈值，需要rehash。

2.**为什么当迭代性能很重要的时候，不要将容量设置得太高呢？**  
这个问题我也一知半解，在这里把自己的理解写出来  
迭代是要遍历哈希表中的元素，如果容量设置很大的话，那么表中的大部分槽都是空的，就会浪费时间过滤为空的槽，这样迭代的性能就受影响了。  

	final Entry<K,V> nextEntry() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            Entry<K,V> e = next;
            if (e == null)
                throw new NoSuchElementException();

            if ((next = e.next) == null) {
                Entry[] t = table;
                while (index < t.length && (next = t[index++]) == null) 
                    ; // 过滤为空的槽
            }
            current = e;
            return e;
    }
#### 注意事项 ####
- **HashMap**不是同步的，多线程环境下，某线程对其结构上的修改（包括改变键值对的数目或者更改内部数据结构，例如，`rehash`）不能保证其他线程能同步到。使用`Collections.synchronizedMap`方法可以获得同步包装视图。但使用同步包装器代码效率奇低，如果需要同步哈希表，则选择`ConcurrentHashMap`，在并发环境下使用`HashMap`可能会出现死循环的危险 [http://coolshell.cn/articles/9606.html](http://coolshell.cn/articles/9606.html "[并发环境下HashMap死循环情况]")
- 所有此类的`Collection`视图方法所返回的迭代器都是快速失败的。

**完**
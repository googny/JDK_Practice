package lang;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by Googny on 2015/3/12.
 */
public final class MyBoolean implements Serializable, Comparator<MyBoolean> {

    Boolean aBoolean;

    public static final MyBoolean FALSE = new MyBoolean(false);

    public static final MyBoolean TRUE = new MyBoolean(true);

    public Class<MyBoolean> TYPE = MyBoolean.class;

    private final boolean value;

    private static final long serialVersionUID = -3665804199014368530L;

    public MyBoolean(boolean value) {
        this.value = value;
    }

    /*public MyBoolean(String value) {
        if ("true".equalsIgnoreCase(value)) {
            this.value = true;
        }
        if ("false".equalsIgnoreCase(value)) {
            this.value = false;
        } else {
            // TODO : 其他情况呢？
        }
    }*/

    public boolean booleanValue() {
        return this.value;
    }

    public static int compare(boolean x, boolean y) {
        if (x == y) {
            return 0;
        } else if (!x && y) {
            return -1;
        } else { // x&&!y
            return 1;
        }
    }

    public int compareTo(MyBoolean myBoolean) {
        return compare(this.value, myBoolean.value);
    }

    @Override
    public boolean equals(Object obj) {

        if ((null == obj) || !(obj instanceof MyBoolean)) {
            return false;
        }
        return compare(this,(MyBoolean)obj) == 0?true:false;
    }


    public static boolean getBoolean(String name){
        return "true".equals(System.getProperty(name))?true:false;
    }

    public int hashCode(){
        return value?1231:1237;
    }

    public static boolean parseBoolean(String s){
        if(null == s){
            return false;
        }
        return "true".equalsIgnoreCase(s)?true:false;
    }

    public String toString(){
        return value?"true":"false";
    }

    public static String toString(boolean b){
        return b?"true":"false";
    }

    public static MyBoolean valueOf(boolean b){
        return b?MyBoolean.TRUE:MyBoolean.FALSE;
    }

    public static MyBoolean valueOf(String s){
        if(null != s && "true".equalsIgnoreCase(s)){
            return MyBoolean.TRUE;
        }
        return MyBoolean.FALSE;
    }

    @Override
    public int compare(MyBoolean o1, MyBoolean o2) {
        return o1.compareTo(o2);
    }
}

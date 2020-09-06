package com.genericity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// 泛型
public class Main {
    public static void main(String[] args) {
        new Main().testErase();
    }

    // ===================================================================================================
    // 泛型定义
    // 可以在类中定义
    public class Genericity<T> {
        private T data;

        public T getData() {
            return data;
        }
    }

    private void testConvert() {
        Integer convert1 = this.<Integer>convert(1);
        // 泛型可以省略
        Integer convert2 = convert(1);
    }

    // 可以在方法中定义。（将泛型定义放到返回类型前面）
    public <T> T convert(T t) {
        return t;
    }

    // 静态方法必须这样写
    public static <T> void show(T t) {
    }

    // 接口泛型——声明定义 T；
    public interface Test<T> {
        // 返回 T；传参为 T；
        public T test(T t);
    }

    // ===================================================================================================
    // 泛型类返回值
    private void testArrayAlg() {
        double middle1 = ArrayAlg.getMiddle(1.0, 2.0);
        // 此处传入 int 和 double，那么它的返回值类型会是他们共同的父类。
        Number middle2 = ArrayAlg.getMiddle(1, 2.0);
    }

    static class ArrayAlg {
        public static <T> T getMiddle(T... a) {
            return a[a.length / 2];
        }
    }

    // ===================================================================================================
    // List<Object> 不等同于 List
    public void testObject() {
        List<String> strings = new ArrayList<>();

        // 为了兼容低版本，没有泛型的集合可以添加到带有泛型的集合中，但是此处有很大可能会报错。
        List list = new ArrayList();
        strings.addAll(list);

        // List 并不等于 List<Object>
        // 这里编译器直接报错，因为集合泛型是 Object，所以无法添加到泛型是 String 的集合里。
        List<Object> objects = new ArrayList<>();
        // strings.addAll(objects); //直接报错
    }

    // ===================================================================================================
    // 泛型通配符<? extends T>来接收返回的数据，此写法的泛型集合不能使用 add 方 法，而<? super T>不能使用 get 方法，作为接口调用赋值时易出错。注：可以适配多个通配符 <? extends String & Integer>
    // 说明：扩展说一下 PECS(Producer Extends Consumer Super)原则：第一、频繁往外读取内容的，适合用<? extends T>。第二、经常往里插入的，适合用<? super T>。
    public void testMin() {
        Integer[] integers = new Integer[0];
        Integer min = Main.min(integers);

        String[] strings = new String[0];
        String min1 = Main.min(strings);
    }

    // 假设有个排序的方法,依靠标准方法 compareTo() 来实现，那么并不是每个类都有 compareTo() 方法,那么我们只能限定只有是 Comparable 子类才可以传入。
    public static <T extends Comparable> T min(T[] a) //almost correct
    {
        if (a == null || a.length == 0)
            return null;
        T smallest = a[0];
        for (int i = 1; i < a.length; i++)
            if (smallest.compareTo(a[i]) > 0) smallest = a[i];
        return smallest;
    }

    // ===================================================================================================
    // 类型擦除
    public void testErase() {
        DateInterval dateInterval = new DateInterval();
        dateInterval.setSecond(LocalDate.MIN);
    }

    // ===================================================================================================
    // 泛型上限应用
    // 可以规范泛型的范围
    public void testTestClass() {
        TestClass<Integer> sdfsdf = new TestClass<>();
        sdfsdf.setValue(1);
        Integer value = sdfsdf.getValue();

        List<String>[] ls = new ArrayList[10];
    }

    public class TestClass<T extends Number> {
        public T value;

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }

    // ===================================================================================================
    // 通配符
    public void test1() {
        //List<?> objects = new ArrayList<>();
        //objects.add(new Object());
    }

    // ===================================================================================================
    // 限定通配符赋值情况
    public void test() {
        List<Object> objects = new ArrayList<>();
        List<MyObject> myObjects = new ArrayList<>();
        List<SubObject> subObjects = new ArrayList<>();

        // 只能接收 MyObject 子类型的列表
        //List<? extends MyObject> extend = objects; // 报错
        List<? extends MyObject> extend1 = myObjects;
        List<? extends MyObject> extend2 = subObjects;

        // 只能接收 MyObject 父类型的列表，super 类型不能用接口
        List<? super MyObject> supers = objects;
        List<? super MyObject> super1 = myObjects;
        //List<? super MyObject> super2 = subObjects; // 报错


        // 以下三行报错，因为 ? extends T 的 add 功能受限，
        // 他不知道该接收什么类型的数据、
        //extend.add(new Object());
        //extend.add(new MyObject(){});
        //extend.add(new SubObject());

        // 因为虚拟机中存储的就是类型就是 MyObject，只要是 MyObject 或其子类就可以添加。
        //supers.add(new Object()); // 报错
        supers.add(new MyObject());
        supers.add(new SubObject());

        // ? extends T 可以获取到数据，因为 ? extends T 的最大公约类型是 T
        //MyObject dog = extend.get(0);

        // ? super T 不能获取数据，只能获取到 Object，因为 ? super T 的最大公约类型只能是 Object
        Object object = supers.get(0);
    }

    class MyObject<T> extends Object implements TestObject {
    }

    class SubObject extends MyObject {
    }

    interface TestObject {
    }

    // ===================================================================================================
    // 多限定通配符赋值情况
    // 只能有 类 和 接口
    // 只能第一个是类，后面都是接口
    // T super 是不支持的
    class testa<T extends SubObject & TestObject> {

    }

    // ===================================================================================================
    // 泛型对象数组会报错
    private void testArray() {
        MyObject<String>[] myObject = new MyObject[10];
//        MyObject<String>[] myObject=new MyObject<String>[10]; //报错
    }
}

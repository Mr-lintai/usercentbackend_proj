package com.yupi.usercent.once;

import com.alibaba.excel.EasyExcel;

import java.util.List;

/**
 * @author lintai
 * @version 1.0
 */
public class ImportExcel {

    /**
     * 监听器读
     * 最简单的读
     * <p>
     * 1. 创建excel对应的实体对象 参照
     * <p>
     * 2. 由于默认一行行的读取excel，所以需要创建excel一行一行的回调监听器，参照{@link TableListener}
     * <p>
     * 3. 直接读即可
     */
    public static void main(String[] args) {
        String fileName = "demo.xlsx";
        readByListener(fileName);
        synchronousRead(fileName);

    }

    public static void readByListener(String fileName){
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
//        String fileName = "demo.xlsx";
        // 这里默认每次会读取100条数据 然后返回过来 直接调用使用数据就行
        // 具体需要返回多少行可以在`PageReadListener`的构造函数设置
        EasyExcel.read(fileName, XingQiuUserInfo.class, new TableListener()).sheet().doRead();
    }

    /**
     * 同步读
     * 同步的返回，不推荐使用，如果数据量大会把数据放到内存里面
     */
    public static void synchronousRead(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<XingQiuUserInfo> totalDataList = EasyExcel.read(fileName).head(XingQiuUserInfo.class).sheet().doReadSync();
        for (XingQiuUserInfo xingQiuUserInfo : totalDataList) {
            System.out.println(xingQiuUserInfo);
        }

    }
}

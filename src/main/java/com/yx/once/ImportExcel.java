package com.yx.once;

import com.alibaba.excel.EasyExcel;
import org.springframework.util.StopWatch;

/**
 * 从excel中导入数据到mysql，该方法执行需要spring容器启动才行，可以在测试类中进行执行该方法
 */
public class ImportExcel {
    public static void main(String[] args) {
        DemoDao demoDao = new DemoDao();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String fileName = "D:\\user\\user.xlsx";
        EasyExcel.read(fileName, ExcelDomainUser.class, new ExcelListenerUser(demoDao)).sheet().doRead();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}

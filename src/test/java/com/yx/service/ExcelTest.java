package com.yx.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.yx.domain.User;
import com.yx.once.DemoDao;
import com.yx.once.ExcelDomainUser;
import com.yx.once.ExcelListenerUser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
@Slf4j
public class ExcelTest {
    @Resource
    UserService userService;
    @Resource
    DemoDao demoDao;

    private ExecutorService executorService = new ThreadPoolExecutor(40, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    @Test
    public void readExcel() {
        String fileName = "D:\\test\\test.xlsx";
        List<ExcelDomainUser> totalDataList =
                EasyExcel.read(fileName).head(ExcelDomainUser.class).sheet().doReadSync();
        for (ExcelDomainUser user : totalDataList) {
            System.out.println(user);
        }
    }


    @Test
    public void readExcelByListener() {
        String fileName = "D:\\test\\test.xlsx";
        EasyExcel.read(fileName, ExcelDomainUser.class, new ExcelListenerUser(demoDao)).sheet().doRead();
    }

    //从excel导入10W条数据然后导入数据库
    @Test
    public void readExcelByListener2() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String fileName = "D:\\test\\test.xlsx";
        EasyExcel.read(fileName, ExcelDomainUser.class, new ExcelListenerUser(demoDao)).sheet().doRead();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    @Test
    public void write() {
        String fileName = "D:\\test\\test.xlsx";
        // 这里 需要指定写用哪个class去写
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try (ExcelWriter excelWriter = EasyExcel.write(fileName, ExcelDomainUser.class).needHead(true).build()) {
            // 这里注意 如果同一个sheet只要创建一次
            WriteSheet writeSheet = EasyExcel.writerSheet("Sheet1").build();
            List<ExcelDomainUser> userList = new ArrayList<>();
            for (int i = 0; i < 100000; i++) {

                    ExcelDomainUser user = new ExcelDomainUser();
                    user.setUsername("userAi001");
                    user.setUserAccount("userAiAccount");
                    user.setAvatarUrl("https://th.wallhaven.cc/small/zy/zygeko.jpg");
                    user.setGender(0);
                    user.setUserPassword("12345678");
                    user.setPhone("12345677654");
                    user.setEmail("123@qq.com");
                    user.setTags("[\"男\",\"大二\",\"Java\"]");
                    user.setPlanetCode("11111111");
                    user.setProfile("大家好，我叫张三，今年大二，擅长Java开发");
                    userList.add(user);

                // 去调用写入,这里我调用了五次，实际使用时根据数据库分页的总的页数来
            }
            // 分页去数据库查询数据 这里可以去数据库查询每一页的数据
            excelWriter.write(userList, writeSheet);
            stopWatch.stop();
            System.out.println(stopWatch.getTotalTimeMillis());

        }
    }
}

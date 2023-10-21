package com.yx.once;

import com.yx.model.domain.User;
import com.yx.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.*;

/**
 * 用于将excel中的数据导入数据库
 */
@Component
public class DemoDao {
    @Resource
    UserService userService;

    private ExecutorService executorService = new ThreadPoolExecutor(40, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));



    public void saveData(List<User> userList,List<CompletableFuture<Void>> futureList) {
        // 分十组
        int batchSize = 5000;

        // 异步执行
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println("threadName: " + Thread.currentThread().getName());
            userService.saveBatch(userList, batchSize);
        }, executorService);
        futureList.add(future);

    }
}


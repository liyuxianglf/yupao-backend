package com.yx.once;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.yx.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 定义Excel监听器用于获取excel数据
 */
@Slf4j
public class ExcelListenerUser implements ReadListener<ExcelDomainUser> {

    private  DemoDao demoDao;


    /**
     * 每隔5000条存储数据库，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5000;
    /**
     * 缓存的数据
     */
    private List<User> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    private List<CompletableFuture<Void>> futureList = new ArrayList<>();

    public ExcelListenerUser(DemoDao demoDao) {
        this.demoDao=demoDao;
    }

    /**
     * 每解析一条数据会调用这个方法
     *
     * @param data    one row value.
     * @param context
     */
    @Override
    public void invoke(ExcelDomainUser data, AnalysisContext context) {
        //log.info("解析到一条数据:{}", data);
        User user = new User();
        BeanUtils.copyProperties(data,user);
        cachedDataList.add(user);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            // 存储完成清理 list
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    /**
     * 所有数据解析完成了都会来调用
     *
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        saveData();
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        log.info("所有数据解析完成！");
    }

    /**
     * 加上存储数据库
     */
    private void saveData() {
       // log.info("{}条数据，开始存储数据库！", cachedDataList.size());
        demoDao.saveData(cachedDataList,futureList);
     //   log.info("存储数据库成功！");
    }
}

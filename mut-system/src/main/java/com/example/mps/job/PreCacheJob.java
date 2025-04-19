package com.example.mps.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mps.pojo.domain.UserDo;
import com.example.mps.pojo.vo.UserVo;
import com.example.mps.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: shayu
 * @date: 2022/12/11
 * @ClassName: yupao-backend01
 * @Description:        数据预热
 */

@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 重点用户
     */
    private final List<Long> mainUserList = List.of(1L);

    /**
     * 每天执行，预热推荐用户
     */
    @Scheduled(cron = "0 12 1 * * *")
    public void doCacheRecommendUser() {
        RLock lock = redissonClient.getLock("mps:precachejob:docache:lock");
        try {
            // 只有一个线程能获取到锁 (等待时间、持有时间、时间单位)
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                System.out.println("getLock: " + Thread.currentThread().getId());
                mainUserList.forEach(userId -> {
                    //查数据库
                    QueryWrapper<UserDo> queryWrapper = new QueryWrapper<>();
                    Page<UserDo> page = userService.page(new Page<>(1,10), queryWrapper);

                    List<UserDo> doList = page.getRecords();
                    List<UserVo> voList = doList.stream().map(UserVo::new).toList();

                    Page<UserVo> pageVo = new Page<>();
                    pageVo.setRecords(voList);
                    pageVo.setTotal(page.getTotal());
                    pageVo.setSize( page.getSize());
                    pageVo.setCurrent(page.getCurrent());

                    String redisKey = String.format("mpu:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    //写缓存,30s过期
                    try {
                        valueOperations.set(redisKey, pageVo,30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e){
                        log.error("redis set key error",e);
                    }
                });
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

}
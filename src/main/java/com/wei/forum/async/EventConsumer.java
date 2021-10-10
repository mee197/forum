package com.wei.forum.async;

import com.alibaba.fastjson.JSON;
import com.wei.forum.util.JedisAdapter;
import com.wei.forum.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
  * @Author:Wei
  * @Description:线程池消费者
  * @Date: 19:00 2019/4/1
  */
@Service
//初始化取出系统中的handle类支持的事件类型并将其放入上下文中，启动线程处理
public class EventConsumer implements InitializingBean, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    private Map<EventType, List<EventHandler>> config = new HashMap<EventType, List<EventHandler>>();
    private ApplicationContext applicationContext;

    @Autowired
        JedisAdapter jedisAdapter;

        @Override
        public void afterPropertiesSet() throws Exception {
            Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
            if (beans != null) {
                for (Map.Entry<String, EventHandler> entry : beans.entrySet()) {
                    List<EventType> eventTypes = entry.getValue().getSupportEventTypes();

                    for (EventType type : eventTypes) {
                        if (!config.containsKey(type)) {
                            config.put(type, new ArrayList<EventHandler>());
                        }
                        config.get(type).add(entry.getValue());
                    }
                }
            }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    //找到线程池队列
                    String key = RedisKeyUtil.getEventQueueKey();
                    //取出队列第一个元素，如果队列为空则等待设置时间再尝试，这里设置为0秒
                    List<String> events = jedisAdapter.brpop(0, key);

                        for (String message : events) {
                            if (message.equals(key)) {
                                continue;
                            }

                        EventModel eventModel = JSON.parseObject(message, EventModel.class);
                        if (!config.containsKey(eventModel.getType())) {
                            logger.error("不能识别的事件");
                            continue;
                        }

                        for (EventHandler handler : config.get(eventModel.getType())) {
                            handler.doHandle(eventModel);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

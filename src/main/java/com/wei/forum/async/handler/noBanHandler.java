package com.wei.forum.async.handler;

import com.wei.forum.async.EventHandler;
import com.wei.forum.async.EventModel;
import com.wei.forum.async.EventType;
import com.wei.forum.model.EntityType;
import com.wei.forum.model.Message;
import com.wei.forum.model.User;
import com.wei.forum.service.MessageService;
import com.wei.forum.service.UserService;
import com.wei.forum.util.ForumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Description Created by weilei on 11:11 2019/4/21
 **/
@Component
public class noBanHandler implements EventHandler {

    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @Override
    public void doHandle(EventModel model) {
        Message message = new Message();
        message.setFromId(ForumUtils.SYSTEM_USERID);
        message.setCreatedDate(new Date());
        User user = userService.getUser(model.getActorId());

        if (model.getEntityType() == EntityType.ENTITY_QUESTION) {
            message.setToId(model.getEntityOwnerId());
            message.setContent("管理员" + user.getName()
                    + "解封了你的问题,标题为：“" + model.getExt("title") + "”");
        } else if (model.getEntityType() == EntityType.ENTITY_USER) {
            message.setToId(model.getEntityId());
            message.setContent("管理员" + user.getName()
                    + "解封了您的账号，所有功能恢复使用");
        }

        messageService.addMessage(message);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.NOBAN);
    }

}

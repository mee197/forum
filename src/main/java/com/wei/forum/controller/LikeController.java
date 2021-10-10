package com.wei.forum.controller;

import com.wei.forum.async.EventModel;
import com.wei.forum.async.EventProducer;
import com.wei.forum.async.EventType;
import com.wei.forum.model.Comment;
import com.wei.forum.model.EntityType;
import com.wei.forum.model.HostHolder;
import com.wei.forum.service.CommentService;
import com.wei.forum.service.LikeService;
import com.wei.forum.util.ForumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by weilei on 20:35 2019/3/20
 **/

@Controller
public class LikeController {
    @Autowired
    LikeService likeService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    CommentService commentService;

    @Autowired
    EventProducer eventProducer;

    @RequestMapping(path = {"/like"}, method = {RequestMethod.POST})
    @ResponseBody
    public String like(@RequestParam("commentId") int commentId) {
        if (hostHolder.getUser() == null) {
            return ForumUtils.getJSONString(999);
        }

        if(hostHolder.getUser().getIsBan() == 1){
            return ForumUtils.getJSONString(1,"账号封禁中，禁止使用相关功能！");
        }

        Comment comment = commentService.getCommentById(commentId);

        eventProducer.fireEvent(new EventModel(EventType.LIKE)
                .setActorId(hostHolder.getUser().getId()).setEntityId(commentId)
                .setEntityType(EntityType.ENTITY_COMMENT).setEntityOwnerId(comment.getUserId())
                .setExt("questionId", String.valueOf(comment.getEntityId())));

        long likeCount = likeService.like(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, commentId);
        return ForumUtils.getJSONString(0, String.valueOf(likeCount));
    }

    @RequestMapping(path = {"/dislike"}, method = {RequestMethod.POST})
    @ResponseBody
    public String dislike(@RequestParam("commentId") int commentId) {
        if (hostHolder.getUser() == null) {
            return ForumUtils.getJSONString(999);
        }

        if(hostHolder.getUser().getIsBan() == 1){
            return ForumUtils.getJSONString(1,"账号封禁中，禁止使用相关功能！");
        }

        long likeCount = likeService.disLike(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, commentId);
        return ForumUtils.getJSONString(0, String.valueOf(likeCount));
    }
}


package com.wei.forum.controller;

import com.wei.forum.async.EventModel;
import com.wei.forum.async.EventProducer;
import com.wei.forum.async.EventType;
import com.wei.forum.model.Comment;
import com.wei.forum.model.EntityType;
import com.wei.forum.model.HostHolder;
import com.wei.forum.service.CommentService;
import com.wei.forum.service.QuestionService;
import com.wei.forum.util.ForumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * Created by weilei on 20:26 2019/3/20
 **/
@Controller
public class CommentController {
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);
    @Autowired
    HostHolder hostHolder;

    @Autowired
    CommentService commentService;

    @Autowired
    QuestionService questionService;

    @Autowired
    EventProducer eventProducer;


    @RequestMapping(path = {"/addComment"}, method = {RequestMethod.POST})
    public String addComment(@RequestParam("questionId") int questionId,
                             @RequestParam("content") String content) {
        try {
            if(hostHolder.getUser().getIsBan() == 1){
                return "redirect:/question/" + questionId;
            }
            Comment comment = new Comment();
            comment.setContent(content);
            if (hostHolder.getUser() != null) {
                comment.setUserId(hostHolder.getUser().getId());
            } else {
                comment.setUserId(ForumUtils.ANONYMOUS_USERID);
                // return "redirect:/reglogin";
            }
            comment.setCreatedDate(new Date());
            comment.setEntityType(EntityType.ENTITY_QUESTION);
            comment.setEntityId(questionId);
            commentService.addComment(comment);

            int count = commentService.getCommentCount(comment.getEntityId(), comment.getEntityType());
            questionService.updateCommentCount(comment.getEntityId(), count);

            eventProducer.fireEvent(new EventModel(EventType.COMMENT).setActorId(comment.getUserId())
                    .setEntityId(questionId));

        } catch (Exception e) {
            logger.error("增加评论失败" + e.getMessage());
        }
        return "redirect:/question/" + questionId;
    }

    @RequestMapping(value = "/banComment", method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String banComment(@RequestParam("commentId") int id){
        try{
            if(commentService.deleteComment(id)){
                eventProducer.fireEvent(new EventModel(EventType.BAN).
                        setActorId(hostHolder.getUser().getId()).
                        setEntityType(EntityType.ENTITY_COMMENT).
                        setEntityId(id).setEntityOwnerId(commentService.getCommentById(id).getUserId()).
                        setExt("content",commentService.getCommentById(id).getContent()));
                return ForumUtils.getJSONString(0,"封禁成功！");
            }
        }catch (Exception e){
            logger.error("封禁评论失败，出现错误" + e.getMessage());
        }
        return ForumUtils.getJSONString(1,"操作失败");
    }

    @RequestMapping(value = "/noBanComment", method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String noBanComment(@RequestParam("commentId") int id){
        try{
            if(commentService.recoverComment(id)){
                eventProducer.fireEvent(new EventModel(EventType.NOBAN).
                        setActorId(hostHolder.getUser().getId()).
                        setEntityType(EntityType.ENTITY_COMMENT).
                        setEntityId(id).setEntityOwnerId(commentService.getCommentById(id).getUserId()).
                        setExt("content",commentService.getCommentById(id).getContent()));
                return ForumUtils.getJSONString(0,"解封成功！");
            }
        }catch (Exception e){
            logger.error("解封评论失败，出现错误" + e.getMessage());
        }
        return ForumUtils.getJSONString(1,"操作失败");
    }
}


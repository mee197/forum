package com.wei.forum.controller;

import com.wei.forum.async.EventModel;
import com.wei.forum.async.EventProducer;
import com.wei.forum.async.EventType;
import com.wei.forum.model.*;
import com.wei.forum.service.*;
import com.wei.forum.util.ForumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by weilei on 20:46 2019/3/20
 **/


@Controller
public class QuestionController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    QuestionService questionService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    @Autowired
    FollowService followService;

    @Autowired
    LikeService likeService;

    @Autowired
    EventProducer eventProducer;

    @RequestMapping(value = "/question/{qid}", method = {RequestMethod.GET})
    public String questionDetail(Model model, @PathVariable("qid") int qid) {
        Question question = questionService.getById(qid);
        model.addAttribute("question", question);

        List<Comment> commentList = commentService.getCommentsByEntity(qid, EntityType.ENTITY_QUESTION);
        List<ViewObject> comments = new ArrayList<ViewObject>();
        for (Comment comment : commentList) {
            ViewObject vo = new ViewObject();
            vo.set("comment", comment);
            if (hostHolder.getUser() == null) {
                vo.set("liked", 0);
            } else {
                vo.set("liked", likeService.getLikeStatus(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, comment.getId()));
            }

            vo.set("likeCount", likeService.getLikeCount(EntityType.ENTITY_COMMENT, comment.getId()));
            vo.set("user", userService.getUser(comment.getUserId()));
            comments.add(vo);
        }

        model.addAttribute("comments", comments);

        List<ViewObject> followUsers = new ArrayList<ViewObject>();
        // 获取关注的用户信息
        List<Integer> users = followService.getFollowers(EntityType.ENTITY_QUESTION, qid, 20);
        for (Integer userId : users) {
            ViewObject vo = new ViewObject();
            User u = userService.getUser(userId);
            if (u == null) {
                continue;
            }
            vo.set("name", u.getName());
            vo.set("headUrl", u.getHeadUrl());
            vo.set("id", u.getId());
            followUsers.add(vo);
        }
        model.addAttribute("followUsers", followUsers);
        if (hostHolder.getUser() != null) {
            model.addAttribute("followed", followService.isFollower(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, qid));
        } else {
            model.addAttribute("followed", false);
        }

        return "detail";
    }

    @RequestMapping(value = "/question/add", method = {RequestMethod.POST})
    @ResponseBody
    public String addQuestion(@RequestParam("title") String title, @RequestParam("content") String content) {
        try {
            Question question = new Question();
            question.setContent(content);
            question.setCreatedDate(new Date());
            question.setTitle(title);
            if (hostHolder.getUser() == null) {
                question.setUserId(ForumUtils.ANONYMOUS_USERID);
            }else if(hostHolder.getUser().getIsBan() == 1){
                return ForumUtils.getJSONString(1,"账号封禁中，禁止使用相关功能！");
            } else {
                question.setUserId(hostHolder.getUser().getId());
            }
            if (questionService.addQuestion(question) > 0) {
                eventProducer.fireEvent(new EventModel(EventType.ADD_QUESTION)
                        .setActorId(question.getUserId()).setEntityId(question.getId())
                        .setExt("title", question.getTitle()).setExt("content", question.getContent()));
                return ForumUtils.getJSONString(0);
            }
        } catch (Exception e) {
            logger.error("增加题目失败" + e.getMessage());
        }
        return ForumUtils.getJSONString(1, "失败");
    }

    @RequestMapping(value = "/banQuestion", method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String banQuestion(@RequestParam("questionId") int id){
        try{
            if(questionService.banQuestion(id) != 0){
                eventProducer.fireEvent(new EventModel(EventType.BAN).
                        setActorId(hostHolder.getUser().getId()).
                        setEntityType(EntityType.ENTITY_QUESTION).
                        setEntityId(id).setEntityOwnerId(questionService.getById(id).getUserId()).
                        setExt("title",questionService.getById(id).getTitle()));
                return ForumUtils.getJSONString(0,"封禁成功！");
            }
        }catch (Exception e){
            logger.error("封禁问题失败，出现错误" + e.getMessage());
        }
        return ForumUtils.getJSONString(1,"操作失败");
    }

    @RequestMapping(value = "/noBanQuestion", method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String noBanQuestion(@RequestParam("questionId") int id){
        try{
            if(questionService.noBanQuestion(id) != 0){
                eventProducer.fireEvent(new EventModel(EventType.NOBAN).
                        setActorId(hostHolder.getUser().getId()).
                        setEntityType(EntityType.ENTITY_QUESTION).
                        setEntityId(id).setEntityOwnerId(questionService.getById(id).getUserId()).
                        setExt("title",questionService.getById(id).getTitle()));
                return ForumUtils.getJSONString(0,"解封成功！");
            }
        }catch (Exception e){
            logger.error("解封问题失败，出现错误" + e.getMessage());
        }
        return ForumUtils.getJSONString(1,"操作失败");
    }

}


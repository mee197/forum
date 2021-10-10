package com.wei.forum.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.wei.forum.async.EventModel;
import com.wei.forum.async.EventProducer;
import com.wei.forum.async.EventType;
import com.wei.forum.model.*;
import com.wei.forum.service.CommentService;
import com.wei.forum.service.FollowService;
import com.wei.forum.service.QuestionService;
import com.wei.forum.service.UserService;
import com.wei.forum.util.ForumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  Created by weilei on 20:40 2019/3/20
 **/

@Controller
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;

    @Autowired
    FollowService followService;

    @Autowired
    CommentService commentService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    EventProducer eventProducer;

    private List<ViewObject> getQuestions(int userId, int offset, int limit) {
        List<Question> questionList = questionService.getLatestQuestions(userId, offset, limit);
        List<ViewObject> vos = new ArrayList<>();
        for (Question question : questionList) {
            ViewObject vo = new ViewObject();
            vo.set("question", question);
            vo.set("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
            vo.set("user", userService.getUser(question.getUserId()));
            vos.add(vo);
        }
        return vos;
    }

    private List<vo> getMore(int userId, int offset, int limit){
        List<Question> questionList = questionService.getLatestQuestions(userId, offset, limit);
        List<vo> vos = new ArrayList<>();
        for (Question question : questionList) {
            vo a = new vo();
            a.setQuestion(question);
            a.setFollowCount(followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
            a.setUser(userService.getUser(question.getUserId()));
            vos.add(a);
        }
        return vos;
    }

    @RequestMapping(path = {"/", "/index"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String index(Model model,
                        @RequestParam(value = "pop", defaultValue = "0") int pop,
                        @RequestParam(value = "page", defaultValue = "0") int page) {
        model.addAttribute("vos", getQuestions(0, page * 10, 10));
        model.addAttribute("page",page);
        return "index";
    }

    @RequestMapping(path = {"/more/"},method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public JSONArray more(
            @RequestParam("page") int page){
        JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(getMore(0,page * 10, 10)));
        return jsonArray;
    }

    @RequestMapping(path = {"/user/{userId}"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String userIndex(Model model, @PathVariable("userId") int userId) {
        model.addAttribute("vos", getQuestions(userId, 0, 10));

        User user = userService.getUser(userId);
        ViewObject vo = new ViewObject();
        vo.set("user", user);
        vo.set("commentCount", commentService.getUserCommentCount(userId));
        vo.set("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, userId));
        vo.set("followeeCount", followService.getFolloweeCount(userId, EntityType.ENTITY_USER));
        if (hostHolder.getUser() != null) {
            vo.set("followed", followService.isFollower(hostHolder.getUser().getId(), EntityType.ENTITY_USER, userId));
        } else {
            vo.set("followed", false);
        }
        model.addAttribute("profileUser", vo);
        return "profile";
    }

    @RequestMapping(value = "/banUser", method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String banUser(@RequestParam("userId") int id){
        try{
            if(userService.BanUser(id) != 0){
                eventProducer.fireEvent(new EventModel(EventType.BAN).
                        setActorId(hostHolder.getUser().getId()).
                        setEntityType(EntityType.ENTITY_USER).
                        setEntityId(id));
                return ForumUtils.getJSONString(0,"封禁成功！");
            }
        }catch (Exception e){
            logger.error("封禁用户失败，出现错误" + e.getMessage());
        }
        return ForumUtils.getJSONString(1,"操作失败");
    }

    @RequestMapping(value = "/noBanUser", method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String noBanUser(@RequestParam("userId") int id){
        try{
            if(userService.noBanUser(id) != 0){
                eventProducer.fireEvent(new EventModel(EventType.NOBAN).
                        setActorId(hostHolder.getUser().getId()).
                        setEntityType(EntityType.ENTITY_USER).
                        setEntityId(id));
                return ForumUtils.getJSONString(0,"解封成功！");
            }
        }catch (Exception e){
            logger.error("解封用户失败，出现错误" + e.getMessage());
        }
        return ForumUtils.getJSONString(1,"操作失败");
    }

    @RequestMapping(value = "/modify", method = {RequestMethod.POST,RequestMethod.GET})
    public String modify(){
        return "modify";
    }

    @RequestMapping(value = "/modifyName", method = {RequestMethod.POST,RequestMethod.GET})
    public String modifyname(){
        return "modifyname";
    }

    @RequestMapping(value = {"/modifypwd"},method = {RequestMethod.GET,RequestMethod.POST})
    public String modifypwd(@RequestParam("UserId") int userId,
                            @RequestParam("Password") String password,
                            @RequestParam("newPassword") String repassword,
                            @RequestParam("reNewPassword") String newpassword,
                            HttpServletResponse response,Model model){
        System.out.println(userId + password + repassword + newpassword);
        Map<String,Object> map = userService.modifypwd(userId,password,repassword,newpassword);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath("/");
            cookie.setMaxAge(3600*24*5);
            response.addCookie(cookie);
            return "success";
        } else {
            model.addAttribute("msg", map.get("msg"));
            return "modify";
        }
    }

    @RequestMapping(value = {"/modifyname"},method = {RequestMethod.GET,RequestMethod.POST})
    public String modifypwd(@RequestParam("UserId") int userId,
                            @RequestParam("Password") String password,
                            @RequestParam("newname") String newname,
                            HttpServletResponse response,Model model){
        Map<String,Object> map = userService.modifyname(userId,password,newname);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath("/");
            cookie.setMaxAge(3600*24*5);
            response.addCookie(cookie);
            return "success";
        } else {
            model.addAttribute("msg", map.get("msg"));
            return "modifyname";
        }
    }

}
package com.wei.forum.controller;

import com.wei.forum.model.Comment;
import com.wei.forum.model.Question;
import com.wei.forum.model.ViewObject;
import com.wei.forum.service.CommentService;
import com.wei.forum.service.QuestionService;
import com.wei.forum.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description Created by weilei on 13:57 2019/4/21
 **/
@Controller
public class recoverController {
    private static final Logger logger = LoggerFactory.getLogger(recoverController.class);

    @Autowired
    CommentService commentService;

    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;

    @RequestMapping(path = {"/recover"}, method = {RequestMethod.GET,RequestMethod.POST})
    public String recover(Model model){
        model.addAttribute("questions",getQuestions());
        model.addAttribute("comments",getComments());
        return "recover";
    }

    private List<ViewObject> getComments() {
        List<Comment> commentList = commentService.getComenetByStatus();
        List<ViewObject> vos = new ArrayList<>();
        for (Comment comment : commentList) {
            ViewObject vo = new ViewObject();
            vo.set("comment", comment);
            vo.set("user", userService.getUser(comment.getUserId()));
            vos.add(vo);
        }
        return vos;
    }

    private List<ViewObject> getQuestions() {
        List<Question> questionList = questionService.getQuestionsByIsBan();
        List<ViewObject> vos = new ArrayList<>();
        for (Question question : questionList) {
            ViewObject vo = new ViewObject();
            vo.set("question", question);
            vo.set("user", userService.getUser(question.getUserId()));
            vos.add(vo);
        }
        return vos;
    }
}

package com.wei.forum.service;

import com.wei.forum.dao.QuestionDao;
import com.wei.forum.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 *  Created by weilei on 20:22 2019/3/20
 **/
@Service
public class QuestionService {
    @Autowired
    QuestionDao questionDAO;

    @Autowired
    SensitiveService sensitiveService;

    public Question getById(int id) {
        return questionDAO.getById(id);
    }

    public int addQuestion(Question question) {
        question.setTitle(HtmlUtils.htmlEscape(question.getTitle()));
        question.setContent(HtmlUtils.htmlEscape(question.getContent()));
        // 敏感词过滤
        question.setTitle(sensitiveService.filter(question.getTitle()));
        question.setContent(sensitiveService.filter(question.getContent()));
        return questionDAO.addQuestion(question) > 0 ? question.getId() : 0;
    }

    public List<Question> getLatestQuestions(int userId, int offset, int limit) {
        return questionDAO.selectLatestQuestions(userId, offset, limit);
    }

    public List<Question> getQuestionsByIsBan() {
        return questionDAO.selectQuestionByIsBan(1);
    }

    public int updateCommentCount(int id, int count) {
        return questionDAO.updateCommentCount(id, count);
    }

    public int banQuestion(int id){
        return questionDAO.BanQuestion(id);
    }

    public int noBanQuestion(int id){
        return questionDAO.noBanQuestion(id);
    }
}

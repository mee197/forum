package com.wei.forum.model;

/**
 * @Description Created by weilei on 17:49 2019/4/18
 **/
public class vo {
    private User user;
    private Question question;
    private long followCount;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public long getFollowCount() {
        return followCount;
    }

    public void setFollowCount(long followCount) {
        this.followCount = followCount;
    }
}

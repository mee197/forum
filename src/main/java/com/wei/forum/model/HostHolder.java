package com.wei.forum.model;

import org.springframework.stereotype.Component;

/**
 * @Description //保存登录用户信息
 * create by weilei on 2018/12/18 10:11
 **/
@Component
public class HostHolder {
    private static ThreadLocal<User> users = new ThreadLocal<>();

    public User getUser(){
        return users.get();
    }

    public void setUser(User user){
        users.set(user);
    }

    public void clear(){
        users.remove();
    }
}

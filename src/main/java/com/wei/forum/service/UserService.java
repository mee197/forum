package com.wei.forum.service;

import com.wei.forum.dao.LoginTicketDao;
import com.wei.forum.dao.UserDao;
import com.wei.forum.model.LoginTicket;
import com.wei.forum.model.User;
import com.wei.forum.util.ForumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @Description //用户模块服务
 * create by weilei on 2018/12/13 10:54
 **/
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserDao userDao;

    @Autowired
    private LoginTicketDao loginTicketDAO;

    public User selectByName(String name) {
        return userDao.selectByName(name);
    }

    public Map<String, Object> register(String username, String password) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (org.apache.commons.lang.StringUtils.isBlank(username)) {
            map.put("msg", "用户名不能为空");
            return map;
        }

        if (org.apache.commons.lang.StringUtils.isBlank(password)) {
            map.put("msg", "密码不能为空");
            return map;
        }

        User user = userDao.selectByName(username);

        if (user != null) {
            map.put("msg", "用户名已经被注册");
            return map;
        }

        // 密码强度
        user = new User();
        user.setName(username);
        user.setSalt(UUID.randomUUID().toString().substring(0, 5));
        String head = String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000));
        user.setHeadUrl(head);
        user.setPassword(ForumUtils.MD5(password+user.getSalt()));
        userDao.addUser(user);

        // 登陆
        String ticket = addLoginTicket(user.getId());
        map.put("ticket", ticket);
        return map;
    }


    public Map<String, Object> login(String username, String password) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (org.apache.commons.lang.StringUtils.isBlank(username)) {
            map.put("msg", "用户名不能为空");
            return map;
        }

        if (org.apache.commons.lang.StringUtils.isBlank(password)) {
            map.put("msg", "密码不能为空");
            return map;
        }

        User user = userDao.selectByName(username);

        if (user == null) {
            map.put("msg", "用户名不存在");
            return map;
        }

        if (!ForumUtils.MD5(password+user.getSalt()).equals(user.getPassword())) {
            map.put("msg", "密码不正确");
            return map;
        }

        String ticket = addLoginTicket(user.getId());
        map.put("ticket", ticket);
        map.put("userId", user.getId());
        return map;
    }

    private String addLoginTicket(int userId) {
        LoginTicket ticket = new LoginTicket();
        ticket.setUserId(userId);
        Date date = new Date();
        date.setTime(date.getTime() + 1000*3600*24);
        ticket.setExpired(date);
        ticket.setStatus(0);
        ticket.setTicket(UUID.randomUUID().toString().replaceAll("-", ""));
        loginTicketDAO.addTicket(ticket);
        return ticket.getTicket();
    }

    public User getUser(int id) {
        return userDao.selectById(id);
    }

    public void logout(String ticket) {
        loginTicketDAO.updateStatus(ticket, 1);
    }

    public int BanUser(int id){
        return userDao.banUser(id);
    }

    public int noBanUser(int id){
        return userDao.noBanUser(id);
    }

    public Map<String, Object> modifypwd(int userId,String password,String repassword,String newpassword){
        Map<String,Object> map = new HashMap<>();
        if(org.apache.commons.lang.StringUtils.isBlank(password)){
            map.put("msg","原密码不能为空");
            return map;
        }

        if(org.apache.commons.lang.StringUtils.isBlank(repassword)){
            map.put("msg","请输入新密码");
            return map;
        }

        if(org.apache.commons.lang.StringUtils.isBlank(newpassword)){
            map.put("msg","请再次输入新密码");
            return map;
        }

        if(!newpassword.equals(repassword)){
            map.put("msg","两次输入的密码不一样，请重新输入");
            return map;
        }

        User user = userDao.selectById(userId);
        if(!ForumUtils.MD5(password+user.getSalt()).equals(user.getPassword())){
            map.put("msg","密码错误");
            return map;
        }

        user.setSalt(UUID.randomUUID().toString().substring(0,5));
        user.setPassword(ForumUtils.MD5(newpassword+user.getSalt()));

        userDao.updatePassword(user);

        //下发t票
        String ticket = addLoginTicket(user.getId());
        map.put("ticket",ticket);
        return map;
    }

    public Map<String, Object> modifyname(int userId,String password,String newname){
        Map<String,Object> map = new HashMap<>();
        if(org.apache.commons.lang.StringUtils.isBlank(password)){
            map.put("msg","原密码不能为空");
            return map;
        }

        if(org.apache.commons.lang.StringUtils.isBlank(newname)){
            map.put("msg","请输入新用户名");
            return map;
        }

        User user = userDao.selectById(userId);
        if(!ForumUtils.MD5(password+user.getSalt()).equals(user.getPassword())){
            map.put("msg","密码错误");
            return map;
        }

        user.setName(newname);

        userDao.updateName(user);

        //下发t票
        String ticket = addLoginTicket(user.getId());
        map.put("ticket",ticket);
        return map;
    }

//    public static void main(String[] args) {
//        System.out.println(ForumUtils.MD5("123456789"+"23179"));
//    }
}

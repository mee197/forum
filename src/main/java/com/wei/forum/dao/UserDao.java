package com.wei.forum.dao;

import com.wei.forum.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserDao {
    String TABLE_NAME = "user";
    String INSERT_FIELDS = " name, password, salt, head_url";
    String SELECT_FIELDS = " id, name, password, salt, head_url, is_admin, is_ban ";

    @Select({"select", SELECT_FIELDS, "from", TABLE_NAME, "where name = #{name}"})
    User selectByName(String name);


    @Select({"select", SELECT_FIELDS, "from", TABLE_NAME, "where id = #{id}"})
    User selectById(int id);

    @Insert({"insert into", TABLE_NAME, "(", INSERT_FIELDS, ") values (#{name},#{password},#{salt},#{headUrl})"})
    int addUser(User user);

    @Update({"update ",TABLE_NAME, "set password = #{password} , salt = #{salt} where id = #{id}"})
    void updatePassword(User user);

    @Update({"update ",TABLE_NAME, "set name = #{name} where id = #{id}"})
    void updateName(User user);

    @Update({"update ",TABLE_NAME, "set is_ban = 1 where id = #{id}"})
    int banUser(int id);

    @Update({"update ",TABLE_NAME, "set is_ban = 0 where id = #{id}"})
    int noBanUser(int id);


}

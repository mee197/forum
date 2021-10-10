package com.wei.forum.dao;

import com.wei.forum.model.Question;
import org.apache.ibatis.annotations.*;

import java.util.List;
@Mapper
public interface QuestionDao {
    String TABLE_NAME = " question ";
    String INSERT_FIELDS = " title, content, created_date, user_id, comment_count ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
            ") values (#{title},#{content},#{createdDate},#{userId},#{commentCount})"})
    int addQuestion(Question question);

    List<Question> selectLatestQuestions(@Param("userId") int userId, @Param("offset") int offset,
                                         @Param("limit") int limit);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where id=#{id}"})
    Question getById(int id);

    @Update({"update ", TABLE_NAME, " set comment_count = #{commentCount} where id=#{id}"})
    int updateCommentCount(@Param("id") int id, @Param("commentCount") int commentCount);

    @Update({"update", TABLE_NAME, "set isBan = 1 where id = #{id}"})
    int BanQuestion(int id);

    @Update({"update", TABLE_NAME, "set isBan = 0 where id = #{id}"})
    int noBanQuestion(int id);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME,
            " where is_ban=#{isBan} order by created_date desc"})
    List<Question> selectQuestionByIsBan(int isBan);

}
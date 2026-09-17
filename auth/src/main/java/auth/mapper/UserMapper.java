package auth.mapper;

import model.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户数据访问层
 */
@Mapper
public interface UserMapper {
    @Insert("insert into user(upassword,uname,usex,urole,uemail) values (#{upassword},#{uname},COALESCE(#{usex},'保密'),'user',#{uemail})")
    void insertUser(User user);
    //根据用户名查询
    @Select("select uaccount, upassword, uname, usex, urole, uavatar, uemail from user where uname=#{username}")
    User findUserByName(String username);
    //根据用户账号查询
    @Select("select uaccount, upassword, uname, usex, urole, uavatar, uemail from user where uaccount=#{uaccount}")
    User findByUaccount(String uaccount);
    //根据邮箱查询
    @Select("select uaccount, upassword, uname, usex, urole, uavatar, uemail from user where uemail=#{email}")
    User findByEmail(String email);
    //查询所有用户
    @Select("select uaccount, uname, usex, urole, uavatar, uemail from user")
    List<User> findAllUsers();
    //更新用户信息(管理员可修改全部字段)
    @Update("update user set uname=#{uname}, usex=#{usex}, uemail=#{uemail}, urole=#{urole} where uaccount=#{uaccount}")
    void updateUser(User user);
    //更新用户头像
    @Update("update user set uavatar=#{uavatar} where uaccount=#{uaccount}")
    void updateAvatar(User user);
    //修改密码
    @Update("update user set upassword=#{upassword} where uaccount=#{uaccount}")
    void updatePassword(User user);
    //删除用户
    @Delete("delete from user where uaccount=#{uaccount}")
    void deleteByUaccount(String uaccount);
}

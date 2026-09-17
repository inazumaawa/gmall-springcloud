package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * 用户实体类
 * role: user=普通用户, admin=管理员
 */
public class User {

    private String uaccount;    // 用户账号（唯一标识）
    private String upassword;   // 用户密码
    private String uname;       // 用户名
    private String usex;        // 性别
    private String urole;       // 角色 user=普通用户 admin=管理员
    private String uavatar;     // 头像URL
    private String uemail;      // 邮箱
    private String token;       // JWT令牌（不存数据库，仅接口返回）

    @JsonIgnore
    public String getUpassword() {
        return upassword;
    }
    @JsonSetter
    public void setUpassword(String upassword) {
        this.upassword = upassword;
    }

    public String getUaccount() {
        return uaccount;
    }

    public void setUaccount(String uaccount) {
        this.uaccount = uaccount;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getUsex() {
        return usex;
    }

    public void setUsex(String usex) {
        this.usex = usex;
    }

    public String getUrole() {
        return urole;
    }

    public void setUrole(String urole) {
        this.urole = urole;
    }

    public String getUavatar() {
        return uavatar;
    }

    public void setUavatar(String uavatar) {
        this.uavatar = uavatar;
    }

    public String getUemail() {
        return uemail;
    }

    public void setUemail(String uemail) {
        this.uemail = uemail;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "User{" +
                "uaccount='" + uaccount + '\'' +
                ", upassword='" + upassword + '\'' +
                ", uname='" + uname + '\'' +
                ", usex='" + usex + '\'' +
                ", urole='" + urole + '\'' +
                ", uavatar='" + uavatar + '\'' +
                ", uemail='" + uemail + '\'' +
                ", token='" + token + '\'' +
                '}';
    }

    public User() {
    }

    public User(String uaccount, String upassword, String uname, String usex, String urole, String uavatar, String uemail, String token) {
        this.uaccount = uaccount;
        this.upassword = upassword;
        this.uname = uname;
        this.usex = usex;
        this.urole = urole;
        this.uavatar = uavatar;
        this.uemail = uemail;
        this.token = token;
    }
}

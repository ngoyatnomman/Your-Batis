package com.shy.pojo;

import java.io.Serializable;

public class User implements Serializable {
    private Integer  userId;
    private String username;
    private String password;
    private String sex;
    private Integer money;

    public User(Integer userId, String username, String password, String sex, Integer money) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.sex = sex;
        this.money = money;
    }

    public User() {
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", sex='" + sex + '\'' +
                ", money=" + money +
                '}';
    }
}

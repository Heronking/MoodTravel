package com.wangliu.moodtravel.users;

import java.io.Serializable;

import cn.bmob.v3.BmobUser;

//让所有实体类都实现Serializable接口，做序列化
public class User extends BmobUser implements Serializable {
    private String nickName;
    private City home;
    private Integer avatar;

    public Integer getAvatar() {
        return avatar;
    }

    public void setAvatar(Integer avatar) {
        this.avatar = avatar;
    }

    public City getHome() {
        return home;
    }

    public void setHome(City home) {
        this.home = home;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

}

class City implements Serializable {
    String province;
    String city;
    String area;

    public City(String province, String city, String area) {
        this.province = province;
        this.city = city;
        this.area = area;
    }

    public String getName() {
        return province + '-' + city + '-' + area;
    }
}
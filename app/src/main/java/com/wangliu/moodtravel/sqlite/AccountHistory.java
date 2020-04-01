package com.wangliu.moodtravel.sqlite;

public class AccountHistory {
    private int id;
    private String account;
    private String password;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public AccountHistory(int id, String username, String password) {
        this.id = id;
        this.account = username;
        this.password = password;
    }
}

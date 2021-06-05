package com.sandysmitsks.sociohub.sociohub;

public class Comments
{
    public String comment ,date , time , user_name;

    public Comments()
    {

    }

    public Comments(String comment, String date, String time, String user_name) {
        this.comment = comment;
        this.date = date;
        this.time = time;
        this.user_name = user_name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }
}

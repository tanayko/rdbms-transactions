package com.lseg.acadia.skills.rdbmstx.models;

public class People {
    public long id;
    public String name;
    public int age;

    public People() {

    }

    public People(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

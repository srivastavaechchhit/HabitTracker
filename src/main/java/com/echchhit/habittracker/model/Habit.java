package com.echchhit.habittracker.model;

public class Habit {

    private int id;
    private String name;
    private String createdAt;

    public Habit(int id, String name, String createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return name;
    }
}

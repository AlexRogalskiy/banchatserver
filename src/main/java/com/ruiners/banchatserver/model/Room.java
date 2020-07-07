package com.ruiners.banchatserver.model;

public class Room {
    private final long id;
    private final String name;

    public Room(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

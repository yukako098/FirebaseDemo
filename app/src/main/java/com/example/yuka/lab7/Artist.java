package com.example.yuka.lab7;

public class Artist {

    private String id;
    private String name;
    private String genre;

    public Artist() {
    }

    public Artist(String id, String name, String genre) {
        this.id = id;
        this.name = name;
        this.genre = genre;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}

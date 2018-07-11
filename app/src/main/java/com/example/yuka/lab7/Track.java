package com.example.yuka.lab7;

public class Track {
    private String id;
    private String trackName;
    private String rating;


    private String artist_id;

    public Track() {
    }

    public Track(String id, String trackName, String rating, String artist_id) {
        this.id = id;
        this.trackName = trackName;
        this.rating = rating;
        this.artist_id = artist_id;
    }

    @Override
    public String toString() {
        return this.trackName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
    public String getArtist_id() {
        return artist_id;
    }

    public void setArtist_id(String artist_id) {
        this.artist_id = artist_id;
    }

}

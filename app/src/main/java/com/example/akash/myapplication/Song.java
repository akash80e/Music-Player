package com.example.akash.myapplication;

/**
 * Created by akash on 16/11/17.
 */

public class Song {
    private long id;
    private String name;
    private String artist;

    public Song(long songID, String songName, String artistName){
        id=songID;
        name=songName;
        artist=artistName;
    }
    public long getID(){
        return id;
    }
    public String getSongName(){
        return name;
    }
    public String getArtistName(){
        return artist;
    }
}

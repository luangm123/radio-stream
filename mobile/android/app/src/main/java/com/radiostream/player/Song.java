package com.radiostream.player;

/**
 * Created by vitaly on 15/11/2016.
 */
public class Song {

    public interface Events {
        void onSongFinish(Song song);
    }
}

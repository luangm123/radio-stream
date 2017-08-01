package com.radiostream.player;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.radiostream.networking.metadata.MetadataBackendGetter;
import com.radiostream.networking.models.SongResult;

import org.jdeferred.DonePipe;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class Playlist {
    private String mPlaylistName;
    private MetadataBackendGetter mMetadataBackendGetter;
    private SongFactory mSongFactory;

    private List<Song> mSongs = new ArrayList<>();
    private int mIndex = 0;

    public Playlist(String playlistName, MetadataBackendGetter metadataBackendGetter, SongFactory songFactory) {
        mPlaylistName = playlistName;
        mMetadataBackendGetter = metadataBackendGetter;
        mSongFactory = songFactory;
    }

    private Promise<Void, Exception, Void> reloadIfNeededForSongIndex(int index) {
        Timber.i("checking if reload is needed. songs count: %d Current index: %d", mSongs.size(), mIndex);
        if (index >= mSongs.size()) {
            Timber.i("reloading songs");

            return mMetadataBackendGetter.get().fetchPlaylist(mPlaylistName).then(new DonePipe<List<SongResult>, Void, Exception, Void>() {
                @Override
                public Promise<Void, Exception, Void> pipeDone(List<SongResult> result) {
                    if (result.size() == 0) {
                        Timber.e("empty playlist was returned");
                        return new DeferredObject<Void, Exception, Void>().reject(new Exception("Empty playlists")).promise();
                    }

                    for (SongResult songResult : result) {
                        mSongs.add(mSongFactory.build(songResult));
                    }

                    return new DeferredObject<Void, Exception, Void>().resolve(null).promise();
                }
            });
        } else {
            Timber.i("no reload required");
            return new DeferredObject<Void, Exception, Void>().resolve(null).promise();
        }
    }

    public Promise<Song, Exception, Void> peekCurrentSong() {
        Timber.i("function start");

        return peekSong(mIndex);
    }

    public Promise<Song, Exception, Void> peekNextSong() {
        Timber.i("function start");

        return peekSong(mIndex + 1);
    }

    public boolean isCurrentSong(Song song) {
        if (mIndex < mSongs.size()) {
            Timber.i("checking if given song '%s' is the current song", song.toString());
            return mSongs.get(mIndex) == song;
        } else {
            Timber.i("current index %d out of songs bounds %d - this can't be the current song", mIndex, mSongs.size());
            // it can't be the current song - current index is already beyond the bounds of the playlist
            return false;
        }
    }

    public void nextSong() {
        Timber.i("function start. From %d to %d", mIndex, mIndex + 1);
        mIndex++;
    }

    private Promise<Song, Exception, Void> peekSong(final int index) {
        Timber.i("function start. index: %d", index);

        return reloadIfNeededForSongIndex(index).then(new DonePipe<Void, Song, Exception, Void>() {
            @Override
            public Promise<Song, Exception, Void> pipeDone(Void result) {
                DeferredObject<Song, Exception, Void> deferredObject = new DeferredObject<>();
                try {
                    Song resolve = mSongs.get(index);
                    Timber.i("peeked next song: %s", resolve.toString());
                    return deferredObject.resolve(resolve).promise();
                } catch (Exception e) {
                    Timber.e(e, "peek failed");
                    return deferredObject.reject(e);
                }
            }
        });
    }

    public WritableMap toBridgeObject() {
        WritableMap map = Arguments.createMap();
        map.putString("name", this.mPlaylistName);
        map.putInt("currentIndex", this.mIndex);

        WritableArray songsArray = Arguments.createArray();
        for (Song song : this.mSongs) {
            songsArray.pushMap(song.toBridgeObject());
        }
        map.putArray("songs", songsArray);

        return map;
    }

    public void close() {
        Timber.i("closing all songs");
        for (Song song : mSongs) {
            song.close();
        }
    }
}

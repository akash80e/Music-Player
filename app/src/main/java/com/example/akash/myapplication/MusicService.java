package com.example.akash.myapplication;

import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;

import java.util.ArrayList;

/**
 * Created by akash on 18/11/17.
 */

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener{


    private MediaPlayer mediaplayer;
    private ArrayList<Song> songs;
    private int songPos;
    private final IBinder musicbind = new MusicBinder();
    private String songTitle="";
    private static final int NOTIFY_ID=1;

    private boolean shuffle=false;
    private Random rand;

    public void onCreate(){
        super.onCreate();
        songPos=0;
        mediaplayer = new MediaPlayer();
        initMediaPlayer();
        rand=new Random();
    }

    public void setShuffle(){
        if(shuffle)
            shuffle = false;
        else
            shuffle = true;
    }

    public void initMediaPlayer(){
        mediaplayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaplayer.setOnPreparedListener(this);
        mediaplayer.setOnErrorListener(this);
        mediaplayer.setOnCompletionListener(this);
    }

    public void setList(ArrayList<Song> theSongs){
        songs = theSongs;
    }

    public class MusicBinder extends Binder {
        MusicService getService(){
            return MusicService.this;
        }
    }

    public void playSong(){
        mediaplayer.reset();
        Song playsong = songs.get(songPos);
        songTitle=playsong.getSongName();
        long currSong = playsong.getID();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
        try {
            mediaplayer.setDataSource(getApplicationContext(), trackUri);
        }
        catch (Exception e){
            Log.e("MUSIC SERVICE", "ERROR SETTING DATA SOURCE", e);
        }
        mediaplayer.prepareAsync();
    }
    public void setSong(int songIndex){
        songPos=songIndex;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        mediaplayer.stop();
        mediaplayer.release();

        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicbind;
    }

    @Override
        public void onCompletion(MediaPlayer mp) {
            if(mp.getCurrentPosition()>0){
                mp.reset();
                playNext();
            }
        }


    @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            mp.reset();
            return false;
        }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
  .setContentText(songTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);

    }
    public int getPosn(){
        return mediaplayer.getCurrentPosition();
    }

    public int getDur(){
        return mediaplayer.getDuration();
    }

    public boolean isPng(){
        return mediaplayer.isPlaying();
    }

    public void pausePlayer(){
        mediaplayer.pause();
    }

    public void seek(int posn){
        mediaplayer.seekTo(posn);
    }

    public void go(){
        mediaplayer.start();
    }
    public void playPrev(){
        songPos--;
        if(songPos<0) songPos=songs.size()-1;
        playSong();
    }

    //skip to next
    public void playNext(){
        if(shuffle){
            int newSong = songPos;
            while(newSong==songPos){
                newSong=rand.nextInt(songs.size());
            }
            songPos=newSong;
        }
        else{
            songPos++;
            if(songPos>=songs.size()) songPos=0;
        }
        playSong();
    }
    @Override
    public void onDestroy() {
        stopForeground(true);
    }
}

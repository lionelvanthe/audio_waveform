package com.mp3cutter.soulappsworld.custom.mix;

import android.media.MediaPlayer;

import com.mp3cutter.soulappsworld.MarkerView;
import com.mp3cutter.soulappsworld.WaveformViewAdvance;

public class Mix {

    private WaveformViewAdvance waveformViewAdvance;
    private MediaPlayer mediaPlayer;
    private MarkerView startMarker;
    private MarkerView endMarker;

    public Mix(WaveformViewAdvance waveformViewAdvance, MediaPlayer mediaPlayer, MarkerView startMarker, MarkerView endMarker) {
        this.waveformViewAdvance = waveformViewAdvance;
        this.mediaPlayer = mediaPlayer;
        this.startMarker = startMarker;
        this.endMarker = endMarker;
    }

    public WaveformViewAdvance getWaveformViewAdvance() {
        return waveformViewAdvance;
    }

    public void setWaveformViewAdvance(WaveformViewAdvance waveformViewAdvance) {
        this.waveformViewAdvance = waveformViewAdvance;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public MarkerView getStartMarker() {
        return startMarker;
    }

    public void setStartMarker(MarkerView startMarker) {
        this.startMarker = startMarker;
    }

    public MarkerView getEndMarker() {
        return endMarker;
    }

    public void setEndMarker(MarkerView endMarker) {
        this.endMarker = endMarker;
    }
}

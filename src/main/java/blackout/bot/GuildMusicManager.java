package br.blackout.bot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

public class GuildMusicManager {

    private final AudioPlayer player;

    private final TrackScheduler scheduler;

    private final AudioPlayerSendHandler audioSendHandler;

    public GuildMusicManager(
            AudioPlayer player
    ) {

        this.player = player;

        this.scheduler =
                new TrackScheduler(
                        player
                );

        this.audioSendHandler =
                new AudioPlayerSendHandler(
                        player
                );

        player.addListener(
                scheduler
        );
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }

    public AudioPlayerSendHandler getAudioSendHandler() {
        return audioSendHandler;
    }
}
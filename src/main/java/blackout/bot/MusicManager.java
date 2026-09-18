package br.blackout.bot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MusicManager {

    private static final AudioPlayerManager PLAYER_MANAGER =
            new DefaultAudioPlayerManager();

    private static final Map<Long, GuildMusicManager> MUSIC_MANAGERS =
            new ConcurrentHashMap<>();

    static {

        AudioSourceManagers.registerRemoteSources(
                PLAYER_MANAGER
        );
    }

    public static GuildMusicManager getGuildMusicManager(
            Guild guild
    ) {

        return MUSIC_MANAGERS.computeIfAbsent(
                guild.getIdLong(),
                id -> {

                    GuildMusicManager manager =
                            new GuildMusicManager(
                                    PLAYER_MANAGER.createPlayer()
                            );

                    guild.getAudioManager()
                            .setSendingHandler(
                                    manager.getAudioSendHandler()
                            );

                    return manager;
                }
        );
    }

    public static void play(
            Guild guild,
            AudioChannel channel,
            String link,
            SlashCommandInteractionEvent event
    ) {

        if (channel == null) {

            event.reply(
                    "❌ Canal de voz inválido."
            ).setEphemeral(true).queue();

            return;
        }

        GuildMusicManager manager =
                getGuildMusicManager(guild);

        guild.getAudioManager()
                .openAudioConnection(channel);

        PLAYER_MANAGER.loadItemOrdered(
                manager,
                link,
                new AudioLoadResultHandler() {

                    @Override
                    public void trackLoaded(
                            AudioTrack track
                    ) {

                        manager.getScheduler()
                                .queue(track);

                        event.reply(
                                "🎵 **Música carregada!**\n\n" +
                                "▶️ `" +
                                track.getInfo().title +
                                "`"
                        ).queue();
                    }

                    @Override
                    public void playlistLoaded(
                            AudioPlaylist playlist
                    ) {

                        if (
                                playlist.getTracks()
                                        .isEmpty()
                        ) {

                            event.reply(
                                    "❌ Nenhuma música encontrada."
                            ).setEphemeral(true).queue();

                            return;
                        }

                        AudioTrack track =
                                playlist.getTracks()
                                        .get(0);

                        manager.getScheduler()
                                .queue(track);

                        event.reply(
                                "🎵 **Música carregada!**\n\n" +
                                "▶️ `" +
                                track.getInfo().title +
                                "`"
                        ).queue();
                    }

                    @Override
                    public void noMatches() {

                        event.reply(
                                "❌ Não encontrei nada nesse link."
                        ).setEphemeral(true).queue();
                    }

                    @Override
                    public void loadFailed(
                            Exception exception
                    ) {

                        event.reply(
                                "❌ Não consegui carregar a música.\n" +
                                "Erro: `" +
                                exception.getMessage() +
                                "`"
                        ).setEphemeral(true).queue();

                        exception.printStackTrace();
                    }
                }
        );
    }

    public static void stop(
            Guild guild
    ) {

        GuildMusicManager manager =
                MUSIC_MANAGERS.get(
                        guild.getIdLong()
                );

        if (manager != null) {

            manager.getScheduler()
                    .getPlayer()
                    .stopTrack();

            manager.getScheduler()
                    .clearQueue();

            guild.getAudioManager()
                    .closeAudioConnection();
        }
    }

    public static boolean pause(
            Guild guild
    ) {

        GuildMusicManager manager =
                MUSIC_MANAGERS.get(
                        guild.getIdLong()
                );

        if (manager == null) {
            return false;
        }

        manager.getScheduler()
                .getPlayer()
                .setPaused(true);

        return true;
    }

    public static boolean resume(
            Guild guild
    ) {

        GuildMusicManager manager =
                MUSIC_MANAGERS.get(
                        guild.getIdLong()
                );

        if (manager == null) {
            return false;
        }

        manager.getScheduler()
                .getPlayer()
                .setPaused(false);

        return true;
    }
}
package br.blackout.bot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import dev.lavalink.youtube.YoutubeAudioSourceManager;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MusicManager {

```
private static final AudioPlayerManager PLAYER_MANAGER =
        new DefaultAudioPlayerManager();

private static final Map<Long, GuildMusicManager> MUSIC_MANAGERS =
        new ConcurrentHashMap<>();

static {

    YoutubeAudioSourceManager youtube =
            new YoutubeAudioSourceManager();

    PLAYER_MANAGER.registerSourceManager(youtube);
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
                "❌ Você precisa estar em um canal de voz."
        ).setEphemeral(true).queue();

        return;
    }

    if (link == null || link.isBlank()) {

        event.reply(
                "❌ Você precisa colocar o link da música."
        ).setEphemeral(true).queue();

        return;
    }

    GuildMusicManager manager =
            getGuildMusicManager(guild);

    /*
     * Responde imediatamente ao Discord.
     * O carregamento do YouTube pode demorar mais
     * de 3 segundos.
     */
    event.deferReply().queue();

    System.out.println(
            "🎵 Carregando música: " + link
    );

    PLAYER_MANAGER.loadItemOrdered(
            manager,
            link,
            new AudioLoadResultHandler() {

                @Override
                public void trackLoaded(
                        AudioTrack track
                ) {

                    try {

                        /*
                         * Só entra no canal depois que
                         * a música foi carregada.
                         */
                        guild.getAudioManager()
                                .openAudioConnection(channel);

                        manager.getScheduler()
                                .queue(track);

                        event.getHook()
                                .editOriginal(
                                        "🎵 **Música carregada!**\n\n" +
                                        "▶️ `" +
                                        track.getInfo().title +
                                        "`"
                                )
                                .queue();

                        System.out.println(
                                "▶️ Tocando: " +
                                track.getInfo().title
                        );

                    } catch (Exception e) {

                        e.printStackTrace();

                        event.getHook()
                                .editOriginal(
                                        "❌ Não consegui entrar no canal de voz."
                                )
                                .queue();
                    }
                }

                @Override
                public void playlistLoaded(
                        AudioPlaylist playlist
                ) {

                    if (
                            playlist == null ||
                            playlist.getTracks().isEmpty()
                    ) {

                        event.getHook()
                                .editOriginal(
                                        "❌ Nenhuma música encontrada."
                                )
                                .queue();

                        return;
                    }

                    AudioTrack track =
                            playlist.getTracks().get(0);

                    try {

                        guild.getAudioManager()
                                .openAudioConnection(channel);

                        manager.getScheduler()
                                .queue(track);

                        event.getHook()
                                .editOriginal(
                                        "🎵 **Música carregada!**\n\n" +
                                        "▶️ `" +
                                        track.getInfo().title +
                                        "`"
                                )
                                .queue();

                        System.out.println(
                                "▶️ Tocando: " +
                                track.getInfo().title
                        );

                    } catch (Exception e) {

                        e.printStackTrace();

                        event.getHook()
                                .editOriginal(
                                        "❌ Não consegui entrar no canal de voz."
                                )
                                .queue();
                    }
                }

                @Override
                public void noMatches() {

                    event.getHook()
                            .editOriginal(
                                    "❌ Não encontrei nenhuma música nesse link."
                            )
                            .queue();
                }

                @Override
                public void loadFailed(
                        FriendlyException exception
                ) {

                    System.err.println(
                            "❌ Erro ao carregar música:"
                    );

                    exception.printStackTrace();

                    String mensagem =
                            exception.getMessage();

                    if (
                            mensagem == null ||
                            mensagem.isBlank()
                    ) {
                        mensagem =
                                "Erro desconhecido.";
                    }

                    event.getHook()
                            .editOriginal(
                                    "❌ Não consegui carregar a música.\n" +
                                    "Erro: `" +
                                    mensagem +
                                    "`"
                            )
                            .queue();
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
    }

    if (
            guild.getAudioManager()
                    .isConnected()
    ) {

        guild.getAudioManager()
                .closeAudioConnection();
    }

    System.out.println(
            "⏹ Música parada no servidor: " +
            guild.getName()
    );
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

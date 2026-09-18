package br.blackout.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class BotListener extends ListenerAdapter {

    private final Random random = new Random();

    @Override
    public void onSlashCommandInteraction(
            SlashCommandInteractionEvent event
    ) {

        switch (event.getName()) {

            case "spin":
                handleRoll(event);
                break;

            case "perfil":
                handlePerfil(event);
                break;

            case "giros":
                handleGiros(event);
                break;

            case "reino":
                handleReino(event);
                break;

            case "classe":
                handleClasse(event);
                break;

            case "subclasse":
                handleSubclasse(event);
                break;

            case "mute":
                handleMute(event);
                break;

            case "ban":
                handleBan(event);
                break;

            case "bantemp":
                handleBanTemp(event);
                break;

            case "music":
                handleMusic(event);
                break;

            case "music-stop":
                handleMusicStop(event);
                break;

            case "music-pause":
                handleMusicPause(event);
                break;

            case "music-resume":
                handleMusicResume(event);
                break;

            default:
                break;
        }
    }

    // =========================================================
    // SPIN
    // =========================================================

    private void handleRoll(
            SlashCommandInteractionEvent event
    ) {

        String tipo =
                event.getOption("tipo")
                        .getAsString();

        tipo = normalize(tipo);

        switch (tipo) {

            case "reino":
                rollReino(event);
                break;

            case "classe":
                rollClasse(event);
                break;

            case "subclasse":
                rollSubclasse(event);
                break;

            case "completo":
                rollCompleto(event);
                break;

            default:

                event.reply(
                        "❌ Tipo inválido.\n\n" +
                        "Use: `reino`, `classe`, `subclasse` ou `completo`."
                )
                .setEphemeral(true)
                .queue();
        }
    }

    private void rollReino(
            SlashCommandInteractionEvent event
    ) {

        if (BotData.REINOS.isEmpty()) {

            event.reply(
                    "❌ Não existem reinos cadastrados."
            ).setEphemeral(true).queue();

            return;
        }

        BotData.Profile profile =
                DataManager.getOrCreateProfile(
                        event.getUser().getIdLong()
                );

        if (profile.girosReino <= 0) {

            event.reply(
                    "❌ Você não possui giros de reino."
            ).setEphemeral(true).queue();

            return;
        }

        String reino =
                randomFrom(BotData.REINOS);

        profile.reino = reino;
        profile.girosReino--;

        DataManager.save();

        event.reply(
                "👑 **REINO DEFINIDO!**\n\n" +
                "🏰 **" + reino + "**\n\n" +
                "Giros restantes: **" +
                profile.girosReino +
                "**"
        ).queue();
    }

    private void rollClasse(
            SlashCommandInteractionEvent event
    ) {

        if (BotData.CLASSES.isEmpty()) {

            event.reply(
                    "❌ Não existem classes cadastradas."
            ).setEphemeral(true).queue();

            return;
        }

        BotData.Profile profile =
                DataManager.getOrCreateProfile(
                        event.getUser().getIdLong()
                );

        if (profile.girosClasse <= 0) {

            event.reply(
                    "❌ Você não possui giros de classe."
            ).setEphemeral(true).queue();

            return;
        }

        String classe =
                randomFrom(BotData.CLASSES);

        profile.classe = classe;
        profile.girosClasse--;

        DataManager.save();

        event.reply(
                "⚔️ **CLASSE DEFINIDA!**\n\n" +
                "🛡️ **" + classe + "**\n\n" +
                "Giros restantes: **" +
                profile.girosClasse +
                "**"
        ).queue();
    }

    private void rollSubclasse(
            SlashCommandInteractionEvent event
    ) {

        if (BotData.SUBCLASSES.isEmpty()) {

            event.reply(
                    "❌ Não existem subclasses cadastradas."
            ).setEphemeral(true).queue();

            return;
        }

        BotData.Profile profile =
                DataManager.getOrCreateProfile(
                        event.getUser().getIdLong()
                );

        if (profile.girosSubclasse <= 0) {

            event.reply(
                    "❌ Você não possui giros de subclasse."
            ).setEphemeral(true).queue();

            return;
        }

        String subclasse =
                randomFrom(BotData.SUBCLASSES);

        profile.subclasse = subclasse;
        profile.girosSubclasse--;

        DataManager.save();

        event.reply(
                "🔮 **SUBCLASSE DEFINIDA!**\n\n" +
                "✨ **" + subclasse + "**\n\n" +
                "Giros restantes: **" +
                profile.girosSubclasse +
                "**"
        ).queue();
    }

    private void rollCompleto(
            SlashCommandInteractionEvent event
    ) {

        if (
                BotData.REINOS.isEmpty() ||
                BotData.CLASSES.isEmpty() ||
                BotData.SUBCLASSES.isEmpty()
        ) {

            event.reply(
                    "❌ É necessário ter reino, classe e subclasse cadastrados."
            ).setEphemeral(true).queue();

            return;
        }

        BotData.Profile profile =
                DataManager.getOrCreateProfile(
                        event.getUser().getIdLong()
                );

        if (
                profile.girosReino <= 0 ||
                profile.girosClasse <= 0 ||
                profile.girosSubclasse <= 0
        ) {

            event.reply(
                    "❌ Você não possui giros suficientes."
            ).setEphemeral(true).queue();

            return;
        }

        profile.reino =
                randomFrom(BotData.REINOS);

        profile.classe =
                randomFrom(BotData.CLASSES);

        profile.subclasse =
                randomFrom(BotData.SUBCLASSES);

        profile.girosReino--;
        profile.girosClasse--;
        profile.girosSubclasse--;

        DataManager.save();

        EmbedBuilder embed =
                new EmbedBuilder()
                        .setTitle(
                                "⚔️ SEU DESTINO FOI DEFINIDO"
                        )
                        .setColor(Color.ORANGE)
                        .addField(
                                "👑 Reino",
                                profile.reino,
                                false
                        )
                        .addField(
                                "⚔️ Classe",
                                profile.classe,
                                false
                        )
                        .addField(
                                "🔮 Subclasse",
                                profile.subclasse,
                                false
                        )
                        .addField(
                                "🎲 Giros restantes",
                                "Reino: `" +
                                        profile.girosReino +
                                        "`\nClasse: `" +
                                        profile.girosClasse +
                                        "`\nSubclasse: `" +
                                        profile.girosSubclasse +
                                        "`",
                                false
                        );

        event.replyEmbeds(
                embed.build()
        ).queue();
    }

    // =========================================================
    // PERFIL
    // =========================================================

    private void handlePerfil(
            SlashCommandInteractionEvent event
    ) {

        User user;

        if (event.getOption("usuario") != null) {

            user =
                    event.getOption("usuario")
                            .getAsUser();

        } else {

            user =
                    event.getUser();
        }

        BotData.Profile profile =
                DataManager.getProfile(
                        user.getIdLong()
                );

        if (profile == null) {

            event.reply(
                    "❌ Esse jogador ainda não possui um perfil."
            ).setEphemeral(true).queue();

            return;
        }

        EmbedBuilder embed =
                new EmbedBuilder()
                        .setTitle(
                                "📜 Perfil de " +
                                        user.getName()
                        )
                        .setColor(Color.CYAN)
                        .addField(
                                "👑 Reino",
                                profile.reino.isBlank()
                                        ? "Não definido"
                                        : profile.reino,
                                false
                        )
                        .addField(
                                "⚔️ Classe",
                                profile.classe.isBlank()
                                        ? "Não definida"
                                        : profile.classe,
                                false
                        )
                        .addField(
                                "🔮 Subclasse",
                                profile.subclasse.isBlank()
                                        ? "Não definida"
                                        : profile.subclasse,
                                false
                        )
                        .addField(
                                "🎲 Giros",
                                "Reino: `" +
                                        profile.girosReino +
                                        "`\nClasse: `" +
                                        profile.girosClasse +
                                        "`\nSubclasse: `" +
                                        profile.girosSubclasse +
                                        "`",
                                false
                        );

        event.replyEmbeds(
                embed.build()
        ).queue();
    }

    // =========================================================
    // GIROS
    // =========================================================

    private void handleGiros(
            SlashCommandInteractionEvent event
    ) {

        if (!isAdmin(event)) {

            event.reply(
                    "❌ Você não possui permissão."
            ).setEphemeral(true).queue();

            return;
        }

        User user =
                event.getOption("usuario")
                        .getAsUser();

        String tipo =
                normalize(
                        event.getOption("tipo")
                                .getAsString()
                );

        int quantidade =
                event.getOption("quantidade")
                        .getAsInt();

        if (quantidade < 0 || quantidade > 1000) {

            event.reply(
                    "❌ A quantidade deve estar entre 0 e 1000."
            ).setEphemeral(true).queue();

            return;
        }

        BotData.Profile profile =
                DataManager.getOrCreateProfile(
                        user.getIdLong()
                );

        switch (tipo) {

            case "reino":
                profile.girosReino = quantidade;
                break;

            case "classe":
                profile.girosClasse = quantidade;
                break;

            case "subclasse":
                profile.girosSubclasse = quantidade;
                break;

            case "todos":
                profile.girosReino = quantidade;
                profile.girosClasse = quantidade;
                profile.girosSubclasse = quantidade;
                break;

            default:

                event.reply(
                        "❌ Tipo inválido."
                ).setEphemeral(true).queue();

                return;
        }

        DataManager.save();

        event.reply(
                "✅ Giros de " +
                        user.getAsMention() +
                        " atualizados.\n\n" +
                        "👑 Reino: `" +
                        profile.girosReino +
                        "`\n" +
                        "⚔️ Classe: `" +
                        profile.girosClasse +
                        "`\n" +
                        "🔮 Subclasse: `" +
                        profile.girosSubclasse +
                        "`"
        ).setEphemeral(true).queue();
    }

    // =========================================================
    // REINO
    // =========================================================

    private void handleReino(
            SlashCommandInteractionEvent event
    ) {

        if (!isAdmin(event)) {

            event.reply(
                    "❌ Você não possui permissão."
            ).setEphemeral(true).queue();

            return;
        }

        String sub =
                event.getSubcommandName();

        if ("adicionar".equals(sub)) {

            String nome =
                    event.getOption("nome")
                            .getAsString()
                            .trim();

            for (String reino : BotData.REINOS) {

                if (reino.equalsIgnoreCase(nome)) {

                    event.reply(
                            "❌ Esse reino já existe."
                    ).setEphemeral(true).queue();

                    return;
                }
            }

            BotData.REINOS.add(nome);

            DataManager.save();

            event.reply(
                    "✅ Reino **" +
                            nome +
                            "** adicionado."
            ).queue();

            return;
        }

        if ("remover".equals(sub)) {

            String nome =
                    event.getOption("nome")
                            .getAsString()
                            .trim();

            boolean removed =
                    BotData.REINOS.removeIf(
                            r ->
                                    r.equalsIgnoreCase(nome)
                    );

            if (!removed) {

                event.reply(
                        "❌ Reino não encontrado."
                ).setEphemeral(true).queue();

                return;
            }

            DataManager.save();

            event.reply(
                    "✅ Reino **" +
                            nome +
                            "** removido."
            ).queue();

            return;
        }

        if ("listar".equals(sub)) {

            if (BotData.REINOS.isEmpty()) {

                event.reply(
                        "❌ Nenhum reino cadastrado."
                ).queue();

                return;
            }

            StringBuilder lista =
                    new StringBuilder();

            for (String reino : BotData.REINOS) {

                lista.append("👑 ")
                        .append(reino)
                        .append("\n");
            }

            event.reply(
                    "🏰 **REINOS CADASTRADOS**\n\n" +
                            lista
            ).queue();
        }
    }

    // =========================================================
    // CLASSE
    // =========================================================

    private void handleClasse(
            SlashCommandInteractionEvent event
    ) {

        if (!isAdmin(event)) {

            event.reply(
                    "❌ Você não possui permissão."
            ).setEphemeral(true).queue();

            return;
        }

        String sub =
                event.getSubcommandName();

        if ("adicionar".equals(sub)) {

            String nome =
                    event.getOption("nome")
                            .getAsString()
                            .trim();

            for (String classe : BotData.CLASSES) {

                if (classe.equalsIgnoreCase(nome)) {

                    event.reply(
                            "❌ Essa classe já existe."
                    ).setEphemeral(true).queue();

                    return;
                }
            }

            BotData.CLASSES.add(nome);

            DataManager.save();

            event.reply(
                    "✅ Classe **" +
                            nome +
                            "** adicionada."
            ).queue();

            return;
        }

        if ("remover".equals(sub)) {

            String nome =
                    event.getOption("nome")
                            .getAsString()
                            .trim();

            boolean removed =
                    BotData.CLASSES.removeIf(
                            c ->
                                    c.equalsIgnoreCase(nome)
                    );

            if (!removed) {

                event.reply(
                        "❌ Classe não encontrada."
                ).setEphemeral(true).queue();

                return;
            }

            DataManager.save();

            event.reply(
                    "✅ Classe **" +
                            nome +
                            "** removida."
            ).queue();

            return;
        }

        if ("listar".equals(sub)) {

            if (BotData.CLASSES.isEmpty()) {

                event.reply(
                        "❌ Nenhuma classe cadastrada."
                ).queue();

                return;
            }

            StringBuilder lista =
                    new StringBuilder();

            for (String classe : BotData.CLASSES) {

                lista.append("⚔️ ")
                        .append(classe)
                        .append("\n");
            }

            event.reply(
                    "⚔️ **CLASSES CADASTRADAS**\n\n" +
                            lista
            ).queue();
        }
    }

    // =========================================================
    // SUBCLASSE
    // =========================================================

    private void handleSubclasse(
            SlashCommandInteractionEvent event
    ) {

        if (!isAdmin(event)) {

            event.reply(
                    "❌ Você não possui permissão."
            ).setEphemeral(true).queue();

            return;
        }

        String sub =
                event.getSubcommandName();

        if ("adicionar".equals(sub)) {

            String nome =
                    event.getOption("nome")
                            .getAsString()
                            .trim();

            for (String classe : BotData.SUBCLASSES) {

                if (classe.equalsIgnoreCase(nome)) {

                    event.reply(
                            "❌ Essa subclasse já existe."
                    ).setEphemeral(true).queue();

                    return;
                }
            }

            BotData.SUBCLASSES.add(nome);

            DataManager.save();

            event.reply(
                    "✅ Subclasse **" +
                            nome +
                            "** adicionada."
            ).queue();

            return;
        }

        if ("remover".equals(sub)) {

            String nome =
                    event.getOption("nome")
                            .getAsString()
                            .trim();

            boolean removed =
                    BotData.SUBCLASSES.removeIf(
                            c ->
                                    c.equalsIgnoreCase(nome)
                    );

            if (!removed) {

                event.reply(
                        "❌ Subclasse não encontrada."
                ).setEphemeral(true).queue();

                return;
            }

            DataManager.save();

            event.reply(
                    "✅ Subclasse **" +
                            nome +
                            "** removida."
            ).queue();

            return;
        }

        if ("listar".equals(sub)) {

            if (BotData.SUBCLASSES.isEmpty()) {

                event.reply(
                        "❌ Nenhuma subclasse cadastrada."
                ).queue();

                return;
            }

            StringBuilder lista =
                    new StringBuilder();

            for (String classe : BotData.SUBCLASSES) {

                lista.append("🔮 ")
                        .append(classe)
                        .append("\n");
            }

            event.reply(
                    "🔮 **SUBCLASSES CADASTRADAS**\n\n" +
                            lista
            ).queue();
        }
    }

    // =========================================================
    // MUTE
    // =========================================================

    private void handleMute(
            SlashCommandInteractionEvent event
    ) {

        if (!isAdmin(event)) {

            event.reply(
                    "❌ Você não possui permissão."
            ).setEphemeral(true).queue();

            return;
        }

        Member alvo =
                event.getOption("usuario")
                        .getAsMember();

        if (alvo == null) {

            event.reply(
                    "❌ Membro não encontrado."
            ).setEphemeral(true).queue();

            return;
        }

        if (!event.getGuild()
                .getSelfMember()
                .hasPermission(
                        Permission.MODERATE_MEMBERS
                )) {

            event.reply(
                    "❌ Preciso da permissão **Moderar Membros**."
            ).setEphemeral(true).queue();

            return;
        }

        alvo.timeoutFor(
                10,
                java.util.concurrent.TimeUnit.MINUTES
        )
        .reason(
                "Mute por " +
                        event.getUser().getAsTag()
        )
        .queue(

                success ->
                        event.reply(
                                "🔇 " +
                                        alvo.getAsMention() +
                                        " foi mutado por **10 minutos**."
                        ).queue(),

                error ->
                        event.reply(
                                "❌ Não consegui mutar. " +
                                        "Verifique a hierarquia do meu cargo."
                        )
                        .setEphemeral(true)
                        .queue()
        );
    }

    // =========================================================
    // BAN
    // =========================================================

    private void handleBan(
            SlashCommandInteractionEvent event
    ) {

        if (!isAdmin(event)) {

            event.reply(
                    "❌ Você não possui permissão."
            ).setEphemeral(true).queue();

            return;
        }

        Member alvo =
                event.getOption("usuario")
                        .getAsMember();

        if (alvo == null) {

            event.reply(
                    "❌ Membro não encontrado."
            ).setEphemeral(true).queue();

            return;
        }

        if (!event.getGuild()
                .getSelfMember()
                .hasPermission(
                        Permission.BAN_MEMBERS
                )) {

            event.reply(
                    "❌ Preciso da permissão **Banir Membros**."
            ).setEphemeral(true).queue();

            return;
        }

        event.getGuild()
                .ban(
                        alvo.getUser(),
                        0,
                        java.util.concurrent.TimeUnit.SECONDS
                )
                .queue(

                        success ->
                                event.reply(
                                        "🔨 " +
                                                alvo.getAsMention() +
                                                " foi banido."
                                ).queue(),

                        error ->
                                event.reply(
                                        "❌ Não consegui banir. " +
                                                "Verifique a hierarquia do meu cargo."
                                )
                                .setEphemeral(true)
                                .queue()
                );
    }

    // =========================================================
    // BAN TEMPORÁRIO
    // =========================================================

    private void handleBanTemp(
            SlashCommandInteractionEvent event
    ) {

        if (!isAdmin(event)) {

            event.reply(
                    "❌ Você não possui permissão."
            ).setEphemeral(true).queue();

            return;
        }

        Member alvo =
                event.getOption("usuario")
                        .getAsMember();

        int minutos =
                event.getOption("minutos")
                        .getAsInt();

        if (alvo == null) {

            event.reply(
                    "❌ Membro não encontrado."
            ).setEphemeral(true).queue();

            return;
        }

        if (minutos < 1 || minutos > 40320) {

            event.reply(
                    "❌ O tempo deve ser entre 1 minuto e 28 dias."
            ).setEphemeral(true).queue();

            return;
        }

        if (!event.getGuild()
                .getSelfMember()
                .hasPermission(
                        Permission.BAN_MEMBERS
                )) {

            event.reply(
                    "❌ Preciso da permissão **Banir Membros**."
            ).setEphemeral(true).queue();

            return;
        }

        String userId =
                alvo.getId();

        event.getGuild()
                .ban(
                        alvo.getUser(),
                        0,
                        java.util.concurrent.TimeUnit.SECONDS
                )
                .queue(

                        success -> {

                            event.reply(
                                    "🔨 " +
                                            alvo.getAsMention() +
                                            " foi banido por **" +
                                            minutos +
                                            " minutos**."
                            ).queue();

                            Thread thread =
                                    new Thread(() -> {

                                        try {

                                            Thread.sleep(
                                                    minutos * 60L * 1000L
                                            );

                                            event.getGuild()
                                                    .unban(
                                                            net.dv8tion.jda.api.entities.UserSnowflake
                                                                    .fromId(userId)
                                                    )
                                                    .queue(
                                                            ok ->
                                                                    System.out.println(
                                                                            "Ban temporário removido de " +
                                                                                    userId
                                                                    ),
                                                            error ->
                                                                    System.err.println(
                                                                            "Não consegui remover o ban temporário."
                                                                    )
                                                    );

                                        } catch (
                                                InterruptedException e
                                        ) {

                                            Thread.currentThread()
                                                    .interrupt();
                                        }

                                    });

                            thread.setDaemon(true);
                            thread.start();
                        },

                        error ->
                                event.reply(
                                        "❌ Não consegui aplicar o ban temporário."
                                )
                                .setEphemeral(true)
                                .queue()
                );
    }

    // =========================================================
    // MUSIC
    // =========================================================

    private void handleMusic(
            SlashCommandInteractionEvent event
    ) {

        Member member =
                event.getMember();

        if (member == null ||
                member.getVoiceState() == null ||
                !member.getVoiceState().inAudioChannel()) {

            event.reply(
                    "❌ Você precisa estar em uma call primeiro."
            ).setEphemeral(true).queue();

            return;
        }

        String link =
                event.getOption("link")
                        .getAsString()
                        .trim();

        MusicManager.play(
                event.getGuild(),
                member.getVoiceState()
                        .getChannel(),
                link,
                event
        );
    }

    private void handleMusicStop(
            SlashCommandInteractionEvent event
    ) {

        MusicManager.stop(
                event.getGuild()
        );

        event.reply(
                "⏹️ Música parada."
        ).queue();
    }

    private void handleMusicPause(
            SlashCommandInteractionEvent event
    ) {

        if (MusicManager.pause(event.getGuild())) {

            event.reply(
                    "⏸️ Música pausada."
            ).queue();

        } else {

            event.reply(
                    "❌ Não há música tocando."
            ).setEphemeral(true).queue();
        }
    }

    private void handleMusicResume(
            SlashCommandInteractionEvent event
    ) {

        if (MusicManager.resume(event.getGuild())) {

            event.reply(
                    "▶️ Música retomada."
            ).queue();

        } else {

            event.reply(
                    "❌ Não há música pausada."
            ).setEphemeral(true).queue();
        }
    }

    // =========================================================
    // ADMIN
    // =========================================================

    private boolean isAdmin(
            SlashCommandInteractionEvent event
    ) {

        Member member =
                event.getMember();

        if (member == null) {
            return false;
        }

        if (
                event.getUser().getIdLong()
                        ==
                        BotData.SUPER_USER_ID
        ) {

            return true;
        }

        return member.getRoles()
                .stream()
                .anyMatch(
                        role ->
                                BotData.ADMIN_ROLE_IDS
                                        .contains(
                                                role.getIdLong()
                                        )
                );
    }

    // =========================================================
    // UTIL
    // =========================================================

    private String randomFrom(
            List<String> lista
    ) {

        return lista.get(
                random.nextInt(
                        lista.size()
                )
        );
    }

    private String normalize(
            String text
    ) {

        return Normalizer
                .normalize(
                        text,
                        Normalizer.Form.NFD
                )
                .replaceAll(
                        "\\p{M}",
                        ""
                )
                .toLowerCase(
                        Locale.ROOT
                )
                .trim();
    }
}
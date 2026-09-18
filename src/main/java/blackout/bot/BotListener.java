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

    private final Random random =
            new Random();


    @Override
    public void onSlashCommandInteraction(
            SlashCommandInteractionEvent event
    ) {

        switch (event.getName()) {

            case "roll" ->
                    handleRoll(event);

            case "perfil" ->
                    handlePerfil(event);

            case "giros" ->
                    handleGiros(event);
        }
    }


    // ==================================================
    // ROLL
    // ==================================================

    private void handleRoll(
            SlashCommandInteractionEvent event
    ) {

        String tipo =
                normalize(
                        event
                                .getOption("tipo")
                                .getAsString()
                );


        switch (tipo) {

            case "reino" ->
                    rollReino(event);

            case "classe" ->
                    rollClasse(event);

            case "subclasse" ->
                    rollSubclasse(event);

            case "completo" ->
                    rollCompleto(event);

            default ->

                    event.reply(
                            "❌ Tipo inválido.\n\n" +
                            "Use:\n" +
                            "`reino`\n" +
                            "`classe`\n" +
                            "`subclasse`\n" +
                            "`completo`"
                    )

                    .setEphemeral(true)

                    .queue();
        }
    }


    // ==================================================
    // ROLL REINO
    // ==================================================

    private void rollReino(
            SlashCommandInteractionEvent event
    ) {

        if (BotData.REINOS.isEmpty()) {

            event.reply(
                    "❌ Nenhum reino foi configurado ainda."
            )

            .setEphemeral(true)

            .queue();

            return;
        }


        BotData.Profile profile =
                DataManager.getOrCreateProfile(
                        event
                                .getUser()
                                .getIdLong()
                );


        if (profile.girosReino <= 0) {

            event.reply(
                    "❌ Você não possui mais giros de Reino."
            )

            .setEphemeral(true)

            .queue();

            return;
        }


        profile.reino =
                randomFrom(
                        BotData.REINOS
                );


        profile.girosReino--;


        DataManager.save();


        event.reply(

                "👑 **REINO DEFINIDO!**\n\n" +

                "🏰 Reino: **" +
                profile.reino +
                "**\n\n" +

                "🎲 Giros restantes: **" +
                profile.girosReino +
                "**"

        ).queue();
    }


    // ==================================================
    // ROLL CLASSE
    // ==================================================

    private void rollClasse(
            SlashCommandInteractionEvent event
    ) {

        if (BotData.CLASSES.isEmpty()) {

            event.reply(
                    "❌ Nenhuma classe foi configurada ainda."
            )

            .setEphemeral(true)

            .queue();

            return;
        }


        BotData.Profile profile =
                DataManager.getOrCreateProfile(
                        event
                                .getUser()
                                .getIdLong()
                );


        if (profile.girosClasse <= 0) {

            event.reply(
                    "❌ Você não possui mais giros de Classe."
            )

            .setEphemeral(true)

            .queue();

            return;
        }


        profile.classe =
                randomFrom(
                        BotData.CLASSES
                );


        profile.girosClasse--;


        DataManager.save();


        event.reply(

                "⚔️ **CLASSE DEFINIDA!**\n\n" +

                "⚔️ Classe: **" +
                profile.classe +
                "**\n\n" +

                "🎲 Giros restantes: **" +
                profile.girosClasse +
                "**"

        ).queue();
    }


    // ==================================================
    // ROLL SUBCLASSE
    // ==================================================

    private void rollSubclasse(
            SlashCommandInteractionEvent event
    ) {

        if (BotData.SUBCLASSES.isEmpty()) {

            event.reply(
                    "❌ Nenhuma subclasse foi configurada ainda."
            )

            .setEphemeral(true)

            .queue();

            return;
        }


        BotData.Profile profile =
                DataManager.getOrCreateProfile(
                        event
                                .getUser()
                                .getIdLong()
                );


        if (profile.girosSubclasse <= 0) {

            event.reply(
                    "❌ Você não possui mais giros de Subclasse."
            )

            .setEphemeral(true)

            .queue();

            return;
        }


        profile.subclasse =
                randomFrom(
                        BotData.SUBCLASSES
                );


        profile.girosSubclasse--;


        DataManager.save();


        event.reply(

                "🛡️ **SUBCLASSE DEFINIDA!**\n\n" +

                "🛡️ Subclasse: **" +
                profile.subclasse +
                "**\n\n" +

                "🎲 Giros restantes: **" +
                profile.girosSubclasse +
                "**"

        ).queue();
    }


    // ==================================================
    // ROLL COMPLETO
    // ==================================================

    private void rollCompleto(
            SlashCommandInteractionEvent event
    ) {

        if (
                BotData.REINOS.isEmpty() ||
                BotData.CLASSES.isEmpty() ||
                BotData.SUBCLASSES.isEmpty()
        ) {

            event.reply(
                    "❌ Configure pelo menos um item em " +
                    "Reino, Classe e Subclasse antes de usar o modo completo."
            )

            .setEphemeral(true)

            .queue();

            return;
        }


        BotData.Profile profile =
                DataManager.getOrCreateProfile(
                        event
                                .getUser()
                                .getIdLong()
                );


        if (
                profile.girosReino <= 0 ||
                profile.girosClasse <= 0 ||
                profile.girosSubclasse <= 0
        ) {

            event.reply(
                    "❌ Você precisa ter pelo menos 1 giro " +
                    "disponível em Reino, Classe e Subclasse."
            )

            .setEphemeral(true)

            .queue();

            return;
        }


        profile.reino =
                randomFrom(
                        BotData.REINOS
                );


        profile.classe =
                randomFrom(
                        BotData.CLASSES
                );


        profile.subclasse =
                randomFrom(
                        BotData.SUBCLASSES
                );


        profile.girosReino--;

        profile.girosClasse--;

        profile.girosSubclasse--;


        DataManager.save();


        EmbedBuilder embed =

                new EmbedBuilder()

                        .setTitle(
                                "⚔️ SEU DESTINO FOI DEFINIDO"
                        )

                        .setColor(
                                new Color(
                                        139,
                                        90,
                                        43
                                )
                        )

                        .setDescription(
                                event
                                        .getUser()
                                        .getAsMention() +
                                " teve seu personagem definido!"
                        )

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
                                "🛡️ Subclasse",
                                profile.subclasse,
                                false
                        )

                        .addField(

                                "🎲 Giros restantes",

                                "👑 Reino: **" +
                                profile.girosReino +
                                "**\n" +

                                "⚔️ Classe: **" +
                                profile.girosClasse +
                                "**\n" +

                                "🛡️ Subclasse: **" +
                                profile.girosSubclasse +
                                "**",

                                false
                        );


        event.replyEmbeds(
                embed.build()
        ).queue();
    }


    // ==================================================
    // PERFIL
    // ==================================================

    private void handlePerfil(
            SlashCommandInteractionEvent event
    ) {

        User target =
                event.getUser();


        if (
                event.getOption("usuario") != null
        ) {

            target =
                    event
                            .getOption("usuario")
                            .getAsUser();
        }


        BotData.Profile profile =
                DataManager.getProfile(
                        target.getIdLong()
                );


        if (profile == null) {

            event.reply(
                    "❌ " +
                    target.getAsMention() +
                    " ainda não possui um perfil."
            ).queue();

            return;
        }


        String reino =

                profile.reino.isEmpty()

                        ? "Não definido"

                        : profile.reino;


        String classe =

                profile.classe.isEmpty()

                        ? "Não definida"

                        : profile.classe;


        String subclasse =

                profile.subclasse.isEmpty()

                        ? "Não definida"

                        : profile.subclasse;


        EmbedBuilder embed =

                new EmbedBuilder()

                        .setTitle(
                                "📜 PERFIL MEDIEVAL"
                        )

                        .setColor(
                                new Color(
                                        139,
                                        90,
                                        43
                                )
                        )

                        .setThumbnail(
                                target
                                        .getEffectiveAvatarUrl()
                        )

                        .setDescription(
                                "Perfil de " +
                                target.getAsMention()
                        )

                        .addField(
                                "👑 Reino",
                                reino,
                                false
                        )

                        .addField(
                                "⚔️ Classe",
                                classe,
                                false
                        )

                        .addField(
                                "🛡️ Subclasse",
                                subclasse,
                                false
                        )

                        .addField(

                                "🎲 Giros",

                                "👑 Reino: **" +
                                profile.girosReino +
                                "**\n" +

                                "⚔️ Classe: **" +
                                profile.girosClasse +
                                "**\n" +

                                "🛡️ Subclasse: **" +
                                profile.girosSubclasse +
                                "**",

                                false
                        );


        event.replyEmbeds(
                embed.build()
        ).queue();
    }


    // ==================================================
    // GIROS
    // ==================================================

    private void handleGiros(
            SlashCommandInteractionEvent event
    ) {

        if (!isAdmin(event)) {

            event.reply(
                    "❌ Você não possui permissão para usar esse comando."
            )

            .setEphemeral(true)

            .queue();

            return;
        }


        User target =
                event
                        .getOption("usuario")
                        .getAsUser();


        String tipo =
                normalize(
                        event
                                .getOption("tipo")
                                .getAsString()
                );


        int quantidade =
                event
                        .getOption("quantidade")
                        .getAsInt();


        if (
                quantidade < 0 ||
                quantidade > 1000
        ) {

            event.reply(
                    "❌ A quantidade deve estar entre 0 e 1000."
            )

            .setEphemeral(true)

            .queue();

            return;
        }


        BotData.Profile profile =
                DataManager.getOrCreateProfile(
                        target.getIdLong()
                );


        switch (tipo) {

            case "reino" ->

                    profile.girosReino =
                            quantidade;


            case "classe" ->

                    profile.girosClasse =
                            quantidade;


            case "subclasse" ->

                    profile.girosSubclasse =
                            quantidade;


            case "todos" -> {

                profile.girosReino =
                        quantidade;

                profile.girosClasse =
                        quantidade;

                profile.girosSubclasse =
                        quantidade;
            }


            default -> {

                event.reply(
                        "❌ Tipo inválido.\n\n" +
                        "Use `reino`, `classe`, " +
                        "`subclasse` ou `todos`."
                )

                .setEphemeral(true)

                .queue();

                return;
            }
        }


        DataManager.save();


        event.reply(

                "✅ Giros de " +
                target.getAsMention() +
                " atualizados.\n\n" +

                "👑 Reino: **" +
                profile.girosReino +
                "**\n" +

                "⚔️ Classe: **" +
                profile.girosClasse +
                "**\n" +

                "🛡️ Subclasse: **" +
                profile.girosSubclasse +
                "**"

        )

        .setEphemeral(true)

        .queue();
    }


    // ==================================================
    // ADMIN
    // ==================================================

    private boolean isAdmin(
            SlashCommandInteractionEvent event
    ) {

        Member member =
                event.getMember();


        if (member == null) {
            return false;
        }


        if (
                event
                        .getUser()
                        .getIdLong()
                        ==
                BotData.SUPER_USER_ID
        ) {

            return true;
        }


        if (
                member.hasPermission(
                        Permission.ADMINISTRATOR
                )
        ) {

            return true;
        }


        return member
                .getRoles()
                .stream()
                .anyMatch(

                        role ->

                                BotData
                                        .ADMIN_ROLE_IDS
                                        .contains(
                                                role.getIdLong()
                                        )
                );
    }


    // ==================================================
    // RANDOM
    // ==================================================

    private String randomFrom(
            List<String> list
    ) {

        return list.get(
                random.nextInt(
                        list.size()
                )
        );
    }


    // ==================================================
    // NORMALIZAR TEXTO
    // ==================================================

    private String normalize(
            String text
    ) {

        String normalized =

                Normalizer.normalize(
                        text,
                        Normalizer.Form.NFD
                );


        return normalized

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
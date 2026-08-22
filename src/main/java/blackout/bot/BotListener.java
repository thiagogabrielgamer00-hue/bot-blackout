package br.blackout.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BotListener extends ListenerAdapter {


    // Sessões de WL
    private final Map<Long, WLSession> wlSessions =
            new ConcurrentHashMap<>();


    // ================================
    // COMANDOS
    // ================================

    @Override
    public void onSlashCommandInteraction(
            SlashCommandInteractionEvent event
    ) {

        switch (event.getName()) {

            case "roll" -> handleRoll(event);

            case "wl" -> handleWLCommand(event);

            case "clan" -> handleClanCommand(event);

            case "raridade" -> handleRaridadeCommand(event);

            case "vila" -> handleVilaCommand(event);

            case "prodigio" -> handleProdigioCommand(event);

        }
    }


    // =====================================================
    // VERIFICAR ADMIN
    // =====================================================

    private boolean isAdmin(
            SlashCommandInteractionEvent event
    ) {

        Member member = event.getMember();

        return member != null &&
                member.hasPermission(
                        Permission.ADMINISTRATOR
                );
    }


    // =====================================================
    // ROLL
    // =====================================================

    private void handleRoll(
            SlashCommandInteractionEvent event
    ) {

        String tipo = event
                .getOption("tipo")
                .getAsString()
                .toLowerCase();


        switch (tipo) {

            case "vila" -> rollVila(event);

            case "cla", "clã" -> rollClan(event);

            case "prodigio", "prodígio" ->
                    rollProdigio(event);

            case "personagem", "completo" ->
                    rollCompleto(event);

            default ->
                    event.reply(
                            "❌ Use: `vila`, `cla`, `prodigio` ou `personagem`."
                    ).setEphemeral(true).queue();

        }
    }


    private void rollVila(
            SlashCommandInteractionEvent event
    ) {

        if (DataManager.data.vilas.isEmpty()) {

            event.reply(
                    "❌ Não existe nenhuma vila configurada."
            ).setEphemeral(true).queue();

            return;
        }


        String vila =
                DataManager.data.vilas.get(
                        new Random().nextInt(
                                DataManager.data.vilas.size()
                        )
                );


        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle("🏯 ROLETA DE VILA");

        embed.setDescription(
                "👤 **Usuário:** "
                        + event.getUser().getAsMention()
                        + "\n\n"
                        + "🏯 **Vila sorteada:** **"
                        + vila
                        + "**\n\n"
                        + "Todas as vilas possuem a mesma chance."
        );


        event.replyEmbeds(
                embed.build()
        ).queue();
    }


    private void rollProdigio(
            SlashCommandInteractionEvent event
    ) {

        boolean prodigio =
                new Random().nextDouble() * 100
                        < DataManager.data.chanceProdigio;


        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle("⭐ ROLETA DE PRODÍGIO");


        if (prodigio) {

            embed.setDescription(
                    "👤 **Usuário:** "
                            + event.getUser().getAsMention()
                            + "\n\n"
                            + "⭐ **RESULTADO: PRODÍGIO!**\n\n"
                            + "🎉 Você teve sorte!"
            );

        } else {

            embed.setDescription(
                    "👤 **Usuário:** "
                            + event.getUser().getAsMention()
                            + "\n\n"
                            + "❌ **RESULTADO: NÃO É PRODÍGIO**\n\n"
                            + "Chance atual: **"
                            + DataManager.data.chanceProdigio
                            + "%**"
            );

        }


        event.replyEmbeds(
                embed.build()
        ).queue();
    }


    private void rollClan(
            SlashCommandInteractionEvent event
    ) {

        if (DataManager.data.clas.isEmpty()) {

            event.reply(
                    "❌ Nenhum clã foi configurado."
            ).setEphemeral(true).queue();

            return;
        }


        String raridade =
                sortearRaridade();


        if (raridade == null) {

            event.reply(
                    "❌ Não foi possível sortear uma raridade."
            ).setEphemeral(true).queue();

            return;
        }


        List<BotData.ClanData> clans =
                new ArrayList<>();


        for (BotData.ClanData clan :
                DataManager.data.clas.values()) {

            if (clan.raridade.equalsIgnoreCase(raridade)) {

                clans.add(clan);

            }
        }


        // Caso a raridade sorteada esteja vazia,
        // procura outra raridade que tenha clãs

        if (clans.isEmpty()) {

            List<BotData.ClanData> todos =
                    new ArrayList<>(
                            DataManager.data.clas.values()
                    );

            BotData.ClanData clan =
                    todos.get(
                            new Random().nextInt(
                                    todos.size()
                            )
                    );

            sendClanResult(event, clan);

            return;
        }


        BotData.ClanData clan =
                clans.get(
                        new Random().nextInt(
                                clans.size()
                        )
                );


        sendClanResult(event, clan);
    }


    private void sendClanResult(
            SlashCommandInteractionEvent event,
            BotData.ClanData clan
    ) {

        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle("🧬 ROLETA DE CLÃ");

        embed.setDescription(
                "👤 **Usuário:** "
                        + event.getUser().getAsMention()
                        + "\n\n"
                        + "🧬 **Clã:** **"
                        + clan.nome
                        + "**\n"
                        + "💎 **Raridade:** **"
                        + clan.raridade
                        + "**\n"
                        + "📊 **Classificação:** **"
                        + clan.classificacao
                        + "**"
        );


        event.replyEmbeds(
                embed.build()
        ).queue();
    }


    private String sortearRaridade() {

        double random =
                new Random().nextDouble() * 100;

        double acumulado = 0;


        for (Map.Entry<String, Double> entry :
                DataManager.data.raridades.entrySet()) {

            acumulado += entry.getValue();

            if (random <= acumulado) {
                return entry.getKey();
            }
        }


        return DataManager.data.raridades
                .keySet()
                .stream()
                .findFirst()
                .orElse(null);
    }


    private void rollCompleto(
            SlashCommandInteractionEvent event
    ) {

        if (DataManager.data.vilas.isEmpty()
                || DataManager.data.clas.isEmpty()) {

            event.reply(
                    "❌ Configure pelo menos uma vila e um clã."
            ).setEphemeral(true).queue();

            return;
        }


        String vila =
                DataManager.data.vilas.get(
                        new Random().nextInt(
                                DataManager.data.vilas.size()
                        )
                );


        List<BotData.ClanData> todos =
                new ArrayList<>(
                        DataManager.data.clas.values()
                );


        BotData.ClanData clan =
                todos.get(
                        new Random().nextInt(
                                todos.size()
                        )
                );


        boolean prodigio =
                new Random().nextDouble() * 100
                        < DataManager.data.chanceProdigio;


        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle("🍥 RESULTADO DA ROLETA NINJA");

        embed.setDescription(
                "👤 **Usuário:** "
                        + event.getUser().getAsMention()
                        + "\n\n"
                        + "🏯 **Vila:** "
                        + vila
                        + "\n"
                        + "🧬 **Clã:** "
                        + clan.nome
                        + "\n"
                        + "💎 **Raridade:** "
                        + clan.raridade
                        + "\n"
                        + "📊 **Classificação:** "
                        + clan.classificacao
                        + "\n"
                        + "⭐ **Prodígio:** "
                        + (prodigio
                        ? "SIM!"
                        : "Não")
        );


        event.replyEmbeds(
                embed.build()
        ).queue();
    }


    // =====================================================
    // PRODÍGIO ADMIN
    // =====================================================

    private void handleProdigioCommand(
            SlashCommandInteractionEvent event
    ) {

        if (!isAdmin(event)) {

            event.reply(
                    "❌ Você não tem permissão."
            ).setEphemeral(true).queue();

            return;
        }


        double chance =
                event.getOption("chance").getAsDouble();


        if (chance < 0 || chance > 100) {

            event.reply(
                    "❌ A chance deve estar entre 0 e 100."
            ).setEphemeral(true).queue();

            return;
        }


        DataManager.data.chanceProdigio =
                chance;

        DataManager.save();


        event.reply(
                "⭐ Chance de prodígio configurada para **"
                        + chance
                        + "%**."
        ).setEphemeral(true).queue();
    }


    // =====================================================
    // VILAS ADMIN
    // =====================================================

    private void handleVilaCommand(
            SlashCommandInteractionEvent event
    ) {

        if (!isAdmin(event)) {

            event.reply(
                    "❌ Você não tem permissão."
            ).setEphemeral(true).queue();

            return;
        }


        String acao =
                event.getOption("acao")
                        .getAsString()
                        .toLowerCase();


        String nome =
                event.getOption("nome") != null
                        ? event.getOption("nome").getAsString()
                        : null;


        switch (acao) {

            case "adicionar" -> {

                if (nome == null) {

                    event.reply(
                            "❌ Informe o nome."
                    ).setEphemeral(true).queue();

                    return;
                }


                DataManager.data.vilas.add(nome);

                DataManager.save();

                event.reply(
                        "🏯 Vila **"
                                + nome
                                + "** adicionada."
                ).setEphemeral(true).queue();
            }


            case "remover" -> {

                if (nome == null) {

                    event.reply(
                            "❌ Informe o nome."
                    ).setEphemeral(true).queue();

                    return;
                }


                boolean removed =
                        DataManager.data.vilas.removeIf(
                                vila ->
                                        vila.equalsIgnoreCase(nome)
                        );


                DataManager.save();


                event.reply(
                        removed
                                ? "🗑️ Vila removida."
                                : "❌ Vila não encontrada."
                ).setEphemeral(true).queue();
            }


            case "listar" -> {

                StringBuilder lista =
                        new StringBuilder();


                for (String vila :
                        DataManager.data.vilas) {

                    lista.append("🏯 ")
                            .append(vila)
                            .append("\n");
                }


                event.reply(
                        lista.isEmpty()
                                ? "Nenhuma vila."
                                : lista.toString()
                ).setEphemeral(true).queue();
            }


            default ->
                    event.reply(
                            "❌ Use: adicionar, remover ou listar."
                    ).setEphemeral(true).queue();

        }
    }


    // =====================================================
    // RARIDADES
    // =====================================================

    private void handleRaridadeCommand(
            SlashCommandInteractionEvent event
    ) {

        if (!isAdmin(event)) {

            event.reply(
                    "❌ Você não tem permissão."
            ).setEphemeral(true).queue();

            return;
        }


        String acao =
                event.getOption("acao")
                        .getAsString()
                        .toLowerCase();


        String nome =
                event.getOption("nome") != null
                        ? event.getOption("nome").getAsString()
                        : null;


        switch (acao) {

            case "configurar" -> {

                if (nome == null
                        || event.getOption("chance") == null) {

                    event.reply(
                            "❌ Informe nome e chance."
                    ).setEphemeral(true).queue();

                    return;
                }


                double chance =
                        event.getOption("chance")
                                .getAsDouble();


                DataManager.data.raridades.put(
                        nome,
                        chance
                );

                DataManager.save();


                event.reply(
                        "💎 Raridade **"
                                + nome
                                + "** configurada com **"
                                + chance
                                + "%**."
                ).setEphemeral(true).queue();
            }


            case "remover" -> {

                if (nome == null) {

                    event.reply(
                            "❌ Informe a raridade."
                    ).setEphemeral(true).queue();

                    return;
                }


                DataManager.data.raridades.remove(nome);

                DataManager.save();


                event.reply(
                        "🗑️ Raridade removida."
                ).setEphemeral(true).queue();
            }


            case "listar" -> {

                StringBuilder lista =
                        new StringBuilder(
                                "💎 **RARIDADES**\n\n"
                        );


                for (Map.Entry<String, Double> entry :
                        DataManager.data.raridades.entrySet()) {

                    lista.append("• **")
                            .append(entry.getKey())
                            .append("** → ")
                            .append(entry.getValue())
                            .append("%\n");
                }


                event.reply(
                        lista.toString()
                ).setEphemeral(true).queue();
            }


            default ->
                    event.reply(
                            "❌ Use: configurar, remover ou listar."
                    ).setEphemeral(true).queue();
        }
    }


    // =====================================================
    // CLÃS
    // =====================================================

    private void handleClanCommand(
            SlashCommandInteractionEvent event
    ) {

        if (!isAdmin(event)) {

            event.reply(
                    "❌ Você não tem permissão."
            ).setEphemeral(true).queue();

            return;
        }


        String acao =
                event.getOption("acao")
                        .getAsString()
                        .toLowerCase();


        String nome =
                event.getOption("nome") != null
                        ? event.getOption("nome").getAsString()
                        : null;


        switch (acao) {

            case "adicionar", "editar" -> {

                if (nome == null
                        || event.getOption("raridade") == null
                        || event.getOption("classificacao") == null) {

                    event.reply(
                            "❌ Informe nome, raridade e classificação."
                    ).setEphemeral(true).queue();

                    return;
                }


                String raridade =
                        event.getOption("raridade")
                                .getAsString();


                String classificacao =
                        event.getOption("classificacao")
                                .getAsString();


                if (!DataManager.data.raridades
                        .containsKey(raridade)) {

                    event.reply(
                            "❌ Essa raridade não existe."
                    ).setEphemeral(true).queue();

                    return;
                }


                BotData.ClanData clan =
                        new BotData.ClanData(
                                nome,
                                raridade,
                                classificacao
                        );


                DataManager.data.clas.put(
                        nome.toLowerCase(),
                        clan
                );

                DataManager.save();


                event.reply(
                        "🧬 Clã **"
                                + nome
                                + "** configurado!\n"
                                + "💎 Raridade: **"
                                + raridade
                                + "**\n"
                                + "📊 Classificação: **"
                                + classificacao
                                + "**"
                ).setEphemeral(true).queue();
            }


            case "remover" -> {

                if (nome == null) {

                    event.reply(
                            "❌ Informe o clã."
                    ).setEphemeral(true).queue();

                    return;
                }


                BotData.ClanData removed =
                        DataManager.data.clas.remove(
                                nome.toLowerCase()
                        );


                DataManager.save();


                event.reply(
                        removed != null
                                ? "🗑️ Clã removido."
                                : "❌ Clã não encontrado."
                ).setEphemeral(true).queue();
            }


            case "info" -> {

                if (nome == null) {

                    event.reply(
                            "❌ Informe o clã."
                    ).setEphemeral(true).queue();

                    return;
                }


                BotData.ClanData clan =
                        DataManager.data.clas.get(
                                nome.toLowerCase()
                        );


                if (clan == null) {

                    event.reply(
                            "❌ Clã não encontrado."
                    ).setEphemeral(true).queue();

                    return;
                }


                event.reply(
                        "🧬 **"
                                + clan.nome
                                + "**\n"
                                + "💎 Raridade: **"
                                + clan.raridade
                                + "**\n"
                                + "📊 Classificação: **"
                                + clan.classificacao
                                + "**"
                ).setEphemeral(true).queue();
            }


            case "listar" -> {

                StringBuilder lista =
                        new StringBuilder(
                                "🧬 **CLÃS CONFIGURADOS**\n\n"
                        );


                for (BotData.ClanData clan :
                        DataManager.data.clas.values()) {

                    lista.append("• **")
                            .append(clan.nome)
                            .append("** | ")
                            .append(clan.raridade)
                            .append(" | ")
                            .append(clan.classificacao)
                            .append("\n");
                }


                event.reply(
                        lista.toString()
                ).setEphemeral(true).queue();
            }


            default ->
                    event.reply(
                            "❌ Use: adicionar, editar, remover, listar ou info."
                    ).setEphemeral(true).queue();
        }
    }


    // =====================================================
    // WL ADMIN
    // =====================================================

    private void handleWLCommand(
            SlashCommandInteractionEvent event
    ) {

        if (!isAdmin(event)) {

            event.reply(
                    "❌ Você não tem permissão."
            ).setEphemeral(true).queue();

            return;
        }


        String acao =
                event.getOption("acao")
                        .getAsString()
                        .toLowerCase();


        switch (acao) {

            case "painel" ->
                    sendWLPanel(event);

            case "set-resultados" ->
                    setWLResultados(event);

            case "set-aprovacao" ->
                    setWLAprovacao(event);

            case "set-categoria" ->
                    setWLCategoria(event);

            case "adicionar-pergunta" ->
                    addPergunta(event);

            case "remover-pergunta" ->
                    removePergunta(event);

            case "listar-perguntas" ->
                    listPerguntas(event);

            default ->
                    event.reply(
                            """
                            ❌ Ações disponíveis:
                            
                            `painel`
                            `set-resultados`
                            `set-aprovacao`
                            `set-categoria`
                            `adicionar-pergunta`
                            `remover-pergunta`
                            `listar-perguntas`
                            """
                    ).setEphemeral(true).queue();
        }
    }


    private void sendWLPanel(
            SlashCommandInteractionEvent event
    ) {

        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle("🛡️ Central de Whitelist");

        embed.setDescription(
                """
                **ShinjuEra RP**
                
                Deseja entrar no nosso universo de **Naruto Roleplay**?
                
                Clique no botão abaixo para iniciar sua entrevista de Whitelist.
                
                📋 **Como funciona?**
                
                • Um canal privado será criado.
                • Você responderá algumas perguntas.
                • A Staff irá analisar suas respostas.
                • Após a análise, você receberá o resultado.
                
                ⚠️ Antes de começar, leia todas as regras do servidor.
                """
        );


        event.replyEmbeds(embed.build())
                .addActionRow(
                        Button.success(
                                "wl_start",
                                "🛡️ Começar Whitelist"
                        )
                )
                .queue();
    }


    private void setWLResultados(
            SlashCommandInteractionEvent event
    ) {

        if (event.getOption("canal") == null) {

            event.reply(
                    "❌ Escolha um canal."
            ).setEphemeral(true).queue();

            return;
        }


        DataManager.data.wlResultadosChannelId =
                event.getOption("canal")
                        .getAsChannel()
                        .getIdLong();


        DataManager.save();


        event.reply(
                "📢 Canal de resultados configurado!"
        ).setEphemeral(true).queue();
    }


    private void setWLAprovacao(
            SlashCommandInteractionEvent event
    ) {

        if (event.getOption("canal") == null) {

            event.reply(
                    "❌ Escolha um canal."
            ).setEphemeral(true).queue();

            return;
        }


        DataManager.data.wlAprovacaoChannelId =
                event.getOption("canal")
                        .getAsChannel()
                        .getIdLong();


        DataManager.save();


        event.reply(
                "✅ Canal de aprovação configurado!"
        ).setEphemeral(true).queue();
    }


    private void setWLCategoria(
            SlashCommandInteractionEvent event
    ) {

        if (event.getOption("canal") == null) {

            event.reply(
                    "❌ Escolha uma categoria."
            ).setEphemeral(true).queue();

            return;
        }


        DataManager.data.wlCategoriaId =
                event.getOption("canal")
                        .getAsChannel()
                        .getIdLong();


        DataManager.save();


        event.reply(
                "📁 Categoria da WL configurada!"
        ).setEphemeral(true).queue();
    }


    private void addPergunta(
            SlashCommandInteractionEvent event
    ) {

        if (event.getOption("texto") == null) {

            event.reply(
                    "❌ Informe a pergunta."
            ).setEphemeral(true).queue();

            return;
        }


        String pergunta =
                event.getOption("texto")
                        .getAsString();


        DataManager.data.perguntasWL.add(
                pergunta
        );

        DataManager.save();


        event.reply(
                "❓ Pergunta adicionada com sucesso!"
        ).setEphemeral(true).queue();
    }


    private void removePergunta(
            SlashCommandInteractionEvent event
    ) {

        if (event.getOption("numero") == null) {

            event.reply(
                    "❌ Informe o número."
            ).setEphemeral(true).queue();

            return;
        }


        int numero =
                event.getOption("numero")
                        .getAsInt() - 1;


        if (numero < 0
                || numero >= DataManager.data.perguntasWL.size()) {

            event.reply(
                    "❌ Número inválido."
            ).setEphemeral(true).queue();

            return;
        }


        DataManager.data.perguntasWL.remove(
                numero
        );

        DataManager.save();


        event.reply(
                "🗑️ Pergunta removida."
        ).setEphemeral(true).queue();
    }


    private void listPerguntas(
            SlashCommandInteractionEvent event
    ) {

        StringBuilder lista =
                new StringBuilder(
                        "❓ **PERGUNTAS DA WL**\n\n"
                );


        for (int i = 0;
             i < DataManager.data.perguntasWL.size();
             i++) {

            lista.append("**")
                    .append(i + 1)
                    .append(".** ")
                    .append(
                            DataManager.data.perguntasWL.get(i)
                    )
                    .append("\n\n");
        }


        event.reply(
                lista.toString()
        ).setEphemeral(true).queue();
    }


    // =====================================================
    // BOTÃO COMEÇAR WL
    // =====================================================

    @Override
    public void onButtonInteraction(
            ButtonInteractionEvent event
    ) {

        if (event.getComponentId().equals("wl_start")) {

            startWL(event);

            return;
        }


        if (event.getComponentId().startsWith("wl_approve:")) {

            approveWL(event);

            return;
        }


        if (event.getComponentId().startsWith("wl_reject:")) {

            rejectWL(event);
        }
    }


    private void startWL(
            ButtonInteractionEvent event
    ) {

        long userId =
                event.getUser().getIdLong();


        if (wlSessions.containsKey(userId)) {

            event.reply(
                    "❌ Você já possui uma WL em andamento."
            ).setEphemeral(true).queue();

            return;
        }


        if (DataManager.data.wlCategoriaId == 0) {

            event.reply(
                    "❌ A categoria da WL não foi configurada."
            ).setEphemeral(true).queue();

            return;
        }


        Category category =
                event.getGuild()
                        .getCategoryById(
                                DataManager.data.wlCategoriaId
                        );


        if (category == null) {

            event.reply(
                    "❌ Categoria não encontrada."
            ).setEphemeral(true).queue();

            return;
        }


        event.deferReply(true).queue();


        category.createTextChannel(
                "wl-" + event.getUser().getName()
        ).queue(channel -> {

            Member member =
                    event.getMember();


            if (member != null) {

                channel.getManager()
                        .putPermissionOverride(
                                event.getGuild()
                                        .getPublicRole(),
                                null,
                                EnumSet.of(
                                        Permission.VIEW_CHANNEL
                                )
                        )
                        .queue();


                channel.getManager()
                        .putPermissionOverride(
                                member,
                                EnumSet.of(
                                        Permission.VIEW_CHANNEL,
                                        Permission.MESSAGE_SEND,
                                        Permission.MESSAGE_HISTORY
                                ),
                                null
                        )
                        .queue();
            }


            WLSession session =
                    new WLSession(
                            userId,
                            channel.getIdLong()
                    );


            wlSessions.put(
                    userId,
                    session
            );


            channel.sendMessage(
                    "🛡️ "
                            + event.getUser().getAsMention()
                            + "\n\n"
                            + "**Bem-vindo à sua Whitelist!**\n\n"
                            + "Responda às perguntas uma por uma."
            ).queue();


            enviarPerguntaAtual(
                    channel,
                    session
            );


            event.getHook().sendMessage(
                    "✅ Seu canal privado foi criado: "
                            + channel.getAsMention()
            ).queue();

        });
    }


    // =====================================================
    // RECEBER RESPOSTAS
    // =====================================================

    @Override
    public void onMessageReceived(
            MessageReceivedEvent event
    ) {

        if (event.getAuthor().isBot()) {
            return;
        }


        long userId =
                event.getAuthor().getIdLong();


        WLSession session =
                wlSessions.get(userId);


        if (session == null) {
            return;
        }


        if (event.getChannel().getIdLong()
                != session.channelId) {

            return;
        }


        if (session.perguntaAtual
                >= DataManager.data.perguntasWL.size()) {

            return;
        }


        session.respostas.add(
                event.getMessage().getContentRaw()
        );


        session.perguntaAtual++;


        TextChannel channel =
                event.getChannel()
                        .asTextChannel();


        if (session.perguntaAtual
                < DataManager.data.perguntasWL.size()) {

            enviarPerguntaAtual(
                    channel,
                    session
            );

        } else {

            finalizarWL(
                    event,
                    session
            );
        }
    }


    private void enviarPerguntaAtual(
            TextChannel channel,
            WLSession session
    ) {

        String pergunta =
                DataManager.data.perguntasWL.get(
                        session.perguntaAtual
                );


        channel.sendMessage(
                "❓ **Pergunta "
                        + (session.perguntaAtual + 1)
                        + "/"
                        + DataManager.data.perguntasWL.size()
                        + "**\n\n"
                        + pergunta
        ).queue();
    }


    // =====================================================
    // ENVIAR PARA STAFF
    // =====================================================

    private void finalizarWL(
            MessageReceivedEvent event,
            WLSession session
    ) {

        TextChannel channel =
                event.getChannel().asTextChannel();


        channel.sendMessage(
                """
                📋 **Você terminou sua Whitelist!**
                
                Aguarde a análise da Staff.
                """
        ).queue();


        if (DataManager.data.wlAprovacaoChannelId == 0) {

            return;
        }


        TextChannel aprovacao =
                event.getGuild()
                        .getTextChannelById(
                                DataManager.data.wlAprovacaoChannelId
                        );


        if (aprovacao == null) {
            return;
        }


        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle("📋 NOVA WL PARA ANÁLISE");

        embed.setDescription(
                "👤 **Discord:** "
                        + event.getAuthor().getAsMention()
                        + "\n"
                        + "📁 **Canal:** "
                        + channel.getAsMention()
                        + "\n\n"
        );


        StringBuilder respostas =
                new StringBuilder();


        for (int i = 0;
             i < DataManager.data.perguntasWL.size();
             i++) {

            respostas.append("**")
                    .append(i + 1)
                    .append(". ")
                    .append(
                            DataManager.data.perguntasWL.get(i)
                    )
                    .append("**\n")
                    .append(
                            session.respostas.get(i)
                    )
                    .append("\n\n");
        }


        embed.addField(
                "📝 Respostas",
                respostas.length() > 1024
                        ? respostas.substring(0, 1020)
                        : respostas.toString(),
                false
        );


        aprovacao.sendMessageEmbeds(
                embed.build()
        ).addActionRow(

                Button.success(
                        "wl_approve:"
                                + session.userId
                                + ":"
                                + session.channelId,
                        "✅ Aprovar"
                ),

                Button.danger(
                        "wl_reject:"
                                + session.userId
                                + ":"
                                + session.channelId,
                        "❌ Reprovar"
                )

        ).queue();
    }


    // =====================================================
    // APROVAR WL
    // =====================================================

    private void approveWL(
            ButtonInteractionEvent event
    ) {

        if (!event.getMember()
                .hasPermission(Permission.ADMINISTRATOR)) {

            event.reply(
                    "❌ Você não tem permissão."
            ).setEphemeral(true).queue();

            return;
        }


        String[] parts =
                event.getComponentId().split(":");


        long userId =
                Long.parseLong(parts[1]);

        long channelId =
                Long.parseLong(parts[2]);


        event.editMessage(
                "✅ WL APROVADA por "
                        + event.getUser().getAsMention()
        ).setComponents(
                new ArrayList<>()
        ).queue();


        enviarResultado(
                event,
                userId,
                true
        );


        fecharWL(
                event,
                userId,
                channelId
        );
    }


    // =====================================================
    // REPROVAR WL
    // =====================================================

    private void rejectWL(
            ButtonInteractionEvent event
    ) {

        if (!event.getMember()
                .hasPermission(Permission.ADMINISTRATOR)) {

            event.reply(
                    "❌ Você não tem permissão."
            ).setEphemeral(true).queue();

            return;
        }


        String[] parts =
                event.getComponentId().split(":");


        long userId =
                Long.parseLong(parts[1]);

        long channelId =
                Long.parseLong(parts[2]);


        event.editMessage(
                "❌ WL REPROVADA por "
                        + event.getUser().getAsMention()
        ).setComponents(
                new ArrayList<>()
        ).queue();


        enviarResultado(
                event,
                userId,
                false
        );


        fecharWL(
                event,
                userId,
                channelId
        );
    }


    // =====================================================
    // RESULTADO
    // =====================================================

    private void enviarResultado(
            ButtonInteractionEvent event,
            long userId,
            boolean aprovada
    ) {

        String resultado =
                aprovada
                        ? "✅ **WHITELIST APROVADA**"
                        : "❌ **WHITELIST REPROVADA**";


        event.getGuild()
                .retrieveMemberById(userId)
                .queue(member -> {

                    member.getUser()
                            .openPrivateChannel()
                            .queue(dm ->

                                    dm.sendMessage(
                                            resultado
                                                    + "\n\n"
                                                    + "Sua Whitelist no **"
                                                    + event.getGuild()
                                                    .getName()
                                                    + "** foi analisada."
                                    ).queue()

                            );

                });


        if (DataManager.data.wlResultadosChannelId == 0) {
            return;
        }


        TextChannel resultados =
                event.getGuild()
                        .getTextChannelById(
                                DataManager.data
                                        .wlResultadosChannelId
                        );


        if (resultados == null) {
            return;
        }


        resultados.sendMessage(
                resultado
                        + "\n\n"
                        + "👤 <@"
                        + userId
                        + ">\n"
                        + "👮 Analisado por: "
                        + event.getUser().getAsMention()
        ).queue();
    }


    // =====================================================
    // FECHAR WL
    // =====================================================

    private void fecharWL(
            ButtonInteractionEvent event,
            long userId,
            long channelId
    ) {

        wlSessions.remove(userId);


        TextChannel channel =
                event.getGuild()
                        .getTextChannelById(channelId);


        if (channel != null) {

            channel.sendMessage(
                    "🔒 Esta Whitelist foi finalizada."
            ).queue();

            // Por enquanto mantém o canal.
            // Depois podemos colocar exclusão automática.
        }
    }


    // =====================================================
    // CLASSE SESSÃO WL
    // =====================================================

    private static class WLSession {

        long userId;
        long channelId;

        int perguntaAtual = 0;

        List<String> respostas =
                new ArrayList<>();


        WLSession(
                long userId,
                long channelId
        ) {

            this.userId = userId;
            this.channelId = channelId;

        }
    }
}
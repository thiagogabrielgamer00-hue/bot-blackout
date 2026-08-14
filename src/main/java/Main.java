import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;


import java.awt.Color;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main extends ListenerAdapter {

    // =========================================================
    // CONFIGURAÇÕES
    // =========================================================

    private static final String SUPER_USER_ID =
            "1333822923194105917";

    private static final String ID_SERVIDOR =
            "1418818172827734057";

    private static final String ARQUIVO_TECNICAS =
            "tecnicas_dados.txt";

    private static final String ARQUIVO_ECONOMIA =
            "economia_dados.txt";

    private static final String ARQUIVO_WARNS =
            "warns_dados.txt";

    // =========================================================
    // DADOS
    // =========================================================

    private final Map<String, int[]> tecnicas =
            new LinkedHashMap<>();

    private final Map<String, Integer> carteiras =
            new HashMap<>();

    private final Map<String, Long> cooldownDaily =
            new HashMap<>();

    private final Map<String, List<String>> listaWarns =
            new HashMap<>();

    private static final Map<String, List<String>> sessoesWl =
            new ConcurrentHashMap<>();

    // =========================================================
    // WEB / RENDER
    // =========================================================

    private static final AtomicBoolean servidorWebAtivo =
            new AtomicBoolean(false);

    private static HttpServerInterno servidorWeb;

    // =========================================================
    // WL
    // =========================================================

    private static final String[] PERGUNTAS = {

            "1️⃣ Qual o nome e a idade do seu personagem dentro do jogo (RP)?",

            "2️⃣ Qual técnica de Jujutsu você roletou?",

            "3️⃣ Escreva uma breve história para o seu personagem:",

            "4️⃣ Quais são as principais regras de RP do servidor?",

            "5️⃣ Qual é a sua idade na vida real?"
    };

    // =========================================================
    // EXECUTOR
    // =========================================================

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(2);

    // =========================================================
    // CONSTRUTOR
    // =========================================================

    public Main() {

        System.out.println("=================================");
        System.out.println(" BLACKOUT COMMUNITY BOT");
        System.out.println("=================================");

        Config.carregar();

        carregarDados();


        System.out.println("[BOT] Configurações carregadas.");
        System.out.println("[BOT] Dados carregados.");
    }

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println(" INICIANDO BLACKOUT BOT");
        System.out.println("=================================");

        String token = System.getenv("DISCORD_TOKEN");

        if (token == null || token.isBlank()) {

            System.err.println(
                    "[ERRO FATAL] DISCORD_TOKEN não encontrada.");

            System.err.println(
                    "Configure DISCORD_TOKEN nas Environment Variables do Render.");

            System.exit(1);
        }

        int porta = obterPorta();

        iniciarServidorWeb(porta);

        Main bot = new Main();

        iniciarDiscord(bot, token);

        iniciarHeartbeat();

        // Mantém o processo principal vivo.
        try {

            new CountDownLatch(1).await();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            System.err.println(
                    "[MAIN] Processo interrompido.");

        }
    }

    // =========================================================
    // PORTA RENDER
    // =========================================================

    private static int obterPorta() {

        String portaEnv =
                System.getenv().getOrDefault("PORT", "10000");

        try {

            return Integer.parseInt(portaEnv);

        } catch (NumberFormatException e) {

            System.err.println(
                    "[WEB] PORT inválida: " + portaEnv);

            return 10000;
        }
    }

    // =========================================================
    // SERVIDOR WEB
    // =========================================================

    private static void iniciarServidorWeb(int porta) {

        if (servidorWebAtivo.get()) {
            return;
        }

        try {

            servidorWeb =
                    new HttpServerInterno(porta);

            servidorWeb.start();

            servidorWebAtivo.set(true);

            System.out.println(
                    "[WEB] Servidor HTTP iniciado na porta " + porta);

        } catch (Exception e) {

            System.err.println(
                    "[WEB] Não foi possível iniciar servidor HTTP:");

            e.printStackTrace();
        }
    }

    // =========================================================
    // HEARTBEAT RENDER
    // =========================================================

    private static void iniciarHeartbeat() {

        String url =
                System.getenv("RENDER_EXTERNAL_URL");

        if (url == null || url.isBlank()) {

            System.out.println(
                    "[RENDER] RENDER_EXTERNAL_URL não configurada.");

            return;
        }

        System.out.println(
                "[RENDER] Health check ativado: " + url);

        Thread heartbeat = new Thread(() -> {

            while (true) {

                try {

                    Thread.sleep(4 * 60 * 1000L);

                    URL healthUrl =
                            new URL(url);

                    HttpURLConnection connection =
                            (HttpURLConnection)
                                    healthUrl.openConnection();

                    connection.setRequestMethod("GET");

                    connection.setConnectTimeout(10000);

                    connection.setReadTimeout(10000);

                    connection.setUseCaches(false);

                    int resposta =
                            connection.getResponseCode();

                    System.out.println(
                            "[RENDER] Health check: HTTP " +
                                    resposta);

                    connection.disconnect();

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                    return;

                } catch (Exception e) {

                    System.err.println(
                            "[RENDER] Falha no health check:");

                    System.err.println(
                            e.getMessage());
                }
            }

        }, "render-heartbeat");

        heartbeat.setDaemon(true);
        heartbeat.start();
    }

    // =========================================================
    // DISCORD
    // =========================================================

    private static void iniciarDiscord(
            Main bot,
            String token) {

        Thread discordThread = new Thread(() -> {

            try {

                System.out.println(
                        "[DISCORD] Criando conexão JDA...");

                JDA jda =
                        JDABuilder
                                .createDefault(token)

                                .enableIntents(
                                        GatewayIntent.GUILD_MESSAGES,
                                        GatewayIntent.DIRECT_MESSAGES,
                                        GatewayIntent.MESSAGE_CONTENT,
                                        GatewayIntent.GUILD_MEMBERS,
                                        GatewayIntent.GUILD_VOICE_STATES)

                                .setMemberCachePolicy(
                                        MemberCachePolicy.ALL)

                                .addEventListeners(bot)

                                .build();

                jda.awaitReady();

JDAHolder.jda = jda;

System.out.println("=================================");
System.out.println("[DISCORD] BOT ONLINE!");
System.out.println("[DISCORD] Usuário: " + jda.getSelfUser().getAsTag());
System.out.println("[DISCORD] Guilds: " + jda.getGuilds().size());
System.out.println("=================================");

CommandRegistry.registrar(jda);

            } catch (LoginException e) {

                System.err.println(
                        "[DISCORD] Token inválido ou erro de login.");

                e.printStackTrace();

                System.exit(1);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                System.err.println(
                        "[DISCORD] awaitReady interrompido.");

            } catch (Exception e) {

                System.err.println(
                        "[DISCORD] Erro inesperado ao iniciar:");

                e.printStackTrace();
            }

        }, "discord-main");

        discordThread.setUncaughtExceptionHandler(
                (thread, throwable) -> {

                    System.err.println(
                            "[DISCORD] Thread terminou com erro:");

                    throwable.printStackTrace();
                });

        discordThread.start();
    }

    // =========================================================
    // SLASH COMMANDS
    // =========================================================

    @Override
    public void onSlashCommandInteraction(
            SlashCommandInteractionEvent e) {

        try {

            if (e.getGuild() == null) {

                if (!e.getName().equals("fazerwl")) {

                    e.reply(
                            "❌ Esse comando precisa ser usado dentro do servidor.")
                            .setEphemeral(true)
                            .queue();

                    return;
                }
            }

            boolean staff =
                    temPermissaoStaff(e);

            String nome =
                    e.getName();

            // =================================================
            // PING
            // =================================================

            if (nome.equals("ping")) {

                e.reply(
                        "🏓 Pong! `" +
                                e.getJDA().getGatewayPing() +
                                "ms`")
                        .queue();

                return;
            }

            // =================================================
            // MODERAÇÃO
            // =================================================

            if (nome.equals("ban")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                User usuario =
                        e.getOption("usuario")
                                .getAsUser();

                String motivo =
                        opcaoString(e, "motivo");

                if (usuario.getId().equals(
                        e.getJDA().getSelfUser().getId())) {

                    e.reply(
                            "❌ Eu não posso me banir.")
                            .queue();

                    return;
                }

                e.getGuild()
                        .ban(usuario, 0,
                                TimeUnit.DAYS)
                        .reason(
                                motivo.isBlank()
                                        ? "Não informado"
                                        : motivo)
                        .queue(
                                success ->
                                        e.reply(
                                                "🔨 **" +
                                                        usuario.getAsTag() +
                                                        "** foi banido.")
                                                .queue(),

                                error ->
                                        e.reply(
                                                "❌ Não consegui banir esse usuário. Verifique minha hierarquia e permissões.")
                                                .queue());

                return;
            }

            if (nome.equals("kick")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                Member membro =
                        e.getOption("usuario")
                                .getAsMember();

                if (membro == null) {

                    e.reply(
                            "❌ Esse usuário não está no servidor.")
                            .queue();

                    return;
                }

                String motivo =
                        opcaoString(e, "motivo");

                e.getGuild()
                        .kick(membro)
                        .reason(
                                motivo.isBlank()
                                        ? "Não informado"
                                        : motivo)
                        .queue(
                                success ->
                                        e.reply(
                                                "👢 Usuário expulso.")
                                                .queue(),

                                error ->
                                        e.reply(
                                                "❌ Não consegui expulsar. Verifique minha hierarquia.")
                                                .queue());

                return;
            }

            if (nome.equals("mute")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                Member membro =
                        e.getOption("usuario")
                                .getAsMember();

                if (membro == null) {

                    e.reply(
                            "❌ Usuário não encontrado.")
                            .queue();

                    return;
                }

                int minutos =
                        e.getOption("minutos")
                                .getAsInt();

                if (minutos < 1 ||
                        minutos > 40320) {

                    e.reply(
                            "❌ O tempo deve ficar entre 1 e 40320 minutos.")
                            .setEphemeral(true)
                            .queue();

                    return;
                }

                membro.timeoutFor(
                                minutos,
                                TimeUnit.MINUTES)
                        .reason(
                                opcaoString(
                                        e,
                                        "motivo"))
                        .queue(
                                success ->
                                        e.reply(
                                                "🤫 Usuário colocado em timeout por `" +
                                                        minutos +
                                                        "` minutos.")
                                                .queue(),

                                error ->
                                        e.reply(
                                                "❌ Não consegui aplicar o timeout.")
                                                .queue());

                return;
            }

            if (nome.equals("unmute")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                Member membro =
                        e.getOption("usuario")
                                .getAsMember();

                if (membro == null) {

                    e.reply(
                            "❌ Usuário não encontrado.")
                            .queue();

                    return;
                }

                membro.removeTimeout()
                        .queue(
                                success ->
                                        e.reply(
                                                "🔊 Timeout removido.")
                                                .queue(),

                                error ->
                                        e.reply(
                                                "❌ Não consegui remover o timeout.")
                                                .queue());

                return;
            }

            if (nome.equals("warn")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                User usuario =
                        e.getOption("usuario")
                                .getAsUser();

                String motivo =
                        e.getOption("motivo")
                                .getAsString();

                List<String> warns =
                        listaWarns.computeIfAbsent(
                                usuario.getId(),
                                k -> new ArrayList<>());

                warns.add(
                        motivo + " | por: " +
                                e.getUser().getAsTag());

                salvarDados();

                e.reply(
                        "⚠️ Advertência aplicada.\n" +
                                "Total: `" +
                                warns.size() +
                                "`")
                        .queue();

                return;
            }

            if (nome.equals("infractions")) {

                User usuario =
                        e.getOption("usuario")
                                .getAsUser();

                List<String> warns =
                        listaWarns.getOrDefault(
                                usuario.getId(),
                                Collections.emptyList());

                if (warns.isEmpty()) {

                    e.reply(
                            "✅ Esse usuário não possui advertências.")
                            .queue();

                    return;
                }

                StringBuilder texto =
                        new StringBuilder(
                                "⚠️ **Advertências de " +
                                        usuario.getAsTag() +
                                        "**\n\n");

                for (int i = 0;
                     i < warns.size();
                     i++) {

                    texto.append("`")
                            .append(i + 1)
                            .append("` ")
                            .append(warns.get(i))
                            .append("\n");
                }

                e.reply(texto.toString())
                        .setEphemeral(true)
                        .queue();

                return;
            }

            if (nome.equals("lock") ||
                    nome.equals("unlock")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                TextChannel canal =
                        e.getChannel()
                                .asTextChannel();

                boolean bloquear =
                        nome.equals("lock");

                if (bloquear) {

                    canal.upsertPermissionOverride(
                                    e.getGuild().getPublicRole())
                            .deny(
                                    Permission.MESSAGE_SEND)
                            .queue();

                    e.reply(
                            "🔒 Canal bloqueado.")
                            .queue();

                } else {

                    canal.upsertPermissionOverride(
                                    e.getGuild().getPublicRole())
                            .clear()
                            .queue();

                    e.reply(
                            "🔓 Canal desbloqueado.")
                            .queue();
                }

                return;
            }

            if (nome.equals("slowmode")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                int segundos =
                        e.getOption("segundos")
                                .getAsInt();

                if (segundos < 0 ||
                        segundos > 21600) {

                    e.reply(
                            "❌ O slowmode deve ficar entre 0 e 21600 segundos.")
                            .setEphemeral(true)
                            .queue();

                    return;
                }

                e.getChannel()
                        .asTextChannel()
                        .getManager()
                        .setSlowmode(segundos)
                        .queue(
                                success ->
                                        e.reply(
                                                "⏱️ Slowmode definido para `" +
                                                        segundos +
                                                        "s`.")
                                                .queue());

                return;
            }

            // =================================================
            // ROLE
            // =================================================

            if (nome.equals("role")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                Member membro =
                        e.getOption("usuario")
                                .getAsMember();

                Role cargo =
                        e.getOption("cargo")
                                .getAsRole();

                if (membro == null) {

                    e.reply(
                            "❌ Membro não encontrado.")
                            .queue();

                    return;
                }

                if (cargo.isManaged()) {

                    e.reply(
                            "❌ Esse cargo é gerenciado pelo Discord.")
                            .queue();

                    return;
                }

                if (e.getSubcommandName()
                        .equals("add")) {

                    e.getGuild()
                            .addRoleToMember(
                                    membro,
                                    cargo)
                            .queue(
                                    success ->
                                            e.reply(
                                                    "✅ Cargo adicionado.")
                                                    .queue(),

                                    error ->
                                            e.reply(
                                                    "❌ Não consegui adicionar o cargo. Verifique minha hierarquia.")
                                                    .queue());

                } else {

                    e.getGuild()
                            .removeRoleFromMember(
                                    membro,
                                    cargo)
                            .queue(
                                    success ->
                                            e.reply(
                                                    "🗑️ Cargo removido.")
                                                    .queue(),

                                    error ->
                                            e.reply(
                                                    "❌ Não consegui remover o cargo.")
                                                    .queue());
                }

                return;
            }

            // =================================================
            // ECONOMIA
            // =================================================

            if (nome.equals("daily")) {

                long agora =
                        System.currentTimeMillis();

                long proximo =
                        cooldownDaily.getOrDefault(
                                e.getUser().getId(),
                                0L);

                if (agora < proximo) {

                    long restante =
                            proximo - agora;

                    long horas =
                            TimeUnit.MILLISECONDS
                                    .toHours(restante);

                    e.reply(
                            "⏱️ Você já recebeu seu Daily.\n" +
                                    "Volte em aproximadamente `" +
                                    horas +
                                    "h`.")
                            .setEphemeral(true)
                            .queue();

                    return;
                }

                int recompensa =
                        new Random()
                                .nextInt(401) + 100;

                String id =
                        e.getUser().getId();

                int saldo =
                        carteiras.getOrDefault(
                                id,
                                0);

                carteiras.put(
                        id,
                        saldo + recompensa);

                cooldownDaily.put(
                        id,
                        agora + 86400000L);

                salvarDados();

                e.reply(
                        "💰 Você recebeu **" +
                                recompensa +
                                " BlackCoins**!")
                        .queue();

                return;
            }

            if (nome.equals("bal")) {

                User usuario =
                        e.getOption("usuario") != null
                                ? e.getOption("usuario")
                                .getAsUser()
                                : e.getUser();

                int saldo =
                        carteiras.getOrDefault(
                                usuario.getId(),
                                0);

                e.reply(
                        "🪙 **Saldo de " +
                                usuario.getName() +
                                ":** `" +
                                saldo +
                                " BlackCoins`")
                        .queue();

                return;
            }

            if (nome.equals("pay")) {

                User destino =
                        e.getOption("usuario")
                                .getAsUser();

                int quantidade =
                        e.getOption("quantidade")
                                .getAsInt();

                String remetente =
                        e.getUser().getId();

                if (destino.isBot()) {

                    e.reply(
                            "❌ Você não pode transferir para bots.")
                            .setEphemeral(true)
                            .queue();

                    return;
                }

                if (destino.getId()
                        .equals(remetente)) {

                    e.reply(
                            "❌ Você não pode transferir para si mesmo.")
                            .setEphemeral(true)
                            .queue();

                    return;
                }

                if (quantidade <= 0) {

                    e.reply(
                            "❌ Quantidade inválida.")
                            .setEphemeral(true)
                            .queue();

                    return;
                }

                int saldo =
                        carteiras.getOrDefault(
                                remetente,
                                0);

                if (saldo < quantidade) {

                    e.reply(
                            "❌ Você não possui moedas suficientes.")
                            .setEphemeral(true)
                            .queue();

                    return;
                }

                carteiras.put(
                        remetente,
                        saldo - quantidade);

                carteiras.put(
                        destino.getId(),
                        carteiras.getOrDefault(
                                destino.getId(),
                                0) + quantidade);

                salvarDados();

                e.reply(
                        "💸 Você enviou `" +
                                quantidade +
                                "` BlackCoins para " +
                                destino.getAsMention() +
                                ".")
                        .queue();

                return;
            }

            // =================================================
            // DIVERSÃO
            // =================================================

            if (nome.equals("piada")) {

                String[] piadas = {

                        "Por que o livro de matemática se suicidou? Porque tinha muitos problemas.",

                        "O que o pato disse para a pata? Vem quá!",

                        "Qual é o doce favorito do Gojo? Chiclete.",

                        "Por que o computador foi ao médico? Porque estava com vírus.",

                        "O que o zero disse para o oito? Belo cinto!"
                };

                e.reply(
                        "😂 " +
                                piadas[
                                        new Random()
                                                .nextInt(
                                                        piadas.length)])
                        .queue();

                return;
            }

            if (nome.equals("ascii")) {

                String texto =
                        e.getOption("texto")
                                .getAsString();

                if (texto.length() > 100) {

                    e.reply(
                            "❌ Máximo de 100 caracteres.")
                            .setEphemeral(true)
                            .queue();

                    return;
                }

                StringBuilder ascii =
                        new StringBuilder();

                for (char c :
                        texto.toUpperCase()
                                .toCharArray()) {

                    ascii.append(c)
                            .append(" ");
                }

                e.reply(
                        "```text\n" +
                                ascii +
                                "\n```")
                        .queue();

                return;
            }

            if (nome.equals("pergunta")) {

                String[] respostas = {

                        "Sim! 🔮",

                        "Não. 🔮",

                        "Talvez...",

                        "Com certeza!",

                        "Nem pensar!",

                        "O futuro está incerto.",

                        "Provavelmente."
                };

                e.reply(
                        "🔮 **Pergunta:** " +
                                e.getOption("texto")
                                        .getAsString() +
                                "\n\n🎱 **Resposta:** " +
                                respostas[
                                        new Random()
                                                .nextInt(
                                                        respostas.length)])
                        .queue();

                return;
            }

            if (nome.equals("ppt")) {

                String escolha =
                        e.getOption("escolha")
                                .getAsString()
                                .toLowerCase()
                                .trim();

                if (!Arrays.asList(
                        "pedra",
                        "papel",
                        "tesoura")
                        .contains(escolha)) {

                    e.reply(
                            "❌ Escolha `pedra`, `papel` ou `tesoura`.")
                            .setEphemeral(true)
                            .queue();

                    return;
                }

                String[] opcoes = {
                        "pedra",
                        "papel",
                        "tesoura"
                };

                String bot =
                        opcoes[
                                new Random()
                                        .nextInt(3)];

                if (escolha.equals(bot)) {

                    e.reply(
                            "🤖 Empate! Eu escolhi **" +
                                    bot +
                                    "**.")
                            .queue();

                } else if (
                        (escolha.equals("pedra")
                                && bot.equals("tesoura"))
                                ||
                                (escolha.equals("papel")
                                        && bot.equals("pedra"))
                                ||
                                (escolha.equals("tesoura")
                                        && bot.equals("papel"))) {

                    e.reply(
                            "🎉 Você ganhou! Eu escolhi **" +
                                    bot +
                                    "**.")
                            .queue();

                } else {

                    e.reply(
                            "😈 Você perdeu! Eu escolhi **" +
                                    bot +
                                    "**.")
                            .queue();
                }

                return;
            }

            if (nome.equals("ship")) {

                User u1 =
                        e.getOption("usuario1")
                                .getAsUser();

                User u2 =
                        e.getOption("usuario2") != null
                                ? e.getOption("usuario2")
                                .getAsUser()
                                : e.getUser();

                long seed =
                        u1.getIdLong() ^
                                u2.getIdLong();

                int porcentagem =
                        new Random(seed)
                                .nextInt(101);

                e.reply(
                        "💞 **Compatibilidade:**\n" +
                                u1.getAsMention() +
                                " ❤️ " +
                                u2.getAsMention() +
                                "\n\n**" +
                                porcentagem +
                                "%**")
                        .queue();

                return;
            }

            if (nome.equals("brincar")) {

                String acao =
                        e.getOption("acao")
                                .getAsString();

                User alvo =
                        e.getOption("usuario")
                                .getAsUser();

                e.reply(
                        "🎭 " +
                                e.getUser()
                                        .getAsMention() +
                                " fez **" +
                                acao +
                                "** em " +
                                alvo.getAsMention() +
                                "!")
                        .queue();

                return;
            }

            if (nome.equals("moeda")) {

                e.reply(
                        new Random().nextBoolean()
                                ? "👑 **CARA**"
                                : "🛡️ **COROA**")
                        .queue();

                return;
            }

            if (nome.equals("dados")) {

                int lados =
                        e.getOption("lados")
                                .getAsInt();

                if (lados < 2 ||
                        lados > 1000000) {

                    e.reply(
                            "❌ Use entre 2 e 1.000.000 lados.")
                            .setEphemeral(true)
                            .queue();

                    return;
                }

                int resultado =
                        new Random()
                                .nextInt(lados) + 1;

                e.reply(
                        "🎲 Resultado: **" +
                                resultado +
                                "**")
                        .queue();

                return;
            }

            // =================================================
            // UTILIDADES
            // =================================================

            if (nome.equals("avatar")) {

                User usuario =
                        e.getOption("usuario") != null
                                ? e.getOption("usuario")
                                .getAsUser()
                                : e.getUser();

                e.reply(
                        usuario.getEffectiveAvatarUrl())
                        .queue();

                return;
            }

            if (nome.equals("userinfo")) {

                User usuario =
                        e.getOption("usuario") != null
                                ? e.getOption("usuario")
                                .getAsUser()
                                : e.getUser();

                DateTimeFormatter formato =
                        DateTimeFormatter.ofPattern(
                                "dd/MM/yyyy HH:mm");

                e.replyEmbeds(
                        new EmbedBuilder()
                                .setColor(Color.CYAN)
                                .setTitle(
                                        "👤 " +
                                                usuario.getName())
                                .addField(
                                        "ID",
                                        usuario.getId(),
                                        false)
                                .addField(
                                        "Conta criada",
                                        usuario.getTimeCreated()
                                                .format(
                                                        formato),
                                        false)
                                .setThumbnail(
                                        usuario
                                                .getEffectiveAvatarUrl())
                                .build())
                        .queue();

                return;
            }

            if (nome.equals("serverinfo")) {

                Guild guild =
                        e.getGuild();

                e.replyEmbeds(
                        new EmbedBuilder()
                                .setColor(Color.ORANGE)
                                .setTitle(
                                        "📊 " +
                                                guild.getName())
                                .addField(
                                        "Membros",
                                        String.valueOf(
                                                guild.getMemberCount()),
                                        true)
                                .addField(
                                        "Canais",
                                        String.valueOf(
                                                guild.getChannels()
                                                        .size()),
                                        true)
                                .addField(
                                        "Cargos",
                                        String.valueOf(
                                                guild.getRoles()
                                                        .size()),
                                        true)
                                .build())
                        .queue();

                return;
            }

            if (nome.equals("sorteio")) {

                List<Member> membros =
                        new ArrayList<>();

                for (Member membro :
                        e.getGuild().getMembers()) {

                    if (!membro.getUser().isBot()) {
                        membros.add(membro);
                    }
                }

                if (membros.isEmpty()) {

                    e.reply(
                            "❌ Não existem membros disponíveis.")
                            .queue();

                    return;
                }

                Member vencedor =
                        membros.get(
                                new Random()
                                        .nextInt(
                                                membros.size()));

                e.reply(
                        "🎉 **Vencedor:** " +
                                vencedor.getAsMention())
                        .queue();

                return;
            }

            // =================================================
            // FICHAS
            // =================================================

            if (nome.equals("ficha")) {

                User usuario =
                        e.getOption("usuario") != null
                                ? e.getOption("usuario")
                                .getAsUser()
                                : e.getUser();

                String ficha =
                        Config.fichas.getOrDefault(
                                usuario.getId(),
                                "❌ Essa ficha ainda está vazia.");

                e.reply(
                        "📝 **Ficha de " +
                                usuario.getName() +
                                "**\n\n" +
                                ficha)
                        .queue();

                return;
            }

            if (nome.equals("setficha")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                User usuario =
                        e.getOption("usuario")
                                .getAsUser();

                String conteudo =
                        e.getOption("conteudo")
                                .getAsString();

                Config.salvarFicha(
                        usuario.getId(),
                        conteudo);

                e.reply(
                        "✅ Ficha de **" +
                                usuario.getName() +
                                "** atualizada.")
                        .queue();

                return;
            }

            // =================================================
            // WL
            // =================================================

            if (nome.equals("fazerwl")) {

                iniciarWL(e);
                return;
            }

            if (nome.equals("setupwl")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                e.reply(
                        "✅ Painel criado.")
                        .setEphemeral(true)
                        .queue();

                e.getChannel()
                        .sendMessageEmbeds(
                                new EmbedBuilder()
                                        .setColor(
                                                Color.GREEN)
                                        .setTitle(
                                                "📋 WHITELIST")
                                        .setDescription(
                                                "Clique no botão abaixo para iniciar sua whitelist.")
                                        .build())
                        .setActionRow(
                                Button.success(
                                        "wl_iniciar_botao",
                                        "🟢 Iniciar Whitelist"))
                        .queue();

                return;
            }

            // =================================================
            // PARCERIA
            // =================================================

            if (nome.equals("setupparceria")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                e.reply(
                        "✅ Painel criado.")
                        .setEphemeral(true)
                        .queue();

                e.getChannel()
                        .sendMessageEmbeds(
                                new EmbedBuilder()
                                        .setColor(
                                                Color.YELLOW)
                                        .setTitle(
                                                "🤝 PARCERIAS")
                                        .setDescription(
                                                "Clique abaixo para solicitar uma parceria.")
                                        .build())
                        .setActionRow(
                                Button.success(
                                        "pr_iniciar_botao",
                                        "🤝 Fazer Parceria"))
                        .queue();

                return;
            }

            // =================================================
            // CONFIGURAÇÃO DE CANAIS
            // =================================================

            if (nome.equals("setcanaiswl")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                String staffId =
                        e.getOption("canal_staff")
                                .getAsChannel()
                                .getId();

                String resultadoId =
                        e.getOption("canal_resultados")
                                .getAsChannel()
                                .getId();

                Config.salvarCanais(
                        staffId,
                        resultadoId);

                e.reply(
                        "✅ Canais configurados.")
                        .setEphemeral(true)
                        .queue();

                return;
            }

            if (nome.equals("setcanalproibido")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                String canal =
                        e.getOption("canal")
                                .getAsChannel()
                                .getId();

                Config.salvarCanalProibido(
                        canal);

                e.reply(
                        "✅ Canal configurado.")
                        .setEphemeral(true)
                        .queue();

                return;
            }

            if (nome.equals("setboasvindas")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                Config.canalResultadoId =
                        e.getOption("canal")
                                .getAsChannel()
                                .getId();

                Config.salvarCanais(
                        Config.canalWlId,
                        Config.canalResultadoId);

                e.reply(
                        "👋 Canal de boas-vindas configurado.")
                        .setEphemeral(true)
                        .queue();

                return;
            }

            if (nome.equals("setsaida")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                Config.salvarCanalProibido(
                        e.getOption("canal")
                                .getAsChannel()
                                .getId());

                e.reply(
                        "🚪 Canal de saída configurado.")
                        .setEphemeral(true)
                        .queue();

                return;
            }

            // =================================================
            // ANÚNCIO
            // =================================================

            if (nome.equals("anuncio")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                TextChannel canal =
                        e.getOption("canal")
                                .getAsChannel()
                                .asTextChannel();

                String titulo =
                        e.getOption("titulo")
                                .getAsString();

                String mensagem =
                        e.getOption("mensagem")
                                .getAsString();

                canal.sendMessageEmbeds(
                                new EmbedBuilder()
                                        .setColor(
                                                Color.MAGENTA)
                                        .setTitle(
                                                "📢 " +
                                                        titulo)
                                        .setDescription(
                                                mensagem)
                                        .setFooter(
                                                "Blackout Community")
                                        .build())
                        .queue();

                e.reply(
                        "✅ Anúncio enviado.")
                        .setEphemeral(true)
                        .queue();

                return;
            }

            // =================================================
            // EMBED
            // =================================================

            if (nome.equals("embed")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                String titulo =
                        e.getOption("titulo")
                                .getAsString();

                String descricao =
                        e.getOption("descricao")
                                .getAsString();

                Color cor =
                        Color.BLUE;

                String hex =
                        opcaoString(e, "cor_hex");

                if (!hex.isBlank()) {

                    try {

                        String valor =
                                hex.replace("#", "");

                        cor =
                                Color.decode(
                                        "#" + valor);

                    } catch (Exception ignored) {
                    }
                }

                e.getChannel()
                        .sendMessageEmbeds(
                                new EmbedBuilder()
                                        .setColor(cor)
                                        .setTitle(titulo)
                                        .setDescription(
                                                descricao)
                                        .build())
                        .queue();

                e.reply(
                        "✅ Embed enviada.")
                        .setEphemeral(true)
                        .queue();

                return;
            }

            // =================================================
            // SAY
            // =================================================

            if (nome.equals("say")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                String mensagem =
                        e.getOption("mensagem")
                                .getAsString();

                e.getChannel()
                        .sendMessage(mensagem)
                        .queue();

                e.reply(
                        "✅")
                        .setEphemeral(true)
                        .queue();

                return;
            }

            // =================================================
            // LIMPAR
            // =================================================

            if (nome.equals("limpar")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                int quantidade =
                        e.getOption("quantidade")
                                .getAsInt();

                if (quantidade < 1 ||
                        quantidade > 100) {

                    e.reply(
                            "❌ Escolha entre 1 e 100.")
                            .setEphemeral(true)
                            .queue();

                    return;
                }

                e.deferReply()
                        .setEphemeral(true)
                        .queue();

                e.getChannel()
                        .getIterableHistory()
                        .takeAsync(quantidade)
                        .thenAccept(mensagens -> {

                            if (mensagens.isEmpty()) {

                                e.getHook()
                                        .sendMessage(
                                                "❌ Nenhuma mensagem.")
                                        .queue();

                                return;
                            }

                            e.getChannel()
                                    .purgeMessages(
                                            mensagens);

                            e.getHook()
                                    .sendMessage(
                                            "🧹 Apaguei `" +
                                                    mensagens.size() +
                                                    "` mensagens.")
                                    .queue();
                        });

                return;
            }

            // =================================================
            // GERENCIAR
            // =================================================

            if (nome.equals("gerenciar")) {

                if (!staff) {
                    semPermissao(e);
                    return;
                }

                gerenciarTecnicas(e);

                return;
            }

            // =================================================
            // AVALIAR STAFF
            // =================================================

            if (nome.equals("avaliarstaff")) {

                User staffUser =
                        e.getOption("staff")
                                .getAsUser();

                int nota =
                        e.getOption("nota")
                                .getAsInt();

                String comentario =
                        opcaoString(
                                e,
                                "comentario");

                if (nota < 0 || nota > 10) {

                    e.reply(
                            "❌ A nota deve estar entre 0 e 10.")
                            .setEphemeral(true)
                            .queue();

                    return;
                }

                String canal =
                        Config.canalResultadoId;

                TextChannel resultado =
                        e.getGuild()
                                .getTextChannelById(
                                        canal);

                EmbedBuilder embed =
                        new EmbedBuilder()
                                .setColor(
                                        Color.YELLOW)
                                .setTitle(
                                        "⭐ Avaliação da Staff")
                                .addField(
                                        "Staff",
                                        staffUser.getAsMention(),
                                        true)
                                .addField(
                                        "Nota",
                                        nota + "/10",
                                        true)
                                .addField(
                                        "Por",
                                        e.getUser()
                                                .getAsMention(),
                                        true)
                                .setDescription(
                                        comentario.isBlank()
                                                ? "Sem comentário."
                                                : comentario);

                if (resultado != null) {

                    resultado
                            .sendMessageEmbeds(
                                    embed.build())
                            .queue();
                }

                e.reply(
                        "⭐ Avaliação enviada.")
                        .setEphemeral(true)
                        .queue();

                return;
            }

            // =================================================
            // TÉCNICAS
            // =================================================

            if (nome.equals("vertecnicas")) {

                StringBuilder texto =
                        new StringBuilder(
                                "📚 **TÉCNICAS E VAGAS**\n\n");

                if (tecnicas.isEmpty()) {

                    texto.append(
                            "❌ Nenhuma técnica cadastrada.");

                } else {

                    for (Map.Entry<String, int[]> entrada :
                            tecnicas.entrySet()) {

                        int ocupadas =
                                entrada.getValue()[0];

                        int total =
                                entrada.getValue()[1];

                        int livres =
                                Math.max(
                                        0,
                                        total - ocupadas);

                        texto.append("**")
                                .append(
                                        entrada.getKey())
                                .append("**\n")
                                .append("👥 ")
                                .append(ocupadas)
                                .append("/")
                                .append(total)
                                .append("\n")
                                .append("🟢 Livres: ")
                                .append(livres)
                                .append("\n\n");
                    }
                }

                e.reply(
                        texto.toString())
                        .queue();

                return;
            }

        } catch (Exception ex) {

            System.err.println(
                    "[COMMAND] Erro executando /" +
                            e.getName());

            ex.printStackTrace();

            try {

                if (!e.isAcknowledged()) {

                    e.reply(
                            "❌ Ocorreu um erro ao executar o comando.")
                            .setEphemeral(true)
                            .queue();

                } else {

                    e.getHook()
                            .sendMessage(
                                    "❌ Ocorreu um erro ao executar o comando.")
                            .setEphemeral(true)
                            .queue();
                }

            } catch (Exception ignored) {
            }
        }
    }

    // =========================================================
    // PERMISSÕES
    // =========================================================

    private boolean temPermissaoStaff(
            SlashCommandInteractionEvent e) {

        if (e.getUser()
                .getId()
                .equals(SUPER_USER_ID)) {

            return true;
        }

        Member membro =
                e.getMember();

        if (membro == null) {
            return false;
        }

        if (membro.isOwner()) {
            return true;
        }

        if (membro.hasPermission(
                Permission.ADMINISTRATOR)) {

            return true;
        }

        return membro.getRoles()
                .stream()
                .anyMatch(role ->
                        role.getName()
                                .toLowerCase()
                                .contains("staff"));
    }

    private void semPermissao(
            SlashCommandInteractionEvent e) {

        e.reply(
                "❌ Você não possui permissão para isso.")
                .setEphemeral(true)
                .queue();
    }

    private String opcaoString(
            SlashCommandInteractionEvent e,
            String nome) {

        OptionMapping option =
                e.getOption(nome);

        if (option == null) {
            return "";
        }

        return option.getAsString();
    }

    // =========================================================
    // GERENCIAR TÉCNICAS
    // =========================================================

    private void gerenciarTecnicas(
            SlashCommandInteractionEvent e) {

        String sub =
                e.getSubcommandName();

        if (sub.equals("addtecnica")) {

            String nome =
                    e.getOption("nome")
                            .getAsString();

            int total =
                    e.getOption("totais")
                            .getAsInt();

            if (total < 1) {

                e.reply(
                        "❌ O total deve ser maior que 0.")
                        .setEphemeral(true)
                        .queue();

                return;
            }

            tecnicas.put(
                    nome,
                    new int[]{0, total});

            salvarDados();

            e.reply(
                    "✅ Técnica **" +
                            nome +
                            "** adicionada com `" +
                            total +
                            "` vagas.")
                    .queue();

            return;
        }

        if (sub.equals("remover_tecnica")) {

            String nome =
                    e.getOption("nome")
                            .getAsString();

            String chave =
                    encontrarChave(nome);

            if (chave == null) {

                e.reply(
                        "❌ Técnica não encontrada.")
                        .setEphemeral(true)
                        .queue();

                return;
            }

            tecnicas.remove(chave);

            salvarDados();

            e.reply(
                    "🗑️ Técnica removida.")
                    .queue();

            return;
        }

        if (sub.equals("remover_membro")) {

            String nome =
                    e.getOption("tecnica")
                            .getAsString();

            String chave =
                    encontrarChave(nome);

            if (chave == null) {

                e.reply(
                        "❌ Técnica não encontrada.")
                        .setEphemeral(true)
                        .queue();

                return;
            }

            int[] dados =
                    tecnicas.get(chave);

            if (dados[0] > 0) {
                dados[0]--;
            }

            salvarDados();

            e.reply(
                    "✅ Uma vaga foi liberada em **" +
                            chave +
                            "**.")
                    .queue();

            return;
        }

        if (sub.equals("setvagas")) {

            String nome =
                    e.getOption("nome")
                            .getAsString();

            int ocupadas =
                    e.getOption("ocupadas")
                            .getAsInt();

            String chave =
                    encontrarChave(nome);

            if (chave == null) {

                e.reply(
                        "❌ Técnica não encontrada.")
                        .setEphemeral(true)
                        .queue();

                return;
            }

            int[] dados =
                    tecnicas.get(chave);

            if (ocupadas < 0 ||
                    ocupadas > dados[1]) {

                e.reply(
                        "❌ A quantidade ocupada deve ficar entre 0 e " +
                                dados[1] +
                                ".")
                        .setEphemeral(true)
                        .queue();

                return;
            }

            dados[0] =
                    ocupadas;

            salvarDados();

            e.reply(
                    "🔄 Vagas atualizadas.")
                    .queue();
        }
    }

    // =========================================================
    // WL
    // =========================================================

    private void iniciarWL(
            SlashCommandInteractionEvent e) {

        User usuario =
                e.getUser();

        usuario.openPrivateChannel()
                .queue(

                        canal -> {

                            List<String> dados =
                                    new ArrayList<>();

                            dados.add("0");

                            sessoesWl.put(
                                    usuario.getId(),
                                    dados);

                            canal.sendMessageEmbeds(
                                            new EmbedBuilder()
                                                    .setColor(
                                                            Color.BLUE)
                                                    .setTitle(
                                                            "📋 WHITELIST")
                                                    .setDescription(
                                                            "Responda às perguntas abaixo. Você será encaminhado para a próxima automaticamente.")
                                                    .build())
                                    .queue();

                            canal.sendMessage(
                                            PERGUNTAS[0])
                                    .queue();

                            e.reply(
                                    "📩 Te enviei a whitelist no privado.")
                                    .setEphemeral(true)
                                    .queue();
                        },

                        erro -> {

                            e.reply(
                                    "❌ Não consegui enviar DM. Ative suas mensagens privadas.")
                                    .setEphemeral(true)
                                    .queue();
                        });
    }

    // =========================================================
    // BUTTONS
    // =========================================================

    @Override
    public void onButtonInteraction(
            ButtonInteractionEvent e) {

        try {

            String id =
                    e.getComponentId();

            if (id.equals(
                    "wl_iniciar_botao")) {

                iniciarWLBotao(e);

                return;
            }

            if (id.equals(
                    "pr_iniciar_botao")) {

                criarCanalParceria(e);

                return;
            }

        } catch (Exception ex) {

            System.err.println(
                    "[BUTTON] Erro:");

            ex.printStackTrace();
        }
    }

    private void iniciarWLBotao(
            ButtonInteractionEvent e) {

        User usuario =
                e.getUser();

        usuario.openPrivateChannel()
                .queue(

                        canal -> {

                            List<String> dados =
                                    new ArrayList<>();

                            dados.add("0");

                            sessoesWl.put(
                                    usuario.getId(),
                                    dados);

                            canal.sendMessage(
                                            PERGUNTAS[0])
                                    .queue();

                            e.reply(
                                    "📩 Verifique sua DM.")
                                    .setEphemeral(true)
                                    .queue();
                        },

                        erro -> {

                            e.reply(
                                    "❌ Não consegui abrir sua DM.")
                                    .setEphemeral(true)
                                    .queue();
                        });
    }

    // =========================================================
    // PARCERIA
    // =========================================================

    private void criarCanalParceria(
            ButtonInteractionEvent e) {

        e.reply(
                "📥 Criando canal de parceria...")
                .setEphemeral(true)
                .queue();

        String nome =
                e.getUser()
                        .getName()
                        .toLowerCase()
                        .replaceAll(
                                "[^a-z0-9-]",
                                "-");

        if (nome.length() > 30) {
            nome = nome.substring(0, 30);
        }

        final String nomeFinal =
                "parceria-" + nome;

        e.getGuild()
                .createTextChannel(
                        nomeFinal)
                .queue(
                        canal -> {

                            configurarCanalStaff(
                                    canal);

                            canal.sendMessageEmbeds(
                                            new EmbedBuilder()
                                                    .setColor(
                                                            Color.YELLOW)
                                                    .setTitle(
                                                            "🤝 PARCERIA")
                                                    .setDescription(
                                                            "**Blackout Community**\n\nEnvie as informações da parceria neste canal.")
                                                    .build())
                                    .queue();
                        },

                        erro -> {

                            System.err.println(
                                    "[PARCERIA] Erro:");

                            erro.printStackTrace();
                        });
    }

    private void configurarCanalStaff(
            TextChannel canal) {

        for (Role role :
                canal.getGuild().getRoles()) {

            if (role.getName()
                    .toLowerCase()
                    .contains("staff")) {

                canal.upsertPermissionOverride(
                                role)
                        .grant(
                                Permission.VIEW_CHANNEL,
                                Permission.MESSAGE_SEND)
                        .queue();
            }
        }
    }

    // =========================================================
    // MENSAGENS / WL
    // =========================================================

    @Override
    public void onMessageReceived(
            MessageReceivedEvent e) {

        if (e.getAuthor().isBot()) {
            return;
        }

        if (e.isFromGuild()) {
            return;
        }

        String id =
                e.getAuthor().getId();

        List<String> dados =
                sessoesWl.get(id);

        if (dados == null) {
            return;
        }

        try {

            int etapa =
                    Integer.parseInt(
                            dados.get(0));

            dados.add(
                    e.getMessage()
                            .getContentRaw());

            etapa++;

            if (etapa < PERGUNTAS.length) {

                dados.set(
                        0,
                        String.valueOf(
                                etapa));

                e.getChannel()
                        .sendMessage(
                                PERGUNTAS[etapa])
                        .queue();

            } else {

                sessoesWl.remove(id);

                enviarResultadoWL(
                        e.getAuthor(),
                        dados);

                e.getChannel()
                        .sendMessage(
                                "✅ **Whitelist enviada para avaliação!**")
                        .queue();
            }

        } catch (Exception ex) {

            sessoesWl.remove(id);

            System.err.println(
                    "[WL] Erro:");

            ex.printStackTrace();
        }
    }

    // =========================================================
    // RESULTADO WL
    // =========================================================

    private void enviarResultadoWL(
            User usuario,
            List<String> respostas) {

        if (Config.canalWlId == null ||
                Config.canalWlId.isBlank()) {

            System.out.println(
                    "[WL] Canal da staff não configurado.");

            return;
        }

        for (Guild guild :
                JDAHolder.getJDA() != null
                        ? JDAHolder.getJDA()
                        .getGuilds()
                        : Collections.<Guild>emptyList()) {

            TextChannel canal =
                    guild.getTextChannelById(
                            Config.canalWlId);

            if (canal == null) {
                continue;
            }

            EmbedBuilder embed =
                    new EmbedBuilder()
                            .setColor(Color.BLUE)
                            .setTitle(
                                    "📋 NOVA WHITELIST")
                            .setAuthor(
                                    usuario.getAsTag(),
                                    null,
                                    usuario.getEffectiveAvatarUrl())
                            .setDescription(
                                    "Usuário: " +
                                            usuario.getAsMention());

            for (int i = 1;
                 i < respostas.size();
                 i++) {

                String resposta =
                        respostas.get(i);

                int numero =
                        i;

                if (resposta.length() > 1000) {
                    resposta =
                            resposta.substring(
                                    0,
                                    1000);
                }

                embed.addField(
                        PERGUNTAS[numero - 1],
                        resposta.isBlank()
                                ? "Sem resposta."
                                : resposta,
                        false);
            }

            canal.sendMessageEmbeds(
                            embed.build())
                    .queue();

            break;
        }
    }

    // =========================================================
    // MEMBER JOIN
    // =========================================================

    @Override
    public void onGuildMemberJoin(
            GuildMemberJoinEvent e) {

        try {

            if (Config.canalResultadoId == null ||
                    Config.canalResultadoId.isBlank()) {

                return;
            }

            TextChannel canal =
                    e.getGuild()
                            .getTextChannelById(
                                    Config.canalResultadoId);

            if (canal == null) {
                return;
            }

            canal.sendMessageEmbeds(
                            new EmbedBuilder()
                                    .setColor(
                                            Color.GREEN)
                                    .setTitle(
                                            "👋 Bem-vindo à Blackout Community!")
                                    .setDescription(
                                            "Olá " +
                                                    e.getMember()
                                                            .getAsMention() +
                                                    ", seja bem-vindo!")
                                    .setThumbnail(
                                            e.getUser()
                                                    .getEffectiveAvatarUrl())
                                    .build())
                    .queue();

        } catch (Exception ex) {

            System.err.println(
                    "[WELCOME] Erro:");

            ex.printStackTrace();
        }
    }

    // =========================================================
    // MEMBER REMOVE
    // =========================================================

    @Override
    public void onGuildMemberRemove(
            GuildMemberRemoveEvent e) {

        try {

            if (Config.canalProibidoId == null ||
                    Config.canalProibidoId.isBlank()) {

                return;
            }

            TextChannel canal =
                    e.getGuild()
                            .getTextChannelById(
                                    Config.canalProibidoId);

            if (canal == null) {
                return;
            }

            canal.sendMessageEmbeds(
                            new EmbedBuilder()
                                    .setColor(
                                            Color.RED)
                                    .setTitle(
                                            "🚪 Membro saiu")
                                    .setDescription(
                                            "**" +
                                                    e.getUser()
                                                            .getName() +
                                                    "** saiu da comunidade.")
                                    .build())
                    .queue();

        } catch (Exception ex) {

            System.err.println(
                    "[GOODBYE] Erro:");

            ex.printStackTrace();
        }
    }

    // =========================================================
    // PERSISTÊNCIA
    // =========================================================

    private synchronized void salvarDados() {

        salvarTecnicas();

        salvarEconomia();

        salvarWarns();
    }

    private void salvarTecnicas() {

        try (BufferedWriter bw =
                     new BufferedWriter(
                             new OutputStreamWriter(
                                     new FileOutputStream(
                                             ARQUIVO_TECNICAS),
                                     StandardCharsets.UTF_8))) {

            for (Map.Entry<String, int[]> entrada :
                    tecnicas.entrySet()) {

                int[] dados =
                        entrada.getValue();

                bw.write(
                        entrada.getKey()
                                .replace(";", ","));

                bw.write(";");

                bw.write(
                        String.valueOf(
                                dados[0]));

                bw.write(";");

                bw.write(
                        String.valueOf(
                                dados[1]));

                bw.newLine();
            }

        } catch (Exception e) {

            System.err.println(
                    "[SAVE] Erro técnicas:");

            e.printStackTrace();
        }
    }

    private void salvarEconomia() {

        try (BufferedWriter bw =
                     new BufferedWriter(
                             new OutputStreamWriter(
                                     new FileOutputStream(
                                             ARQUIVO_ECONOMIA),
                                     StandardCharsets.UTF_8))) {

            for (Map.Entry<String, Integer> entrada :
                    carteiras.entrySet()) {

                bw.write(
                        entrada.getKey());

                bw.write(";");

                bw.write(
                        String.valueOf(
                                entrada.getValue()));

                bw.newLine();
            }

        } catch (Exception e) {

            System.err.println(
                    "[SAVE] Erro economia:");

            e.printStackTrace();
        }
    }

    private void salvarWarns() {

        try (BufferedWriter bw =
                     new BufferedWriter(
                             new OutputStreamWriter(
                                     new FileOutputStream(
                                             ARQUIVO_WARNS),
                                     StandardCharsets.UTF_8))) {

            for (Map.Entry<String, List<String>> entrada :
                    listaWarns.entrySet()) {

                bw.write(
                        entrada.getKey());

                bw.write(";");

                bw.write(
                        String.join(
                                "|||",
                                entrada.getValue())
                                .replace("\n", " "));

                bw.newLine();
            }

        } catch (Exception e) {

            System.err.println(
                    "[SAVE] Erro warns:");

            e.printStackTrace();
        }
    }

    // =========================================================
    // CARREGAR
    // =========================================================

    private void carregarDados() {

        carregarTecnicas();

        carregarEconomia();

        carregarWarns();
    }

    private void carregarTecnicas() {

        File arquivo =
                new File(
                        ARQUIVO_TECNICAS);

        if (!arquivo.exists()) {
            return;
        }

        try (BufferedReader br =
                     new BufferedReader(
                             new InputStreamReader(
                                     new FileInputStream(
                                             arquivo),
                                     StandardCharsets.UTF_8))) {

            String linha;

            while ((linha =
                    br.readLine()) != null) {

                String[] partes =
                        linha.split(";");

                if (partes.length != 3) {
                    continue;
                }

                try {

                    String nome =
                            partes[0];

                    int ocupadas =
                            Integer.parseInt(
                                    partes[1]);

                    int total =
                            Integer.parseInt(
                                    partes[2]);

                    tecnicas.put(
                            nome,
                            new int[]{
                                    ocupadas,
                                    total
                            });

                } catch (NumberFormatException ignored) {
                }
            }

        } catch (Exception e) {

            System.err.println(
                    "[LOAD] Erro técnicas:");

            e.printStackTrace();
        }
    }

    private void carregarEconomia() {

        File arquivo =
                new File(
                        ARQUIVO_ECONOMIA);

        if (!arquivo.exists()) {
            return;
        }

        try (BufferedReader br =
                     new BufferedReader(
                             new InputStreamReader(
                                     new FileInputStream(
                                             arquivo),
                                     StandardCharsets.UTF_8))) {

            String linha;

            while ((linha =
                    br.readLine()) != null) {

                String[] partes =
                        linha.split(";");

                if (partes.length != 2) {
                    continue;
                }

                try {

                    carteiras.put(
                            partes[0],
                            Integer.parseInt(
                                    partes[1]));

                } catch (NumberFormatException ignored) {
                }
            }

        } catch (Exception e) {

            System.err.println(
                    "[LOAD] Erro economia:");

            e.printStackTrace();
        }
    }

    private void carregarWarns() {

        File arquivo =
                new File(
                        ARQUIVO_WARNS);

        if (!arquivo.exists()) {
            return;
        }

        try (BufferedReader br =
                     new BufferedReader(
                             new InputStreamReader(
                                     new FileInputStream(
                                             arquivo),
                                     StandardCharsets.UTF_8))) {

            String linha;

            while ((linha =
                    br.readLine()) != null) {

                String[] partes =
                        linha.split(";", 2);

                if (partes.length != 2) {
                    continue;
                }

                List<String> warns =
                        new ArrayList<>(
                                Arrays.asList(
                                        partes[1]
                                                .split(
                                                        "\\|\\|\\|")));

                listaWarns.put(
                        partes[0],
                        warns);
            }

        } catch (Exception e) {

            System.err.println(
                    "[LOAD] Erro warns:");

            e.printStackTrace();
        }
    }

    // =========================================================
    // BUSCAR TÉCNICA
    // =========================================================

    private String encontrarChave(
            String nome) {

        for (String chave :
                tecnicas.keySet()) {

            if (chave.equalsIgnoreCase(
                    nome)) {

                return chave;
            }
        }

        return null;
    }

    // =========================================================
    // HTTP SERVER
    // =========================================================

    private static class HttpServerInterno {

        private final int porta;

        private ServerSocket serverSocket;

        HttpServerInterno(int porta) {

            this.porta = porta;
        }

        void start() throws IOException {

            serverSocket =
                    new ServerSocket(
                            porta,
                            50,
                            InetAddress.getByName(
                                    "0.0.0.0"));

            Thread thread =
                    new Thread(
                            this::loop,
                            "render-http-server");

            thread.setDaemon(false);

            thread.start();
        }

        private void loop() {

            while (true) {

                try {

                    Socket socket =
                            serverSocket.accept();

                    responder(socket);

                } catch (Exception e) {

                    if (serverSocket != null &&
                            !serverSocket.isClosed()) {
                        System.err.println(
                                "[WEB] Erro no servidor:");

                        e.printStackTrace();
                    }
                }
            }
        }

        private void responder(
                Socket socket) {

            try (Socket s = socket;
                 BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(
                                         s.getInputStream()));
                 PrintWriter writer =
                         new PrintWriter(
                                 new OutputStreamWriter(
                                         s.getOutputStream(),
                                         StandardCharsets.UTF_8))) {

                String primeiraLinha =
                        reader.readLine();

                if (primeiraLinha == null) {
                    return;
                }

                String corpo =
                        "Blackout Community Bot - ONLINE";

                String resposta =
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/plain; charset=UTF-8\r\n" +
                                "Content-Length: " +
                                corpo.getBytes(
                                        StandardCharsets.UTF_8)
                                        .length +
                                "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n" +
                                corpo;

                writer.print(
                        resposta);

                writer.flush();

            } catch (Exception e) {

                System.err.println(
                        "[WEB] Erro respondendo HTTP:");

                System.err.println(
                        e.getMessage());
            }
        }
    }

    // =========================================================
    // JDA HOLDER
    // =========================================================

    private static class JDAHolder {

        private static volatile JDA jda;

        static JDA getJDA() {

            return jda;
        }
    }
}
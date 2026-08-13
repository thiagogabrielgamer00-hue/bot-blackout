import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandRegistry {

    private static final String ID_SERVIDOR =
            "1418818172827734057";

    public static void registrar(JDA jda) {

        System.out.println("[COMMANDS] Registrando comandos...");

        // =====================================================
        // GERENCIAMENTO
        // =====================================================

        SubcommandData removerTecnica =
                new SubcommandData(
                        "remover_tecnica",
                        "Exclui uma técnica")
                        .addOption(
                                OptionType.STRING,
                                "nome",
                                "Nome da técnica",
                                true);

        SubcommandData removerMembro =
                new SubcommandData(
                        "remover_membro",
                        "Libera uma vaga")
                        .addOption(
                                OptionType.STRING,
                                "tecnica",
                                "Nome da técnica",
                                true);

        SubcommandData addTecnica =
                new SubcommandData(
                        "addtecnica",
                        "Adiciona uma técnica")
                        .addOption(
                                OptionType.STRING,
                                "nome",
                                "Nome da técnica",
                                true)
                        .addOption(
                                OptionType.INTEGER,
                                "totais",
                                "Total de vagas",
                                true);

        SubcommandData setVagas =
                new SubcommandData(
                        "setvagas",
                        "Define vagas ocupadas")
                        .addOption(
                                OptionType.STRING,
                                "nome",
                                "Nome da técnica",
                                true)
                        .addOption(
                                OptionType.INTEGER,
                                "ocupadas",
                                "Quantidade ocupada",
                                true);

        Commands gerenciar =
                Commands.slash(
                        "gerenciar",
                        "Gerenciamento administrativo")
                        .addSubcommands(
                                removerTecnica,
                                removerMembro,
                                addTecnica,
                                setVagas);

        // =====================================================
        // CARGOS
        // =====================================================

        SubcommandData roleAdd =
                new SubcommandData(
                        "add",
                        "Adiciona um cargo")
                        .addOption(
                                OptionType.USER,
                                "usuario",
                                "Membro",
                                true)
                        .addOption(
                                OptionType.ROLE,
                                "cargo",
                                "Cargo",
                                true);

        SubcommandData roleRemove =
                new SubcommandData(
                        "remover",
                        "Remove um cargo")
                        .addOption(
                                OptionType.USER,
                                "usuario",
                                "Membro",
                                true)
                        .addOption(
                                OptionType.ROLE,
                                "cargo",
                                "Cargo",
                                true);

        Commands role =
                Commands.slash(
                        "role",
                        "Gerenciamento de cargos")
                        .addSubcommands(
                                roleAdd,
                                roleRemove);

        // =====================================================
        // COMANDOS PRINCIPAIS
        // =====================================================

        var comandos = new java.util.ArrayList<
                net.dv8tion.jda.api.interactions.commands.build.CommandData>();

        comandos.add(
                Commands.slash(
                        "ficha",
                        "Exibe a ficha")
                        .addOption(
                                OptionType.USER,
                                "usuario",
                                "Membro",
                                false));

        comandos.add(
                Commands.slash(
                        "setficha",
                        "Edita uma ficha")
                        .addOption(
                                OptionType.USER,
                                "usuario",
                                "Membro",
                                true)
                        .addOption(
                                OptionType.STRING,
                                "conteudo",
                                "Conteúdo",
                                true));

        comandos.add(
                Commands.slash(
                        "fazerwl",
                        "Inicia a whitelist via DM"));

        comandos.add(
                Commands.slash(
                        "setupwl",
                        "Cria o painel de whitelist"));

        comandos.add(
                Commands.slash(
                        "setupparceria",
                        "Cria o painel de parceria"));

        comandos.add(
                Commands.slash(
                        "setcanaiswl",
                        "Configura canais da WL")
                        .addOption(
                                OptionType.CHANNEL,
                                "canal_staff",
                                "Canal da staff",
                                true)
                        .addOption(
                                OptionType.CHANNEL,
                                "canal_resultados",
                                "Canal de resultados",
                                true));

        comandos.add(
                Commands.slash(
                        "setcanalproibido",
                        "Configura canal de saída")
                        .addOption(
                                OptionType.CHANNEL,
                                "canal",
                                "Canal",
                                true));

        comandos.add(
                Commands.slash(
                        "avaliarstaff",
                        "Avalia um membro da staff")
                        .addOption(
                                OptionType.USER,
                                "staff",
                                "Staff",
                                true)
                        .addOption(
                                OptionType.INTEGER,
                                "nota",
                                "Nota de 0 a 10",
                                true)
                        .addOption(
                                OptionType.STRING,
                                "comentario",
                                "Comentário",
                                false));

        comandos.add(
                Commands.slash(
                        "vertecnicas",
                        "Lista técnicas e vagas"));

        // =====================================================
        // ADMINISTRAÇÃO
        // =====================================================

        comandos.add(
                Commands.slash(
                        "anuncio",
                        "Envia um anúncio")
                        .addOption(
                                OptionType.CHANNEL,
                                "canal",
                                "Canal",
                                true)
                        .addOption(
                                OptionType.STRING,
                                "titulo",
                                "Título",
                                true)
                        .addOption(
                                OptionType.STRING,
                                "mensagem",
                                "Mensagem",
                                true));

        comandos.add(
                Commands.slash(
                        "embed",
                        "Cria uma embed")
                        .addOption(
                                OptionType.STRING,
                                "titulo",
                                "Título",
                                true)
                        .addOption(
                                OptionType.STRING,
                                "descricao",
                                "Descrição",
                                true)
                        .addOption(
                                OptionType.STRING,
                                "cor_hex",
                                "Cor HEX",
                                false));

        comandos.add(
                Commands.slash(
                        "limpar",
                        "Apaga mensagens")
                        .addOption(
                                OptionType.INTEGER,
                                "quantidade",
                                "De 1 a 100",
                                true));

        comandos.add(
                Commands.slash(
                        "say",
                        "Faz o bot falar")
                        .addOption(
                                OptionType.STRING,
                                "mensagem",
                                "Mensagem",
                                true));

        comandos.add(
                Commands.slash(
                        "ban",
                        "Bane um usuário")
                        .addOption(
                                OptionType.USER,
                                "usuario",
                                "Usuário",
                                true)
                        .addOption(
                                OptionType.STRING,
                                "motivo",
                                "Motivo",
                                false));

        comandos.add(
                Commands.slash(
                        "kick",
                        "Expulsa um usuário")
                        .addOption(
                                OptionType.USER,
                                "usuario",
                                "Usuário",
                                true)
                        .addOption(
                                OptionType.STRING,
                                "motivo",
                                "Motivo",
                                false));

        comandos.add(
                Commands.slash(
                        "mute",
                        "Aplica timeout")
                        .addOption(
                                OptionType.USER,
                                "usuario",
                                "Usuário",
                                true)
                        .addOption(
                                OptionType.INTEGER,
                                "minutos",
                                "Minutos",
                                true)
                        .addOption(
                                OptionType.STRING,
                                "motivo",
                                "Motivo",
                                false));

        comandos.add(
                Commands.slash(
                        "unmute",
                        "Remove timeout")
                        .addOption(
                                OptionType.USER,
                                "usuario",
                                "Usuário",
                                true));

        comandos.add(
                Commands.slash(
                        "warn",
                        "Aplica advertência")
                        .addOption(
                                OptionType.USER,
                                "usuario",
                                "Usuário",
                                true)
                        .addOption(
                                OptionType.STRING,
                                "motivo",
                                "Motivo",
                                true));

        comandos.add(
                Commands.slash(
                        "infractions",
                        "Lista advertências")
                        .addOption(
                                OptionType.USER,
                                "usuario",
                                "Usuário",
                                true));

        comandos.add(
                Commands.slash(
                        "lock",
                        "Bloqueia o canal"));

        comandos.add(
                Commands.slash(
                        "unlock",
                        "Desbloqueia o canal"));

        comandos.add(
                Commands.slash(
                        "slowmode",
                        "Define slowmode")
                        .addOption(
                                OptionType.INTEGER,
                                "segundos",
                                "Segundos",
                                true));

        // =====================================================
        // MÚSICA
        // =====================================================

        comandos.add(
                Commands.slash(
                        "play",
                        "Toca uma música")
                        .addOption(
                                OptionType.STRING,
                                "link",
                                "URL ou busca",
                                true));

        comandos.add(
                Commands.slash(
                        "skip",
                        "Pula a música"));

        comandos.add(
                Commands.slash(
                        "stop",
                        "Para a música"));

        comandos.add(
                Commands.slash(
                        "queue",
                        "Mostra a fila"));

        comandos.add(
                Commands.slash(
                        "pause",
                        "Pausa a música"));

        comandos.add(
                Commands.slash(
                        "resume",
                        "Continua a música"));

        comandos.add(
                Commands.slash(
                        "volume",
                        "Altera o volume")
                        .addOption(
                                OptionType.INTEGER,
                                "volume",
                                "0 a 100",
                                true));

        comandos.add(
                Commands.slash(
                        "nowplaying",
                        "Mostra música atual"));

        // =====================================================
        // ECONOMIA
        // =====================================================

        comandos.add(
                Commands.slash(
                        "daily",
                        "Recebe moedas diariamente"));

        comandos.add(
                Commands.slash(
                        "bal",
                        "Mostra saldo")
                        .addOption(
                                OptionType.USER,
                                "usuario",
                                "Usuário",
                                false));

        comandos.add(
                Commands.slash(
                        "pay",
                        "Transfere moedas")
                        .addOption(
                                OptionType.USER,
                                "usuario",
                                "Destinatário",
                                true)
                        .addOption(
                                OptionType.INTEGER,
                                "quantidade",
                                "Quantidade",
                                true));

        // =====================================================
        // DIVERSÃO
        // =====================================================

        comandos.add(
                Commands.slash(
                        "piada",
                        "Conta uma piada"));

        comandos.add(
                Commands.slash(
                        "ascii",
                        "Texto estilizado")
                        .addOption(
                                OptionType.STRING,
                                "texto",
                                "Texto",
                                true));

        comandos.add(
                Commands.slash(
                        "pergunta",
                        "Bola 8")
                        .addOption(
                                OptionType.STRING,
                                "texto",
                                "Pergunta",
                                true));

        comandos.add(
                Commands.slash(
                        "ppt",
                        "Pedra Papel Tesoura")
                        .addOption(
                                OptionType.STRING,
                                "escolha",
                                "pedra, papel ou tesoura",
                                true));

        comandos.add(
                Commands.slash(
                        "ship",
                        "Compatibilidade")
                        .addOption(
                                OptionType.USER,
                                "usuario1",
                                "Pessoa 1",
                                true)
                        .addOption(
                                OptionType.USER,
                                "usuario2",
                                "Pessoa 2",
                                false));

        comandos.add(
                Commands.slash(
                        "brincar",
                        "Interação divertida")
                        .addOption(
                                OptionType.STRING,
                                "acao",
                                "Ação",
                                true)
                        .addOption(
                                OptionType.USER,
                                "usuario",
                                "Alvo",
                                true));

        comandos.add(
                Commands.slash(
                        "moeda",
                        "Cara ou coroa"));

        comandos.add(
                Commands.slash(
                        "dados",
                        "Rola dados")
                        .addOption(
                                OptionType.INTEGER,
                                "lados",
                                "Número de lados",
                                true));

        comandos.add(
                Commands.slash(
                        "avatar",
                        "Mostra avatar")
                        .addOption(
                                OptionType.USER,
                                "usuario",
                                "Usuário",
                                false));

        comandos.add(
                Commands.slash(
                        "userinfo",
                        "Informações do usuário")
                        .addOption(
                                OptionType.USER,
                                "usuario",
                                "Usuário",
                                false));

        comandos.add(
                Commands.slash(
                        "serverinfo",
                        "Informações do servidor"));

        comandos.add(
                Commands.slash(
                        "sorteio",
                        "Sorteia um membro"));

        comandos.add(
                Commands.slash(
                        "ping",
                        "Mostra o ping"));

        // =====================================================
        // BOAS-VINDAS
        // =====================================================

        comandos.add(
                Commands.slash(
                        "setboasvindas",
                        "Define canal de entrada")
                        .addOption(
                                OptionType.CHANNEL,
                                "canal",
                                "Canal",
                                true));

        comandos.add(
                Commands.slash(
                        "setsaida",
                        "Define canal de saída")
                        .addOption(
                                OptionType.CHANNEL,
                                "canal",
                                "Canal",
                                true));

        // =====================================================
        // REGISTRO
        // =====================================================

        comandos.add(role);
        comandos.add(gerenciar);

        var guild = jda.getGuildById(ID_SERVIDOR);

        if (guild != null) {

            guild.updateCommands()
                    .addCommands(comandos)
                    .queue(
                            success -> System.out.println(
                                    "[COMMANDS] Comandos registrados no servidor."),
                            error -> {
                                System.err.println(
                                        "[COMMANDS] Erro ao registrar comandos:");
                                error.printStackTrace();
                            });

        } else {

            System.err.println(
                    "[COMMANDS] Servidor " + ID_SERVIDOR +
                    " não encontrado.");

            // Fallback global
            jda.updateCommands()
                    .addCommands(comandos)
                    .queue(
                            success -> System.out.println(
                                    "[COMMANDS] Comandos registrados globalmente."),
                            error -> {
                                System.err.println(
                                        "[COMMANDS] Erro no registro global:");
                                error.printStackTrace();
                            });
        }
    }
}
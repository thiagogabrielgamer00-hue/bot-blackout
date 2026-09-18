package br.blackout.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandRegistry {

    public static void register(
            JDA jda
    ) {

        jda.updateCommands().addCommands(

                // =========================
                // SPIN
                // =========================

                Commands.slash(
                        "spin",
                        "Rola reino, classe ou subclasse"
                )
                .addOption(
                        OptionType.STRING,
                        "tipo",
                        "reino, classe, subclasse ou completo",
                        true
                ),

                // =========================
                // PERFIL
                // =========================

                Commands.slash(
                        "perfil",
                        "Mostra o perfil medieval de um jogador"
                )
                .addOption(
                        OptionType.USER,
                        "usuario",
                        "Jogador que deseja consultar",
                        false
                ),

                // =========================
                // GIROS
                // =========================

                Commands.slash(
                        "giros",
                        "Gerencia os giros de um jogador"
                )
                .addOption(
                        OptionType.USER,
                        "usuario",
                        "Jogador que receberá os giros",
                        true
                )
                .addOption(
                        OptionType.STRING,
                        "tipo",
                        "reino, classe, subclasse ou todos",
                        true
                )
                .addOption(
                        OptionType.INTEGER,
                        "quantidade",
                        "Quantidade de giros",
                        true
                ),

                // =========================
                // REINO
                // =========================

                Commands.slash(
                        "reino",
                        "Gerencia os reinos"
                )
                .addSubcommands(

                        new SubcommandData(
                                "adicionar",
                                "Adiciona um novo reino"
                        )
                        .addOption(
                                OptionType.STRING,
                                "nome",
                                "Nome do reino",
                                true
                        ),

                        new SubcommandData(
                                "remover",
                                "Remove um reino"
                        )
                        .addOption(
                                OptionType.STRING,
                                "nome",
                                "Nome do reino",
                                true
                        ),

                        new SubcommandData(
                                "listar",
                                "Lista todos os reinos"
                        )
                ),

                // =========================
                // CLASSE
                // =========================

                Commands.slash(
                        "classe",
                        "Gerencia as classes"
                )
                .addSubcommands(

                        new SubcommandData(
                                "adicionar",
                                "Adiciona uma nova classe"
                        )
                        .addOption(
                                OptionType.STRING,
                                "nome",
                                "Nome da classe",
                                true
                        ),

                        new SubcommandData(
                                "remover",
                                "Remove uma classe"
                        )
                        .addOption(
                                OptionType.STRING,
                                "nome",
                                "Nome da classe",
                                true
                        ),

                        new SubcommandData(
                                "listar",
                                "Lista todas as classes"
                        )
                ),

                // =========================
                // SUBCLASSE
                // =========================

                Commands.slash(
                        "subclasse",
                        "Gerencia as subclasses"
                )
                .addSubcommands(

                        new SubcommandData(
                                "adicionar",
                                "Adiciona uma nova subclasse"
                        )
                        .addOption(
                                OptionType.STRING,
                                "nome",
                                "Nome da subclasse",
                                true
                        ),

                        new SubcommandData(
                                "remover",
                                "Remove uma subclasse"
                        )
                        .addOption(
                                OptionType.STRING,
                                "nome",
                                "Nome da subclasse",
                                true
                        ),

                        new SubcommandData(
                                "listar",
                                "Lista todas as subclasses"
                        )
                ),

                // =========================
                // MUTE
                // =========================

                Commands.slash(
                        "mute",
                        "Silencia um jogador por 10 minutos"
                )
                .addOption(
                        OptionType.USER,
                        "usuario",
                        "Jogador que será mutado",
                        true
                ),

                // =========================
                // BAN
                // =========================

                Commands.slash(
                        "ban",
                        "Bane um jogador"
                )
                .addOption(
                        OptionType.USER,
                        "usuario",
                        "Jogador que será banido",
                        true
                ),

                // =========================
                // BAN TEMP
                // =========================

                Commands.slash(
                        "bantemp",
                        "Bane temporariamente um jogador"
                )
                .addOption(
                        OptionType.USER,
                        "usuario",
                        "Jogador que será banido",
                        true
                )
                .addOption(
                        OptionType.INTEGER,
                        "minutos",
                        "Duração do ban em minutos",
                        true
                ),

                // =========================
                // MUSIC
                // =========================

                Commands.slash(
                        "music",
                        "Toca uma música na call"
                )
                .addOption(
                        OptionType.STRING,
                        "link",
                        "Link da música",
                        true
                ),

                Commands.slash(
                        "music-stop",
                        "Para a música"
                ),

                Commands.slash(
                        "music-pause",
                        "Pausa a música"
                ),

                Commands.slash(
                        "music-resume",
                        "Continua a música"
                )

        ).queue(

                success ->
                        System.out.println(
                                "Comandos registrados."
                        ),

                error -> {

                    System.err.println(
                            "Erro ao registrar comandos:"
                    );

                    error.printStackTrace();
                }
        );
    }
}
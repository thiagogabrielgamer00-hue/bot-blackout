package br.blackout.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandRegistry {

    public static void register(JDA jda) {

        jda.updateCommands().addCommands(

                // =========================
                // ROLL
                // =========================

                Commands.slash(
                        "roll",
                        "Rolete uma característica ninja"
                )
                .addOption(
                        OptionType.STRING,
                        "tipo",
                        "O que deseja roletar",
                        true
                ),

                // =========================
                // WL
                // =========================

                Commands.slash(
                        "wl",
                        "Sistema administrativo da Whitelist"
                )
                .addOption(
                        OptionType.STRING,
                        "acao",
                        "Ação que deseja executar",
                        true
                )
                .addOption(
                        OptionType.CHANNEL,
                        "canal",
                        "Canal para configurar",
                        false
                )
                .addOption(
                        OptionType.STRING,
                        "texto",
                        "Texto ou pergunta",
                        false
                )
                .addOption(
                        OptionType.INTEGER,
                        "numero",
                        "Número da pergunta",
                        false
                ),

                // =========================
                // CLÃ
                // =========================

                Commands.slash(
                        "clan",
                        "Gerencie os clãs"
                )
                .addOption(
                        OptionType.STRING,
                        "acao",
                        "adicionar, editar, remover, listar ou info",
                        true
                )
                .addOption(
                        OptionType.STRING,
                        "nome",
                        "Nome do clã",
                        false
                )
                .addOption(
                        OptionType.STRING,
                        "raridade",
                        "Raridade do clã",
                        false
                )
                .addOption(
                        OptionType.STRING,
                        "classificacao",
                        "Classificação do clã",
                        false
                ),

                // =========================
                // RARIDADE
                // =========================

                Commands.slash(
                        "raridade",
                        "Configure as raridades"
                )
                .addOption(
                        OptionType.STRING,
                        "acao",
                        "configurar, remover ou listar",
                        true
                )
                .addOption(
                        OptionType.STRING,
                        "nome",
                        "Nome da raridade",
                        false
                )
                .addOption(
                        OptionType.NUMBER,
                        "chance",
                        "Chance da raridade",
                        false
                ),

                // =========================
                // VILA
                // =========================

                Commands.slash(
                        "vila",
                        "Gerencie as vilas"
                )
                .addOption(
                        OptionType.STRING,
                        "acao",
                        "adicionar, remover ou listar",
                        true
                )
                .addOption(
                        OptionType.STRING,
                        "nome",
                        "Nome da vila",
                        false
                ),

                // =========================
                // PRODÍGIO
                // =========================

                Commands.slash(
                        "prodigio",
                        "Configure a chance de prodígio"
                )
                .addOption(
                        OptionType.NUMBER,
                        "chance",
                        "Nova chance em porcentagem",
                        true
                )

        ).queue(
                success ->
                        System.out.println(
                                "✅ Comandos registrados!"
                        ),

                error -> {
                    System.err.println(
                            "❌ Erro ao registrar comandos:"
                    );

                    error.printStackTrace();
                }
        );
    }
}
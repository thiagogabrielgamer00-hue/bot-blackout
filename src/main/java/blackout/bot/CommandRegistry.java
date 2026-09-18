package br.blackout.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandRegistry {

    public static void register(JDA jda) {

        jda.updateCommands().addCommands(

                // ==========================================
                // ROLL
                // ==========================================

                Commands.slash(
                        "roll",
                        "Rola reino, classe ou subclasse"
                )

                .addOption(
                        OptionType.STRING,
                        "tipo",
                        "reino, classe, subclasse ou completo",
                        true
                ),


                // ==========================================
                // PERFIL
                // ==========================================

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


                // ==========================================
                // GIROS
                // ==========================================

                Commands.slash(
                        "giros",
                        "Define a quantidade de giros de um jogador"
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
                )

        )

        .queue(

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
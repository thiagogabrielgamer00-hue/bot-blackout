package br.blackout.bot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println(
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        );

        System.out.println(
                "🍥 Iniciando Naruto RP Bot..."
        );

        System.out.println(
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        );


        // =========================
        // CARREGAR DADOS
        // =========================

        DataManager.load();


        // =========================
        // TOKEN
        // =========================

        String token = System.getenv("DISCORD_TOKEN");

        if (token == null || token.isBlank()) {

            System.err.println(
                    "❌ DISCORD_TOKEN não foi encontrado!"
            );

            System.err.println(
                    "Configure a variável DISCORD_TOKEN."
            );

            return;
        }


        // =========================
        // INICIAR JDA
        // =========================

        var jda = JDABuilder.createDefault(token)
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .addEventListeners(
                        new BotListener()
                )
                .build();


        jda.awaitReady();


        // =========================
        // REGISTRAR COMANDOS
        // =========================

        CommandRegistry.register(jda);


        System.out.println(
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        );

        System.out.println(
                "✅ BOT ONLINE: "
                        + jda.getSelfUser().getName()
        );

        System.out.println(
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        );
    }
}
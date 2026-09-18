package br.blackout.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class BotManager {

    private static JDA jda;


    public static void start(
            String token
    ) throws Exception {

        jda =
                JDABuilder
                        .createDefault(token)

                        .enableIntents(
                                GatewayIntent.GUILD_MESSAGES,
                                GatewayIntent.MESSAGE_CONTENT
                        )

                        .addEventListeners(
                                new BotListener()
                        )

                        .build();


        jda.awaitReady();


        CommandRegistry.register(jda);


        System.out.println(
                "Bot online: " +
                jda.getSelfUser().getName()
        );
    }


    public static JDA getJDA() {

        return jda;
    }
}
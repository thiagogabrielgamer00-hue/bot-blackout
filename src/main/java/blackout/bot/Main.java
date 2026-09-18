package br.blackout.bot;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("==============================");
        System.out.println("       Blackout RP BOT");
        System.out.println("==============================");

        DataManager.load();

        String token = System.getenv("DISCORD_TOKEN");

        if (token == null || token.isBlank()) {
            System.err.println("DISCORD_TOKEN nao encontrado.");
            return;
        }

        BotManager.start(token);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Salvando dados...");
            DataManager.save();
        }));

        System.out.println("Sistema iniciado.");
    }
}
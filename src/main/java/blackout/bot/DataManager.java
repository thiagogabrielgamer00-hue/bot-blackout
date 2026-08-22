package br.blackout.bot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class DataManager {

    private static final File FOLDER = new File("data");
    private static final File FILE = new File(FOLDER, "config.json");

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    public static BotData data;


    public static void load() {

        try {

            if (!FOLDER.exists()) {
                FOLDER.mkdirs();
            }

            if (!FILE.exists()) {

                data = new BotData();
                save();

                System.out.println(
                        "📁 Arquivo de configuração criado."
                );

                return;
            }

            Reader reader = new InputStreamReader(
                    new FileInputStream(FILE),
                    StandardCharsets.UTF_8
            );

            data = GSON.fromJson(reader, BotData.class);

            reader.close();

            if (data == null) {
                data = new BotData();
            }

            System.out.println(
                    "✅ Configurações carregadas."
            );

        } catch (Exception e) {

            e.printStackTrace();

            data = new BotData();

        }
    }


    public static synchronized void save() {

        try {

            if (!FOLDER.exists()) {
                FOLDER.mkdirs();
            }

            Writer writer = new OutputStreamWriter(
                    new FileOutputStream(FILE),
                    StandardCharsets.UTF_8
            );

            GSON.toJson(data, writer);

            writer.flush();
            writer.close();

        } catch (Exception e) {

            System.err.println(
                    "❌ Erro ao salvar dados:"
            );

            e.printStackTrace();

        }
    }
}
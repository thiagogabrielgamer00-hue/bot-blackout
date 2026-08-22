package br.blackout.bot;

import java.util.*;

public class BotData {

    // =========================
    // CONFIGURAÇÕES DA WL
    // =========================

    public long wlResultadosChannelId = 0L;
    public long wlAprovacaoChannelId = 0L;
    public long wlCategoriaId = 0L;

    // Chance padrão de prodígio
    public double chanceProdigio = 5.0;

    // =========================
    // VILAS
    // =========================

    public List<String> vilas = new ArrayList<>();

    // =========================
    // RARIDADES
    // =========================

    // Nome da raridade -> chance
    public Map<String, Double> raridades = new LinkedHashMap<>();

    // =========================
    // CLÃS
    // =========================

    public Map<String, ClanData> clas = new LinkedHashMap<>();

    // =========================
    // PERGUNTAS WL
    // =========================

    public List<String> perguntasWL = new ArrayList<>();

    public BotData() {

        // Raridades padrão
        raridades.put("Comum", 40.0);
        raridades.put("Incomum", 30.0);
        raridades.put("Raro", 18.0);
        raridades.put("Épico", 9.0);
        raridades.put("Lendário", 3.0);

        // Vilas padrão
        vilas.add("Konohagakure");
        vilas.add("Sunagakure");
        vilas.add("Kirigakure");
        vilas.add("Kumogakure");
        vilas.add("Iwagakure");

        // Perguntas padrão
        perguntasWL.add("Qual é seu nome e sua idade?");
        perguntasWL.add("Qual é seu nick no Minecraft?");
        perguntasWL.add("Por que deseja entrar no servidor?");
        perguntasWL.add("Você possui experiência com Roleplay?");
        perguntasWL.add("Como você reagiria a uma situação de conflito no RP?");
    }


    public static class ClanData {

        public String nome;
        public String raridade;
        public String classificacao;

        public ClanData() {
        }

        public ClanData(
                String nome,
                String raridade,
                String classificacao
        ) {

            this.nome = nome;
            this.raridade = raridade;
            this.classificacao = classificacao;

        }
    }
}
package br.blackout.bot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private static final Path DATA_FILE =
            Paths.get(
                    "data",
                    "medieval_profiles.txt"
            );

    private static final Path REINOS_FILE =
            Paths.get(
                    "data",
                    "reinos.txt"
            );

    private static final Path CLASSES_FILE =
            Paths.get(
                    "data",
                    "classes.txt"
            );

    private static final Path SUBCLASSES_FILE =
            Paths.get(
                    "data",
                    "subclasses.txt"
            );


    private static final Map<Long, BotData.Profile> profiles =
            new ConcurrentHashMap<>();


    // ==================================================
    // CARREGAR
    // ==================================================

    public static synchronized void load() {

        profiles.clear();

        try {

            Files.createDirectories(
                    DATA_FILE.getParent()
            );


            // ==========================================
            // REINOS
            // ==========================================

            carregarLista(
                    REINOS_FILE,
                    BotData.REINOS
            );


            // ==========================================
            // CLASSES
            // ==========================================

            carregarLista(
                    CLASSES_FILE,
                    BotData.CLASSES
            );


            // ==========================================
            // SUBCLASSES
            // ==========================================

            carregarLista(
                    SUBCLASSES_FILE,
                    BotData.SUBCLASSES
            );


            // ==========================================
            // PERFIS
            // ==========================================

            if (!Files.exists(DATA_FILE)) {

                System.out.println(
                        "Nenhum banco de dados de perfis encontrado."
                );

                return;
            }


            for (
                    String line :
                    Files.readAllLines(
                            DATA_FILE,
                            StandardCharsets.UTF_8
                    )
            ) {

                if (
                        line.isBlank() ||
                        line.equals(
                                "MEDIEVAL_BOT_V1"
                        )
                ) {
                    continue;
                }


                try {

                    String[] parts =
                            line.split(
                                    "\\|",
                                    -1
                            );


                    if (parts.length != 7) {
                        continue;
                    }


                    long userId =
                            Long.parseLong(
                                    parts[0]
                            );


                    BotData.Profile profile =
                            new BotData.Profile();


                    profile.girosReino =
                            Integer.parseInt(
                                    parts[1]
                            );


                    profile.girosClasse =
                            Integer.parseInt(
                                    parts[2]
                            );


                    profile.girosSubclasse =
                            Integer.parseInt(
                                    parts[3]
                            );


                    profile.reino =
                            decode(parts[4]);


                    profile.classe =
                            decode(parts[5]);


                    profile.subclasse =
                            decode(parts[6]);


                    profiles.put(
                            userId,
                            profile
                    );


                } catch (Exception e) {

                    System.err.println(
                            "Erro ao carregar uma linha dos dados."
                    );
                }
            }


            System.out.println(
                    "Perfis carregados: " +
                    profiles.size()
            );


        } catch (IOException e) {

            System.err.println(
                    "Erro ao carregar banco de dados:"
            );

            e.printStackTrace();
        }
    }


    // ==================================================
    // SALVAR
    // ==================================================

    public static synchronized void save() {

        try {

            Files.createDirectories(
                    DATA_FILE.getParent()
            );


            // ==========================================
            // SALVAR REINOS
            // ==========================================

            salvarLista(
                    REINOS_FILE,
                    BotData.REINOS
            );


            // ==========================================
            // SALVAR CLASSES
            // ==========================================

            salvarLista(
                    CLASSES_FILE,
                    BotData.CLASSES
            );


            // ==========================================
            // SALVAR SUBCLASSES
            // ==========================================

            salvarLista(
                    SUBCLASSES_FILE,
                    BotData.SUBCLASSES
            );


            // ==========================================
            // SALVAR PERFIS
            // ==========================================

            StringBuilder data =
                    new StringBuilder();


            data.append(
                    "MEDIEVAL_BOT_V1\n"
            );


            for (
                    Map.Entry<Long, BotData.Profile> entry :
                    profiles.entrySet()
            ) {

                long userId =
                        entry.getKey();


                BotData.Profile p =
                        entry.getValue();


                data.append(userId)

                        .append("|")
                        .append(p.girosReino)

                        .append("|")
                        .append(p.girosClasse)

                        .append("|")
                        .append(p.girosSubclasse)

                        .append("|")
                        .append(
                                encode(
                                        p.reino
                                )
                        )

                        .append("|")
                        .append(
                                encode(
                                        p.classe
                                )
                        )

                        .append("|")
                        .append(
                                encode(
                                        p.subclasse
                                )
                        )

                        .append("\n");
            }


            Path temp =
                    Paths.get(
                            DATA_FILE +
                            ".tmp"
                    );


            Files.writeString(

                    temp,

                    data.toString(),

                    StandardCharsets.UTF_8,

                    StandardOpenOption.CREATE,

                    StandardOpenOption.TRUNCATE_EXISTING
            );


            try {

                Files.move(

                        temp,

                        DATA_FILE,

                        StandardCopyOption.REPLACE_EXISTING,

                        StandardCopyOption.ATOMIC_MOVE
                );


            } catch (
                    AtomicMoveNotSupportedException e
            ) {

                Files.move(

                        temp,

                        DATA_FILE,

                        StandardCopyOption.REPLACE_EXISTING
                );
            }


        } catch (IOException e) {

            System.err.println(
                    "Erro ao salvar dados:"
            );

            e.printStackTrace();
        }
    }


    // ==================================================
    // SALVAR LISTA
    // ==================================================

    private static void salvarLista(
            Path arquivo,
            java.util.List<String> lista
    ) throws IOException {

        Files.write(

                arquivo,

                lista,

                StandardCharsets.UTF_8,

                StandardOpenOption.CREATE,

                StandardOpenOption.TRUNCATE_EXISTING
        );
    }


    // ==================================================
    // CARREGAR LISTA
    // ==================================================

    private static void carregarLista(
            Path arquivo,
            java.util.List<String> lista
    ) throws IOException {

        if (!Files.exists(arquivo)) {

            salvarLista(
                    arquivo,
                    lista
            );

            return;
        }


        java.util.List<String> linhas =
                Files.readAllLines(
                        arquivo,
                        StandardCharsets.UTF_8
                );


        lista.clear();


        for (String linha : linhas) {

            String valor =
                    linha.trim();


            if (!valor.isEmpty()) {

                lista.add(valor);
            }
        }
    }


    // ==================================================
    // PEGAR PERFIL
    // ==================================================

    public static BotData.Profile getProfile(
            long userId
    ) {

        return profiles.get(
                userId
        );
    }


    // ==================================================
    // CRIAR / PEGAR PERFIL
    // ==================================================

    public static BotData.Profile getOrCreateProfile(
            long userId
    ) {

        return profiles.computeIfAbsent(

                userId,

                id ->
                        new BotData.Profile()
        );
    }


    // ==================================================
    // BASE64
    // ==================================================

    private static String encode(
            String text
    ) {

        if (text == null) {
            text = "";
        }


        return Base64
                .getEncoder()
                .encodeToString(

                        text.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }


    private static String decode(
            String text
    ) {

        if (
                text == null ||
                text.isEmpty()
        ) {

            return "";
        }


        return new String(

                Base64
                        .getDecoder()
                        .decode(text),

                StandardCharsets.UTF_8
        );
    }
}
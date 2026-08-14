import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Config {

    private static final String CONFIG_FILE = "config.txt";
    private static final String FICHAS_FILE = "fichas.txt";

    public static String canalWlId = "";
    public static String canalResultadoId = "";
    public static String canalProibidoId = "";

    public static final Map<String, String> fichas = new HashMap<>();

    // =========================================================
    // CARREGAMENTO
    // =========================================================

    public static synchronized void carregar() {
        carregarConfig();
        carregarFichas();
    }

    private static void carregarConfig() {

        File arquivo = new File(CONFIG_FILE);

        try {

            if (!arquivo.exists()) {
                arquivo.createNewFile();
                salvarTudo();
                return;
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(arquivo),
                            StandardCharsets.UTF_8))) {

                String linha;

                linha = br.readLine();
                if (linha != null) {
                    canalWlId = linha.trim();
                }

                linha = br.readLine();
                if (linha != null) {
                    canalResultadoId = linha.trim();
                }

                linha = br.readLine();
                if (linha != null) {
                    canalProibidoId = linha.trim();
                }
            }

        } catch (Exception ex) {

            System.err.println("[CONFIG] Erro ao carregar config.txt:");
            ex.printStackTrace();
        }
    }

    private static void carregarFichas() {

        fichas.clear();

        File arquivo = new File(FICHAS_FILE);

        try {

            if (!arquivo.exists()) {
                arquivo.createNewFile();
                return;
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(arquivo),
                            StandardCharsets.UTF_8))) {

                String linha;

                while ((linha = br.readLine()) != null) {

                    if (linha.trim().isEmpty()) {
                        continue;
                    }

                    String[] partes = linha.split(";", 2);

                    if (partes.length == 2) {

                        String id = partes[0].trim();
                        String texto = partes[1];

                        if (!id.isEmpty()) {
                            fichas.put(id, texto);
                        }
                    }
                }
            }

        } catch (Exception ex) {

            System.err.println("[CONFIG] Erro ao carregar fichas.txt:");
            ex.printStackTrace();
        }
    }

    // =========================================================
    // CANAIS
    // =========================================================

    public static synchronized void salvarCanais(
            String wlId,
            String resultadoId) {

        canalWlId = wlId == null ? "" : wlId;
        canalResultadoId = resultadoId == null ? "" : resultadoId;

        salvarTudo();
    }

    public static synchronized void salvarCanalProibido(String id) {

        canalProibidoId = id == null ? "" : id;

        salvarTudo();
    }

    // =========================================================
    // FICHAS
    // =========================================================

    public static synchronized void salvarFicha(
            String userId,
            String texto) {

        if (userId == null || userId.isBlank()) {
            return;
        }

        if (texto == null) {
            texto = "";
        }

        fichas.put(userId, texto);

        salvarFichas();
    }

    private static void salvarFichas() {

        File arquivo = new File(FICHAS_FILE);

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(arquivo),
                        StandardCharsets.UTF_8))) {

            for (Map.Entry<String, String> entrada : fichas.entrySet()) {

                String id = entrada.getKey();
                String texto = entrada.getValue();

                if (texto == null) {
                    texto = "";
                }

                // Evita quebrar o arquivo.
                texto = texto
                        .replace("\r", " ")
                        .replace("\n", "\\n");

                bw.write(id);
                bw.write(";");
                bw.write(texto);
                bw.newLine();
            }

        } catch (Exception ex) {

            System.err.println("[CONFIG] Erro ao salvar fichas:");
            ex.printStackTrace();
        }
    }

    // =========================================================
    // SALVAR CONFIG
    // =========================================================

    private static synchronized void salvarTudo() {

        File arquivo = new File(CONFIG_FILE);

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(arquivo),
                        StandardCharsets.UTF_8))) {

            bw.write(canalWlId == null ? "" : canalWlId);
            bw.newLine();

            bw.write(canalResultadoId == null ? "" : canalResultadoId);
            bw.newLine();

            bw.write(canalProibidoId == null ? "" : canalProibidoId);
            bw.newLine();

        } catch (Exception ex) {

            System.err.println("[CONFIG] Erro ao salvar config:");
            ex.printStackTrace();
        }
    }
}
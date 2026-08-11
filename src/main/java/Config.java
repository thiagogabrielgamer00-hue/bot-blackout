import java.io.*;
import java.util.*;

public class Config {
    private static final String CONFIG_FILE = "config.txt";
    private static final String FICHAS_FILE = "fichas.txt";
    public static String canalWlId = "";
    public static String canalResultadoId = "";
    public static String canalProibidoId = ""; // Nova variável
    public static final Map<String, String> fichas = new HashMap<>();

    public static void carregar() {
        try {
            File f1 = new File(CONFIG_FILE);
            if (f1.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(f1));
                canalWlId = br.readLine();
                String l2 = br.readLine();
                if (l2 != null) canalResultadoId = l2;
                String l3 = br.readLine();
                if (l3 != null) canalProibidoId = l3;
                br.close();
            }
            File f2 = new File(FICHAS_FILE);
            if (f2.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(f2));
                String l;
                while ((l = br.readLine()) != null) {
                    String[] p = l.split(";", 2);
                    if (p.length == 2) fichas.put(p[0], p[1]);
                }
                br.close();
            }
        } catch (IOException ignored) {}
    }

    public static void salvarCanais(String wlId, String resultadoId) {
        canalWlId = wlId;
        canalResultadoId = resultadoId;
        salvarTudo();
    }

    public static void salvarCanalProibido(String id) {
        canalProibidoId = id;
        salvarTudo();
    }

    private static void salvarTudo() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CONFIG_FILE))) {
            bw.write(canalWlId); bw.newLine();
            bw.write(canalResultadoId); bw.newLine();
            bw.write(canalProibidoId);
        } catch (IOException ignored) {}
    }

    public static void salvarFicha(String userId, String texto) {
        fichas.put(userId, texto);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FICHAS_FILE))) {
            for (Map.Entry<String, String> en : fichas.entrySet()) {
                bw.write(en.getKey() + ";" + en.getValue());
                bw.newLine();
            }
        } catch (IOException ignored) {}
    }
}

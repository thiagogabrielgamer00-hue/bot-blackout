import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main extends ListenerAdapter {
    private final Map<String, int[]> tecnicas = new LinkedHashMap<>();
    private final String ARQUIVO = "tecnicas_dados.txt";
    private static final String SUPER_USER_ID = "1333822923194105917";

    public Main() { 
        Config.carregar();
        carregarDados(); 
    }

    public static void main(String[] args) throws InterruptedException {
        String tk = System.getenv("DISCORD_TOKEN");
        var jda = JDABuilder.createDefault(tk)
            .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
            .addEventListeners(new Main()).build().awaitReady();
        CommandRegistry.registrar(jda);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        if (e.getGuild() == null) return;

        boolean st = e.getUser().getId().equals(SUPER_USER_ID) ||
                     e.getMember().isOwner() || 
                     e.getMember().hasPermission(Permission.ADMINISTRATOR) || 
                     e.getMember().getRoles().stream().anyMatch(r -> r.getName().toLowerCase().contains("staff"));

        if (e.getName().equals("gerenciar")) {
            if (!st) { e.reply("❌ Permissao negada.").setEphemeral(true).queue(); return; }
            if (e.getSubcommandName().equals("addtecnica")) {
                String n = e.getOption("nome").getAsString();
                tecnicas.put(n, new int[]{0, e.getOption("totais").getAsInt()});
                salvarDados(); e.reply("✅ Técnica cadastrada!").queue();
            } else if (e.getSubcommandName().equals("setvagas")) {
                String nb = e.getOption("nome").getAsString(); int no = e.getOption("ocupadas").getAsInt();
                String cr = encontrarChave(nb); if (cr == null) { e.reply("❌ Nao encontrada.").setEphemeral(true).queue(); return; }
                int[] d = tecnicas.get(cr); int max = java.lang.reflect.Array.getInt(d, 1);
                if (no <= max) {
                    java.lang.reflect.Array.setInt(d, 0, no); salvarDados(); 
                    e.reply("🔄 Vagas de " + cr + " alteradas para " + no + "/" + max).queue();
                } else { e.reply("❌ Ocupadas nao superam o total.").setEphemeral(true).queue(); }
            } else { AdminCommands.executar(e, tecnicas, this::salvarDados); }
        } 
        else if (e.getName().equals("setcanaiswl")) {
            if (!st) { e.reply("❌ Permissao negada.").setEphemeral(true).queue(); return; }
            Config.salvarCanais(e.getOption("canal_staff").getAsChannel().getId(), e.getOption("canal_resultados").getAsChannel().getId());
            e.reply("✅ Canais salvos!").queue();
        }
        else if (e.getName().equals("setcanalproibido")) {
            if (!st) { e.reply("❌ Permissao negada.").setEphemeral(true).queue(); return; }
            Config.salvarCanalProibido(e.getOption("canal").getAsChannel().getId());
            e.reply("✅ Canal proibido definido!").queue();
        }
        else if (e.getName().equals("setupparceria")) {
            if (!st) { e.reply("❌ Permissao negada.").setEphemeral(true).queue(); return; }
            ParceriaManager.enviarPainelParceria(e);
        }
        else if (e.getName().equals("fazerwl")) { WlManager.iniciarWlPorUsuario(e.getUser(), e); } 
        else if (e.getName().equals("setupwl")) {
            if (!st) { e.reply("❌ Permissao negada.").setEphemeral(true).queue(); return; }
            WlManager.enviarPainelWl(e);
        }
        else if (e.getName().equals("ficha") || e.getName().equals("setficha")) { FichaCommands.executar(e, st); } 
        else if (e.getName().equals("vertecnicas")) { listarTecnicas(e); }
        else if (e.getName().equals("avaliarstaff")) {
            int nt = e.getOption("nota").getAsInt(); if (nt < 1 || nt > 5) { e.reply("❌ Nota de 1 a 5.").setEphemeral(true).queue(); return; }
            String pnl = String.format("⭐ **Avaliacao Anonima!**\n\n**Staff:** %s\n**Nota:** %d/5\n**Comentario:** %s", e.getOption("staff").getAsUser().getAsMention(), nt, e.getOption("comentario").getAsString());
            e.getChannel().sendMessage(pnl).queue(); e.reply("✅ Enviada!").setEphemeral(true).queue();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot()) return;
        if (e.isFromGuild() && !Config.canalProibidoId.isEmpty()) {
            if (e.getChannel().getId().equals(Config.canalProibidoId)) {
                e.getMessage().delete().queue();
                e.getGuild().ban(e.getAuthor(), 0, TimeUnit.DAYS).reason("Mensagem no canal proibido").queue();
                return;
            }
        }
        if (e.isFromGuild()) return;
        WlManager.processarMensagemDm(e);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent e) {
        if (e.getMember() == null) return;
        boolean st = e.getUser().getId().equals(SUPER_USER_ID) || e.getMember().isOwner() || e.getMember().hasPermission(Permission.ADMINISTRATOR) || e.getMember().getRoles().stream().anyMatch(r -> r.getName().toLowerCase().contains("staff"));
        if (e.getComponentId().startsWith("wl_")) { WlManager.tratarBotoes(e, st); }
        else if (e.getComponentId().equals("pr_iniciar_botao")) { ParceriaManager.criarCanalPrivado(e); }
    }

    private void listarTecnicas(SlashCommandInteractionEvent e) {
        StringBuilder sb = new StringBuilder("**__Lista de Vagas (Jujutsu RP)__**\n\n"); int c = 0;
        for (Map.Entry<String, int[]> en : tecnicas.entrySet()) {
            String n = en.getKey(); int[] d = en.getValue();
            int vOcu = java.lang.reflect.Array.getInt(d, 0); int vTot = java.lang.reflect.Array.getInt(d, 1);
            sb.append(String.format("%s **%s**: %d/%d\n", (vOcu >= vTot ? "🔴" : "🟢"), n, vOcu, vTot)); c++;
            if (c >= 20) { e.getChannel().sendMessage(sb.toString()).queue(); sb.setLength(0); sb.append("\n"); c = 0; }
        }
        if (sb.length() > 1) e.getChannel().sendMessage(sb.toString()).queue();
        e.reply("📋 Lista gerada.").setEphemeral(true).queue();
    }

    private String encontrarChave(String b) { for (String k : tecnicas.keySet()) { if (k.equalsIgnoreCase(b)) return k; } return null; }

    private void salvarDados() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARQUIVO))) {
            for (Map.Entry<String, int[]> en : tecnicas.entrySet()) {
                int[] v = en.getValue();
                bw.write(en.getKey() + ";" + java.lang.reflect.Array.getInt(v, 0) + ";" + java.lang.reflect.Array.getInt(v, 1)); bw.newLine();
            }
        } catch (IOException ignored) {}
    }

    private void carregarDados() {
        File f = new File(ARQUIVO); if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String l; while ((l = br.readLine()) != null) {
                String[] pt = l.split(";");
                if (pt.length == 3) { tecnicas.put(pt[0], new int[]{Integer.parseInt(pt[1]), Integer.parseInt(pt[2])}); }
            }
        } catch (IOException ignored) {}
    }
}

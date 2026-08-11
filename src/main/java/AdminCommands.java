import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import java.util.Map;

public class AdminCommands {
    public static void executar(SlashCommandInteractionEvent e, Map<String, int[]> tecnicas, Runnable salvar) {
        String sub = e.getSubcommandName();
        if (sub == null) return;

        if (sub.equals("remover_tecnica")) {
            String nome = e.getOption("nome").getAsString();
            String chave = null;
            for (String k : tecnicas.keySet()) { if (k.equalsIgnoreCase(nome)) chave = k; }
            
            if (chave != null) {
                tecnicas.remove(chave);
                salvar.run();
                e.reply("🗑️ A técnica **" + chave + "** foi totalmente deletada do sistema.").queue();
            } else {
                e.reply("❌ Técnica não encontrada.").setEphemeral(true).queue();
            }
        } 
        else if (sub.equals("remover_membro")) {
            String nome = e.getOption("tecnica").getAsString();
            String chave = null;
            for (String k : tecnicas.keySet()) { if (k.equalsIgnoreCase(nome)) chave = k; }

            if (chave != null) {
                int[] dados = tecnicas.get(chave);
                if (dados[0] > 0) {
                    dados[0]--;
                    salvar.run();
                    e.reply("👤 Uma vaga foi liberada na técnica **" + chave + "**. (Atual: " + dados[0] + "/" + dados[1] + ")").queue();
                } else {
                    e.reply("❌ Essa técnica já possui 0 vagas ocupadas.").setEphemeral(true).queue();
                }
            } else {
                e.reply("❌ Técnica não encontrada.").setEphemeral(true).queue();
            }
        }
    }
}

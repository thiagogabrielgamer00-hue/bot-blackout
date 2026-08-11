import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class FichaCommands {
    public static void executar(SlashCommandInteractionEvent e, boolean isStaff) {
        if (e.getName().equals("ficha")) {
            User alvo = e.getOption("usuario") != null ? e.getOption("usuario").getAsUser() : e.getUser();
            String dadosFicha = Config.fichas.getOrDefault(alvo.getId(), "❌ Nenhuma ficha registrada para este usuário.");
            
            e.reply("📝 **Ficha de RP — " + alvo.getName() + "**\n\n" + dadosFicha).queue();
        } 
        else if (e.getName().equals("setficha")) {
            if (!isStaff) { e.reply("❌ Apenas Staff.").setEphemeral(true).queue(); return; }
            User alvo = e.getOption("usuario").getAsUser();
            String texto = e.getOption("conteudo").getAsString();
            
            Config.salvarFicha(alvo.getId(), texto);
            e.reply("✅ A ficha de " + alvo.getAsMention() + " foi actualizada com sucesso!").queue();
        }
    }
}

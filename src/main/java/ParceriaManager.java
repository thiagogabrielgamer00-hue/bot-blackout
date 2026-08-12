import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import java.util.EnumSet;

public class ParceriaManager {

    // Envia o painel bonito idêntico ao da imagem
    public static void enviarPainelParceria(SlashCommandInteractionEvent e) {
        e.reply("✅ Painel de Parcerias enviado!").setEphemeral(true).queue();

        String mensagem = "🤝 **CENTRAL DE PARCERIAS**\n\n" +
                "**Blackout Community RP**\n" +
                "Deseja fazer uma parceria oficial com o nosso servidor?\n\n" +
                "📋 **COMO FUNCIONA?**\n" +
                "• Clique em 🤝 **Fazer Parceria**.\n" +
                "• Um canal privado será criado automaticamente.\n" +
                "• Envie o modelo de divulgação do seu servidor.\n" +
                "• Publique nossa divulgação no seu servidor.\n" +
                "• Envie um print comprovando a divulgação.\n\n" +
                "⚠️ **IMPORTANTE**\n" +
                "Certifique-se de seguir todas as instruções apresentadas no canal privado.\n\n" +
                "🤝 **Faça sua parceria e cresça junto conosco!**";

        e.getChannel().sendMessage(mensagem)
            .addActionRow(Button.success("pr_iniciar_botao", "🤝 Fazer Parceria"))
            .queue();
    }

    // Cria o canal privado quando alguém clica no botão
    public static void criarCanalPrivado(ButtonInteractionEvent e) {
        var guild = e.getGuild();
        if (guild == null) return;

        var membro = e.getMember();
        String nomeCanal = "🤝-parceria-" + e.getUser().getName().toLowerCase();

        // Configura as permissões para o canal nascer privado
        var roleTodoMundo = guild.getPublicRole();
        
        e.reply("📥 Criando seu canal privado de parceria...").setEphemeral(true).queue();

        guild.createTextChannel(nomeCanal)
            .addRolePermissionOverride(roleTodoMundo.getIdLong(), null, EnumSet.of(Permission.VIEW_CHANNEL)) // Esconde de todos
            .addMemberPermissionOverride(membro.getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null) // Libera pro player
            .queue(canal -> {
                
                // Se houver um cargo Staff, ele também ganha acesso automático para avaliar
                for (Role role : guild.getRoles()) {
                    if (role.getName().toLowerCase().contains("staff")) {
                        canal.upsertPermissionOverride(role).grant(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND).queue();
                    }
                }

                // Envia as instruções dentro do canal recém-criado
                String instrucao = String.format(
                    "👋 Olá %s, bem-vindo ao seu canal de parceria!\n\n" +
                    "Por favor, envie aqui:\n" +
                    "1️⃣ O **modelo de divulgação** do seu servidor.\n" +
                    "2️⃣ O **print comprovando** que você postou a nossa divulgação lá no seu servidor.\n\n" +
                    "A Staff analisará o seu envio em breve! Aguarde um momento.", 
                    e.getUser().getAsMention()
                );
                
                canal.sendMessage(instrucao).queue();
            });
    }
}

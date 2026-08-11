import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import java.util.*;

public class WlManager {
    private static final Map<String, List<String>> sessoes = new HashMap<>();
    private static final String SUPER_USER_ID = "1333822923194105917";
    
    private static final String[] PERGUNTAS = {
        "1️⃣ Qual o nome e a idade do seu personagem dentro do jogo (RP)?",
        "2️⃣ Qual técnica de Jujutsu você roletou?",
        "3️⃣ Escreva uma breve história para o seu personagem:",
        "4️⃣ Quais são as principais regras de RP do servidor?",
        "5️⃣ Qual é a sua idade na vida real?"
    };

    public static void enviarPainelWl(SlashCommandInteractionEvent e) {
        e.reply("✅ Painel de Whitelist enviado com sucesso no canal!").setEphemeral(true).queue();
        
        e.getChannel().sendMessage("📌 **SISTEMA DE WHITELIST**\n\nAperte no botão verde abaixo para fazer a WL.")
            .addActionRow(Button.success("wl_iniciar_botao", "🟢 Iniciar Whitelist"))
            .queue();
    }

    public static void iniciarWlPorUsuario(net.dv8tion.jda.api.entities.User usuario, Object eventObj) {
        usuario.openPrivateChannel().queue(pc -> {
            sessoes.put(usuario.getId(), new ArrayList<>(Arrays.asList("0")));
            pc.sendMessage("👋 Bem-vindo ao teste de Whitelist de Jujutsu RP!\nResponda as perguntas uma por vez enviando uma mensagem para cada.\n\n" + java.lang.reflect.Array.get(PERGUNTAS, 0).toString()).queue();
            
            if (eventObj instanceof SlashCommandInteractionEvent) {
                ((SlashCommandInteractionEvent) eventObj).reply("📩 O questionário da Whitelist foi enviado com sucesso na sua DM privada!").setEphemeral(true).queue();
            } else if (eventObj instanceof ButtonInteractionEvent) {
                ((ButtonInteractionEvent) eventObj).reply("📩 O questionário foi enviado na sua DM privada! Verifique suas mensagens.").setEphemeral(true).queue();
            }
        }, erro -> {
            String msgErro = "❌ Sua DM está fechada! Ative as mensagens privadas nas configurações de segurança do servidor.";
            if (eventObj instanceof SlashCommandInteractionEvent) {
                ((SlashCommandInteractionEvent) eventObj).reply(msgErro).setEphemeral(true).queue();
            } else if (eventObj instanceof ButtonInteractionEvent) {
                ((ButtonInteractionEvent) eventObj).reply(msgErro).setEphemeral(true).queue();
            }
        });
    }

    public static void processarMensagemDm(MessageReceivedEvent e) {
        String uid = e.getAuthor().getId();
        if (!sessoes.containsKey(uid)) return;

        List<String> dados = sessoes.get(uid);
        int etapa = Integer.parseInt(java.lang.reflect.Array.get(dados.toArray(), 0).toString());
        
        dados.add(e.getMessage().getContentRaw());
        etapa++;

        if (etapa < PERGUNTAS.length) {
            dados.set(0, String.valueOf(etapa));
            e.getChannel().sendMessage(java.lang.reflect.Array.get(PERGUNTAS, etapa).toString()).queue();
        } else {
            sessoes.remove(uid);
            e.getChannel().sendMessage("✅ Obrigado! Suas respostas foram computadas e enviadas para a análise da Staff.").queue();
            
            if (Config.canalWlId.isEmpty()) return;
            TextChannel ch = e.getJDA().getTextChannelById(Config.canalWlId);
            if (ch != null) {
                String r1 = java.lang.reflect.Array.get(dados.toArray(), 1).toString();
                String r2 = java.lang.reflect.Array.get(dados.toArray(), 2).toString();
                String r3 = java.lang.reflect.Array.get(dados.toArray(), 3).toString();
                String r4 = java.lang.reflect.Array.get(dados.toArray(), 4).toString();
                String r5 = java.lang.reflect.Array.get(dados.toArray(), 5).toString();

                String painel = String.format(
                    "📝 **Nova Whitelist para Avaliação!**\n\n" +
                    "**Membro:** %s\n" +
                    "👤 **Nome/Idade (RP):** %s\n" +
                    "🎲 **Técnica Roletada:** %s\n" +
                    "📖 **História:** %s\n" +
                    "⚖️ **Regras de RP:** %s\n" +
                    "🎂 **Idade OOC (Vida Real):** %s",
                    e.getAuthor().getAsMention(), r1, r2, r3, r4, r5
                );
                
                ch.sendMessage(painel).addActionRow(
                    Button.success("wl_apr_" + uid, "Aprovar Jogador"),
                    Button.danger("wl_rec_" + uid, "Reprovar Jogador")
                ).queue();
            }
        }
    }

    public static void tratarBotoes(ButtonInteractionEvent e, boolean isStaff) {
        if (e.getComponentId().equals("wl_iniciar_botao")) {
            iniciarWlPorUsuario(e.getUser(), e);
            return;
        }

        boolean temSuperAcesso = e.getUser().getId().equals(SUPER_USER_ID) || isStaff;
        if (!temSuperAcesso) { 
            e.reply("❌ Apenas membros da Staff podem julgar formulários de Whitelist.").setEphemeral(true).queue(); 
            return; 
        }
        
        String[] p = e.getComponentId().split("_");
        if (p.length < 3) return;
        
        String acao = java.lang.reflect.Array.get(p, 1).toString(); 
        String targetUid = java.lang.reflect.Array.get(p, 2).toString();

        TextChannel canalResultado = null;
        if (!Config.canalResultadoId.isEmpty()) {
            canalResultado = e.getJDA().getTextChannelById(Config.canalResultadoId);
        }

        if (acao.equals("apr")) {
            e.editMessage(e.getMessage().getContentRaw() + "\n\n🟢 **STATUS: APROVADO POR " + e.getUser().getAsMention() + "**").setComponents().queue();
            
            if (canalResultado != null) {
                canalResultado.sendMessage("🎉 **RESULTADO WHITELIST**\n\nO membro <@" + targetUid + "> foi **APROVADO** na Whitelist! Bem-vindo ao RP! 🟢").queue();
            }
            
            e.getJDA().retrieveUserById(targetUid).queue(u -> u.openPrivateChannel().queue(pc -> pc.sendMessage("🎉 Parabéns! Sua Whitelist no servidor de RP foi **Aprovada** pela Staff!").queue()), t -> {});
        } else {
            e.editMessage(e.getMessage().getContentRaw() + "\n\n🔴 **STATUS: REPROVADO POR " + e.getUser().getAsMention() + "**").setComponents().queue();
            
            if (canalResultado != null) {
                canalResultado.sendMessage("❌ **RESULTADO WHITELIST**\n\nO membro <@" + targetUid + "> foi **REPROVADO** na Whitelist pela Staff. 🔴").queue();
            }
            
            e.getJDA().retrieveUserById(targetUid).queue(u -> u.openPrivateChannel().queue(pc -> pc.sendMessage("❌ Sua Whitelist foi **Reprovada**. Procure um Staff para saber o motivo.").queue()), t -> {});
        }
    }
}

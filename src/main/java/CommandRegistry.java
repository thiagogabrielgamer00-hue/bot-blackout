import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandRegistry {

    public static void registrar(JDA jda) {
        // Substitua pelo ID real do seu servidor da Blackout:
        String idServidor = "1418818172827734057"; 

        var sub1 = new SubcommandData("remover_tecnica", "Exclui tecnica")
            .addOption(OptionType.STRING, "nome", "Nome do poder", true);
        var sub2 = new SubcommandData("remover_membro", "Libera vaga")
            .addOption(OptionType.STRING, "tecnica", "Nome da tecnica", true);
        var sub3 = new SubcommandData("addtecnica", "Nova tecnica")
            .addOption(OptionType.STRING, "nome", "Nome do poder", true)
            .addOption(OptionType.INTEGER, "totais", "Vagas totais", true);
        var sub4 = new SubcommandData("setvagas", "Preenche vagas")
            .addOption(OptionType.STRING, "nome", "Nome do poder", true)
            .addOption(OptionType.INTEGER, "ocupadas", "Vagas ocupadas", true);

        var gerenciar = Commands.slash("gerenciar", "Comandos ADM")
            .addSubcommands(sub1, sub2, sub3, sub4);

        jda.updateCommands().queue();

        var guild = jda.getGuildById(idServidor);
        if (guild != null) {
            guild.updateCommands().addCommands(
                Commands.slash("ficha", "Exibe a ficha de um player")
                    .addOption(OptionType.USER, "usuario", "Usuario opcional", false),
                Commands.slash("setficha", "Edita ficha de um usuario")
                    .addOption(OptionType.USER, "usuario", "Membro", true)
                    .addOption(OptionType.STRING, "conteudo", "Texto da ficha", true),
                Commands.slash("fazerwl", "Inicia questionario via DM"),
                Commands.slash("setupwl", "Mensagem fixa com botao verde"),
                Commands.slash("setupparceria", "Envia o painel de parceria"),
                Commands.slash("setcanaiswl", "Canais de votacao e resultados")
                    .addOption(OptionType.CHANNEL, "canal_staff", "Canal Staff", true)
                    .addOption(OptionType.CHANNEL, "canal_resultados", "Canal Resultados", true),
                Commands.slash("setcanalproibido", "Define o canal de banimento")
                    .addOption(OptionType.CHANNEL, "canal", "Selecione o canal", true),
                Commands.slash("avaliarstaff", "Avalie a Staff anonimamente")
                    .addOption(OptionType.USER, "staff", "Staff", true)
                    .addOption(OptionType.INTEGER, "nota", "Nota de 1 a 5", true)
                    .addOption(OptionType.STRING, "comentario", "Critica/Elogio", true),
                Commands.slash("vertecnicas", "Visualiza listagem de tecnicas"),
                gerenciar
            ).queue();
            System.out.println("🚀 Comandos injetados instantaneamente!");
        }
    }
}

package br.blackout.bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BotData {

    public static final long SUPER_USER_ID =
            1333822923194105917L;

    public static final List<Long> ADMIN_ROLE_IDS =
            Arrays.asList(
                    1419779498660073552L,
                    1542736177839218708L,
                    1419382153073922192L,
                    1419778797376372846L
            );

    public static final int GIROS_PADRAO = 4;


    // ==================================================
    // REINOS
    // ==================================================

    public static final List<String> REINOS =
            new ArrayList<>(Arrays.asList(

                    // COLOQUE OS REINOS AQUI

                    // "Reino de Eldoria",
                    // "Reino de Valoria",
                    // "Reino de Astoria"

            ));


    // ==================================================
    // CLASSES
    // ==================================================

    public static final List<String> CLASSES =
            new ArrayList<>(Arrays.asList(

                    // COLOQUE AS CLASSES AQUI

                    // "Guerreiro",
                    // "Mago",
                    // "Arqueiro",
                    // "Espadachin"

            ));


    // ==================================================
    // SUBCLASSES
    // ==================================================

    public static final List<String> SUBCLASSES =
            new ArrayList<>(Arrays.asList(

                    // COLOQUE AS SUBCLASSES AQUI

                    // "pescador",
                    // "ferreiro",
                    // "armadureiro",
                    // "Caçador"
                    // "Camponês"

            ));


    // ==================================================
    // PERFIL
    // ==================================================

    public static class Profile {

        public String reino = "eldoria";

        public String classe = "guerreiro";

        public String subclasse = "pescador";


        public int girosReino =
                GIROS_PADRAO;

        public int girosClasse =
                GIROS_PADRAO;

        public int girosSubclasse =
                GIROS_PADRAO;
    }
}
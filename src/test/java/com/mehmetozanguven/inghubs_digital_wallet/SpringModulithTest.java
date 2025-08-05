package com.mehmetozanguven.inghubs_digital_wallet;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class SpringModulithTest {

    ApplicationModules modules = ApplicationModules.of(InghubsDigitalWalletApplication.class);

    @Test
    void shouldBeCompliant() {
        System.out.println(modules.toString());
        modules.verify();
    }

    @Test
    void writeDocumentationSnippets() {
        new Documenter(modules)
                .writeModuleCanvases()
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }
}

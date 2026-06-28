package com.payloadnotebook;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import com.payloadnotebook.service.DataService;
import com.payloadnotebook.ui.MainPanel;

import java.io.File;

public class BurpPayloadNotebook implements BurpExtension {

    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName("Payload Notebook");

        // Use the project source directory as the default data directory
        File projectDir = new File("C:/Users/24767/Desktop/AI-Project/BurpSuite Payload Notebook/burp-payload-notebook");
        if (!projectDir.exists()) {
            projectDir.mkdirs();
        }

        DataService dataService = new DataService(projectDir);
        MainPanel mainPanel = new MainPanel(dataService);

        api.userInterface().registerSuiteTab("Payload Notebook", mainPanel);
        api.logging().logToOutput("[Payload Notebook] Loaded successfully. Data dir: " + projectDir.getAbsolutePath());
    }
}

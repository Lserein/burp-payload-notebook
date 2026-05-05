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

        // Derive data directory from extension JAR location
        String extensionPath = api.extension().filename();
        File extensionFile = new File(extensionPath);
        File extensionDir = new File(extensionFile.getParent(), "payload-notebook-data");
        extensionDir.mkdirs();

        DataService dataService = new DataService(extensionDir);
        MainPanel mainPanel = new MainPanel(dataService);

        api.userInterface().registerSuiteTab("Payload Notebook", mainPanel);
        api.logging().logToOutput("[Payload Notebook] Loaded successfully. Data dir: " + extensionDir.getAbsolutePath());
    }
}

package com.payloadnotebook.service;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class ClipboardService {

    public static void copyToClipboard(String text) {
        if (text == null || text.isEmpty()) return;
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }
}

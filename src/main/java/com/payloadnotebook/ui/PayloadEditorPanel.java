package com.payloadnotebook.ui;

import com.payloadnotebook.model.Entry;

import javax.swing.*;
import java.awt.*;

public class PayloadEditorPanel extends JPanel {

    private final JTextArea payloadArea;
    private final JLabel pathLabel;
    private Entry currentEntry;
    private boolean editing = false;

    public PayloadEditorPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        pathLabel = new JLabel(" ");
        pathLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        pathLabel.setFont(pathLabel.getFont().deriveFont(11f));
        pathLabel.setForeground(Color.GRAY);

        payloadArea = new JTextArea();
        payloadArea.setEditable(false);
        payloadArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        payloadArea.setLineWrap(true);

        JScrollPane scrollPane = new JScrollPane(payloadArea);
        // Add line numbers via row header
        JTextArea lineNumbers = new JTextArea("1");
        lineNumbers.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        lineNumbers.setBackground(new Color(240, 240, 240));
        lineNumbers.setEditable(false);
        lineNumbers.setLineWrap(false);
        scrollPane.setRowHeaderView(lineNumbers);

        add(pathLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void displayEntry(Entry entry, String categoryPath) {
        // Auto-save if editing
        if (editing && currentEntry != null) {
            saveCurrentEdit();
        }

        this.currentEntry = entry;
        this.editing = false;
        payloadArea.setEditable(false);

        if (entry == null) {
            payloadArea.setText("");
            pathLabel.setText(" ");
        } else {
            payloadArea.setText(entry.getPayload());
            pathLabel.setText(categoryPath);
            payloadArea.setCaretPosition(0);
        }
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
        payloadArea.setEditable(editing);
    }

    public boolean isEditing() {
        return editing;
    }

    public void saveCurrentEdit() {
        if (currentEntry != null && editing) {
            currentEntry.setPayload(payloadArea.getText());
        }
        editing = false;
        payloadArea.setEditable(false);
    }

    public String getCurrentPayload() {
        return payloadArea.getText();
    }

    public Entry getCurrentEntry() {
        return currentEntry;
    }
}

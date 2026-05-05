package com.payloadnotebook.ui;

import com.payloadnotebook.model.Entry;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class EntryListPanel extends JPanel {

    private final DefaultListModel<Entry> listModel;
    private final JList<Entry> entryList;
    private Consumer<Entry> onEntrySelected;

    public EntryListPanel() {
        setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        entryList = new JList<>(listModel);
        entryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        entryList.setCellRenderer(new EntryCellRenderer());

        entryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && onEntrySelected != null) {
                onEntrySelected.accept(entryList.getSelectedValue());
            }
        });

        add(new JScrollPane(entryList), BorderLayout.CENTER);
    }

    public void setOnEntrySelected(Consumer<Entry> listener) {
        this.onEntrySelected = listener;
    }

    public void refreshEntries(List<Entry> entries) {
        listModel.clear();
        if (entries != null) {
            for (Entry entry : entries) {
                listModel.addElement(entry);
            }
        }
    }

    public Entry getSelectedEntry() {
        return entryList.getSelectedValue();
    }

    public void clearSelection() {
        entryList.clearSelection();
    }

    private static class EntryCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Entry entry) {
                setText(entry.getTitle());
            }
            return this;
        }
    }
}

package com.payloadnotebook.ui;

import javax.swing.*;
import java.awt.*;

public class ToolbarPanel extends JPanel {

    private final JTextField searchField;
    private final JButton btnAddCategory;
    private final JButton btnAddSubCategory;
    private final JButton btnAddEntry;
    private final JButton btnDelete;
    private final JButton btnCopy;
    private final JButton btnEdit;
    private final JButton btnChooseFile;
    private final JButton btnDefaultPath;

    public ToolbarPanel() {
        setLayout(new BorderLayout(5, 0));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        searchField = new JTextField();
        searchField.setToolTipText("搜索标题或Payload内容 (Ctrl+F)");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        btnAddCategory = new JButton("+一级分类");
        btnAddSubCategory = new JButton("+二级分类");
        btnAddEntry = new JButton("添加条目");
        btnDelete = new JButton("删除");
        btnCopy = new JButton("复制");
        btnEdit = new JButton("编辑");
        btnChooseFile = new JButton("选择文件");
        btnDefaultPath = new JButton("默认路径");

        Font btnFont = btnAddCategory.getFont().deriveFont(11f);
        for (JButton btn : new JButton[]{btnAddCategory, btnAddSubCategory, btnAddEntry, btnDelete, btnCopy, btnEdit, btnChooseFile, btnDefaultPath}) {
            btn.setFont(btnFont);
            buttonPanel.add(btn);
        }

        add(searchField, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.EAST);
    }

    public JTextField getSearchField() { return searchField; }
    public JButton getBtnAddCategory() { return btnAddCategory; }
    public JButton getBtnAddSubCategory() { return btnAddSubCategory; }
    public JButton getBtnAddEntry() { return btnAddEntry; }
    public JButton getBtnDelete() { return btnDelete; }
    public JButton getBtnCopy() { return btnCopy; }
    public JButton getBtnEdit() { return btnEdit; }
    public JButton getBtnChooseFile() { return btnChooseFile; }
    public JButton getBtnDefaultPath() { return btnDefaultPath; }
}

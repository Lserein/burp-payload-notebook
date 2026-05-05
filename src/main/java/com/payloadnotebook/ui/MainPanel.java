package com.payloadnotebook.ui;

import com.payloadnotebook.model.*;
import com.payloadnotebook.service.ClipboardService;
import com.payloadnotebook.service.DataService;
import com.payloadnotebook.util.DialogUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class MainPanel extends JPanel {

    private static final String PREFS_FILE = "notebook_prefs.properties";
    private static final String KEY_CUSTOM_PATH = "custom.json.path";

    private final DataService dataService;
    private final CategoryTreePanel categoryTreePanel;
    private final EntryListPanel entryListPanel;
    private final PayloadEditorPanel payloadEditorPanel;
    private final ToolbarPanel toolbarPanel;
    private final File prefsFile;

    // Current selection state
    private String selectedCategoryId;
    private String selectedSubCategoryId;

    public MainPanel(DataService dataService) {
        this.dataService = dataService;
        this.prefsFile = new File(dataService.getDefaultDataFile().getParent(), PREFS_FILE);
        setLayout(new BorderLayout());

        // Restore last used custom JSON path if exists
        String savedPath = loadCustomPath();
        if (savedPath != null && new File(savedPath).exists()) {
            dataService.loadFromFile(new File(savedPath));
        }

        // Create panels
        categoryTreePanel = new CategoryTreePanel();
        entryListPanel = new EntryListPanel();
        payloadEditorPanel = new PayloadEditorPanel();
        toolbarPanel = new ToolbarPanel();

        // Right side: toolbar + entry list + payload editor
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(toolbarPanel, BorderLayout.NORTH);

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(entryListPanel), payloadEditorPanel);
        rightSplit.setDividerLocation(250);
        rightSplit.setResizeWeight(0.5);
        rightPanel.add(rightSplit, BorderLayout.CENTER);

        // Main split pane
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                categoryTreePanel, rightPanel);
        mainSplit.setDividerLocation(220);
        mainSplit.setDividerSize(3);
        add(mainSplit, BorderLayout.CENTER);

        // Wire up interactions
        setupCategorySelection();
        setupToolbarActions();
        setupSearch();
        setupShortcuts();

        // Initial load
        categoryTreePanel.refreshTree(dataService.getData());
    }

    private void setupCategorySelection() {
        categoryTreePanel.setOnSelectionChange(selection -> {
            selectedCategoryId = selection[0];
            selectedSubCategoryId = selection[1];
            refreshEntryList();
            payloadEditorPanel.displayEntry(null, null);
        });
    }

    private void setupToolbarActions() {
        // Add category
        toolbarPanel.getBtnAddCategory().addActionListener(e -> {
            String name = DialogUtil.showInputDialog(this, "新增一级分类", "分类名称：");
            if (name != null && !name.trim().isEmpty()) {
                dataService.addCategory(name.trim());
                categoryTreePanel.refreshTree(dataService.getData());
            }
        });

        // Add subcategory
        toolbarPanel.getBtnAddSubCategory().addActionListener(e -> {
            if (selectedCategoryId == null) {
                showToast("请先选择一个一级分类");
                return;
            }
            Category parent = dataService.findCategory(selectedCategoryId);
            if (parent == null) return;

            String name = DialogUtil.showInputDialog(this, "新增二级分类（" + parent.getName() + "）", "分类名称：");
            if (name != null && !name.trim().isEmpty()) {
                dataService.addSubCategory(selectedCategoryId, name.trim());
                categoryTreePanel.refreshTree(dataService.getData());
            }
        });

        // Add entry
        toolbarPanel.getBtnAddEntry().addActionListener(e -> showAddEntryDialog());

        // Delete
        toolbarPanel.getBtnDelete().addActionListener(e -> handleDelete());

        // Copy
        toolbarPanel.getBtnCopy().addActionListener(e -> handleCopy());

        // Edit
        toolbarPanel.getBtnEdit().addActionListener(e -> handleEdit());

        // Choose custom JSON file
        toolbarPanel.getBtnChooseFile().addActionListener(e -> handleChooseFile());

        // Switch back to default path
        toolbarPanel.getBtnDefaultPath().addActionListener(e -> handleDefaultPath());

        // Entry selection
        entryListPanel.setOnEntrySelected(entry -> {
            if (entry != null) {
                String path = dataService.getCategoryPath(entry.getId());
                payloadEditorPanel.displayEntry(entry, path);
            }
        });
    }

    private void showAddEntryDialog() {
        List<SubCategory> allSubs = dataService.getAllSubCategories();
        if (allSubs.isEmpty()) {
            showToast("请先创建二级分类");
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "添加条目", true);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // SubCategory combo
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("二级分类："), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        SubCategory[] subArray = allSubs.toArray(new SubCategory[0]);
        JComboBox<SubCategory> subCombo = new JComboBox<>(subArray);
        formPanel.add(subCombo, gbc);

        // Title
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("标题："), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JTextField titleField = new JTextField();
        formPanel.add(titleField, gbc);

        // Payload
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Payload："), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea payloadArea = new JTextArea();
        payloadArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        formPanel.add(new JScrollPane(payloadArea), gbc);

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okBtn = new JButton("确定");
        JButton cancelBtn = new JButton("取消");
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        okBtn.addActionListener(ev -> {
            SubCategory selected = (SubCategory) subCombo.getSelectedItem();
            String title = titleField.getText().trim();
            String payload = payloadArea.getText();
            if (selected == null || title.isEmpty()) {
                showToast("标题不能为空");
                return;
            }
            dataService.addEntry(selected.getId(), title, payload);
            refreshEntryList();
            dialog.dispose();
        });
        cancelBtn.addActionListener(ev -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void handleDelete() {
        Entry selectedEntry = entryListPanel.getSelectedEntry();
        Object selectedObj = categoryTreePanel.getSelectedUserObject();

        if (selectedEntry != null) {
            // Delete entry
            if (DialogUtil.showConfirmDialog(this, "确定删除条目「" + selectedEntry.getTitle() + "」？")) {
                dataService.deleteEntry(selectedEntry.getId());
                refreshEntryList();
                payloadEditorPanel.displayEntry(null, null);
            }
        } else if (selectedObj instanceof Category cat) {
            String msg = "确定删除一级分类「" + cat.getName() + "」？\n将同时删除其下所有二级分类和条目。";
            if (DialogUtil.showConfirmDialog(this, msg)) {
                dataService.deleteCategory(cat.getId());
                selectedCategoryId = null;
                selectedSubCategoryId = null;
                categoryTreePanel.refreshTree(dataService.getData());
                refreshEntryList();
                payloadEditorPanel.displayEntry(null, null);
            }
        } else if (selectedObj instanceof SubCategory sub) {
            String msg = "确定删除二级分类「" + sub.getName() + "」？\n将同时删除其下所有条目。";
            if (DialogUtil.showConfirmDialog(this, msg)) {
                dataService.deleteSubCategory(sub.getId());
                selectedSubCategoryId = null;
                categoryTreePanel.refreshTree(dataService.getData());
                refreshEntryList();
                payloadEditorPanel.displayEntry(null, null);
            }
        }
    }

    private void handleCopy() {
        Entry entry = entryListPanel.getSelectedEntry();
        if (entry == null) {
            showToast("请先选择一个条目");
            return;
        }
        ClipboardService.copyToClipboard(entry.getPayload());
        showToast("已复制");
    }

    private void handleEdit() {
        Entry entry = entryListPanel.getSelectedEntry();
        if (entry == null) {
            showToast("请先选择一个条目");
            return;
        }

        if (payloadEditorPanel.isEditing()) {
            // Save and exit edit mode
            payloadEditorPanel.saveCurrentEdit();
            dataService.saveData();
            toolbarPanel.getBtnEdit().setText("编辑");
            showToast("已保存");
        } else {
            // Enter edit mode
            payloadEditorPanel.setEditing(true);
            toolbarPanel.getBtnEdit().setText("保存");
        }
    }

    private void setupSearch() {
        toolbarPanel.getSearchField().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { doSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { doSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { doSearch(); }
        });
    }

    private void doSearch() {
        String keyword = toolbarPanel.getSearchField().getText();
        List<Entry> results = dataService.search(keyword, selectedCategoryId, selectedSubCategoryId);
        entryListPanel.refreshEntries(results);
        payloadEditorPanel.displayEntry(null, null);
    }

    private void setupShortcuts() {
        // Ctrl+F -> focus search
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), "focusSearch");
        getActionMap().put("focusSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toolbarPanel.getSearchField().requestFocusInWindow();
                toolbarPanel.getSearchField().selectAll();
            }
        });

        // Ctrl+C -> copy (only when not in text editing)
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "copyPayload");
        getActionMap().put("copyPayload", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Only handle if focus is not in a text component
                Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (!(focusOwner instanceof JTextComponent)) {
                    handleCopy();
                }
            }
        });
    }

    private void refreshEntryList() {
        String keyword = toolbarPanel.getSearchField().getText();
        List<Entry> entries = dataService.search(keyword, selectedCategoryId, selectedSubCategoryId);
        entryListPanel.refreshEntries(entries);
    }

    private void showToast(String message) {
        JWindow toast = new JWindow(SwingUtilities.getWindowAncestor(this));
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(new Color(60, 60, 60));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        toast.getContentPane().add(label);
        toast.pack();

        // Position at bottom center of parent
        Window parent = SwingUtilities.getWindowAncestor(this);
        if (parent != null) {
            int x = parent.getX() + (parent.getWidth() - toast.getWidth()) / 2;
            int y = parent.getY() + parent.getHeight() - toast.getHeight() - 30;
            toast.setLocation(x, y);
        }

        toast.setVisible(true);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.dispose();
            }
        }, 1000);
    }

    // --- Custom file path ---

    private void handleChooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择 Payload JSON 文件");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON 文件 (*.json)", "json"));
        chooser.setSelectedFile(new File("payload_notebook.json"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            boolean ok = dataService.loadFromFile(file);
            if (ok) {
                saveCustomPath(file.getAbsolutePath());
                refreshAll();
                showToast("已加载: " + file.getName());
            } else {
                showToast("文件读取失败");
            }
        }
    }

    private void handleDefaultPath() {
        dataService.loadDefault();
        clearCustomPath();
        refreshAll();
        showToast("已切回默认路径");
    }

    private void refreshAll() {
        selectedCategoryId = null;
        selectedSubCategoryId = null;
        categoryTreePanel.refreshTree(dataService.getData());
        entryListPanel.refreshEntries(dataService.getAllEntries());
        payloadEditorPanel.displayEntry(null, null);
    }

    private String loadCustomPath() {
        if (!prefsFile.exists()) return null;
        Properties props = new Properties();
        try (Reader r = Files.newBufferedReader(prefsFile.toPath(), StandardCharsets.UTF_8)) {
            props.load(r);
            return props.getProperty(KEY_CUSTOM_PATH);
        } catch (Exception e) {
            return null;
        }
    }

    private void saveCustomPath(String path) {
        Properties props = new Properties();
        props.setProperty(KEY_CUSTOM_PATH, path);
        try (Writer w = Files.newBufferedWriter(prefsFile.toPath(), StandardCharsets.UTF_8)) {
            props.store(w, "Payload Notebook preferences");
        } catch (Exception e) {
            System.err.println("[Payload Notebook] Failed to save preferences: " + e.getMessage());
        }
    }

    private void clearCustomPath() {
        if (!prefsFile.exists()) return;
        Properties props = new Properties();
        try (Reader r = Files.newBufferedReader(prefsFile.toPath(), StandardCharsets.UTF_8)) {
            props.load(r);
        } catch (Exception ignored) {}
        props.remove(KEY_CUSTOM_PATH);
        try (Writer w = Files.newBufferedWriter(prefsFile.toPath(), StandardCharsets.UTF_8)) {
            props.store(w, "Payload Notebook preferences");
        } catch (Exception e) {
            System.err.println("[Payload Notebook] Failed to save preferences: " + e.getMessage());
        }
    }
}

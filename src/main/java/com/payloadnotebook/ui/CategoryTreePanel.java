package com.payloadnotebook.ui;

import com.payloadnotebook.model.Category;
import com.payloadnotebook.model.NotebookData;
import com.payloadnotebook.model.SubCategory;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.util.function.Consumer;

public class CategoryTreePanel extends JPanel {

    private final JTree tree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;

    // Selection callback: (categoryId, subCategoryId) - either can be null
    private Consumer<String[]> selectionListener;

    public CategoryTreePanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(220, 0));

        rootNode = new DefaultMutableTreeNode("root");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new CategoryCellRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node == null || selectionListener == null) return;

            if (node.getUserObject() instanceof Category cat) {
                selectionListener.accept(new String[]{cat.getId(), null});
            } else if (node.getUserObject() instanceof SubCategory sub) {
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
                if (parent != null && parent.getUserObject() instanceof Category cat) {
                    selectionListener.accept(new String[]{cat.getId(), sub.getId()});
                }
            }
        });

        add(new JScrollPane(tree), BorderLayout.CENTER);
    }

    public void setOnSelectionChange(Consumer<String[]> listener) {
        this.selectionListener = listener;
    }

    public void refreshTree(NotebookData data) {
        rootNode.removeAllChildren();
        if (data == null || data.getCategories() == null) {
            treeModel.reload();
            return;
        }

        for (Category cat : data.getCategories()) {
            DefaultMutableTreeNode catNode = new DefaultMutableTreeNode(cat);
            for (SubCategory sub : cat.getSubCategories()) {
                catNode.add(new DefaultMutableTreeNode(sub));
            }
            rootNode.add(catNode);
        }
        treeModel.reload();

        // Expand all categories
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    public Object getSelectedUserObject() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        return node != null ? node.getUserObject() : null;
    }

    public DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
    }

    private static class CategoryCellRenderer extends DefaultTreeCellRenderer {

        private static final Color SUB_TEXT_COLOR = new Color(80, 80, 80);
        private static final Color[] CATEGORY_COLORS = {
                new Color(41, 98, 168),   // SQL注入 - 蓝
                new Color(46, 139, 87),   // XSS - 绿
                new Color(196, 120, 30),  // SSRF - 橙
                new Color(138, 43, 226),  // 越权/未授权 - 紫
                new Color(44, 120, 115),  // 文件上传 - 青
                new Color(178, 34, 34),   // 命令执行 - 暗红
                new Color(70, 100, 150),  // XXE - 灰蓝
                new Color(120, 100, 60),  // CSRF - 棕
                new Color(88, 88, 88)     // 备用 - 灰
        };

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object obj = node.getUserObject();

            if (obj instanceof SubCategory sub) {
                setText(sub.getName());
                setBorder(BorderFactory.createEmptyBorder(1, 20, 1, 2));
                if (!sel) {
                    setForeground(SUB_TEXT_COLOR);
                }
            } else if (obj instanceof Category cat) {
                setText(cat.getName());
                setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));
                if (!sel) {
                    int index = node.getParent() != null
                            ? node.getParent().getIndex(node) : 0;
                    setForeground(CATEGORY_COLORS[index % CATEGORY_COLORS.length]);
                }
                setFont(getFont().deriveFont(Font.BOLD));
            }

            return this;
        }
    }
}

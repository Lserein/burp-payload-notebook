# BurpSuite Payload Notebook

面向个人渗透测试的 BurpSuite Payload 笔记管理插件，支持自定义二级分类、实时搜索、一键复制。

## 功能概览

| 功能 | 说明 |
|------|------|
| 二级分类管理 | 支持新增/删除/重命名一级和二级分类，删除一级分类时级联删除子分类及条目 |
| 条目管理 | 添加/编辑/删除 Payload 条目，编辑后自动保存 |
| 实时搜索 | 输入即搜索，模糊匹配标题和 Payload 内容，支持分类联动过滤 |
| 一键复制 | 选中条目后一键复制 Payload 到剪贴板，Toast 提示反馈 |
| 数据持久化 | 自动保存为 JSON 文件，重启 Burp 数据不丢失 |
| 内置分类 | 预置 SQL注入、XSS、SSRF、越权/未授权、文件上传、命令执行、XXE、CSRF 八大类 |

## 安装

### 环境要求

- Burp Suite Professional 2024.2+
- JDK 17+（编译用）

### 编译

```bash
cd burp-payload-notebook
mvn clean package
```

生成的 JAR 文件位于 `target/burp-payload-notebook-1.0.0.jar`。

### 加载插件

1. 打开 Burp Suite → Extender → Extensions
2. 点击 Add → Extension type 选 Java
3. 选择 `target/burp-payload-notebook-1.0.0.jar`
4. 加载成功后，顶部导航栏出现 **Payload Notebook** 标签页

## 使用说明

### 界面布局

```
┌────────────┬──────────────────────────────────┐
│            │  [搜索框]  [+一级] [+二级] [添加] [删除] [复制] [编辑] │
│  分类树     │──────────────────────────────────│
│            │  条目列表                          │
│  一级分类   │                                  │
│   ├ 二级   │──────────────────────────────────│
│   └ 二级   │  Payload 预览/编辑区               │
│            │  一级分类 > 二级分类                 │
└────────────┴──────────────────────────────────┘
```

### 分类操作

- **新增一级分类**：点击 `+一级分类` 按钮，输入名称确认
- **新增二级分类**：先选中一个一级分类，点击 `+二级分类` 按钮，输入名称确认
- **删除分类**：选中要删除的分类（一级或二级），点击 `删除` 按钮，二次确认后删除
- **重命名分类**：双击分类名称即可编辑（待实现）

### 条目操作

- **添加条目**：点击 `添加条目` 按钮，选择二级分类，输入标题和 Payload 内容，点击确定
- **编辑条目**：选中条目后点击 `编辑` 按钮，Payload 区域变为可编辑，修改后点击 `保存` 或切换条目自动保存
- **删除条目**：选中条目后点击 `删除` 按钮，二次确认后删除
- **复制 Payload**：选中条目后点击 `复制` 按钮或按 `Ctrl+C`，弹出"已复制"提示

### 搜索

- 顶部搜索框输入关键词，实时过滤条目
- 支持模糊匹配，搜索标题和 Payload 内容
- 选中某一分类后再搜索，仅搜索该分类下的条目

### 快捷键

| 快捷键 | 功能 |
|--------|------|
| `Ctrl+F` | 聚焦搜索框 |
| `Ctrl+C` | 复制当前选中条目的 Payload |

## 数据存储

- 数据文件：`payload_notebook.json`，存储在插件 JAR 同级目录的 `payload-notebook-data/` 下
- 格式：JSON，可手动备份或迁移
- 容错：若数据文件损坏，自动加载内置默认分类（8 个一级分类，无条目）

## 技术栈

- [Burp Montoya API](https://portswigger.github.io/burp-extensions-montoya-api/) 2025.5
- Java Swing（原生组件，无第三方 UI 库）
- [Gson](https://github.com/google/gson) 2.11.0（JSON 序列化）
- Maven + maven-shade-plugin（构建打包）

## 项目结构

```
src/main/java/com/payloadnotebook/
├── BurpPayloadNotebook.java        # 插件入口
├── model/
│   ├── Category.java               # 一级分类模型
│   ├── SubCategory.java            # 二级分类模型
│   ├── Entry.java                  # 条目模型
│   └── NotebookData.java           # 根数据模型
├── service/
│   ├── DataService.java            # 数据 CRUD、搜索、持久化
│   └── ClipboardService.java       # 剪贴板操作
├── ui/
│   ├── MainPanel.java              # 主面板（交互逻辑）
│   ├── CategoryTreePanel.java      # 左侧分类树
│   ├── EntryListPanel.java         # 条目列表
│   ├── PayloadEditorPanel.java     # Payload 预览/编辑区
│   └── ToolbarPanel.java           # 搜索框 + 操作按钮
└── util/
    └── DialogUtil.java             # 对话框工具
```

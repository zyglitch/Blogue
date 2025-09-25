package com.sept3rd.bloggenerator.ui;

import com.sept3rd.bloggenerator.model.Article;
import com.sept3rd.bloggenerator.parser.MarkdownParser;
import com.sept3rd.bloggenerator.generator.HtmlTemplateGenerator;
import com.sept3rd.bloggenerator.manager.ArticleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * 主窗口类
 * 提供Markdown转HTML的图形用户界面
 */
public class MainWindow extends JFrame {
    
    private static final String TITLE = "博客文章生成器";
    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 700;
    
    // UI组件
    private JList<File> fileList;
    private DefaultListModel<File> fileListModel;
    private JTextArea previewArea;
    private JTextArea logArea;
    private JButton selectFilesButton;
    private JButton selectFolderButton;
    private JButton generateButton;
    private JButton clearButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    // 业务组件
    private MarkdownParser markdownParser;
    private HtmlTemplateGenerator templateGenerator;
    private ArticleManager articleManager;
    
    public MainWindow() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        initializeBusinessComponents();
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeComponents() {
        setTitle(TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        
        // 文件列表
        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fileList.setCellRenderer(new FileListCellRenderer());
        
        // 预览区域
        previewArea = new JTextArea();
        previewArea.setEditable(false);
        previewArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        previewArea.setBackground(new Color(248, 248, 248));
        
        // 日志区域
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        logArea.setBackground(new Color(245, 245, 245));
        
        // 按钮
        selectFilesButton = new JButton("选择Markdown文件");
        selectFolderButton = new JButton("选择文件夹");
        generateButton = new JButton("生成HTML文章");
        clearButton = new JButton("清空列表");
        
        // 进度条和状态
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        statusLabel = new JLabel("就绪");
        
        // 设置按钮样式
        styleButton(selectFilesButton);
        styleButton(selectFolderButton);
        styleButton(generateButton);
        styleButton(clearButton);
        
        // 特别设置生成按钮的样式，使其更加醒目
        generateButton.setBackground(new Color(40, 167, 69)); // 绿色背景
        generateButton.setForeground(Color.WHITE);
        generateButton.setOpaque(true);
        generateButton.setBorderPainted(false);
        generateButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
    }
    
    /**
     * 设置按钮样式
     */
    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 顶部工具栏
        JPanel toolbarPanel = createToolbarPanel();
        add(toolbarPanel, BorderLayout.NORTH);
        
        // 中央分割面板
        JSplitPane mainSplitPane = createMainSplitPane();
        add(mainSplitPane, BorderLayout.CENTER);
        
        // 底部状态栏
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 创建工具栏面板
     */
    private JPanel createToolbarPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new EmptyBorder(10, 10, 5, 10));
        panel.setBackground(Color.WHITE);
        
        panel.add(selectFilesButton);
        panel.add(selectFolderButton);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(generateButton);
        panel.add(clearButton);
        
        return panel;
    }
    
    /**
     * 创建主分割面板
     */
    private JSplitPane createMainSplitPane() {
        // 左侧文件列表面板
        JPanel leftPanel = createFileListPanel();
        
        // 右侧预览和日志面板
        JPanel rightPanel = createPreviewPanel();
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.3);
        
        return splitPane;
    }
    
    /**
     * 创建文件列表面板
     */
    private JPanel createFileListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Markdown文件列表"));
        
        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setPreferredSize(new Dimension(280, 400));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建预览面板
     */
    private JPanel createPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 创建选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // 预览选项卡
        JScrollPane previewScrollPane = new JScrollPane(previewArea);
        previewScrollPane.setPreferredSize(new Dimension(500, 300));
        tabbedPane.addTab("文章预览", previewScrollPane);
        
        // 日志选项卡
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setPreferredSize(new Dimension(500, 300));
        tabbedPane.addTab("处理日志", logScrollPane);
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建状态栏面板
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 10, 10, 10));
        panel.setBackground(Color.WHITE);
        
        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 选择文件按钮
        selectFilesButton.addActionListener(e -> selectMarkdownFiles());
        
        // 选择文件夹按钮
        selectFolderButton.addActionListener(e -> selectMarkdownFolder());
        
        // 生成按钮
        generateButton.addActionListener(e -> generateArticles());
        
        // 清空按钮
        clearButton.addActionListener(e -> clearFileList());
        
        // 文件列表选择事件
        fileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                previewSelectedFile();
            }
        });
    }
    
    /**
     * 初始化业务组件
     */
    private void initializeBusinessComponents() {
        try {
            markdownParser = new MarkdownParser();
            templateGenerator = new HtmlTemplateGenerator();
            articleManager = new ArticleManager();
            
            // 验证模板
            if (!templateGenerator.validateTemplate()) {
                showError("HTML模板验证失败，请检查模板文件");
            }
            
            logMessage("组件初始化完成");
        } catch (Exception e) {
            showError("初始化失败: " + e.getMessage());
        }
    }
    
    /**
     * 选择Markdown文件
     */
    private void selectMarkdownFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Markdown文件 (*.md, *.markdown)", "md", "markdown"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            addFilesToList(selectedFiles);
        }
    }
    
    /**
     * 选择Markdown文件夹
     */
    private void selectMarkdownFolder() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = fileChooser.getSelectedFile();
            scanMarkdownFiles(selectedFolder);
        }
    }
    
    /**
     * 扫描文件夹中的Markdown文件
     */
    private void scanMarkdownFiles(File folder) {
        List<File> markdownFiles = new ArrayList<>();
        scanMarkdownFilesRecursive(folder, markdownFiles);
        
        if (markdownFiles.isEmpty()) {
            showInfo("在选定文件夹中未找到Markdown文件");
        } else {
            addFilesToList(markdownFiles.toArray(new File[0]));
            logMessage("从文件夹扫描到 " + markdownFiles.size() + " 个Markdown文件");
        }
    }
    
    /**
     * 递归扫描Markdown文件
     */
    private void scanMarkdownFilesRecursive(File folder, List<File> markdownFiles) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanMarkdownFilesRecursive(file, markdownFiles);
                } else if (isMarkdownFile(file)) {
                    markdownFiles.add(file);
                }
            }
        }
    }
    
    /**
     * 判断是否为Markdown文件
     */
    private boolean isMarkdownFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".md") || name.endsWith(".markdown");
    }
    
    /**
     * 添加文件到列表
     */
    private void addFilesToList(File[] files) {
        for (File file : files) {
            if (isMarkdownFile(file) && !fileListModel.contains(file)) {
                fileListModel.addElement(file);
            }
        }
        updateStatus();
    }
    
    /**
     * 清空文件列表
     */
    private void clearFileList() {
        fileListModel.clear();
        previewArea.setText("");
        updateStatus();
        logMessage("已清空文件列表");
    }
    
    /**
     * 预览选中的文件
     */
    private void previewSelectedFile() {
        File selectedFile = fileList.getSelectedValue();
        if (selectedFile != null) {
            try {
                String content = Files.readString(selectedFile.toPath());
                previewArea.setText(content);
                previewArea.setCaretPosition(0);
            } catch (IOException e) {
                previewArea.setText("无法读取文件: " + e.getMessage());
            }
        }
    }
    
    /**
     * 生成文章
     */
    private void generateArticles() {
        if (fileListModel.isEmpty()) {
            showInfo("请先选择Markdown文件");
            return;
        }
        
        // 在后台线程中执行生成任务
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                generateArticlesInBackground();
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    logMessage(message);
                }
            }
            
            @Override
            protected void done() {
                generateButton.setEnabled(true);
                progressBar.setValue(0);
                statusLabel.setText("生成完成");
            }
        };
        
        generateButton.setEnabled(false);
        worker.execute();
    }
    
    /**
     * 在后台线程中生成文章
     */
    private void generateArticlesInBackground() {
        int totalFiles = fileListModel.getSize();
        int successCount = 0;
        int errorCount = 0;
        
        for (int i = 0; i < totalFiles; i++) {
            File file = fileListModel.getElementAt(i);
            
            try {
                // 更新进度
                int progress = (i * 100) / totalFiles;
                progressBar.setValue(progress);
                statusLabel.setText("处理: " + file.getName());
                
                // 解析Markdown文件
                Article article = markdownParser.parseFile(file.toPath());
                
                // 生成HTML
                String html = templateGenerator.generateHtml(article);
                String fileName = templateGenerator.generateFileName(article);
                
                // 保存文件
                articleManager.saveHtmlFile(fileName, html);
                
                // 添加到articles.js
                articleManager.addArticle(article);
                
                successCount++;
                logMessage("✓ 成功处理: " + file.getName() + " -> " + fileName);
                
            } catch (Exception e) {
                errorCount++;
                logMessage("✗ 处理失败: " + file.getName() + " - " + e.getMessage());
            }
        }
        
        progressBar.setValue(100);
        logMessage("=== 处理完成 ===");
        logMessage("成功: " + successCount + " 个文件");
        logMessage("失败: " + errorCount + " 个文件");
    }
    
    /**
     * 更新状态
     */
    private void updateStatus() {
        int fileCount = fileListModel.getSize();
        statusLabel.setText("已选择 " + fileCount + " 个文件");
    }
    
    /**
     * 记录日志消息
     */
    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + java.time.LocalTime.now().toString() + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    /**
     * 显示错误消息
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
        logMessage("错误: " + message);
    }
    
    /**
     * 显示信息消息
     */
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "信息", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 文件列表单元格渲染器
     */
    private static class FileListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof File) {
                File file = (File) value;
                setText(file.getName());
                setToolTipText(file.getAbsolutePath());
            }
            
            return this;
        }
    }
}
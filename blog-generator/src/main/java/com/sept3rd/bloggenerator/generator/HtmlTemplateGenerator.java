package com.sept3rd.bloggenerator.generator;

import com.sept3rd.bloggenerator.model.Article;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HTML模板生成器
 * 负责将Article对象转换为完整的HTML页面
 */
public class HtmlTemplateGenerator {
    
    private static final String TEMPLATE_PATH = "/templates/article-template.html";
    private String templateContent;
    
    public HtmlTemplateGenerator() throws IOException {
        loadTemplate();
    }

    /**
     * 加载HTML模板文件
     */
    private void loadTemplate() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(TEMPLATE_PATH)) {
            if (inputStream == null) {
                throw new IOException("模板文件不存在: " + TEMPLATE_PATH);
            }
            this.templateContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    
    /**
     * 生成HTML页面
     * @param article 文章对象
     * @return 完整的HTML页面内容
     */
    public String generateHtml(Article article) {
        if (article == null || !article.isValid()) {
            throw new IllegalArgumentException("文章对象无效");
        }
        
        Map<String, String> placeholders = createPlaceholders(article);
        String html = templateContent;
        
        // 替换所有占位符
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            html = html.replace(entry.getKey(), entry.getValue());
        }
        
        return html;
    }
    
    /**
     * 创建模板占位符映射
     */
    private Map<String, String> createPlaceholders(Article article) {
        Map<String, String> placeholders = new HashMap<>();
        
        // 基本信息
        placeholders.put("{{ARTICLE_TITLE}}", escapeHtml(article.getTitle()));
        placeholders.put("{{ARTICLE_SUMMARY}}", escapeHtml(article.getSummary()));
        placeholders.put("{{ARTICLE_CONTENT}}", article.getContent()); // HTML内容不需要转义
        placeholders.put("{{ARTICLE_AUTHOR}}", escapeHtml(article.getAuthor()));
        placeholders.put("{{ARTICLE_DATE}}", article.getDate().toString());
        placeholders.put("{{ARTICLE_DATE_DISPLAY}}", escapeHtml(article.getDateDisplay()));
        
        // 标签处理
        String tagsHtml = generateTagsHtml(article.getTags());
        placeholders.put("{{ARTICLE_TAGS}}", tagsHtml);
        
        return placeholders;
    }
    
    /**
     * 生成标签HTML
     */
    private String generateTagsHtml(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        
        return tags.stream()
                .map(tag -> "<span class=\"tag\">" + escapeHtml(tag.trim()) + "</span>")
                .collect(Collectors.joining(" "));
    }
    
    /**
     * HTML转义
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
    
    /**
     * 生成文章文件名
     * @param article 文章对象
     * @return HTML文件名
     */
    public String generateFileName(Article article) {
        if (article.getSlug() != null && !article.getSlug().trim().isEmpty()) {
            return article.getSlug() + ".html";
        }
        return "article-" + article.getId() + ".html";
    }
    
    /**
     * 验证模板是否包含所有必需的占位符
     */
    public boolean validateTemplate() {
        String[] requiredPlaceholders = {
            "{{ARTICLE_TITLE}}",
            "{{ARTICLE_SUMMARY}}",
            "{{ARTICLE_CONTENT}}",
            "{{ARTICLE_TAGS}}",
            "{{ARTICLE_AUTHOR}}",
            "{{ARTICLE_DATE}}",
            "{{ARTICLE_DATE_DISPLAY}}"
        };
        
        for (String placeholder : requiredPlaceholders) {
            if (!templateContent.contains(placeholder)) {
                System.err.println("模板缺少占位符: " + placeholder);
                return false;
            }
        }
        return true;
    }
    
    /**
     * 重新加载模板（用于模板文件更新后）
     */
    public void reloadTemplate() throws IOException {
        loadTemplate();
    }
    
    /**
     * 获取模板内容（用于调试）
     */
    public String getTemplateContent() {
        return templateContent;
    }
}
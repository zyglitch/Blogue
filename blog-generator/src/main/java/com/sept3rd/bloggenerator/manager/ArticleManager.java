package com.sept3rd.bloggenerator.manager;

import com.sept3rd.bloggenerator.model.Article;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文章管理器
 * 负责管理articles.js文件的读取和更新
 */
public class ArticleManager {
    
    private static final String ARTICLES_JS_PATH = "js/articles.js";
    private static final String HTML_DIR = "html";
    
    private final ObjectMapper objectMapper;
    private final Path articlesJsPath;
    private final Path htmlDir;
    
    public ArticleManager() {
        this.objectMapper = new ObjectMapper();
        this.articlesJsPath = Paths.get(ARTICLES_JS_PATH);
        this.htmlDir = Paths.get(HTML_DIR);
    }
    
    /**
     * 从articles.js文件中读取现有文章列表
     */
    public List<Article> loadExistingArticles() throws IOException {
        List<Article> articles = new ArrayList<>();
        
        if (!Files.exists(articlesJsPath)) {
            System.out.println("articles.js文件不存在，将创建新文件");
            return articles;
        }
        
        String content = Files.readString(articlesJsPath, StandardCharsets.UTF_8);
        
        // 使用正则表达式提取articles数组
        Pattern pattern = Pattern.compile("const articles = (\\[.*?\\]);", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String articlesJson = matcher.group(1);
            try {
                JsonNode articlesNode = objectMapper.readTree(articlesJson);
                if (articlesNode.isArray()) {
                    for (JsonNode articleNode : articlesNode) {
                        Article article = parseArticleFromJson(articleNode);
                        if (article != null) {
                            articles.add(article);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("解析articles.js中的JSON数据失败: " + e.getMessage());
            }
        }
        
        return articles;
    }
    
    /**
     * 从JSON节点解析文章对象
     */
    private Article parseArticleFromJson(JsonNode node) {
        try {
            Article article = new Article();
            
            if (node.has("id")) article.setId(node.get("id").asInt());
            if (node.has("title")) article.setTitle(node.get("title").asText());
            if (node.has("slug")) article.setSlug(node.get("slug").asText());
            if (node.has("link")) article.setLink(node.get("link").asText());
            if (node.has("summary")) article.setSummary(node.get("summary").asText());
            if (node.has("date")) article.setDate(LocalDate.parse(node.get("date").asText()));
            if (node.has("dateDisplay")) article.setDateDisplay(node.get("dateDisplay").asText());
            if (node.has("author")) article.setAuthor(node.get("author").asText());
            
            // 处理标签数组
            if (node.has("tags") && node.get("tags").isArray()) {
                ArrayNode tagsArray = (ArrayNode) node.get("tags");
                List<String> tags = new ArrayList<>();
                for (int i = 0; i < tagsArray.size(); i++) {
                    tags.add(tagsArray.get(i).asText());
                }
                article.setTags(tags);
            }
            
            return article;
        } catch (Exception e) {
            System.err.println("解析文章JSON失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 添加新文章到articles.js
     */
    public void addArticle(Article article) throws IOException {
        List<Article> existingArticles = loadExistingArticles();
        
        // 生成新的ID
        int maxId = existingArticles.stream()
                .mapToInt(Article::getId)
                .max()
                .orElse(0);
        article.setId(maxId + 1);
        
        // 添加到列表
        existingArticles.add(0, article); // 新文章添加到开头
        
        // 更新articles.js文件
        updateArticlesJs(existingArticles);
    }
    
    /**
     * 更新articles.js文件
     */
    private void updateArticlesJs(List<Article> articles) throws IOException {
        // 确保js目录存在
        Path jsDir = articlesJsPath.getParent();
        if (jsDir != null && !Files.exists(jsDir)) {
            Files.createDirectories(jsDir);
        }
        
        // 生成新的articles数组JSON
        ArrayNode articlesArray = objectMapper.createArrayNode();
        
        for (Article article : articles) {
            ObjectNode articleNode = objectMapper.createObjectNode();
            articleNode.put("id", article.getId());
            articleNode.put("title", article.getTitle());
            articleNode.put("slug", article.getSlug());
            articleNode.put("link", article.getLink());
            articleNode.put("summary", article.getSummary());
            articleNode.put("date", article.getDate().toString());
            articleNode.put("dateDisplay", article.getDateDisplay());
            articleNode.put("author", article.getAuthor());
            
            // 添加标签数组
            ArrayNode tagsArray = objectMapper.createArrayNode();
            if (article.getTags() != null) {
                for (String tag : article.getTags()) {
                    tagsArray.add(tag);
                }
            }
            articleNode.set("tags", tagsArray);
            
            articlesArray.add(articleNode);
        }
        
        // 格式化JSON输出
        String articlesJson = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(articlesArray);
        
        // 读取现有的articles.js文件内容
        String existingContent = "";
        if (Files.exists(articlesJsPath)) {
            existingContent = Files.readString(articlesJsPath, StandardCharsets.UTF_8);
        }
        
        // 替换articles数组部分
        String newContent;
        Pattern pattern = Pattern.compile("const articles = \\[.*?\\];", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(existingContent);
        
        if (matcher.find()) {
            // 替换现有的articles数组
            newContent = existingContent.replaceFirst(
                "const articles = \\[.*?\\];", 
                "const articles = " + articlesJson + ";"
            );
        } else {
            // 如果文件不存在或格式不正确，创建完整的articles.js内容
            newContent = generateCompleteArticlesJs(articlesJson);
        }
        
        // 写入文件
        Files.writeString(articlesJsPath, newContent, StandardCharsets.UTF_8);
        System.out.println("已更新articles.js文件");
    }
    
    /**
     * 生成完整的articles.js文件内容
     */
    private String generateCompleteArticlesJs(String articlesJson) {
        return "// 文章数据\n" +
               "const articles = " + articlesJson + ";\n\n" +
               "// 文章管理器类\n" +
               "class ArticleManager {\n" +
               "    constructor() {\n" +
               "        this.articles = articles;\n" +
               "    }\n\n" +
               "    // 获取所有文章\n" +
               "    getAllArticles() {\n" +
               "        return this.articles;\n" +
               "    }\n\n" +
               "    // 根据ID获取文章\n" +
               "    getArticleById(id) {\n" +
               "        return this.articles.find(article => article.id === id);\n" +
               "    }\n\n" +
               "    // 根据slug获取文章\n" +
               "    getArticleBySlug(slug) {\n" +
               "        return this.articles.find(article => article.slug === slug);\n" +
               "    }\n\n" +
               "    // 获取文章统计信息\n" +
               "    getStats() {\n" +
               "        return {\n" +
               "            total: this.articles.length,\n" +
               "            tags: [...new Set(this.articles.flatMap(a => a.tags))]\n" +
               "        };\n" +
               "    }\n" +
               "}\n\n" +
               "// 页面加载完成后执行\n" +
               "document.addEventListener('DOMContentLoaded', function() {\n" +
               "    const manager = new ArticleManager();\n" +
               "    const articlesList = document.getElementById('articles-list');\n" +
               "    \n" +
               "    if (articlesList) {\n" +
               "        const articles = manager.getAllArticles();\n" +
               "        \n" +
               "        articles.forEach(article => {\n" +
               "            const articleElement = document.createElement('div');\n" +
               "            articleElement.className = 'article-item';\n" +
               "            \n" +
               "            const tagsHtml = article.tags.map(tag => \n" +
               "                `<span class=\"tag\">${tag}</span>`\n" +
               "            ).join(' ');\n" +
               "            \n" +
               "            articleElement.innerHTML = `\n" +
               "                <h3><a href=\"${article.link}\">${article.title}</a></h3>\n" +
               "                <div class=\"article-meta\">\n" +
               "                    <span class=\"date\">${article.dateDisplay}</span>\n" +
               "                    <span class=\"author\">作者: ${article.author}</span>\n" +
               "                </div>\n" +
               "                <div class=\"article-tags\">${tagsHtml}</div>\n" +
               "                <p class=\"summary\">${article.summary}</p>\n" +
               "            `;\n" +
               "            \n" +
               "            articlesList.appendChild(articleElement);\n" +
               "        });\n" +
               "    }\n" +
               "});\n";
    }
    
    /**
     * 保存HTML文件到html目录
     */
    public void saveHtmlFile(String fileName, String htmlContent) throws IOException {
        // 确保html目录存在
        if (!Files.exists(htmlDir)) {
            Files.createDirectories(htmlDir);
        }
        
        Path htmlFilePath = htmlDir.resolve(fileName);
        Files.writeString(htmlFilePath, htmlContent, StandardCharsets.UTF_8);
        System.out.println("已保存HTML文件: " + htmlFilePath);
    }
    
    /**
     * 检查HTML文件是否已存在
     */
    public boolean htmlFileExists(String fileName) {
        return Files.exists(htmlDir.resolve(fileName));
    }
    
    /**
     * 获取下一个可用的文章ID
     */
    public int getNextArticleId() throws IOException {
        List<Article> articles = loadExistingArticles();
        return articles.stream()
                .mapToInt(Article::getId)
                .max()
                .orElse(0) + 1;
    }
}
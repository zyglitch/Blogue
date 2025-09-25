package com.sept3rd.bloggenerator.parser;

import com.sept3rd.bloggenerator.model.Article;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown Parser
 * Markdown解析器，支持标准语法和扩展语法
 */
public class MarkdownParser {
    private static final Logger logger = LoggerFactory.getLogger(MarkdownParser.class);
    
    private final Parser parser;
    private final HtmlRenderer renderer;
    
    // 元数据正则表达式
    private static final Pattern FRONT_MATTER_PATTERN = Pattern.compile(
        "^---\\s*\\n(.*?)\\n---\\s*\\n", Pattern.DOTALL);
    private static final Pattern TITLE_PATTERN = Pattern.compile("^#\\s+(.+)$", Pattern.MULTILINE);
    private static final Pattern SUMMARY_PATTERN = Pattern.compile("^>\\s*(.+)$", Pattern.MULTILINE);

    public MarkdownParser() {
        // 配置Flexmark选项
        MutableDataSet options = new MutableDataSet();
        
        // 启用扩展
        options.set(Parser.EXTENSIONS, Arrays.asList(
            TablesExtension.create(),           // 表格支持
            StrikethroughExtension.create(),    // 删除线支持
            AutolinkExtension.create(),         // 自动链接
            TocExtension.create()               // 目录生成
        ));
        
        // HTML渲染选项
        options.set(HtmlRenderer.SOFT_BREAK, "<br />\\n");
        options.set(HtmlRenderer.HARD_BREAK, "<br />\\n");
        
        this.parser = Parser.builder(options).build();
        this.renderer = HtmlRenderer.builder(options).build();
    }

    /**
     * 解析Markdown文件为Article对象
     */
    public Article parseFile(Path markdownFile) throws IOException {
        logger.info("Parsing markdown file: {}", markdownFile);
        
        String content = Files.readString(markdownFile);
        return parseContent(content, markdownFile.getFileName().toString());
    }

    /**
     * 解析Markdown内容为Article对象
     */
    public Article parseContent(String markdownContent, String filename) {
        Article article = new Article();
        
        // 解析前置元数据
        Map<String, String> frontMatter = extractFrontMatter(markdownContent);
        String contentWithoutFrontMatter = removeFrontMatter(markdownContent);
        
        // 设置基本信息
        article.setTitle(extractTitle(frontMatter, contentWithoutFrontMatter, filename));
        article.setSummary(extractSummary(frontMatter, contentWithoutFrontMatter));
        article.setDate(extractDate(frontMatter));
        article.setDateDisplay(formatDateDisplay(article.getDate()));
        article.setTags(extractTags(frontMatter));
        article.setAuthor(frontMatter.getOrDefault("author", "Sept3rd"));
        
        // 生成slug和链接
        String slug = generateSlug(article.getTitle());
        article.setSlug(slug);
        
        // 转换Markdown为HTML
        Document document = parser.parse(contentWithoutFrontMatter);
        String htmlContent = renderer.render(document);
        article.setContent(htmlContent);
        
        logger.info("Successfully parsed article: {}", article.getTitle());
        return article;
    }

    /**
     * 提取前置元数据
     */
    private Map<String, String> extractFrontMatter(String content) {
        Map<String, String> frontMatter = new HashMap<>();
        
        Matcher matcher = FRONT_MATTER_PATTERN.matcher(content);
        if (matcher.find()) {
            String yamlContent = matcher.group(1);
            String[] lines = yamlContent.split("\\n");
            
            for (String line : lines) {
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim().replaceAll("^[\"']|[\"']$", "");
                        frontMatter.put(key, value);
                    }
                }
            }
        }
        
        return frontMatter;
    }

    /**
     * 移除前置元数据
     */
    private String removeFrontMatter(String content) {
        Matcher matcher = FRONT_MATTER_PATTERN.matcher(content);
        if (matcher.find()) {
            return content.substring(matcher.end());
        }
        return content;
    }

    /**
     * 提取标题
     */
    private String extractTitle(Map<String, String> frontMatter, String content, String filename) {
        // 优先使用前置元数据中的标题
        if (frontMatter.containsKey("title")) {
            return frontMatter.get("title");
        }
        
        // 从内容中提取第一个一级标题
        Matcher matcher = TITLE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // 使用文件名作为标题
        return filename.replaceAll("\\.[^.]+$", "").replace("-", " ").replace("_", " ");
    }

    /**
     * 提取摘要
     */
    private String extractSummary(Map<String, String> frontMatter, String content) {
        // 优先使用前置元数据中的摘要
        if (frontMatter.containsKey("summary") || frontMatter.containsKey("description")) {
            return frontMatter.getOrDefault("summary", frontMatter.get("description"));
        }
        
        // 查找引用块作为摘要
        Matcher matcher = SUMMARY_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // 提取第一段作为摘要
        String[] paragraphs = content.split("\\n\\s*\\n");
        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (!paragraph.isEmpty() && !paragraph.startsWith("#")) {
                // 移除Markdown格式，限制长度
                String cleanText = paragraph.replaceAll("[*_`#>-]", "").trim();
                if (cleanText.length() > 100) {
                    cleanText = cleanText.substring(0, 100) + "...";
                }
                return cleanText;
            }
        }
        
        return "暂无摘要";
    }

    /**
     * 提取日期
     */
    private LocalDate extractDate(Map<String, String> frontMatter) {
        String dateStr = frontMatter.get("date");
        if (StringUtils.isNotBlank(dateStr)) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                logger.warn("Failed to parse date: {}", dateStr, e);
            }
        }
        return LocalDate.now();
    }

    /**
     * 格式化日期显示
     */
    private String formatDateDisplay(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
    }

    /**
     * 提取标签
     */
    private List<String> extractTags(Map<String, String> frontMatter) {
        String tagsStr = frontMatter.get("tags");
        if (StringUtils.isNotBlank(tagsStr)) {
            // 支持逗号分隔或数组格式
            tagsStr = tagsStr.replaceAll("[\\[\\]]", "");
            return Arrays.asList(tagsStr.split("[,;]"))
                    .stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
        return new ArrayList<>();
    }

    /**
     * 生成URL slug
     */
    private String generateSlug(String title) {
        if (StringUtils.isBlank(title)) {
            return "article-" + System.currentTimeMillis();
        }
        
        // 简单的中文转拼音映射
        Map<String, String> pinyinMap = Map.of(
            "矿泉水", "kuangquanshui",
            "结冰", "jiebbing",
            "晃", "huang",
            "一下", "yixia",
            "就", "jiu",
            "的", "de",
            "和", "he",
            "与", "yu",
            "或", "huo"
        );
        
        String slug = title.toLowerCase();
        
        // 应用拼音映射
        for (Map.Entry<String, String> entry : pinyinMap.entrySet()) {
            slug = slug.replace(entry.getKey(), entry.getValue());
        }
        
        // 移除特殊字符，用连字符连接
        slug = slug.replaceAll("[^\\w\\s-]", "")
                  .replaceAll("\\s+", "-")
                  .replaceAll("-+", "-")
                  .replaceAll("^-|-$", "");
        
        return slug.isEmpty() ? "article-" + System.currentTimeMillis() : slug;
    }
}
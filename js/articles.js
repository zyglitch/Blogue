// 文章数据数组 - 支持动态管理和自动生成
const articles = [
    {
        id: 1,
        title: "矿泉水晃一下就结冰",
        slug: "kuangquanshui-jiebbing",
        link: "kuangquanshui-jiebbing.html",
        summary: "解释了矿泉水晃动后结冰的科学原理，涉及过冷现象和结晶过程。",
        date: "2023-10-15",
        dateDisplay: "2023年10月15日",
        tags: ["科学", "物理"],
        author: "Sept3rd"
    }
    // 新文章将通过桌面应用自动添加到此数组
];

// 文章管理工具类
class ArticleManager {
    constructor() {
        this.articles = articles;
    }

    // 获取所有文章
    getAllArticles() {
        return this.articles.sort((a, b) => new Date(b.date) - new Date(a.date));
    }

    // 根据ID获取文章
    getArticleById(id) {
        return this.articles.find(article => article.id === id);
    }

    // 根据slug获取文章
    getArticleBySlug(slug) {
        return this.articles.find(article => article.slug === slug);
    }

    // 添加新文章（供桌面应用调用）
    addArticle(articleData) {
        const newId = this.getNextId();
        const newArticle = {
            id: newId,
            title: articleData.title,
            slug: articleData.slug || this.generateSlug(articleData.title),
            link: `${articleData.slug || this.generateSlug(articleData.title)}.html`,
            summary: articleData.summary,
            date: articleData.date || new Date().toISOString().split('T')[0],
            dateDisplay: articleData.dateDisplay || this.formatDate(articleData.date),
            tags: articleData.tags || [],
            author: articleData.author || "Sept3rd"
        };
        this.articles.push(newArticle);
        return newArticle;
    }

    // 生成下一个ID
    getNextId() {
        return this.articles.length > 0 ? Math.max(...this.articles.map(a => a.id)) + 1 : 1;
    }

    // 生成URL slug
    generateSlug(title) {
        // 简单的中文转拼音映射（实际项目中可使用更完善的库）
        const pinyinMap = {
            '矿泉水': 'kuangquanshui',
            '结冰': 'jiebbing',
            '晃': 'huang',
            '一下': 'yixia',
            '就': 'jiu'
        };
        
        let slug = title.toLowerCase();
        for (const [chinese, pinyin] of Object.entries(pinyinMap)) {
            slug = slug.replace(new RegExp(chinese, 'g'), pinyin);
        }
        
        // 移除特殊字符，用连字符连接
        slug = slug.replace(/[^\w\s-]/g, '')
                  .replace(/\s+/g, '-')
                  .replace(/-+/g, '-')
                  .trim('-');
        
        return slug || `article-${Date.now()}`;
    }

    // 格式化日期显示
    formatDate(dateStr) {
        if (!dateStr) return new Date().toLocaleDateString('zh-CN');
        const date = new Date(dateStr);
        return date.toLocaleDateString('zh-CN', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    }

    // 获取文章统计信息
    getStats() {
        return {
            total: this.articles.length,
            tags: [...new Set(this.articles.flatMap(a => a.tags))],
            latestDate: this.articles.length > 0 ? 
                Math.max(...this.articles.map(a => new Date(a.date))) : null
        };
    }
}

// 创建全局文章管理器实例
const articleManager = new ArticleManager();

// 动态生成文章列表
document.addEventListener('DOMContentLoaded', function() {
    const articleList = document.getElementById('articleList');
    if (!articleList) return;
    
    const allArticles = articleManager.getAllArticles();
    
    allArticles.forEach(article => {
        const articleItem = document.createElement('div');
        articleItem.className = 'article-item';
        
        // 生成标签HTML
        const tagsHtml = article.tags && article.tags.length > 0 ? 
            `<div class="article-tags">${article.tags.map(tag => `<span class="tag">${tag}</span>`).join('')}</div>` : '';
        
        articleItem.innerHTML = `
            <h2><a href="${article.link}">${article.title}</a></h2>
            <p class="article-summary">${article.summary}</p>
            <p class="article-meta">
                <span class="date">发布于 ${article.dateDisplay}</span>
                <span class="author">作者: ${article.author}</span>
            </p>
            ${tagsHtml}
        `;
        articleList.appendChild(articleItem);
    });

    // 显示统计信息（如果有统计容器的话）
    const statsContainer = document.getElementById('articleStats');
    if (statsContainer) {
        const stats = articleManager.getStats();
        statsContainer.innerHTML = `
            <p>共 ${stats.total} 篇文章</p>
            <p>标签: ${stats.tags.join(', ')}</p>
        `;
    }
});
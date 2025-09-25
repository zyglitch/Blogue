// 文章数据
const articles = [ {
  "id" : 1,
  "title" : "博客部署指南",
  "slug" : "article-1758788227437",
  "link" : "article-1758788227437.html",
  "summary" : "本指南说明如何将Java桌面应用生成的博客文章部署到服务器上。",
  "date" : "2025-09-25",
  "dateDisplay" : "2025年09月25日",
  "author" : "Sept3rd",
  "tags" : [ ]
} ];

// 文章管理器类
class ArticleManager {
    constructor() {
        this.articles = articles;
    }

    // 获取所有文章
    getAllArticles() {
        return this.articles;
    }

    // 根据ID获取文章
    getArticleById(id) {
        return this.articles.find(article => article.id === id);
    }

    // 根据slug获取文章
    getArticleBySlug(slug) {
        return this.articles.find(article => article.slug === slug);
    }

    // 获取文章统计信息
    getStats() {
        return {
            total: this.articles.length,
            tags: [...new Set(this.articles.flatMap(a => a.tags))]
        };
    }
}

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    const manager = new ArticleManager();
    const articlesList = document.getElementById('articles-list');
    
    if (articlesList) {
        const articles = manager.getAllArticles();
        
        articles.forEach(article => {
            const articleElement = document.createElement('div');
            articleElement.className = 'article-item';
            
            const tagsHtml = article.tags.map(tag => 
                `<span class="tag">${tag}</span>`
            ).join(' ');
            
            articleElement.innerHTML = `
                <h3><a href="${article.link}">${article.title}</a></h3>
                <div class="article-meta">
                    <span class="date">${article.dateDisplay}</span>
                    <span class="author">作者: ${article.author}</span>
                </div>
                <div class="article-tags">${tagsHtml}</div>
                <p class="summary">${article.summary}</p>
            `;
            
            articlesList.appendChild(articleElement);
        });
    }
});

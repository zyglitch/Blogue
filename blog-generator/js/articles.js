// 文章数据
const articles = [ {
  "id" : 1,
  "title" : "博客改造",
  "slug" : "article-1758785688708",
  "link" : "article-1758785688708.html",
  "summary" : "你看对于这个功能你还也没有什么不明的，有的话你提出来，如果没有的话，你先给我列一下，实现这个功能，前后端的一个任务列表，没问题的话再介入开发。",
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
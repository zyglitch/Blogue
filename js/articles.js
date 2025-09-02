// 文章数据数组 - 实际项目中可以从外部文件或API获取
const articles = [
    {
        title: "矿泉水晃一下就结冰",
        link: "page1.html",
        summary: "解释了矿泉水晃动后结冰的科学原理，涉及过冷现象和结晶过程。",
        date: "2023年10月15日"
    }
    // 可以继续添加更多文章对象
    // 新增文章只需在此数组中添加新项
];

// 动态生成文章列表
document.addEventListener('DOMContentLoaded', function() {
    const articleList = document.getElementById('articleList');
    
    articles.forEach(article => {
        const articleItem = document.createElement('div');
        articleItem.className = 'article-item';
        
        articleItem.innerHTML = `
            <h2><a href="${article.link}">${article.title}</a></h2>
            <p class="article-summary">${article.summary}</p>
            <p class="article-meta">发布于 ${article.date}</p>
        `;
        articleList.appendChild(articleItem);
    });
});
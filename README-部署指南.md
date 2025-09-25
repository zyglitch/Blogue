# 博客生成器 & 部署指南

## 📋 概述

本项目包含一个完整的博客生成和部署解决方案：
- **Java桌面应用程序**：图形界面的博客文章生成器（已打包为Windows可执行文件）
- **自动化部署脚本**：将生成的文章同步到服务器的多种方案
- **完整的博客网站**：包含样式、脚本和模板的静态博客

## 🏗️ 完整文件结构

```
blog/
├── blog-generator/              # 博客生成器目录
│   ├── BlogGenerator/          # 可执行程序目录 ⭐
│   │   ├── BlogGenerator.exe   # 主程序（双击运行）
│   │   ├── app/               # 应用程序文件
│   │   └── runtime/           # 内置Java运行时
│   ├── BlogGenerator-Launcher.bat  # 启动脚本（推荐使用）⭐
│   ├── html/                  # 生成的HTML文章文件
│   ├── js/                    # 更新的articles.js索引
│   ├── templates/             # HTML模板
│   ├── src/                   # Java源代码
│   ├── target/                # 编译输出
│   ├── pom.xml               # Maven配置
│   └── 使用说明.txt           # 详细使用说明 ⭐
├── html/                      # 主博客HTML文件目录
├── css/                       # 样式文件
├── js/                        # JavaScript文件
├── img/                       # 图片资源
├── deploy-to-server.bat       # Windows批处理同步脚本
├── sync-articles.ps1          # PowerShell同步脚本（推荐）
└── README-部署指南.md         # 本文档
```

## 🚀 快速开始

### 第一步：启动博客生成器

**最简单的方式（推荐）：**
1. 进入 `blog-generator` 目录
2. 双击 `BlogGenerator-Launcher.bat` 启动程序
3. 或者进入 `BlogGenerator` 文件夹，双击 `BlogGenerator.exe`

**特点：**
- ✅ 无需安装Java - 程序已包含完整运行时
- ✅ 双击即用 - 像普通Windows软件一样
- ✅ 图形界面 - 友好的用户体验
- ✅ 便携性强 - 可复制到其他电脑使用

### 第二步：生成文章

1. 在程序界面中点击"选择文件"
2. 选择您的Markdown文件
3. 点击"生成HTML"按钮
4. 查看生成结果（保存在 `html/` 目录）

### 第三步：部署到服务器

选择以下任一方式进行部署：

**方式一：PowerShell脚本（推荐）**
```powershell
.\sync-articles.ps1
```

**方式二：批处理脚本**
```cmd
.\deploy-to-server.bat
```

## 🚀 部署方式详解

### 方式一：使用PowerShell脚本（推荐）

#### 基本使用
```powershell
# 在blog目录下运行
.\sync-articles.ps1
```

#### 高级使用
```powershell
# 预览模式（不实际复制文件）
.\sync-articles.ps1 -DryRun

# 跳过服务器上传
.\sync-articles.ps1 -SkipUpload

# 指定服务器信息
.\sync-articles.ps1 -ServerHost "192.168.1.100" -ServerUser "username"
```

### 方式二：使用批处理脚本

双击运行 `deploy-to-server.bat` 文件，按提示操作。

### 方式三：手动同步

1. **复制HTML文件**
   ```
   从：blog-generator\html\*.html
   到：blog\html\
   ```

2. **更新文章索引**
   ```
   从：blog-generator\js\articles.js
   到：blog\js\articles.js
   ```

3. **上传到服务器**
   - 使用FTP客户端
   - 使用SCP命令
   - 使用rsync同步

## 🔄 工作流程

### 日常使用流程

1. **创建新文章**
   - 编写Markdown文件
   - 使用Java桌面应用生成HTML

2. **本地同步**
   - 运行同步脚本
   - 检查生成的文件

3. **服务器部署**
   - 选择合适的上传方式
   - 执行文件同步

### 首次部署

1. **准备服务器环境**
   ```bash
   # 在服务器上创建博客目录
   mkdir -p /var/www/html/blog
   chmod 755 /var/www/html/blog
   ```

2. **上传完整博客**
   ```bash
   # 使用SCP上传整个博客目录
   scp -r d:\study_code\web\blog\* username@server:/var/www/html/blog/
   ```

3. **配置Web服务器**
   - 确保服务器支持静态文件访问
   - 配置正确的文档根目录

## 📡 服务器同步选项

### SCP上传（推荐）
```bash
# 上传整个博客目录
scp -r "d:\study_code\web\blog\*" username@server:/var/www/html/blog/

# 只上传HTML文件
scp "d:\study_code\web\blog\html\*.html" username@server:/var/www/html/blog/html/

# 只上传articles.js
scp "d:\study_code\web\blog\js\articles.js" username@server:/var/www/html/blog/js/
```

### rsync同步
```bash
# 完整同步（删除服务器上多余文件）
rsync -avz --delete "d:\study_code\web\blog/" username@server:/var/www/html/blog/

# 只同步特定目录
rsync -avz "d:\study_code\web\blog/html/" username@server:/var/www/html/blog/html/
```

### FTP上传
使用FTP客户端（如FileZilla）：
1. 连接到服务器
2. 导航到博客目录
3. 上传修改的文件

## 🔧 自动化部署

### 使用Git Hooks
```bash
# 在服务器上设置Git仓库
git init --bare /var/git/blog.git

# 创建post-receive钩子
#!/bin/bash
cd /var/www/html/blog
git --git-dir=/var/git/blog.git --work-tree=/var/www/html/blog checkout -f
```

### 使用CI/CD
- GitHub Actions
- GitLab CI
- Jenkins

## 🛠️ 故障排除

### 常见问题

1. **权限问题**
   ```bash
   # 修复文件权限
   chmod -R 644 /var/www/html/blog/*
   chmod -R 755 /var/www/html/blog/*/
   ```

2. **路径问题**
   - 检查脚本中的路径配置
   - 确保目录存在

3. **编码问题**
   - 确保文件使用UTF-8编码
   - 检查中文字符显示

### 日志检查
```bash
# 检查Web服务器日志
tail -f /var/log/nginx/access.log
tail -f /var/log/apache2/access.log
```

## 📝 注意事项

1. **备份重要文件**
   - 定期备份articles.js
   - 备份自定义样式文件

2. **版本控制**
   - 使用Git管理博客源码
   - 标记重要版本

3. **性能优化**
   - 压缩CSS和JS文件
   - 优化图片大小
   - 启用Gzip压缩

4. **安全考虑**
   - 使用SSH密钥认证
   - 限制文件上传权限
   - 定期更新服务器

## 🎯 项目特色

### 🖥️ Windows桌面应用程序
- **一键启动**：双击即可运行，无需命令行操作
- **内置Java运行时**：无需额外安装Java环境
- **图形用户界面**：直观的文件选择和生成操作
- **便携性强**：整个程序可复制到任何Windows电脑使用

### 🔄 自动化部署系统
- **多种同步方式**：PowerShell脚本、批处理脚本、手动同步
- **灵活的上传选项**：SCP、rsync、FTP、手动上传
- **预览模式**：可预览同步操作而不实际执行
- **详细日志**：完整的操作记录和错误提示

### 📝 完整的博客解决方案
- **响应式设计**：适配各种设备的现代化界面
- **文章索引管理**：自动维护文章列表和导航
- **模板系统**：可自定义的HTML模板
- **静态文件优化**：CSS、JS、图片资源完整管理

## 💡 使用场景

1. **个人博客写作**：使用Markdown编写，一键生成HTML
2. **团队文档管理**：多人协作的文档发布系统
3. **项目文档网站**：技术文档的在线展示平台
4. **学习笔记分享**：将学习笔记快速发布为网站

## 🛠️ 技术栈

- **后端**：Java 24 + Swing GUI
- **前端**：HTML5 + CSS3 + JavaScript
- **构建工具**：Maven + jpackage
- **部署工具**：PowerShell + Batch Scripts
- **版本控制**：Git友好的项目结构

## 🎯 最佳实践

1. **开发流程**
   - 本地测试 → 生成文章 → 同步文件 → 部署服务器

2. **文件管理**
   - 保持目录结构一致
   - 使用有意义的文件名
   - 定期清理无用文件

3. **监控部署**
   - 检查部署后的网站功能
   - 验证新文章是否正确显示
   - 测试文章链接和导航

## 📞 支持与反馈

如果您在使用过程中遇到问题或有改进建议，请：

1. **查看使用说明**：`blog-generator/使用说明.txt`
2. **检查故障排除**：参考本文档的故障排除部分
3. **提交问题反馈**：详细描述问题和操作步骤

## 🔄 更新日志

### v1.0.0 (当前版本)
- ✅ 完整的Java桌面应用程序
- ✅ Windows可执行文件打包
- ✅ 图形用户界面
- ✅ 自动化部署脚本
- ✅ 多种服务器同步方案
- ✅ 完整的文档和使用说明

---

🎉 **恭喜！您现在拥有了一个完整的博客生成和部署解决方案！**
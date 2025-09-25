@echo off
echo ========================================
echo         博客文件服务器同步工具
echo ========================================
echo.

REM 配置区域 - 请根据您的服务器信息修改
set SERVER_USER=your_username
set SERVER_HOST=your_server_ip
set SERVER_PATH=/var/www/html/blog
set LOCAL_BLOG_PATH=d:\study_code\web\blog

echo [1/4] 检查本地文件...
if not exist "%LOCAL_BLOG_PATH%" (
    echo 错误：本地博客目录不存在！
    pause
    exit /b 1
)

echo [2/4] 同步生成的HTML文件...
REM 将blog-generator生成的HTML文件复制到主博客目录
xcopy "%LOCAL_BLOG_PATH%\blog-generator\html\*.html" "%LOCAL_BLOG_PATH%\html\" /Y /Q
echo HTML文件已同步到主博客目录

echo [3/4] 更新文章索引...
REM 将生成器的articles.js复制到主博客目录
copy "%LOCAL_BLOG_PATH%\blog-generator\js\articles.js" "%LOCAL_BLOG_PATH%\js\articles.js" /Y
echo 文章索引已更新

echo [4/4] 上传到服务器...
echo.
echo 请选择上传方式：
echo 1. 使用SCP上传（需要配置SSH）
echo 2. 使用FTP上传（需要配置FTP客户端）
echo 3. 手动上传（显示需要上传的文件列表）
echo 4. 跳过上传
echo.
set /p choice=请输入选择 (1-4): 

if "%choice%"=="1" goto scp_upload
if "%choice%"=="2" goto ftp_upload
if "%choice%"=="3" goto manual_upload
if "%choice%"=="4" goto skip_upload

:scp_upload
echo 使用SCP上传文件...
echo 执行命令：scp -r "%LOCAL_BLOG_PATH%\*" %SERVER_USER%@%SERVER_HOST%:%SERVER_PATH%/
scp -r "%LOCAL_BLOG_PATH%\*" %SERVER_USER%@%SERVER_HOST%:%SERVER_PATH%/
goto end

:ftp_upload
echo 请使用您的FTP客户端上传以下目录：
echo 源目录：%LOCAL_BLOG_PATH%
echo 目标目录：%SERVER_PATH%
goto end

:manual_upload
echo 需要上传的文件和目录：
echo.
echo 📁 主要目录：
echo   - html/ (包含所有HTML文件)
echo   - css/ (样式文件)
echo   - js/ (包含更新的articles.js)
echo   - img/ (图片资源)
echo.
echo 📄 主要文件：
echo   - index.html
echo   - about.html
echo   - articles.html
echo.
echo 🔄 每次生成新文章后需要更新：
echo   - html/ 目录下的新HTML文件
echo   - js/articles.js 文件
goto end

:skip_upload
echo 跳过上传步骤
goto end

:end
echo.
echo ========================================
echo           同步完成！
echo ========================================
echo.
echo 本地文件已准备就绪，位置：
echo %LOCAL_BLOG_PATH%
echo.
echo 下次生成新文章后，只需再次运行此脚本即可！
echo.
pause
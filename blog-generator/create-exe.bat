@echo off
echo ========================================
echo         博客生成器 EXE 打包工具
echo ========================================
echo.

REM 检查Java环境
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：未找到Java环境！
    echo 请确保已安装Java 11或更高版本
    pause
    exit /b 1
)

echo [1/5] 检查项目文件...
if not exist "target\blog-generator-1.0.0-jar-with-dependencies.jar" (
    echo 错误：JAR文件不存在！
    echo 正在重新编译项目...
    call mvn clean package -q
    if %errorlevel% neq 0 (
        echo 编译失败！
        pause
        exit /b 1
    )
)
echo JAR文件检查通过

echo.
echo [2/5] 选择打包方式...
echo 1. 使用Launch4j（推荐，需要下载）
echo 2. 使用jpackage（Java 14+内置）
echo 3. 创建启动脚本（简单方式）
echo 4. 显示手动打包说明
echo.
set /p choice=请选择打包方式 (1-4): 

if "%choice%"=="1" goto launch4j_package
if "%choice%"=="2" goto jpackage_package
if "%choice%"=="3" goto script_package
if "%choice%"=="4" goto manual_instructions

:launch4j_package
echo.
echo [3/5] Launch4j 打包方式...
echo Launch4j是一个优秀的Java应用程序包装器，可以创建Windows原生exe文件。
echo.
echo 请按以下步骤操作：
echo 1. 下载Launch4j: https://launch4j.sourceforge.net/
echo 2. 安装Launch4j到默认位置
echo 3. 使用提供的配置文件 launch4j-config.xml
echo.
echo 配置文件已创建：launch4j-config.xml
echo.
echo Launch4j使用步骤：
echo 1. 打开Launch4j
echo 2. 点击 "Load config" 加载 launch4j-config.xml
echo 3. 点击齿轮图标开始构建
echo 4. 生成的BlogGenerator.exe将出现在当前目录
goto end

:jpackage_package
echo.
echo [3/5] jpackage 打包方式...
java -version 2>&1 | findstr "version" | findstr /R "1[4-9]\|[2-9][0-9]" >nul
if %errorlevel% neq 0 (
    echo 错误：jpackage需要Java 14或更高版本！
    echo 当前Java版本不支持jpackage
    goto script_package
)

echo 正在使用jpackage创建exe文件...
jpackage --input target ^
         --name "BlogGenerator" ^
         --main-jar blog-generator-1.0.0-jar-with-dependencies.jar ^
         --type exe ^
         --dest . ^
         --description "博客文章生成器" ^
         --vendor "Sept3rd" ^
         --app-version "1.0.0"

if %errorlevel% equ 0 (
    echo jpackage打包成功！
    echo 生成的安装程序位于当前目录
) else (
    echo jpackage打包失败，尝试其他方式...
    goto script_package
)
goto end

:script_package
echo.
echo [3/5] 创建启动脚本...
echo 正在创建Windows启动脚本...

echo @echo off > BlogGenerator.bat
echo cd /d "%%~dp0" >> BlogGenerator.bat
echo java -jar target\blog-generator-1.0.0-jar-with-dependencies.jar >> BlogGenerator.bat
echo pause >> BlogGenerator.bat

echo 启动脚本已创建：BlogGenerator.bat
echo 双击即可运行博客生成器
goto end

:manual_instructions
echo.
echo [3/5] 手动打包说明...
echo.
echo 方式一：Launch4j（推荐）
echo 1. 下载Launch4j: https://launch4j.sourceforge.net/
echo 2. 使用提供的配置文件 launch4j-config.xml
echo 3. 生成原生Windows exe文件
echo.
echo 方式二：jpackage（Java 14+）
echo 命令：jpackage --input target --name BlogGenerator --main-jar blog-generator-1.0.0-jar-with-dependencies.jar --type exe
echo.
echo 方式三：批处理脚本
echo 创建.bat文件包装jar运行命令
echo.
echo 方式四：第三方工具
echo - exe4j: https://www.ej-technologies.com/products/exe4j/overview.html
echo - Install4J: https://www.ej-technologies.com/products/install4j/overview.html
echo - JSmooth: http://jsmooth.sourceforge.net/
goto end

:end
echo.
echo [4/5] 创建图标文件...
echo 提示：如果需要自定义图标，请将.ico文件命名为blog-icon.ico放在当前目录

echo.
echo [5/5] 完成！
echo.
echo 文件说明：
echo - launch4j-config.xml: Launch4j配置文件
echo - BlogGenerator.bat: 启动脚本（已创建）
echo - target\blog-generator-1.0.0-jar-with-dependencies.jar: 原始JAR文件
echo.
echo 推荐使用Launch4j创建专业的exe文件！
echo.
pause
@echo off
chcp 65001 >nul
title Blog Generator
echo ========================================
echo         Blog Generator Launcher
echo ========================================
echo.

REM Check if BlogGenerator.exe exists
if exist "BlogGenerator\BlogGenerator.exe" (
    echo Starting Blog Generator...
    cd BlogGenerator
    start "" "BlogGenerator.exe"
    echo Blog Generator started successfully!
) else (
    echo Error: BlogGenerator.exe not found
    echo Please run create-exe.bat first to create the executable
    pause
)

REM Wait before closing
timeout /t 2 /nobreak >nul
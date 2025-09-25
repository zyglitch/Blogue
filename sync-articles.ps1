# Blog Articles Sync Script - PowerShell Version
# Sync Java generator created articles to main blog directory

param(
    [string]$ServerHost = "",
    [string]$ServerUser = "",
    [string]$ServerPath = "/var/www/html/blog",
    [switch]$SkipUpload = $false,
    [switch]$DryRun = $false
)

# Configuration
$LocalBlogPath = "d:\study_code\web\blog"
$GeneratorPath = "$LocalBlogPath\blog-generator"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "         Blog Articles Sync Tool v1.0" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check paths
if (-not (Test-Path $LocalBlogPath)) {
    Write-Host "Error: Local blog directory not found!" -ForegroundColor Red
    Write-Host "Path: $LocalBlogPath" -ForegroundColor Yellow
    exit 1
}

if (-not (Test-Path $GeneratorPath)) {
    Write-Host "Error: Blog generator directory not found!" -ForegroundColor Red
    Write-Host "Path: $GeneratorPath" -ForegroundColor Yellow
    exit 1
}

Write-Host "Path check passed" -ForegroundColor Green
Write-Host ""

# Step 1: Sync HTML files
Write-Host "[1/4] Syncing generated HTML files..." -ForegroundColor Yellow
$generatedHtmlPath = "$GeneratorPath\html"
$mainHtmlPath = "$LocalBlogPath\html"

if (Test-Path $generatedHtmlPath) {
    $htmlFiles = Get-ChildItem -Path $generatedHtmlPath -Filter "*.html"
    if ($htmlFiles.Count -gt 0) {
        if (-not $DryRun) {
            Copy-Item -Path "$generatedHtmlPath\*.html" -Destination $mainHtmlPath -Force
        }
        Write-Host "   Synced $($htmlFiles.Count) HTML files" -ForegroundColor Green
        foreach ($file in $htmlFiles) {
            Write-Host "      - $($file.Name)" -ForegroundColor Gray
        }
    } else {
        Write-Host "   No new HTML files found" -ForegroundColor Yellow
    }
} else {
    Write-Host "   Generator HTML directory not found" -ForegroundColor Red
}

# Step 2: Update articles.js
Write-Host ""
Write-Host "[2/4] Updating articles index..." -ForegroundColor Yellow
$generatedJsPath = "$GeneratorPath\js\articles.js"
$mainJsPath = "$LocalBlogPath\js\articles.js"

if (Test-Path $generatedJsPath) {
    if (-not $DryRun) {
        Copy-Item -Path $generatedJsPath -Destination $mainJsPath -Force
    }
    Write-Host "   articles.js updated" -ForegroundColor Green
    
    # Show article statistics
    $jsContent = Get-Content $generatedJsPath -Raw
    $articleCount = ([regex]::Matches($jsContent, '"id":\s*\d+')).Count
    Write-Host "   Total articles: $articleCount" -ForegroundColor Cyan
} else {
    Write-Host "   Generator articles.js file not found" -ForegroundColor Red
}

# Step 3: Check file integrity
Write-Host ""
Write-Host "[3/4] Checking file integrity..." -ForegroundColor Yellow
$requiredDirs = @("html", "css", "js", "img")
$missingDirs = @()

foreach ($dir in $requiredDirs) {
    $dirPath = "$LocalBlogPath\$dir"
    if (Test-Path $dirPath) {
        $fileCount = (Get-ChildItem -Path $dirPath -File).Count
        Write-Host "   $dir/ ($fileCount files)" -ForegroundColor Green
    } else {
        $missingDirs += $dir
        Write-Host "   $dir/ directory missing" -ForegroundColor Red
    }
}

if ($missingDirs.Count -eq 0) {
    Write-Host "   All required directories exist" -ForegroundColor Green
}

# Step 4: Server sync options
Write-Host ""
Write-Host "[4/4] Server sync..." -ForegroundColor Yellow

if ($SkipUpload) {
    Write-Host "   Skipping server upload" -ForegroundColor Yellow
} elseif ($DryRun) {
    Write-Host "   Preview mode: Skipping actual upload" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "Choose sync method:" -ForegroundColor Cyan
    Write-Host "1. Generate SCP command (Recommended)" -ForegroundColor White
    Write-Host "2. Generate rsync command" -ForegroundColor White
    Write-Host "3. Show manual upload checklist" -ForegroundColor White
    Write-Host "4. Skip upload" -ForegroundColor White
    Write-Host ""
    
    $choice = Read-Host "Enter choice (1-4)"
    
    switch ($choice) {
        "1" {
            Write-Host ""
            Write-Host "SCP Upload Command:" -ForegroundColor Green
            if ($ServerHost -and $ServerUser) {
                Write-Host "scp -r `"$LocalBlogPath\*`" $ServerUser@$ServerHost`:$ServerPath/" -ForegroundColor Yellow
            } else {
                Write-Host "scp -r `"$LocalBlogPath\*`" username@server_ip:$ServerPath/" -ForegroundColor Yellow
                Write-Host ""
                Write-Host "Tip: Next time use parameters:" -ForegroundColor Cyan
                Write-Host ".\sync-articles.ps1 -ServerHost 'your_server_ip' -ServerUser 'username'" -ForegroundColor Gray
            }
        }
        "2" {
            Write-Host ""
            Write-Host "rsync Sync Command:" -ForegroundColor Green
            if ($ServerHost -and $ServerUser) {
                Write-Host "rsync -avz --delete `"$LocalBlogPath/`" $ServerUser@$ServerHost`:$ServerPath/" -ForegroundColor Yellow
            } else {
                Write-Host "rsync -avz --delete `"$LocalBlogPath/`" username@server_ip:$ServerPath/" -ForegroundColor Yellow
            }
        }
        "3" {
            Write-Host ""
            Write-Host "Manual Upload Checklist:" -ForegroundColor Green
            Write-Host "Directories and files to upload:" -ForegroundColor White
            Write-Host "  html/ - All article HTML files" -ForegroundColor Cyan
            Write-Host "  css/ - Style files" -ForegroundColor Cyan
            Write-Host "  js/ - JavaScript files (including articles.js)" -ForegroundColor Cyan
            Write-Host "  img/ - Image resources" -ForegroundColor Cyan
            Write-Host "  index.html - Homepage" -ForegroundColor Cyan
            Write-Host "  about.html - About page" -ForegroundColor Cyan
            Write-Host "  articles.html - Articles list page" -ForegroundColor Cyan
        }
        "4" {
            Write-Host "   Skipping server upload" -ForegroundColor Yellow
        }
        default {
            Write-Host "   Invalid choice, skipping upload" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "           Sync Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Local blog path: $LocalBlogPath" -ForegroundColor White
Write-Host "Run this script again after generating new articles" -ForegroundColor White
Write-Host ""
Write-Host "Usage tips:" -ForegroundColor Cyan
Write-Host "   - Preview mode: .\sync-articles.ps1 -DryRun" -ForegroundColor Gray
Write-Host "   - Skip upload: .\sync-articles.ps1 -SkipUpload" -ForegroundColor Gray
Write-Host "   - With server info: .\sync-articles.ps1 -ServerHost 'IP' -ServerUser 'username'" -ForegroundColor Gray
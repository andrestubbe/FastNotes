@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo Initializing Git repository...
git init -b main
git add .
git commit -m "Initial commit: FastNotes 0.1.0 - Markdown & Obsidian Vault parser with bidirectional graph linking & block indexing"

echo Creating GitHub repository andrestubbe/FastNotes...
gh repo create andrestubbe/FastNotes --public --source=. --push

echo Creating tag 0.1.0...
git tag 0.1.0
git push origin 0.1.0

echo Creating GitHub release 0.1.0...
gh release create 0.1.0 --title "FastNotes 0.1.0" --notes "Initial release of FastNotes: Ultra-high-throughput Markdown and Obsidian Vault parser with bidirectional graph linking, block indexing, and PageRank topology."

echo Release 0.1.0 complete!

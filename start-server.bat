@echo off
echo 🚀 启动快递扫码工具本地服务器...
echo.
echo 选择启动方式:
echo 1. Python 3 (推荐)
echo 2. Python 2
echo 3. Node.js
echo 4. PHP
echo.
set /p choice=请输入选择 (1-4): 

if "%choice%"=="1" (
    echo 使用 Python 3 启动服务器...
    python -m http.server 8000
) else if "%choice%"=="2" (
    echo 使用 Python 2 启动服务器...
    python -m SimpleHTTPServer 8000
) else if "%choice%"=="3" (
    echo 使用 Node.js 启动服务器...
    npx http-server -p 8000
) else if "%choice%"=="4" (
    echo 使用 PHP 启动服务器...
    php -S localhost:8000
) else (
    echo 无效选择，使用默认 Python 3
    python -m http.server 8000
)

echo.
echo ✅ 服务器启动完成！
echo 📱 请在浏览器中访问: http://localhost:8000
echo 🛑 按 Ctrl+C 停止服务器
pause
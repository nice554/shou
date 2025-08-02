#!/bin/bash

echo "🚀 快递扫码助手 - 应用打包脚本"
echo "=================================="

# 创建assets目录和示例图标
create_assets() {
    echo "📁 创建assets目录和示例图标..."
    mkdir -p assets
    mkdir -p src-tauri/icons
    
    # 创建一个简单的SVG图标作为示例
    cat > assets/icon.svg << 'EOF'
<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 512 512">
  <rect width="512" height="512" fill="#2196F3" rx="64"/>
  <path fill="white" d="M128 128h256v64H128zm0 96h256v64H128zm0 96h192v64H128z"/>
  <circle cx="384" cy="320" r="32" fill="white"/>
</svg>
EOF
    
    echo "✅ Assets目录创建完成"
}

# 构建Electron应用
build_electron() {
    echo "🔧 构建Electron应用..."
    
    if ! command -v npm &> /dev/null; then
        echo "❌ 错误: 需要安装Node.js和npm"
        return 1
    fi
    
    echo "📦 安装依赖..."
    npm install
    
    echo "🏗️ 构建应用..."
    npm run build
    
    echo "✅ Electron应用构建完成！输出目录: ./dist/"
}

# 构建Tauri应用
build_tauri() {
    echo "🦀 构建Tauri应用..."
    
    if ! command -v cargo &> /dev/null; then
        echo "❌ 错误: 需要安装Rust"
        return 1
    fi
    
    if ! command -v tauri &> /dev/null; then
        echo "📦 安装Tauri CLI..."
        cargo install tauri-cli
    fi
    
    echo "🏗️ 构建应用..."
    cd src-tauri
    cargo tauri build
    cd ..
    
    echo "✅ Tauri应用构建完成！输出目录: ./src-tauri/target/release/bundle/"
}

# 设置PWA
setup_pwa() {
    echo "🌐 PWA已配置完成！"
    echo "📋 PWA使用说明:"
    echo "   1. 将项目部署到HTTPS服务器"
    echo "   2. 在支持PWA的浏览器中访问"
    echo "   3. 浏览器会提示'添加到主屏幕'或'安装应用'"
    echo "   4. 用户可以像原生应用一样使用"
}

# 显示使用说明
show_usage() {
    echo "📖 使用方法:"
    echo "   $0 [选项]"
    echo ""
    echo "选项:"
    echo "   electron    构建Electron桌面应用"
    echo "   tauri       构建Tauri桌面应用"
    echo "   pwa         显示PWA配置信息"
    echo "   all         构建所有类型的应用"
    echo "   assets      仅创建assets目录"
    echo ""
    echo "示例:"
    echo "   $0 electron     # 构建Electron应用"
    echo "   $0 all          # 构建所有应用"
}

# 主函数
main() {
    case "$1" in
        "electron")
            create_assets
            build_electron
            ;;
        "tauri")
            create_assets
            build_tauri
            ;;
        "pwa")
            setup_pwa
            ;;
        "all")
            create_assets
            echo "🚀 开始构建所有应用类型..."
            build_electron
            echo ""
            build_tauri
            echo ""
            setup_pwa
            ;;
        "assets")
            create_assets
            ;;
        *)
            show_usage
            ;;
    esac
}

# 运行主函数
main "$@"
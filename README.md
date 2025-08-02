# 快递扫码助手 - 应用打包版本

这个项目将你的HTML快递扫码助手打包成多种类型的应用，包括桌面应用和PWA。

## 🚀 支持的应用类型

### 1. Electron 桌面应用
- **特点**: 跨平台桌面应用（Windows、macOS、Linux）
- **大小**: ~150MB（包含Chromium内核）
- **性能**: 良好，接近原生体验
- **适用场景**: 需要桌面应用的用户

### 2. PWA (渐进式Web应用)
- **特点**: 可安装的Web应用，支持离线使用
- **大小**: 很小（仅缓存必要文件）
- **性能**: 优秀，原生Web性能
- **适用场景**: 移动设备和现代浏览器

### 3. Tauri 桌面应用
- **特点**: 使用Rust后端的轻量级桌面应用
- **大小**: ~10-20MB（使用系统WebView）
- **性能**: 优秀，资源占用少
- **适用场景**: 追求小体积和高性能的桌面应用

## 📦 快速开始

### 使用构建脚本（推荐）

```bash
# 查看所有选项
./build.sh

# 构建Electron应用
./build.sh electron

# 构建Tauri应用
./build.sh tauri

# 查看PWA配置信息
./build.sh pwa

# 构建所有类型的应用
./build.sh all
```

### 手动构建

#### Electron应用

1. 安装依赖：
```bash
npm install
```

2. 开发模式运行：
```bash
npm start
```

3. 构建应用：
```bash
npm run build
```

构建完成后，应用程序将在 `dist/` 目录中。

#### PWA应用

PWA已经配置完成，只需要：

1. 将项目部署到HTTPS服务器
2. 在支持PWA的浏览器中访问
3. 浏览器会提示"添加到主屏幕"或"安装应用"
4. 用户可以像原生应用一样使用

#### Tauri应用

1. 安装Rust（如果尚未安装）：
```bash
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
```

2. 安装Tauri CLI：
```bash
cargo install tauri-cli
```

3. 构建应用：
```bash
cd src-tauri
cargo tauri build
```

构建完成后，应用程序将在 `src-tauri/target/release/bundle/` 目录中。

## 📁 项目结构

```
.
├── index.html              # 主HTML文件
├── manifest.json           # PWA配置文件
├── sw.js                   # Service Worker
├── package.json            # Node.js依赖配置
├── main.js                 # Electron主进程
├── build.sh                # 构建脚本
├── src-tauri/              # Tauri配置目录
│   ├── tauri.conf.json     # Tauri配置
│   ├── Cargo.toml          # Rust依赖配置
│   ├── build.rs            # 构建脚本
│   └── src/
│       └── main.rs         # Rust主文件
└── assets/                 # 图标和资源文件
    └── (图标文件)
```

## 🛠️ 自定义配置

### 修改应用信息

编辑 `package.json` 中的应用信息：
```json
{
  "name": "your-app-name",
  "productName": "你的应用名称",
  "description": "应用描述",
  "author": "你的名字"
}
```

### 替换应用图标

1. 准备不同尺寸的图标文件
2. 将图标放入 `assets/` 目录
3. 更新配置文件中的图标路径

### PWA自定义

编辑 `manifest.json` 修改PWA配置：
- 应用名称和描述
- 主题颜色
- 图标路径
- 启动URL

## 🔧 开发环境要求

### Electron
- Node.js 16+
- npm 或 yarn

### PWA
- 任何现代Web服务器
- HTTPS协议（生产环境）

### Tauri
- Rust 1.60+
- 系统依赖（根据操作系统）

## 📱 平台支持

| 应用类型 | Windows | macOS | Linux | Android | iOS |
|---------|---------|-------|-------|---------|-----|
| Electron | ✅ | ✅ | ✅ | ❌ | ❌ |
| PWA | ✅ | ✅ | ✅ | ✅ | ✅ |
| Tauri | ✅ | ✅ | ✅ | ❌ | ❌ |

## 🚀 部署建议

### 桌面应用
- 使用代码签名证书签名应用
- 提供安装程序
- 考虑自动更新机制

### PWA
- 部署到支持HTTPS的服务器
- 配置适当的缓存策略
- 测试离线功能

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📞 支持

如果遇到问题，请：
1. 查看构建日志
2. 检查依赖是否正确安装
3. 确认系统环境符合要求
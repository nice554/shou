# 🚀 启用GitHub Pages - 详细步骤

## 📋 启用GitHub Pages

### 方法1：通过GitHub网站启用

1. **打开您的GitHub仓库**：
   ```
   https://github.com/nice554/shou
   ```

2. **点击Settings（设置）标签**：
   - 在仓库页面顶部找到"Settings"标签
   - 如果看不到，点击"..."菜单查找

3. **找到Pages设置**：
   - 在左侧边栏找到"Pages"选项
   - 在"Code and automation"部分下

4. **配置发布源**：
   - **Source**: 选择"Deploy from a branch"
   - **Branch**: 选择以下之一：
     - `main` 分支
     - `cursor/upload-scanned-codes-to-cloud-excel-a462` 分支
   - **Folder**: 选择 `/ (root)`

5. **点击Save保存**

6. **等待部署**：
   - GitHub会显示部署状态
   - 通常需要几分钟时间

### 方法2：直接访问链接测试

即使没有启用Pages，您也可以尝试访问：

**测试链接**：
- https://nice554.github.io/shou/test-upload.html
- https://nice554.github.io/shou/index.html

## 🔍 诊断步骤

### 检查1：网络连接
打开浏览器开发者工具：
1. 按F12打开开发者工具
2. 点击"Network"（网络）标签
3. 点击"测试上传到云端"
4. 查看请求状态

### 检查2：控制台错误
1. 在开发者工具中点击"Console"（控制台）
2. 查看是否有红色错误信息
3. 特别注意CORS相关错误

### 检查3：Google Apps Script状态
1. 访问您的Google Apps Script
2. 查看执行日志
3. 确认Web应用部署状态

## 🛠️ 常见问题解决

### 问题1：404错误
- **原因**：GitHub Pages未启用
- **解决**：按照上述步骤启用Pages

### 问题2：CORS错误
- **原因**：本地文件访问限制
- **解决**：使用GitHub Pages或HTTP服务器

### 问题3：Google Apps Script错误
- **原因**：脚本部署或权限问题
- **解决**：重新部署Web应用，检查权限设置

## 📱 立即测试

一旦GitHub Pages启用，您可以：

1. **访问主工具**：https://nice554.github.io/shou/
2. **测试上传**：https://nice554.github.io/shou/test-upload.html
3. **查看指南**：https://nice554.github.io/shou/quick-start.html

## ✅ 成功标志

当一切正常时，您应该看到：
- ✅ 测试页面显示"上传成功"
- ✅ Google Sheets中出现新数据
- ✅ 主工具显示"已自动上传到云端"

## 🆘 如果仍然失败

请提供以下信息：
1. 浏览器控制台的错误信息
2. 网络请求的状态码
3. Google Apps Script的执行日志
4. 具体的错误提示文字
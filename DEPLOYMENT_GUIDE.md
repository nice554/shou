# 📋 Google Sheets 云端部署指南

## 🎯 目标
将您的HTML快递扫码工具连接到Google Sheets，实现实时云端同步。

## 📝 第一步：创建Google Sheets

1. 打开 [Google Sheets](https://sheets.google.com)
2. 点击 **空白** 创建新的电子表格
3. 将电子表格重命名为：`快递扫码记录`
4. 在第一行设置表头：

| A1 | B1 | C1 | D1 |
|----|----|----|----|
| 时间 | 快递公司 | 处理后条码 | 原始条码 |

## ⚙️ 第二步：创建Google Apps Script

1. 在Google Sheets中，点击 **扩展程序** → **Apps Script**
2. 会打开新的标签页，显示Apps Script编辑器
3. 删除默认的 `myFunction()` 代码
4. 将 `google-apps-script.js` 文件中的所有代码复制粘贴进去
5. 点击 **保存** (Ctrl+S)
6. 给项目命名：`快递扫码工具API`

## 🔧 第三步：初始化和测试

1. **运行初始化函数**：
   - 在函数下拉菜单中选择 `initializeSheet`
   - 点击 **运行** 按钮
   - 首次运行会要求授权，点击 **审核权限**
   - 选择您的Google账户
   - 点击 **高级** → **转至快递扫码工具API(不安全)**
   - 点击 **允许**

2. **验证初始化结果**：
   - 返回Google Sheets标签页
   - 检查表头是否已自动设置（蓝色背景，白色文字）
   - 列宽是否已自动调整

3. **测试脚本功能**：
   - 返回Apps Script标签页
   - 在函数下拉菜单中选择 `testFunction`
   - 点击 **运行**
   - 查看执行日志，应该显示"测试结果"
   - 返回Google Sheets检查是否添加了测试数据

## 🚀 第四步：部署为Web应用

1. **创建部署**：
   - 在Apps Script编辑器中，点击右上角 **部署** → **新建部署**
   - 在"类型"旁边点击齿轮图标，选择 **Web应用**

2. **配置部署设置**：
   - **描述**：`快递扫码工具API v1.0`
   - **执行身份**：选择 **我**
   - **访问权限**：选择 **任何人**
   - 点击 **部署**

3. **获取Web应用URL**：
   - 部署完成后，会显示Web应用URL
   - **重要**：复制这个URL，格式类似：
     ```
     https://script.google.com/macros/s/AKfycby_YOUR_SCRIPT_ID_HERE/exec
     ```
   - 点击 **完成**

## 🔗 第五步：更新HTML文件

现在您需要将获得的Web应用URL替换到HTML文件中：

### 📁 需要更新的文件：

1. **index.html** (主扫码工具)
   - 第200行左右：实时上传功能
   - 第353行左右：批量上传功能

2. **test-upload.html** (测试页面)
   - 第34行左右：单条测试
   - 第68行左右：批量测试

### 🔍 查找和替换：

1. 打开文件，搜索：
   ```
   YOUR_GOOGLE_APPS_SCRIPT_URL_HERE
   ```

2. 替换为您的实际URL：
   ```
   https://script.google.com/macros/s/AKfycby_YOUR_SCRIPT_ID_HERE/exec
   ```

## 🧪 第六步：测试功能

1. **使用测试页面**：
   - 打开 `test-upload.html`
   - 点击 **测试上传到云端**
   - 应该显示绿色的成功消息

2. **检查Google Sheets**：
   - 返回您的Google Sheets
   - 应该看到新添加的测试数据

3. **测试主应用**：
   - 打开 `index.html`
   - 确保"启用实时云端上传"已勾选
   - 扫描或输入一个快递条码
   - 观察状态提示和Google Sheets更新

## 🛠️ 故障排除

### ❌ 常见问题

1. **403 Forbidden错误**：
   - 检查Apps Script部署权限是否设置为"任何人"
   - 重新部署Web应用

2. **CORS错误**：
   - 确保使用的是Web应用URL，不是脚本编辑器URL
   - URL必须以 `/exec` 结尾

3. **数据不显示**：
   - 检查Google Sheets权限
   - 查看Apps Script执行日志
   - 确认数据格式正确

4. **权限被拒绝**：
   - 重新授权Apps Script权限
   - 检查Google账户访问权限

### 🔍 调试步骤

1. **查看执行日志**：
   - Apps Script编辑器 → **执行** → **查看执行记录**

2. **测试单独组件**：
   - 先运行 `testFunction`
   - 再测试 `test-upload.html`
   - 最后测试完整应用

3. **检查网络请求**：
   - 浏览器开发者工具 → **网络**标签
   - 查看POST请求状态和响应

## ✅ 验证清单

- [ ] Google Sheets已创建并设置表头
- [ ] Apps Script代码已粘贴并保存
- [ ] `initializeSheet`函数已运行
- [ ] `testFunction`测试通过
- [ ] Web应用已部署并获得URL
- [ ] HTML文件中的URL已更新
- [ ] 测试页面功能正常
- [ ] 主应用实时上传正常工作

## 🎉 完成！

现在您的快递扫码工具已经完全连接到Google Sheets云端！每次扫码都会自动保存到您的电子表格中。

### 📊 Google Sheets功能

- **实时数据同步**：每次扫码立即保存
- **自动时间戳**：记录扫码时间
- **数据分类**：按快递公司分类
- **历史记录**：永久保存所有扫码记录
- **数据导出**：可导出为Excel、PDF等格式
- **共享协作**：可与团队成员共享表格

### 🔄 后续维护

- **数据清理**：定期运行 `cleanOldData` 函数
- **统计查看**：运行 `getStats` 函数查看统计
- **权限管理**：根据需要调整表格共享权限
- **备份数据**：定期下载表格备份

**您的云端快递扫码系统现在已经完全就绪！** 🚀
/**
 * 快递扫码工具配置文件
 * 请将YOUR_GOOGLE_APPS_SCRIPT_URL_HERE替换为您的实际Google Apps Script URL
 */

// Google Apps Script Web应用URL
// 格式：https://script.google.com/macros/s/YOUR_SCRIPT_ID/exec
const GOOGLE_APPS_SCRIPT_URL = "YOUR_GOOGLE_APPS_SCRIPT_URL_HERE";

// 配置说明
const CONFIG_INSTRUCTIONS = `
🔧 配置步骤：

1. 打开 Google Sheets (https://sheets.google.com)
2. 创建新的电子表格
3. 点击 扩展程序 → Apps Script
4. 粘贴 google-apps-script.js 中的代码
5. 保存并运行 initializeSheet 函数
6. 部署为Web应用：
   - 部署 → 新建部署
   - 类型：Web应用
   - 执行身份：我
   - 访问权限：任何人
   - 点击部署
7. 复制生成的Web应用URL
8. 将URL替换到下面的位置：
   - index.html 第200行和第353行
   - test-upload.html 第34行和第68行

示例URL格式：
https://script.google.com/macros/s/AKfycby.../exec
`;

console.log(CONFIG_INSTRUCTIONS);
/**
 * 快递扫码工具 - Google Apps Script后端 (支持CORS)
 * 接收来自HTML扫码工具的数据并写入Google Sheets
 */

function doPost(e) {
  try {
    // 获取当前电子表格
    const sheet = SpreadsheetApp.getActiveSheet();
    
    // 解析请求数据
    const requestData = JSON.parse(e.postData.contents);
    console.log('接收到数据:', requestData);
    
    // 确保数据是数组格式
    const records = Array.isArray(requestData) ? requestData : [requestData];
    
    // 处理每条记录
    records.forEach(record => {
      if (record.expressType && record.processedCode) {
        // 获取当前时间
        const timestamp = new Date().toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        });
        
        // 添加到表格末尾
        sheet.appendRow([
          timestamp,                    // A列：时间
          record.expressType,           // B列：快递公司
          record.processedCode,         // C列：处理后条码
          record.originalCode || ''     // D列：原始条码（可选）
        ]);
        
        console.log('已添加记录:', record);
      }
    });
    
    // 返回支持CORS的成功响应
    return ContentService
      .createTextOutput(JSON.stringify({
        status: 'success',
        message: `成功处理 ${records.length} 条记录`,
        timestamp: new Date().toISOString()
      }))
      .setMimeType(ContentService.MimeType.JSON)
      .setHeaders({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
        'Access-Control-Allow-Headers': 'Content-Type'
      });
      
  } catch (error) {
    console.error('处理请求时出错:', error);
    
    // 返回支持CORS的错误响应
    return ContentService
      .createTextOutput(JSON.stringify({
        status: 'error',
        message: error.toString(),
        timestamp: new Date().toISOString()
      }))
      .setMimeType(ContentService.MimeType.JSON)
      .setHeaders({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
        'Access-Control-Allow-Headers': 'Content-Type'
      });
  }
}

// 处理OPTIONS请求（CORS预检）
function doOptions(e) {
  return ContentService
    .createTextOutput('')
    .setHeaders({
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type'
    });
}

/**
 * 测试函数 - 用于验证脚本是否正常工作
 */
function testFunction() {
  const testData = {
    postData: {
      contents: JSON.stringify([
        {
          expressType: 'UPS',
          processedCode: '1Z999AA1234567890',
          originalCode: '1Z999AA1234567890'
        }
      ])
    }
  };
  
  const result = doPost(testData);
  console.log('测试结果:', result.getContent());
}

/**
 * 初始化表格 - 设置表头
 */
function initializeSheet() {
  const sheet = SpreadsheetApp.getActiveSheet();
  
  // 检查是否已有表头
  if (sheet.getRange(1, 1).getValue() === '') {
    // 设置表头
    sheet.getRange(1, 1, 1, 4).setValues([
      ['时间', '快递公司', '处理后条码', '原始条码']
    ]);
    
    // 设置表头样式
    const headerRange = sheet.getRange(1, 1, 1, 4);
    headerRange.setFontWeight('bold');
    headerRange.setBackground('#4285f4');
    headerRange.setFontColor('white');
    
    // 自动调整列宽
    sheet.autoResizeColumns(1, 4);
    
    console.log('表格初始化完成');
  }
}
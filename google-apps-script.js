/**
 * 快递扫码工具 - Google Apps Script后端
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
    
    // 返回成功响应
    return ContentService
      .createTextOutput(JSON.stringify({
        status: 'success',
        message: `成功处理 ${records.length} 条记录`,
        timestamp: new Date().toISOString()
      }))
      .setMimeType(ContentService.MimeType.JSON);
      
  } catch (error) {
    console.error('处理请求时出错:', error);
    
    // 返回错误响应
    return ContentService
      .createTextOutput(JSON.stringify({
        status: 'error',
        message: error.toString(),
        timestamp: new Date().toISOString()
      }))
      .setMimeType(ContentService.MimeType.JSON);
  }
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

/**
 * 清理旧数据 - 保留最近1000条记录
 */
function cleanOldData() {
  const sheet = SpreadsheetApp.getActiveSheet();
  const lastRow = sheet.getLastRow();
  
  if (lastRow > 1001) { // 保留表头+1000条数据
    const rowsToDelete = lastRow - 1001;
    sheet.deleteRows(2, rowsToDelete); // 从第2行开始删除
    console.log(`已清理 ${rowsToDelete} 条旧数据`);
  }
}

/**
 * 获取统计信息
 */
function getStats() {
  const sheet = SpreadsheetApp.getActiveSheet();
  const data = sheet.getDataRange().getValues();
  
  if (data.length <= 1) {
    return { total: 0, byType: {} };
  }
  
  const stats = { total: data.length - 1, byType: {} };
  
  // 统计各快递公司数量（跳过表头）
  for (let i = 1; i < data.length; i++) {
    const type = data[i][1]; // B列：快递公司
    stats.byType[type] = (stats.byType[type] || 0) + 1;
  }
  
  console.log('统计信息:', stats);
  return stats;
}
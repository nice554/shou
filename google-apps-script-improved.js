/**
 * 快递扫码工具 - Google Apps Script后端 (改进版)
 * 接收来自HTML扫码工具的数据并写入Google Sheets
 */

function doPost(e) {
  try {
    // 检查请求参数
    if (!e || !e.postData || !e.postData.contents) {
      throw new Error('无效的请求数据');
    }
    
    // 获取当前电子表格
    const sheet = SpreadsheetApp.getActiveSheet();
    
    // 解析请求数据
    const requestData = JSON.parse(e.postData.contents);
    console.log('接收到数据:', requestData);
    
    // 确保数据是数组格式
    const records = Array.isArray(requestData) ? requestData : [requestData];
    
    // 处理每条记录
    let processedCount = 0;
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
        
        processedCount++;
        console.log('已添加记录:', record);
      }
    });
    
    // 返回成功响应
    return ContentService
      .createTextOutput(JSON.stringify({
        status: 'success',
        message: `成功处理 ${processedCount} 条记录`,
        timestamp: new Date().toISOString(),
        totalReceived: records.length,
        processed: processedCount
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
 * 处理GET请求 - 用于健康检查
 */
function doGet(e) {
  return ContentService
    .createTextOutput(JSON.stringify({
      status: 'ok',
      message: '快递扫码工具API正常运行',
      timestamp: new Date().toISOString(),
      version: '1.0'
    }))
    .setMimeType(ContentService.MimeType.JSON);
}

/**
 * 测试函数 - 用于验证脚本是否正常工作
 */
function testFunction() {
  console.log('开始测试...');
  
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
  
  try {
    const result = doPost(testData);
    const content = result.getContent();
    console.log('测试结果:', content);
    
    const response = JSON.parse(content);
    if (response.status === 'success') {
      console.log('✅ 测试成功！数据已添加到电子表格');
    } else {
      console.log('❌ 测试失败:', response.message);
    }
    
    return response;
  } catch (error) {
    console.error('测试出错:', error);
    return { status: 'error', message: error.toString() };
  }
}

/**
 * 批量测试函数
 */
function testBatchFunction() {
  console.log('开始批量测试...');
  
  const testData = {
    postData: {
      contents: JSON.stringify([
        { expressType: 'UPS', processedCode: '1Z999AA1234567890' },
        { expressType: 'FedEx', processedCode: '123456789012' },
        { expressType: 'USPS', processedCode: '9400123456789012345678' }
      ])
    }
  };
  
  try {
    const result = doPost(testData);
    const content = result.getContent();
    console.log('批量测试结果:', content);
    return JSON.parse(content);
  } catch (error) {
    console.error('批量测试出错:', error);
    return { status: 'error', message: error.toString() };
  }
}

/**
 * 初始化表格 - 设置表头
 */
function initializeSheet() {
  console.log('开始初始化表格...');
  
  try {
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
      
      console.log('✅ 表格初始化完成');
    } else {
      console.log('ℹ️ 表格已经初始化过了');
    }
    
    return { status: 'success', message: '表格初始化完成' };
  } catch (error) {
    console.error('初始化表格出错:', error);
    return { status: 'error', message: error.toString() };
  }
}

/**
 * 获取表格状态
 */
function getSheetStatus() {
  try {
    const sheet = SpreadsheetApp.getActiveSheet();
    const lastRow = sheet.getLastRow();
    const lastColumn = sheet.getLastColumn();
    
    const status = {
      sheetName: sheet.getName(),
      totalRows: lastRow,
      totalColumns: lastColumn,
      dataRows: Math.max(0, lastRow - 1), // 排除表头
      hasHeader: sheet.getRange(1, 1).getValue() !== '',
      timestamp: new Date().toISOString()
    };
    
    console.log('表格状态:', status);
    return status;
  } catch (error) {
    console.error('获取表格状态出错:', error);
    return { status: 'error', message: error.toString() };
  }
}

/**
 * 清理测试数据
 */
function clearTestData() {
  try {
    const sheet = SpreadsheetApp.getActiveSheet();
    const data = sheet.getDataRange().getValues();
    
    let deletedCount = 0;
    // 从最后一行开始删除，避免行号变化
    for (let i = data.length - 1; i >= 1; i--) {
      if (data[i][1] === 'UPS' && data[i][2] === '1Z999AA1234567890') {
        sheet.deleteRow(i + 1);
        deletedCount++;
      }
    }
    
    console.log(`已清理 ${deletedCount} 条测试数据`);
    return { status: 'success', message: `已清理 ${deletedCount} 条测试数据` };
  } catch (error) {
    console.error('清理测试数据出错:', error);
    return { status: 'error', message: error.toString() };
  }
}
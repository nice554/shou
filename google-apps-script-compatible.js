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
    
    // 🔧 使用JSONP方式返回，绕过CORS限制
    const callback = e.parameter.callback || 'callback';
    const responseData = {
      status: 'success',
      message: `成功处理 ${records.length} 条记录`,
      timestamp: new Date().toISOString()
    };
    
    const jsonpResponse = callback + '(' + JSON.stringify(responseData) + ');';
    
    return ContentService
      .createTextOutput(jsonpResponse)
      .setMimeType(ContentService.MimeType.JAVASCRIPT);
      
  } catch (error) {
    console.error('处理请求时出错:', error);
    
    // 错误响应也使用JSONP
    const callback = e.parameter.callback || 'callback';
    const errorData = {
      status: 'error',
      message: error.toString(),
      timestamp: new Date().toISOString()
    };
    
    const jsonpResponse = callback + '(' + JSON.stringify(errorData) + ');';
    
    return ContentService
      .createTextOutput(jsonpResponse)
      .setMimeType(ContentService.MimeType.JAVASCRIPT);
  }
}

// 处理GET请求（健康检查和JSONP支持）
function doGet(e) {
  console.log('收到GET请求');
  
  try {
    const sheet = SpreadsheetApp.getActiveSheet();
    const lastRow = sheet.getLastRow();
    
    const callback = e.parameter.callback || 'callback';
    const responseData = {
      status: 'healthy',
      message: '服务正常运行',
      totalRecords: Math.max(0, lastRow - 1),
      timestamp: new Date().toISOString(),
      sheetName: sheet.getName()
    };
    
    const jsonpResponse = callback + '(' + JSON.stringify(responseData) + ');';
    
    return ContentService
      .createTextOutput(jsonpResponse)
      .setMimeType(ContentService.MimeType.JAVASCRIPT);
    
  } catch (error) {
    console.error('健康检查失败:', error);
    
    const callback = e.parameter.callback || 'callback';
    const errorData = {
      status: 'error',
      error: error.toString(),
      timestamp: new Date().toISOString()
    };
    
    const jsonpResponse = callback + '(' + JSON.stringify(errorData) + ');';
    
    return ContentService
      .createTextOutput(jsonpResponse)
      .setMimeType(ContentService.MimeType.JAVASCRIPT);
  }
}

/**
 * 测试函数 - 用于验证脚本是否正常工作
 */
function testFunction() {
  console.log('🧪 开始测试...');
  
  const testData = {
    postData: {
      contents: JSON.stringify([
        {
          expressType: 'UPS',
          processedCode: '1Z999AA1234567890',
          originalCode: '1Z999AA1234567890'
        }
      ])
    },
    parameter: {
      callback: 'testCallback'
    }
  };
  
  try {
    const result = doPost(testData);
    console.log('✅ POST测试结果:', result.getContent());
    
    // 测试健康检查
    const healthCheck = doGet({ parameter: { callback: 'healthCallback' } });
    console.log('🏥 GET测试结果:', healthCheck.getContent());
    
    console.log('✅ 所有测试完成！');
    return '测试成功！请查看执行日志。';
    
  } catch (error) {
    console.error('❌ 测试失败:', error);
    return '测试失败: ' + error.toString();
  }
}

/**
 * 简单测试 - 只测试数据写入，不涉及HTTP响应
 */
function simpleTest() {
  console.log('🧪 开始简单测试...');
  
  try {
    // 获取当前电子表格
    const sheet = SpreadsheetApp.getActiveSheet();
    
    // 测试数据
    const testRecord = {
      expressType: 'TEST简单测试',
      processedCode: 'SIMPLE_TEST_' + new Date().getTime(),
      originalCode: 'ORIGINAL_TEST'
    };
    
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
      timestamp,
      testRecord.expressType,
      testRecord.processedCode,
      testRecord.originalCode
    ]);
    
    console.log('✅ 简单测试成功！已添加测试记录:', testRecord);
    return '简单测试成功！请检查Google Sheets中的新记录。';
    
  } catch (error) {
    console.error('❌ 简单测试失败:', error);
    return '简单测试失败: ' + error.toString();
  }
}

/**
 * 初始化表格 - 设置表头
 */
function initializeSheet() {
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
      return '表格初始化成功！';
    } else {
      console.log('ℹ️ 表格已经初始化过了');
      return '表格已经初始化过了';
    }
  } catch (error) {
    console.error('❌ 初始化失败:', error);
    return '初始化失败: ' + error.toString();
  }
}

/**
 * 清理旧数据 - 保留最近1000条记录
 */
function cleanOldData() {
  try {
    const sheet = SpreadsheetApp.getActiveSheet();
    const lastRow = sheet.getLastRow();
    
    if (lastRow > 1001) { // 保留表头+1000条数据
      const rowsToDelete = lastRow - 1001;
      sheet.deleteRows(2, rowsToDelete); // 从第2行开始删除
      console.log(`🧹 已清理 ${rowsToDelete} 条旧数据`);
      return `清理完成！删除了 ${rowsToDelete} 条旧数据`;
    } else {
      console.log('ℹ️ 数据量未超限，无需清理');
      return '数据量正常，无需清理';
    }
  } catch (error) {
    console.error('❌ 清理失败:', error);
    return '清理失败: ' + error.toString();
  }
}

/**
 * 获取统计信息
 */
function getStats() {
  try {
    const sheet = SpreadsheetApp.getActiveSheet();
    const data = sheet.getDataRange().getValues();
    
    if (data.length <= 1) {
      const stats = { total: 0, byType: {} };
      console.log('📊 统计信息:', stats);
      return JSON.stringify(stats, null, 2);
    }
    
    const stats = { total: data.length - 1, byType: {} };
    
    // 统计各快递公司数量（跳过表头）
    for (let i = 1; i < data.length; i++) {
      const type = data[i][1]; // B列：快递公司
      stats.byType[type] = (stats.byType[type] || 0) + 1;
    }
    
    console.log('📊 统计信息:', stats);
    return JSON.stringify(stats, null, 2);
    
  } catch (error) {
    console.error('❌ 获取统计失败:', error);
    return '获取统计失败: ' + error.toString();
  }
}
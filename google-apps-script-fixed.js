/**
 * 🚀 快递扫码工具 - Google Apps Script 后端
 * 修复CORS问题的完整版本
 */

// ✅ 处理POST请求（接收扫码数据）
function doPost(e) {
  try {
    console.log('📨 收到POST请求');
    
    // 解析请求数据
    let data;
    if (e.postData && e.postData.contents) {
      data = JSON.parse(e.postData.contents);
      console.log('📦 解析数据:', data);
    } else {
      throw new Error('没有收到数据');
    }
    
    // 获取活动的工作表
    const sheet = SpreadsheetApp.getActiveSheet();
    
    // 检查是否是批量上传
    if (Array.isArray(data)) {
      console.log('📚 批量上传，共' + data.length + '条记录');
      
      // 批量添加数据
      data.forEach(record => {
        const timestamp = new Date().toLocaleString('zh-CN', {timeZone: 'Asia/Shanghai'});
        sheet.appendRow([
          record.expressType || '未知',
          record.processedCode || record.code || '无',
          timestamp
        ]);
      });
      
      return createCORSResponse({
        success: true,
        message: `成功上传${data.length}条记录`,
        timestamp: new Date().toISOString()
      });
      
    } else {
      // 单条记录上传
      console.log('📝 单条记录上传');
      
      const timestamp = new Date().toLocaleString('zh-CN', {timeZone: 'Asia/Shanghai'});
      sheet.appendRow([
        data.expressType || '未知',
        data.processedCode || data.code || '无',
        timestamp
      ]);
      
      return createCORSResponse({
        success: true,
        message: '上传成功',
        data: data,
        timestamp: new Date().toISOString()
      });
    }
    
  } catch (error) {
    console.error('❌ 处理请求时出错:', error);
    return createCORSResponse({
      success: false,
      error: error.toString(),
      timestamp: new Date().toISOString()
    });
  }
}

// ✅ 处理GET请求（健康检查）
function doGet(e) {
  console.log('🔍 收到GET请求 - 健康检查');
  
  try {
    const sheet = SpreadsheetApp.getActiveSheet();
    const lastRow = sheet.getLastRow();
    
    return createCORSResponse({
      success: true,
      message: '服务正常运行',
      status: 'healthy',
      totalRecords: Math.max(0, lastRow - 1), // 减去标题行
      timestamp: new Date().toISOString(),
      sheetName: sheet.getName()
    });
    
  } catch (error) {
    console.error('❌ 健康检查失败:', error);
    return createCORSResponse({
      success: false,
      error: error.toString(),
      timestamp: new Date().toISOString()
    });
  }
}

// ✅ 处理OPTIONS请求（CORS预检）
function doOptions(e) {
  console.log('🔧 收到OPTIONS请求 - CORS预检');
  
  return ContentService
    .createTextOutput('')
    .setMimeType(ContentService.MimeType.TEXT)
    .setHeaders({
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization',
      'Access-Control-Max-Age': '86400'
    });
}

// 🛠️ 创建带CORS头部的响应
function createCORSResponse(data) {
  return ContentService
    .createTextOutput(JSON.stringify(data))
    .setMimeType(ContentService.MimeType.JSON)
    .setHeaders({
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization',
      'Access-Control-Max-Age': '86400'
    });
}

// 🧪 测试函数
function testFunction() {
  console.log('🧪 开始测试...');
  
  try {
    // 测试单条记录
    const testData = {
      expressType: '测试快递',
      processedCode: 'TEST123456789'
    };
    
    const mockEvent = {
      postData: {
        contents: JSON.stringify(testData)
      }
    };
    
    console.log('📤 测试数据:', testData);
    const result = doPost(mockEvent);
    console.log('📥 测试结果:', result.getContent());
    
    // 测试健康检查
    const healthCheck = doGet({});
    console.log('🏥 健康检查:', healthCheck.getContent());
    
    console.log('✅ 测试完成！');
    return '测试成功！请查看执行日志。';
    
  } catch (error) {
    console.error('❌ 测试失败:', error);
    return '测试失败: ' + error.toString();
  }
}

// 📊 初始化工作表
function initializeSheet() {
  console.log('🏗️ 初始化工作表...');
  
  try {
    const sheet = SpreadsheetApp.getActiveSheet();
    
    // 设置标题行
    const headers = ['快递类型', '处理后单号', '扫描时间'];
    sheet.clear();
    sheet.getRange(1, 1, 1, headers.length).setValues([headers]);
    
    // 设置标题行格式
    const headerRange = sheet.getRange(1, 1, 1, headers.length);
    headerRange.setFontWeight('bold');
    headerRange.setBackground('#4285f4');
    headerRange.setFontColor('white');
    
    // 自动调整列宽
    sheet.autoResizeColumns(1, headers.length);
    
    console.log('✅ 工作表初始化完成');
    return '工作表初始化成功！';
    
  } catch (error) {
    console.error('❌ 初始化失败:', error);
    return '初始化失败: ' + error.toString();
  }
}

// 📈 获取统计信息
function getStats() {
  try {
    const sheet = SpreadsheetApp.getActiveSheet();
    const lastRow = sheet.getLastRow();
    const totalRecords = Math.max(0, lastRow - 1); // 减去标题行
    
    let stats = {
      totalRecords: totalRecords,
      sheetName: sheet.getName(),
      lastUpdate: new Date().toLocaleString('zh-CN', {timeZone: 'Asia/Shanghai'})
    };
    
    if (totalRecords > 0) {
      // 获取最近的记录
      const lastRecord = sheet.getRange(lastRow, 1, 1, 3).getValues()[0];
      stats.lastRecord = {
        expressType: lastRecord[0],
        code: lastRecord[1],
        timestamp: lastRecord[2]
      };
    }
    
    console.log('📊 统计信息:', stats);
    return JSON.stringify(stats, null, 2);
    
  } catch (error) {
    console.error('❌ 获取统计失败:', error);
    return '获取统计失败: ' + error.toString();
  }
}

// 🧹 清理测试数据
function clearTestData() {
  try {
    const sheet = SpreadsheetApp.getActiveSheet();
    const data = sheet.getDataRange().getValues();
    
    // 保留标题行，删除包含"测试"的行
    const filteredData = data.filter((row, index) => {
      if (index === 0) return true; // 保留标题行
      return !row.some(cell => 
        cell && cell.toString().includes('测试') || 
        cell && cell.toString().includes('TEST')
      );
    });
    
    sheet.clear();
    if (filteredData.length > 0) {
      sheet.getRange(1, 1, filteredData.length, filteredData[0].length)
           .setValues(filteredData);
    }
    
    console.log('🧹 清理完成，删除了' + (data.length - filteredData.length) + '条测试数据');
    return '清理完成！';
    
  } catch (error) {
    console.error('❌ 清理失败:', error);
    return '清理失败: ' + error.toString();
  }
}
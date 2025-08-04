/**
 * 自动备份脚本 - 添加到Google Apps Script中
 * 可设置定时触发器自动运行
 */

function autoBackup() {
  try {
    const sheet = SpreadsheetApp.getActiveSheet();
    const data = sheet.getDataRange().getValues();
    
    // 创建备份文件名（包含日期）
    const today = new Date();
    const dateStr = Utilities.formatDate(today, Session.getScriptTimeZone(), 'yyyy-MM-dd');
    const backupName = `快递扫码备份_${dateStr}`;
    
    // 创建新的电子表格作为备份
    const backupSheet = SpreadsheetApp.create(backupName);
    const backupRange = backupSheet.getActiveSheet().getRange(1, 1, data.length, data[0].length);
    backupRange.setValues(data);
    
    console.log(`✅ 备份完成: ${backupName}`);
    console.log(`📊 备份数据量: ${data.length} 行`);
    
    return {
      status: 'success',
      backupName: backupName,
      recordCount: data.length,
      backupId: backupSheet.getId()
    };
    
  } catch (error) {
    console.error('❌ 备份失败:', error);
    return {
      status: 'error',
      message: error.toString()
    };
  }
}

/**
 * 清理旧备份（保留最近30天）
 */
function cleanOldBackups() {
  try {
    const files = DriveApp.getFilesByName('快递扫码备份_');
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
    
    let deletedCount = 0;
    while (files.hasNext()) {
      const file = files.next();
      if (file.getDateCreated() < thirtyDaysAgo) {
        DriveApp.removeFile(file);
        deletedCount++;
      }
    }
    
    console.log(`🗑️ 清理了 ${deletedCount} 个旧备份文件`);
    return { deletedCount: deletedCount };
    
  } catch (error) {
    console.error('❌ 清理失败:', error);
    return { error: error.toString() };
  }
}

/**
 * 设置自动备份触发器（每周日凌晨2点）
 */
function setupAutoBackupTrigger() {
  // 删除现有触发器
  const triggers = ScriptApp.getProjectTriggers();
  triggers.forEach(trigger => {
    if (trigger.getHandlerFunction() === 'autoBackup') {
      ScriptApp.deleteTrigger(trigger);
    }
  });
  
  // 创建新触发器
  ScriptApp.newTrigger('autoBackup')
    .timeBased()
    .everyWeeks(1)
    .onWeekDay(ScriptApp.WeekDay.SUNDAY)
    .atHour(2)
    .create();
    
  console.log('✅ 自动备份触发器已设置（每周日凌晨2点）');
}
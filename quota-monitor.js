/**
 * 配额监控脚本 - 添加到Google Apps Script中
 */

function checkQuotaUsage() {
  try {
    // 获取今天的执行记录
    const today = new Date();
    const startOfDay = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    
    // 这个函数可以查看执行历史（需要在Apps Script控制台手动查看）
    console.log('📊 配额使用情况检查');
    console.log('⏰ 检查时间:', new Date().toLocaleString('zh-CN'));
    console.log('📅 今日日期:', startOfDay.toLocaleDateString('zh-CN'));
    
    // 简单的执行计数（每次调用时递增）
    const properties = PropertiesService.getScriptProperties();
    const todayKey = 'executions_' + startOfDay.toDateString();
    const currentCount = parseInt(properties.getProperty(todayKey) || '0');
    
    properties.setProperty(todayKey, (currentCount + 1).toString());
    
    console.log('🔢 今日执行次数:', currentCount + 1);
    console.log('⚡ 预估剩余配额: 约', Math.max(0, 360 - (currentCount + 1)), '次单次上传');
    
    // 清理7天前的计数
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
    const oldKey = 'executions_' + sevenDaysAgo.toDateString();
    properties.deleteProperty(oldKey);
    
    return {
      date: today.toLocaleDateString('zh-CN'),
      executionCount: currentCount + 1,
      estimatedRemaining: Math.max(0, 360 - (currentCount + 1))
    };
    
  } catch (error) {
    console.error('❌ 配额检查失败:', error);
    return { error: error.toString() };
  }
}

/**
 * 配额警告检查
 */
function checkQuotaWarning() {
  const usage = checkQuotaUsage();
  
  if (usage.executionCount > 300) {
    console.warn('⚠️ 配额警告: 今日已使用超过80%配额');
    console.warn('💡 建议: 减少今日使用或升级到付费账户');
  } else if (usage.executionCount > 250) {
    console.warn('⚠️ 配额提醒: 今日已使用超过70%配额');
  }
  
  return usage;
}

/**
 * 重置每日计数器（可设置每天凌晨自动运行）
 */
function resetDailyCounter() {
  const properties = PropertiesService.getScriptProperties();
  const today = new Date();
  const todayKey = 'executions_' + today.toDateString();
  
  properties.deleteProperty(todayKey);
  console.log('🔄 每日计数器已重置');
}
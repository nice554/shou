package com.expressscanner.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class TestActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // 创建一个简单的文本视图
            TextView textView = new TextView(this);
            textView.setText("快递扫码助手\n\n应用启动成功！\n\n这是一个测试版本，用于验证应用能否正常启动。");
            textView.setTextSize(18);
            textView.setPadding(50, 100, 50, 100);
            
            setContentView(textView);
            
            Toast.makeText(this, "启动成功！", Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "启动失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
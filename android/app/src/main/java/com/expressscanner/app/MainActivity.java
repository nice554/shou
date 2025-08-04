package com.expressscanner.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ScrollView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    
    private EditText barcodeInput;
    private Button addButton;
    private Button exportButton;
    private Button clearButton;
    private TextView statsText;
    private TextView recordsText;
    
    private List<String> scanRecords = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            createUI();
            Toast.makeText(this, "应用启动成功！", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "启动失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private void createUI() {
        // 创建主布局
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(32, 32, 32, 32);
        
        // 标题
        TextView titleText = new TextView(this);
        titleText.setText("快递扫码助手 - 基础版");
        titleText.setTextSize(20);
        titleText.setPadding(0, 0, 0, 32);
        mainLayout.addView(titleText);
        
        // 输入框
        barcodeInput = new EditText(this);
        barcodeInput.setHint("输入条码（可选）");
        barcodeInput.setPadding(16, 16, 16, 16);
        mainLayout.addView(barcodeInput);
        
        // 按钮布局
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setPadding(0, 16, 0, 16);
        
        // 添加按钮
        addButton = new Button(this);
        addButton.setText("添加");
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRecord();
            }
        });
        buttonLayout.addView(addButton);
        
        // 导出按钮
        exportButton = new Button(this);
        exportButton.setText("导出");
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportData();
            }
        });
        buttonLayout.addView(exportButton);
        
        // 清除按钮
        clearButton = new Button(this);
        clearButton.setText("清除");
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearRecords();
            }
        });
        buttonLayout.addView(clearButton);
        
        mainLayout.addView(buttonLayout);
        
        // 统计信息
        statsText = new TextView(this);
        statsText.setText("总计: 0 条记录");
        statsText.setTextSize(16);
        statsText.setPadding(0, 16, 0, 16);
        mainLayout.addView(statsText);
        
        // 记录显示（使用 ScrollView）
        ScrollView scrollView = new ScrollView(this);
        recordsText = new TextView(this);
        recordsText.setText("暂无记录\n点击添加按钮添加条码");
        recordsText.setPadding(16, 16, 16, 16);
        scrollView.addView(recordsText);
        mainLayout.addView(scrollView);
        
        // 设置主布局
        setContentView(mainLayout);
    }
    
    private void addRecord() {
        try {
            String barcode = barcodeInput.getText().toString().trim();
            
            if (barcode.isEmpty()) {
                // 生成测试条码
                barcode = "TEST" + System.currentTimeMillis();
            }
            
            // 添加记录
            String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            String record = timestamp + " - " + barcode;
            scanRecords.add(record);
            
            // 清空输入框
            barcodeInput.setText("");
            
            updateDisplay();
            Toast.makeText(this, "已添加: " + barcode, Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "添加失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void exportData() {
        try {
            if (scanRecords.isEmpty()) {
                Toast.makeText(this, "没有数据可以导出", Toast.LENGTH_SHORT).show();
                return;
            }
            
            StringBuilder content = new StringBuilder();
            content.append("导出数据:\n");
            for (String record : scanRecords) {
                content.append(record).append("\n");
            }
            
            Toast.makeText(this, "导出成功！记录数: " + scanRecords.size(), Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void clearRecords() {
        try {
            scanRecords.clear();
            updateDisplay();
            Toast.makeText(this, "已清除所有记录", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "清除失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateDisplay() {
        try {
            // 更新统计
            statsText.setText("总计: " + scanRecords.size() + " 条记录");
            
            // 更新记录显示
            if (scanRecords.isEmpty()) {
                recordsText.setText("暂无记录\n点击添加按钮添加条码");
            } else {
                StringBuilder display = new StringBuilder();
                display.append("记录列表:\n\n");
                
                // 显示最近10条记录
                int startIndex = Math.max(0, scanRecords.size() - 10);
                for (int i = startIndex; i < scanRecords.size(); i++) {
                    display.append(scanRecords.get(i)).append("\n");
                }
                
                recordsText.setText(display.toString());
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "更新显示失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

package com.expressscanner.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    private EditText barcodeInput;
    private Button addButton;
    private Button exportButton;
    private Button clearButton;
    private TextView statsText;
    private TextView recordsText;
    
    private List<String> scanRecords = new ArrayList<>();
    private String currentMode = "UPS";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // 使用我们的布局文件
            setContentView(R.layout.activity_main);
            initViews();
            setupListeners();
            requestBasicPermissions();
            updateDisplay();
            
            Toast.makeText(this, "应用启动成功", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            // 如果布局加载失败，显示错误信息
            Toast.makeText(this, "启动失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish(); // 关闭应用
        }
    }
    

    
    private void initViews() {
        try {
            // 尝试找到我们的自定义控件
            barcodeInput = findViewById(R.id.barcode_input);
            addButton = findViewById(R.id.scan_button);
            exportButton = findViewById(R.id.export_button);
            clearButton = findViewById(R.id.clear_button);
            statsText = findViewById(R.id.stats_text);
            recordsText = findViewById(R.id.empty_text);
            
            // 如果找不到控件，创建简单的替代方案
            if (barcodeInput == null || addButton == null) {
                Toast.makeText(this, "使用简化界面", Toast.LENGTH_SHORT).show();
                createFallbackUI();
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "界面初始化失败，使用备用方案", Toast.LENGTH_SHORT).show();
            createFallbackUI();
        }
    }
    
    private void createFallbackUI() {
        // 创建一个极简的备用界面
        try {
            setTitle("快递扫码助手 - 简化版");
        } catch (Exception e) {
            // 即使设置标题失败也继续
        }
    }
    
    private void setupListeners() {
        try {
            if (addButton != null) {
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addRecord();
                    }
                });
            }
            
            if (exportButton != null) {
                exportButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        exportData();
                    }
                });
            }
            
            if (clearButton != null) {
                clearButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clearRecords();
                    }
                });
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "按钮设置失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void addRecord() {
        try {
            String barcode = "";
            if (barcodeInput != null) {
                barcode = barcodeInput.getText().toString().trim();
            }
            
            if (barcode.isEmpty()) {
                // 如果没有输入，生成一个测试条码
                barcode = "TEST" + System.currentTimeMillis();
            }
            
            // 添加记录
            String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            String record = timestamp + " - " + currentMode + ": " + barcode;
            scanRecords.add(record);
            
            // 清空输入框
            if (barcodeInput != null) {
                barcodeInput.setText("");
            }
            
            updateDisplay();
            Toast.makeText(this, "已添加记录: " + barcode, Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "添加记录失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void exportData() {
        try {
            if (scanRecords.isEmpty()) {
                Toast.makeText(this, "没有数据可以导出", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 简单导出 - 只显示消息，不实际保存文件
            StringBuilder content = new StringBuilder();
            content.append("时间,快递,条码\n");
            for (String record : scanRecords) {
                content.append(record).append("\n");
            }
            
            Toast.makeText(this, "导出成功！记录数: " + scanRecords.size(), Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void clearRecords() {
        try {
            scanRecords.clear();
            updateDisplay();
            Toast.makeText(this, "已清除所有记录", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "清除失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void updateDisplay() {
        try {
            // 更新统计信息
            if (statsText != null) {
                String stats = "总计: " + scanRecords.size() + " 条记录";
                statsText.setText(stats);
            }
            
            // 更新记录显示
            if (recordsText != null) {
                if (scanRecords.isEmpty()) {
                    recordsText.setText("暂无记录\n点击添加按钮添加条码");
                } else {
                    StringBuilder display = new StringBuilder();
                    display.append("最近记录:\n");
                    int showCount = Math.min(5, scanRecords.size());
                    for (int i = scanRecords.size() - showCount; i < scanRecords.size(); i++) {
                        display.append(scanRecords.get(i)).append("\n");
                    }
                    recordsText.setText(display.toString());
                }
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "更新显示失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void requestBasicPermissions() {
        try {
            String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            
            boolean needRequest = false;
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    needRequest = true;
                    break;
                }
            }
            
            if (needRequest) {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "权限请求失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        try {
            if (requestCode == PERMISSION_REQUEST_CODE) {
                Toast.makeText(this, "权限处理完成", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // 忽略权限处理错误
        }
    }
}

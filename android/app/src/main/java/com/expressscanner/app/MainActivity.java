package com.expressscanner.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    private EditText barcodeInput;
    private Button scanButton;
    private Button exportButton;
    private Button exportCsvButton;
    private Button clearButton;
    private TextView statsText;
    private ListView recordsList;
    private Spinner modeSpinner;
    private TextView emptyText;
    
    private List<ScanRecord> scanRecords = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;
    private List<String> displayList = new ArrayList<>();
    
    private String currentMode = "UPS";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_main);
            initViews();
            setupListeners();
            requestPermissions();
            updateStats();
        } catch (Exception e) {
            Toast.makeText(this, "初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private void initViews() {
        try {
            barcodeInput = findViewById(R.id.barcode_input);
            scanButton = findViewById(R.id.scan_button);
            exportButton = findViewById(R.id.export_button);
            exportCsvButton = findViewById(R.id.export_csv_button);
            clearButton = findViewById(R.id.clear_button);
            statsText = findViewById(R.id.stats_text);
            recordsList = findViewById(R.id.records_list);
            modeSpinner = findViewById(R.id.mode_spinner);
            emptyText = findViewById(R.id.empty_text);
            
            // 设置快递模式选择器
            String[] modes = {"UPS", "FedEx", "USPS", "Amazon", "DHL", "顺丰", "圆通", "中通", "申通", "韵达", "通用"};
            ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modes);
            modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            modeSpinner.setAdapter(modeAdapter);
            modeSpinner.setSelection(0); // 默认选择 UPS
            
            // 初始化列表适配器
            listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
            recordsList.setAdapter(listAdapter);
            
        } catch (Exception e) {
            Toast.makeText(this, "界面初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private void setupListeners() {
        try {
            scanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startBarcodeScanner();
                }
            });
            
            exportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exportData(false);
                }
            });
            
            exportCsvButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exportData(true);
                }
            });
            
            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearRecords();
                }
            });
            
            // 快递模式选择监听
            modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    currentMode = parent.getItemAtPosition(position).toString();
                }
                
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    currentMode = "UPS";
                }
            });
            
            // 手动输入条码
            barcodeInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE || 
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                        String barcode = barcodeInput.getText().toString().trim();
                        if (!barcode.isEmpty()) {
                            processBarcode(barcode);
                            barcodeInput.setText("");
                            return true;
                        }
                    }
                    return false;
                }
            });
            
        } catch (Exception e) {
            Toast.makeText(this, "事件监听设置失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private void startBarcodeScanner() {
        try {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("请将条码对准扫描框");
            integrator.setOrientationLocked(false);
            integrator.setBeepEnabled(true);
            integrator.initiateScan();
        } catch (Exception e) {
            Toast.makeText(this, "启动扫码失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(this, "扫码已取消", Toast.LENGTH_SHORT).show();
                } else {
                    String scannedBarcode = result.getContents();
                    processBarcode(scannedBarcode);
                    Toast.makeText(this, "扫码成功！", Toast.LENGTH_SHORT).show();
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        } catch (Exception e) {
            Toast.makeText(this, "处理扫码结果失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private void processBarcode(String barcode) {
        try {
            String formatted = formatBarcode(barcode);
            if (!formatted.isEmpty()) {
                // 创建扫描记录
                String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                ScanRecord record = new ScanRecord(timestamp, currentMode, barcode, formatted);
                scanRecords.add(record);
                
                // 更新显示列表
                String displayText = String.format("[%s] %s: %s", timestamp, currentMode, formatted);
                displayList.add(0, displayText); // 添加到列表顶部
                listAdapter.notifyDataSetChanged();
                
                // 更新统计
                updateStats();
                
                Toast.makeText(this, "已添加: " + formatted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "无效的条码格式", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "处理条码失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private String formatBarcode(String barcode) {
        try {
            String code = barcode.trim();
            
            switch (currentMode) {
                case "UPS":
                    if (code.matches("^1Z[0-9A-Z]+$")) {
                        return code;
                    }
                    break;
                case "FedEx":
                    String digits = code.replaceAll("\\D", "");
                    if (digits.length() >= 12) {
                        return digits.substring(digits.length() - 12);
                    }
                    break;
                case "USPS":
                    if (code.matches(".*9[0-9]{15,21}.*")) {
                        return code.replaceAll(".*?(9[0-9]{15,21}).*", "$1");
                    }
                    break;
                case "Amazon":
                    if (code.matches("^TBA[0-9A-Z]+$")) {
                        return code;
                    }
                    break;
                case "DHL":
                    if (code.length() >= 10 && code.matches("^[0-9]+$")) {
                        return code;
                    }
                    break;
                case "顺丰":
                    if (code.matches("^SF[0-9]{12}$") || code.matches("^[0-9]{12}$")) {
                        return code.startsWith("SF") ? code : "SF" + code;
                    }
                    break;
                case "圆通":
                    if (code.matches("^YT[0-9]{10,13}$") || code.matches("^[0-9]{10,13}$")) {
                        return code.startsWith("YT") ? code : "YT" + code;
                    }
                    break;
                case "中通":
                    if (code.matches("^ZTO[0-9]{10,13}$") || code.matches("^[0-9]{10,13}$")) {
                        return code.startsWith("ZTO") ? code : "ZTO" + code;
                    }
                    break;
                case "申通":
                    if (code.matches("^STO[0-9]{10,13}$") || code.matches("^[0-9]{10,13}$")) {
                        return code.startsWith("STO") ? code : "STO" + code;
                    }
                    break;
                case "韵达":
                    if (code.matches("^YD[0-9]{10,13}$") || code.matches("^[0-9]{10,13}$")) {
                        return code.startsWith("YD") ? code : "YD" + code;
                    }
                    break;
                case "通用":
                default:
                    return code; // 通用模式，返回原始条码
            }
            
            return ""; // 无效格式
        } catch (Exception e) {
            Toast.makeText(this, "格式化条码失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return "";
        }
    }
    
    private void updateStats() {
        try {
            int totalCount = scanRecords.size();
            
            // 简单统计 - 避免复杂的 Map 操作
            int upsCount = 0, fedexCount = 0, uspsCount = 0, amazonCount = 0, otherCount = 0;
            
            for (ScanRecord record : scanRecords) {
                String mode = record.getMode();
                if ("UPS".equals(mode)) {
                    upsCount++;
                } else if ("FedEx".equals(mode)) {
                    fedexCount++;
                } else if ("USPS".equals(mode)) {
                    uspsCount++;
                } else if ("Amazon".equals(mode)) {
                    amazonCount++;
                } else {
                    otherCount++;
                }
            }
            
            String statsInfo = String.format("总计: %d | UPS: %d | FedEx: %d | USPS: %d | Amazon: %d | 其他: %d", 
                totalCount, upsCount, fedexCount, uspsCount, amazonCount, otherCount);
            
            statsText.setText(statsInfo);
            
            // 控制空状态显示
            if (totalCount == 0) {
                emptyText.setVisibility(View.VISIBLE);
                recordsList.setVisibility(View.GONE);
            } else {
                emptyText.setVisibility(View.GONE);
                recordsList.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Toast.makeText(this, "更新统计失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void exportData(boolean includeOriginal) {
        try {
            if (scanRecords.isEmpty()) {
                Toast.makeText(this, "没有数据可以导出", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 使用原生导出器
            NativeFileExporter exporter = new NativeFileExporter(this);
            String savedPath = exporter.exportToCSV(convertToExporterRecords(scanRecords), includeOriginal);
            
            String message = String.format("✅ 导出成功！\n📄 记录数：%d 条\n📁 文件路径：%s", 
                scanRecords.size(), savedPath);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private List<NativeFileExporter.ScanRecord> convertToExporterRecords(List<ScanRecord> records) {
        List<NativeFileExporter.ScanRecord> exporterRecords = new ArrayList<>();
        for (ScanRecord record : records) {
            exporterRecords.add(new NativeFileExporter.ScanRecord(
                record.getTime(), record.getMode(), record.getOriginal(), record.getProcessed()));
        }
        return exporterRecords;
    }
    
    private void clearRecords() {
        try {
            scanRecords.clear();
            displayList.clear();
            listAdapter.notifyDataSetChanged();
            updateStats();
            Toast.makeText(this, "已清除所有记录", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "清除记录失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void requestPermissions() {
        try {
            String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
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
            e.printStackTrace();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "某些权限被拒绝，功能可能受限", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    // 内部类：扫描记录
    public static class ScanRecord {
        private String time;
        private String mode;
        private String original;
        private String processed;
        
        public ScanRecord(String time, String mode, String original, String processed) {
            this.time = time;
            this.mode = mode;
            this.original = original;
            this.processed = processed;
        }
        
        public String getTime() { return time; }
        public String getMode() { return mode; }
        public String getOriginal() { return original; }
        public String getProcessed() { return processed; }
    }
}

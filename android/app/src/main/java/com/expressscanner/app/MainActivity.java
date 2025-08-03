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
        setContentView(R.layout.activity_main);
        
        initViews();
        setupListeners();
        requestPermissions();
        
        // 初始化列表适配器
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        recordsList.setAdapter(listAdapter);
        
        updateStats();
    }
    
    private void initViews() {
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
        
        // 设置默认选择 UPS
        modeSpinner.setSelection(0);
    }
    
    private void setupListeners() {
        scanButton.setOnClickListener(v -> startBarcodeScanner());
        
        exportButton.setOnClickListener(v -> exportData(false));
        exportCsvButton.setOnClickListener(v -> exportData(true));
        clearButton.setOnClickListener(v -> clearRecords());
        
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
        barcodeInput.setOnEditorActionListener((v, actionId, event) -> {
            String barcode = barcodeInput.getText().toString().trim();
            if (!barcode.isEmpty()) {
                processBarcode(barcode);
                barcodeInput.setText("");
                return true;
            }
            return false;
        });
    }
    
    private void startBarcodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("扫描条码");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "扫描取消", Toast.LENGTH_SHORT).show();
            } else {
                processBarcode(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    private void processBarcode(String barcode) {
        String processedBarcode = formatBarcode(barcode);
        
        if (processedBarcode.isEmpty()) {
            Toast.makeText(this, "无效的条码格式", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建扫描记录
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        ScanRecord record = new ScanRecord(currentTime, currentMode, barcode, processedBarcode);
        
        scanRecords.add(record);
        
        // 更新显示列表
        String displayText = String.format("[%s] %s: %s", currentTime, currentMode, processedBarcode);
        displayList.add(0, displayText); // 添加到顶部
        listAdapter.notifyDataSetChanged();
        
        updateStats();
        
        // 播放提示音
        Toast.makeText(this, "✅ 扫描成功: " + processedBarcode, Toast.LENGTH_SHORT).show();
    }
    
    private String formatBarcode(String barcode) {
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
    }
    
    private void updateStats() {
        int totalCount = scanRecords.size();
        
        // 统计各快递公司数量
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        
        for (ScanRecord record : scanRecords) {
            String mode = record.getMode();
            counts.put(mode, counts.getOrDefault(mode, 0) + 1);
        }
        
        StringBuilder statsBuilder = new StringBuilder();
        statsBuilder.append("总计: ").append(totalCount);
        
        // 显示前5个最多的快递公司
        counts.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(5)
            .forEach(entry -> {
                statsBuilder.append(" | ").append(entry.getKey()).append(": ").append(entry.getValue());
            });
        
        statsText.setText(statsBuilder.toString());
        
        // 控制空状态显示
        if (totalCount == 0) {
            emptyText.setVisibility(View.VISIBLE);
            recordsList.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recordsList.setVisibility(View.VISIBLE);
        }
    }
    
    private void exportData(boolean includeOriginal) {
        if (scanRecords.isEmpty()) {
            Toast.makeText(this, "没有数据可以导出", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            NativeFileExporter exporter = new NativeFileExporter(this);
            String savedPath = exporter.exportToCSV(convertToExporterRecords(), includeOriginal);
            
            String message = String.format("✅ 导出成功！\n文件已保存到: %s\n记录数: %d", 
                savedPath, scanRecords.size());
            
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private List<NativeFileExporter.ScanRecord> convertToExporterRecords() {
        List<NativeFileExporter.ScanRecord> exportRecords = new ArrayList<>();
        for (ScanRecord record : scanRecords) {
            exportRecords.add(new NativeFileExporter.ScanRecord(
                record.getTime(), record.getMode(), record.getOriginal(), record.getProcessed()
            ));
        }
        return exportRecords;
    }
    
    private void clearRecords() {
        scanRecords.clear();
        displayList.clear();
        listAdapter.notifyDataSetChanged();
        updateStats();
        Toast.makeText(this, "记录已清除", Toast.LENGTH_SHORT).show();
    }
    
    private void requestPermissions() {
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
            
            if (!allGranted) {
                Toast.makeText(this, "需要相机和存储权限才能正常使用", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    // 内部数据类
    private static class ScanRecord {
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

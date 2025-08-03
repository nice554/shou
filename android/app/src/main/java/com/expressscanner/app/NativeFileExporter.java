package com.expressscanner.app;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NativeFileExporter {
    
    private Context context;
    
    public NativeFileExporter(Context context) {
        this.context = context;
    }
    
    /**
     * 导出数据到 CSV 文件
     * @param records 记录列表
     * @param includeOriginal 是否包含原始数据
     * @return 保存的文件路径
     */
    public String exportToCSV(List<ScanRecord> records, boolean includeOriginal) throws IOException {
        if (records == null || records.isEmpty()) {
            throw new IOException("没有数据可以导出");
        }
        
        // 生成文件名
        String timestamp = new SimpleDateFormat("MMddyyyy", Locale.getDefault()).format(new Date());
        String filename = timestamp + "_TrackingNumber" + (includeOriginal ? "_Full" : "") + ".csv";
        
        // 生成 CSV 内容
        StringBuilder csvContent = new StringBuilder();
        if (includeOriginal) {
            csvContent.append("时间,快递,原始,处理后\n");
            for (ScanRecord record : records) {
                csvContent.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    record.getTime(), record.getMode(), record.getOriginal(), record.getProcessed()));
            }
        } else {
            csvContent.append("时间,快递,处理后\n");
            for (ScanRecord record : records) {
                csvContent.append(String.format("\"%s\",\"%s\",\"%s\"\n",
                    record.getTime(), record.getMode(), record.getProcessed()));
            }
        }
        
        // 保存文件
        return saveToFile(filename, csvContent.toString());
    }
    
    /**
     * 保存文件到存储
     */
    private String saveToFile(String filename, String content) throws IOException {
        // 方案1: 尝试保存到公共下载目录
        try {
            return saveToPublicDownloads(filename, content);
        } catch (Exception e) {
            // 方案2: 保存到应用外部文件目录
            return saveToAppExternalDir(filename, content);
        }
    }
    
    /**
     * 保存到公共下载目录
     */
    private String saveToPublicDownloads(String filename, String content) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 使用 MediaStore
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/快递扫码助手");
            
            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                    if (outputStream == null) {
                        throw new IOException("无法创建输出流");
                    }
                    outputStream.write(content.getBytes("UTF-8"));
                    outputStream.flush();
                }
                
                // 显示成功消息
                showToast("✅ 文件已保存到下载文件夹/快递扫码助手/");
                
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) 
                    + "/快递扫码助手/" + filename;
            } else {
                throw new IOException("无法创建文件");
            }
        } else {
            // Android 9 及以下直接文件操作
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File appDir = new File(downloadsDir, "快递扫码助手");
            
            if (!appDir.exists() && !appDir.mkdirs()) {
                throw new IOException("无法创建目录");
            }
            
            File file = new File(appDir, filename);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content.getBytes("UTF-8"));
                fos.flush();
            }
            
            // 通知媒体扫描器
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(file));
            context.sendBroadcast(intent);
            
            showToast("✅ 文件已保存到下载文件夹/快递扫码助手/");
            
            return file.getAbsolutePath();
        }
    }
    
    /**
     * 保存到应用外部文件目录
     */
    private String saveToAppExternalDir(String filename, String content) throws IOException {
        File externalFilesDir = context.getExternalFilesDir(null);
        if (externalFilesDir == null) {
            throw new IOException("外部存储不可用");
        }
        
        File appDir = new File(externalFilesDir, "快递扫码助手");
        if (!appDir.exists() && !appDir.mkdirs()) {
            throw new IOException("无法创建应用目录");
        }
        
        File file = new File(appDir, filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes("UTF-8"));
            fos.flush();
        }
        
        showToast("✅ 文件已保存到应用目录/快递扫码助手/");
        
        return file.getAbsolutePath();
    }
    
    /**
     * 显示提示消息
     */
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * 扫描记录数据类
     */
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
        
        // Getters
        public String getTime() { return time; }
        public String getMode() { return mode; }
        public String getOriginal() { return original; }
        public String getProcessed() { return processed; }
    }
}
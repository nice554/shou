package com.expressscanner.app;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.JSObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@CapacitorPlugin(name = "FileManager")
public class FileManagerPlugin extends Plugin {

    private static final int REQUEST_PERMISSION_CODE = 1001;
    private static final int REQUEST_MANAGE_STORAGE = 1002;

    @PluginMethod
    public void requestPermissions(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 需要 MANAGE_EXTERNAL_STORAGE 权限
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                getActivity().startActivityForResult(intent, REQUEST_MANAGE_STORAGE);
                
                JSObject result = new JSObject();
                result.put("success", true);
                result.put("message", "请在设置中允许应用管理所有文件");
                call.resolve(result);
            } else {
                JSObject result = new JSObject();
                result.put("success", true);
                result.put("message", "已有文件管理权限");
                call.resolve(result);
            }
        } else {
            // Android 10 及以下
            String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            };
            
            boolean allGranted = true;
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(getContext(), permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (!allGranted) {
                ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_PERMISSION_CODE);
            }
            
            JSObject result = new JSObject();
            result.put("success", allGranted);
            result.put("message", allGranted ? "权限已授予" : "正在请求权限");
            call.resolve(result);
        }
    }

    @PluginMethod
    public void saveFileToDownloads(PluginCall call) {
        String filename = call.getString("filename");
        String content = call.getString("content");
        
        if (filename == null || content == null) {
            call.reject("文件名和内容不能为空");
            return;
        }

        try {
            String savedPath = saveToDownloads(filename, content);
            
            JSObject result = new JSObject();
            result.put("success", true);
            result.put("path", savedPath);
            result.put("message", "文件已保存到下载文件夹");
            call.resolve(result);
            
        } catch (Exception e) {
            call.reject("保存文件失败: " + e.getMessage());
        }
    }

    @PluginMethod
    public void saveFileToDocuments(PluginCall call) {
        String filename = call.getString("filename");
        String content = call.getString("content");
        
        if (filename == null || content == null) {
            call.reject("文件名和内容不能为空");
            return;
        }

        try {
            String savedPath = saveToDocuments(filename, content);
            
            JSObject result = new JSObject();
            result.put("success", true);
            result.put("path", savedPath);
            result.put("message", "文件已保存到文档文件夹");
            call.resolve(result);
            
        } catch (Exception e) {
            call.reject("保存文件失败: " + e.getMessage());
        }
    }

    @PluginMethod
    public void openFileManager(PluginCall call) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            
            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                getActivity().startActivity(Intent.createChooser(intent, "选择文件管理器"));
                
                JSObject result = new JSObject();
                result.put("success", true);
                result.put("message", "文件管理器已打开");
                call.resolve(result);
            } else {
                call.reject("未找到文件管理器应用");
            }
        } catch (Exception e) {
            call.reject("打开文件管理器失败: " + e.getMessage());
        }
    }

    private String saveToDownloads(String filename, String content) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 使用 MediaStore
            ContentResolver resolver = getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                    outputStream.write(content.getBytes("UTF-8"));
                }
                return uri.toString();
            } else {
                throw new IOException("无法创建文件");
            }
        } else {
            // Android 9 及以下直接写入文件
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, filename);
            
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content.getBytes("UTF-8"));
            }
            
            return file.getAbsolutePath();
        }
    }

    private String saveToDocuments(String filename, String content) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 使用 MediaStore
            ContentResolver resolver = getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, filename);
            values.put(MediaStore.Files.FileColumns.MIME_TYPE, "text/csv");
            values.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

            Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), values);
            if (uri != null) {
                try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                    outputStream.write(content.getBytes("UTF-8"));
                }
                return uri.toString();
            } else {
                throw new IOException("无法创建文件");
            }
        } else {
            // Android 9 及以下直接写入文件
            File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            if (!documentsDir.exists()) {
                documentsDir.mkdirs();
            }
            File file = new File(documentsDir, filename);
            
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content.getBytes("UTF-8"));
            }
            
            return file.getAbsolutePath();
        }
    }
}
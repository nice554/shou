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
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ 需要 MANAGE_EXTERNAL_STORAGE 权限
                if (!Environment.isExternalStorageManager()) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                        getActivity().startActivity(intent);
                        
                        JSObject result = new JSObject();
                        result.put("success", true);
                        result.put("message", "已跳转到权限设置页面，请允许应用管理所有文件");
                        result.put("needsPermission", true);
                        call.resolve(result);
                    } catch (Exception e) {
                        JSObject result = new JSObject();
                        result.put("success", false);
                        result.put("message", "无法打开权限设置页面: " + e.getMessage());
                        call.resolve(result);
                    }
                } else {
                    JSObject result = new JSObject();
                    result.put("success", true);
                    result.put("message", "已有文件管理权限");
                    result.put("needsPermission", false);
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
                result.put("success", true);
                result.put("message", allGranted ? "权限已授予" : "正在请求权限");
                result.put("needsPermission", !allGranted);
                call.resolve(result);
            }
        } catch (Exception e) {
            JSObject result = new JSObject();
            result.put("success", false);
            result.put("message", "权限请求失败: " + e.getMessage());
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
            // 检查存储权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    call.reject("需要文件管理权限，请在设置中允许应用管理所有文件");
                    return;
                }
            } else {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                    call.reject("需要存储权限，请允许应用访问存储");
                    return;
                }
            }
            
            String savedPath = saveToDownloads(filename, content);
            
            JSObject result = new JSObject();
            result.put("success", true);
            result.put("path", savedPath);
            result.put("filename", filename);
            result.put("size", content.getBytes("UTF-8").length);
            result.put("message", "文件已保存到下载文件夹/快递扫码助手/");
            call.resolve(result);
            
        } catch (Exception e) {
            call.reject("保存文件失败: " + e.getMessage() + " (文件: " + filename + ")");
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
            // 检查存储权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    call.reject("需要文件管理权限，请在设置中允许应用管理所有文件");
                    return;
                }
            } else {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                    call.reject("需要存储权限，请允许应用访问存储");
                    return;
                }
            }
            
            String savedPath = saveToDocuments(filename, content);
            
            JSObject result = new JSObject();
            result.put("success", true);
            result.put("path", savedPath);
            result.put("filename", filename);
            result.put("size", content.getBytes("UTF-8").length);
            result.put("message", "文件已保存到文档文件夹/快递扫码助手/");
            call.resolve(result);
            
        } catch (Exception e) {
            call.reject("保存文件失败: " + e.getMessage() + " (文件: " + filename + ")");
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

    @PluginMethod
    public void openAppFolder(PluginCall call) {
        try {
            // 尝试打开下载文件夹中的应用文件夹
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File appDir = new File(downloadsDir, "快递扫码助手");
            
            if (!appDir.exists()) {
                // 如果下载文件夹中没有，尝试文档文件夹
                File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                appDir = new File(documentsDir, "快递扫码助手");
            }
            
            if (appDir.exists()) {
                // 尝试使用文件管理器打开特定文件夹
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.fromFile(appDir);
                intent.setDataAndType(uri, "resource/folder");
                
                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                    getActivity().startActivity(intent);
                    
                    JSObject result = new JSObject();
                    result.put("success", true);
                    result.put("path", appDir.getAbsolutePath());
                    result.put("message", "快递扫码助手文件夹已打开");
                    call.resolve(result);
                } else {
                    // 如果无法直接打开文件夹，回退到通用文件管理器
                    openFileManager(call);
                }
            } else {
                JSObject result = new JSObject();
                result.put("success", false);
                result.put("message", "快递扫码助手文件夹不存在，请先导出文件");
                call.resolve(result);
            }
        } catch (Exception e) {
            call.reject("打开应用文件夹失败: " + e.getMessage());
        }
    }

    private String saveToDownloads(String filename, String content) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 使用 MediaStore，在下载目录创建快递扫码助手文件夹
            ContentResolver resolver = getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/快递扫码助手");

            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                    if (outputStream == null) {
                        throw new IOException("无法打开输出流");
                    }
                    byte[] bytes = content.getBytes("UTF-8");
                    outputStream.write(bytes);
                    outputStream.flush();
                }
                
                // 验证文件是否真的被创建
                String expectedPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/快递扫码助手/" + filename;
                File verifyFile = new File(expectedPath);
                if (verifyFile.exists()) {
                    return expectedPath;
                } else {
                    return uri.toString(); // 返回 URI 作为路径
                }
            } else {
                throw new IOException("MediaStore 无法创建文件，可能是权限问题");
            }
        } else {
            // Android 9 及以下直接写入文件
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (downloadsDir == null || !downloadsDir.exists()) {
                throw new IOException("下载目录不存在或不可访问");
            }
            
            File appDir = new File(downloadsDir, "快递扫码助手");
            
            // 创建应用专用文件夹
            if (!appDir.exists()) {
                if (!appDir.mkdirs()) {
                    throw new IOException("无法创建应用文件夹: " + appDir.getAbsolutePath());
                }
            }
            
            File file = new File(appDir, filename);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] bytes = content.getBytes("UTF-8");
                fos.write(bytes);
                fos.flush();
                fos.getFD().sync(); // 强制同步到磁盘
            }
            
            // 验证文件是否真的被创建
            if (!file.exists() || file.length() == 0) {
                throw new IOException("文件创建失败或为空: " + file.getAbsolutePath());
            }
            
            return file.getAbsolutePath();
        }
    }

    private String saveToDocuments(String filename, String content) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 使用 MediaStore，在文档目录创建快递扫码助手文件夹
            ContentResolver resolver = getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, filename);
            values.put(MediaStore.Files.FileColumns.MIME_TYPE, "text/csv");
            values.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/快递扫码助手");

            Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), values);
            if (uri != null) {
                try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                    outputStream.write(content.getBytes("UTF-8"));
                }
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/快递扫码助手/" + filename;
            } else {
                throw new IOException("无法创建文件");
            }
        } else {
            // Android 9 及以下直接写入文件
            File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            if (!documentsDir.exists()) {
                documentsDir.mkdirs();
            }
            
            // 创建应用专用文件夹
            File appDir = new File(documentsDir, "快递扫码助手");
            if (!appDir.exists()) {
                if (!appDir.mkdirs()) {
                    throw new IOException("无法创建应用文件夹");
                }
            }
            
            File file = new File(appDir, filename);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content.getBytes("UTF-8"));
            }
            
            return file.getAbsolutePath();
        }
    }
}
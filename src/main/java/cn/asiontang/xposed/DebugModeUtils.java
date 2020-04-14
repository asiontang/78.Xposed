package cn.asiontang.xposed;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public class DebugModeUtils
{
    public static final File DEBUG_MODE_NEW_APK_FULL_PATH_CONFIG = new File(Environment.getExternalStorageDirectory(), "!Ye.DebugModeUtils");
    private static final String TAG = "Xposed.DebugModeUtils";

    public static String getNewApkFullPath()
    {
        //向/data/local/tmp目录写入DEBUG模式的APK所在新路径
        File config = DEBUG_MODE_NEW_APK_FULL_PATH_CONFIG;
        if (!config.exists())
            return null;
        String[] list = config.list();
        if (list == null || list.length == 0)
            return null;

        return list[0].replace("._.", "/");
    }

    public static boolean updateNewApkFullPath()
    {
        //向/data/local/tmp目录写入DEBUG模式的APK所在新路径
        File config = DEBUG_MODE_NEW_APK_FULL_PATH_CONFIG;
        if (config.exists())
        {
            File[] files = config.listFiles();
            if (files != null && files.length > 0)
                for (File file : files)
                    Log.d(TAG, String.format("updateDebugModeNewApkFullPath | file.delete=%s | file=%s", file.delete(), file));
        }
        else
        {
            Log.d(TAG, "updateDebugModeNewApkFullPath | dir.mkdirs=" + config.mkdirs());
        }

        //输入:dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/cn.asiontang.xposed.auto_js_pro-1/base.apk"],nativeLibraryDirectories
        String apkFilePath = String.valueOf(DebugModeUtils.class.getClassLoader());
        //结果:
        //    /data/app/cn.asiontang.xposed.auto_js_pro-1/base.apk
        //    /data/app/cn.asiontang.xposed.auto_js_pro-TbJBoU7ixhS0Fiv8Z6qf7g==/base.apk
        apkFilePath = apkFilePath.substring(apkFilePath.indexOf("/data/app"), apkFilePath.indexOf(".apk\"]") + 4);
        Log.d(TAG, "updateDebugModeNewApkFullPath | apkFilePath1=" + apkFilePath);

        //以完整路径名创建目录.
        File newFile = new File(config, apkFilePath.replace("/", "._."));
        newFile.mkdirs();

        boolean exists = newFile.exists();

        Log.d(TAG, String.format("updateDebugModeNewApkFullPath | apkFilePath2=%s, exists=%s", newFile, exists));
        Log.d(TAG, String.format("updateDebugModeNewApkFullPath | apkFilePath3=%s, getNewApkFullPathTest=%s", getNewApkFullPath(), apkFilePath.equals(getNewApkFullPath())));

        return exists;
    }
}

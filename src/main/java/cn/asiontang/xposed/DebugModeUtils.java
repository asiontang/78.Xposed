package cn.asiontang.xposed;

import android.os.Environment;
import android.util.Log;

import java.io.File;

import cn.asiontang.ProcessShell;

public class DebugModeUtils
{
    public static final File DEBUG_MODE_NEW_APK_FULL_PATH_CONFIG = new File(Environment.getExternalStorageDirectory(), "!Ye.DebugModeUtils");
    private static final String TAG = "Xposed.DebugModeUtils";

    /**
     * 当前APP最新的APK包路径
     */
    public static String getApkFileFullPath()
    {
        //输入:dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/cn.asiontang.xposed.auto_js_pro-1/base.apk"],nativeLibraryDirectories
        String apkFilePath = String.valueOf(DebugModeUtils.class.getClassLoader());
        //结果:
        //    /data/app/cn.asiontang.xposed.auto_js_pro-1/base.apk
        //    /data/app/cn.asiontang.xposed.auto_js_pro-TbJBoU7ixhS0Fiv8Z6qf7g==/base.apk
        apkFilePath = apkFilePath.substring(apkFilePath.indexOf("/data/app"), apkFilePath.indexOf(".apk\"]") + 4);
        Log.d(TAG, "getApkFileFullPath | apkFilePath1=" + apkFilePath);
        return apkFilePath;
    }

    public static String getNewApkFullPathAcrossProcess()
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

    /**
     * 一种使用ROOT权限直接写入Hook包路径下的方式来传递必要的信息.方便快速调试.
     */
    public static String getNewApkFullPathAcrossProcessByRoot(final String selfPackageName, final String hookPackageName)
    {
        LogEx.log(TAG, "getNewApkFullPathAcrossProcess2", "selfPackageName=", selfPackageName, "hookPackageName=", hookPackageName);

        String dir = String.format("/data/data/%s/cache/!Ye.DebugModeUtils", hookPackageName);
        File[] files = new File(dir).listFiles();
        if (files != null)
            for (File file : files)
            {
                final String safeApkFilePath = file.getName();
                /*3.反转义目录分隔符  pA7qafl89Esi_vchJuQBFA==._.base.apk*/
                String shortApkFilePath = safeApkFilePath.replace("._.", "/");

                String prefix = "/data/app/{SELF_PACKAGE_NAME}-".replace("{SELF_PACKAGE_NAME}", selfPackageName);
                final String apkFilePath = prefix + shortApkFilePath;

                LogEx.log(TAG, "apkFilePath=", apkFilePath);

                return apkFilePath;
            }
        return null;
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
        String apkFilePath = getApkFileFullPath();

        //以完整路径名创建目录.
        String safeApkFilePath = apkFilePath.replace("/", "._.");

        File newFile = new File(config, safeApkFilePath);
        newFile.mkdirs();

        boolean exists = newFile.exists();

        Log.d(TAG, String.format("updateDebugModeNewApkFullPath | apkFilePath2=%s, exists=%s", newFile, exists));
        Log.d(TAG, String.format("updateDebugModeNewApkFullPath | apkFilePath3=%s, getNewApkFullPathTest=%s", getNewApkFullPathAcrossProcess(), apkFilePath.equals(getNewApkFullPathAcrossProcess())));

        return exists;
    }

    /**
     * 一种使用ROOT权限直接写入Hook包路径下的方式来传递必要的信息.方便快速调试.
     *
     * @param selfPackageName 自己的包名
     * @param hookPackageName 需要Hook的包名
     */
    public static boolean updateNewApkFullPathByRoot(final String selfPackageName, final String hookPackageName)
    {
        LogEx.log(TAG, "updateNewApkFullPath2", "selfPackageName=", selfPackageName, "hookPackageName=", hookPackageName);

        /*1.获取完整路径
        /data/app/cn.asiontang.xposed.heart_study-pA7qafl89Esi_vchJuQBFA==/base.apk */
        String apkFilePath = getApkFileFullPath();
        /*2.缩短路径防止太长创建失败
        pA7qafl89Esi_vchJuQBFA==/base.apk*/
        String prefix = "/data/app/{SELF_PACKAGE_NAME}-".replace("{SELF_PACKAGE_NAME}", selfPackageName);
        String shortApkFilePath = apkFilePath.replace(prefix, "");
        /*3.转义目录分隔符
        pA7qafl89Esi_vchJuQBFA==._.base.apk*/
        String safeApkFilePath = shortApkFilePath.replace("/", "._.");
        /*4. 创建一个目录到目标APP的Cache文件夹里.
         */
        String dir = String.format("/data/data/%s/cache/!Ye.DebugModeUtils", hookPackageName);
        String path = String.format("%s/%s", dir, safeApkFilePath);

        {
            String cmd = String.format("ls -p %s", dir);
            ProcessShell.execCommand(cmd, true);
        }
        {
            /*
             * usage: rm [-fiRr] FILE...
             *
             * Remove each argument from the filesystem.
             *
             * -f      force: remove without confirmation, no error if it doesn't exist
             * -i      interactive: prompt for confirmation
             * -rR     recursive: remove directory contents
             */
            String cmd = String.format("rm -fRr %s", dir);
            ProcessShell.execCommand(cmd, true);
        }
        {
            /*mkdir [-vp] [-m mode] [dirname...]
             *Create one or more directories.
             *
             *-m	set permissions of directory to mode
             *-p	make parent directories as needed
             *-v	verbose
             */
            String cmd = String.format("mkdir -vp %s", path);
            ProcessShell.execCommand(cmd, true);
        }
        {
            /*
             * usage: ls --color[=auto] [-ACFHLRSZacdfhiklmnpqrstux1] [directory...]
             *
             * list files
             *
             * what to show:
             * -a  all files including .hidden    -b  escape nongraphic chars
             * -c  use ctime for timestamps       -d  directory, not contents
             * -i  inode number                   -p  put a '/' after dir names
             * -q  unprintable chars as '?'       -s  storage used (1024 byte units)
             * -u  use access time for timestamps -A  list all files but . and ..
             * -H  follow command line symlinks   -L  follow symlinks
             * -R  recursively list in subdirs    -F  append /dir *exe @sym |FIFO
             * -Z  security context
             *
             * output formats:
             * -1  list one file per line         -C  columns (sorted vertically)
             * -g  like -l but no owner           -h  human readable sizes
             * -l  long (show full details)       -m  comma separated
             * -n  like -l but numeric uid/gid    -o  like -l but no group
             * -x  columns (horizontal sort)      -ll long with nanoseconds (--full-time)
             *
             * sorting (default is alphabetical):
             * -f  unsorted    -r  reverse    -t  timestamp    -S  size
             * --color  device=yellow  symlink=turquoise/red  dir=blue  socket=purple
             *          files: exe=green  suid=red  suidfile=redback  stickydir=greenback
             *          =auto means detect if output is a tty.
             */
            String cmd = String.format("ls -l %s", path);
            ProcessShell.Result result = ProcessShell.execCommand(cmd, true);
            return result.toString().contains("total 0");
        }
    }
}

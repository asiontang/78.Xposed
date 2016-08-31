package cn.asiontang.xposed;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.PathClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public abstract class BaseXposedHookLoadPackage implements IXposedHookLoadPackage
{
    /**
     * 通过读取Xposed的配置文件得到实际项目中的入口HookLoadPackage类.
     */
    private String getClassNameFromAsset(final PathClassLoader pathClassLoader) throws IOException
    {
        InputStream is = pathClassLoader.getResourceAsStream("assets/xposed_init");
        BufferedReader moduleClassesReader = null;
        try
        {
            moduleClassesReader = new BufferedReader(new InputStreamReader(is));
            return moduleClassesReader.readLine().trim();
        }
        finally
        {
            if (moduleClassesReader != null)
                moduleClassesReader.close();
        }
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable
    {
        try
        {
            //在发布时，直接调用即可。
            boolean isHandled;
            if (BuildConfig.DEBUG)
                //在调试模式为了不频繁重启，使用反射的方式调用自身的指定函数。
                isHandled = invokeHandleLoadPackage4release(loadPackageParam);
            else
                isHandled = handleLoadPackage4release(loadPackageParam);

            //只有在确定要 Hook 的包才会输出 Hook 日志.减少垃圾日志量.
            if (isHandled)
            {
                //调试模式输出标识以示区分.
                if (BuildConfig.DEBUG)
                    LogEx.log("[DEBUG MODE]");

                LogEx.log(this.getClass().getPackage().getName(), "HookingPackageName:" + loadPackageParam.packageName);
            }
        }
        catch (final Exception e)
        {
            LogEx.log("handleLoadPackage Exception:");
            LogEx.log(e);
        }
    }

    public abstract boolean handleLoadPackage4release(final XC_LoadPackage.LoadPackageParam loadPackageParam);

    /**
     * 在调试模式为了不频繁重启，使用反射的方式调用指定函数。来动态触发 Hook.
     */
    private boolean invokeHandleLoadPackage4release(final XC_LoadPackage.LoadPackageParam loadPackageParam)//
            throws IOException, NoSuchMethodException, IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException
    {
        /*【方法3】*/
        /************************************************************
         *获取当前APK的包名,而不是当前框架的包名
         *以下方法都不行:返回值为 cn.asiontang.xposed
         * 1.BaseXposedHookLoadPackage.class.getPackage().getName();
         * 2.BuildConfig.APPLICATION_ID
         ************************************************************/
        final String packageName = this.getClass().getPackage().getName();
        String apkFilePath = String.format("/data/app/%s-%s.apk", packageName, 1);
        if (!new File(apkFilePath).exists())
        {
            apkFilePath = String.format("/data/app/%s-%s.apk", packageName, 2);
            if (!new File(apkFilePath).exists())
            {
                LogEx.log("Error:在/data/app找不到.apk包" + packageName);
                return false;
            }
        }
        final PathClassLoader pathClassLoader = new PathClassLoader(apkFilePath, ClassLoader.getSystemClassLoader());
        final Class<?> aClass = Class.forName(getClassNameFromAsset(pathClassLoader), true, pathClassLoader);
        final Method aClassMethod = aClass.getMethod("handleLoadPackage4release", XC_LoadPackage.LoadPackageParam.class);
        return (Boolean) aClassMethod.invoke(aClass.newInstance(), loadPackageParam);

        /*【方法2】*/
        //final String packageName = 项目的类.class.getPackage().getName();
        //String apkFilePath = String.format("/data/app/%s-%s.apk", packageName, 1);
        //if (!new File(apkFilePath).exists())
        //{
        //    apkFilePath = String.format("/data/app/%s-%s.apk", packageName, 2);
        //    if (!new File(apkFilePath).exists())
        //    {
        //        LogEx.log("Error:在/data/app找不到APK文件" + packageName);
        //        return;
        //    }
        //}
        //final PathClassLoader pathClassLoader = new PathClassLoader(apkFilePath, ClassLoader.getSystemClassLoader());
        //final Class<?> aClass = Class.forName(getClassNameFromAsset(pathClassLoader), true, pathClassLoader);
        //final Method aClassMethod = aClass.getMethod("handleLoadPackage4release", XC_LoadPackage.LoadPackageParam.class);
        //Object isHandled = aClassMethod.invoke(aClass.newInstance(), loadPackageParam);

        /*【方法1】：无法达到效果*/
        //final Class<MainActivity> pathClassLoader = MainActivity.class;
        //final Class<?> aClass = Class.forName(pathClassLoader.getPackage().getName() + "." + XposedHookLoadPackage.class.getSimpleName(), true, pathClassLoader.getClassLoader());
        //final Method aClassMethod = aClass.getMethod("handleLoadPackage4release", XC_LoadPackage.LoadPackageParam.class);
        //aClassMethod.invoke(aClass.newInstance(), mLoadPackageParam);
    }
}
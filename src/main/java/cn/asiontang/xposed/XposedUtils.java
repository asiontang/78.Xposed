package cn.asiontang.xposed;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XposedUtils
{
    /**
     * 将指定类的所有方法都显示出来看看
     */
    public static void dumpAllMethods(ClassLoader mClassLoader, String className)
    {
        LogEx.log("dumpAllMethods:Start | mClassLoader:" + mClassLoader + " | className:" + className);
        //获取指定类的全部方法:
        try
        {
            //通过以下正则表达式可获取所有get方法
            //public [^ ]+ [^ \(\)]+get\w+\(\)

            Class<?> aClass = mClassLoader.loadClass(className);
            LogEx.log("dumpAllMethods:Found Class:" + aClass);
            Map<String, List<Method>> allMethods = new HashMap<>();
            for (Method method : aClass.getMethods())
            {
                String key = method.getDeclaringClass().toString();
                List<Method> methodList = allMethods.get(key);
                if (methodList == null)
                    allMethods.put(key, methodList = new ArrayList<>());
                methodList.add(method);
            }
            LogEx.log("dumpAllMethods:Found DeclaringClass Count:" + allMethods.size());

            //以对齐的方式输出所有的方法.
            StringBuilder info = new StringBuilder();
            for (Map.Entry<String, List<Method>> entry : allMethods.entrySet())
            {
                info.append("Declaring Class:");
                info.append(entry.getKey());
                info.append("\n");
                for (Method method : entry.getValue())
                {
                    info.append(padRight(method.getReturnType().getSimpleName(), 12, ' '));

                    info.append(method.getName());
                    info.append("(");
                    for (Class<?> pClass : method.getParameterTypes())
                    {
                        info.append(pClass.getSimpleName());
                        info.append(",");
                    }
                    if (method.getParameterTypes().length > 0)
                        info.delete(info.length() - 1, info.length());
                    info.append(")");

                    info.append("\n");
                }
                LogEx.log(info.toString());

                //reset
                info.delete(0, info.length());
            }
        }
        catch (Exception e)
        {
            LogEx.log("dumpAllMethods:Exception:" + LogEx.getDebugString(e));
        }
        LogEx.log("dumpAllMethods:End | mClassLoader:" + mClassLoader + " | className:" + className);
    }

    /**
     * 填充右边多少个英文字符，中文会自动减半
     *
     * @param padLength 需要填充的英文字符长度
     */
    public static String padRight(String txt, int padLength, char padChar)
    {
        int count = txt.length();

        //最终填充到指定长度
        //如：7，填充为10
        StringBuilder tmp = new StringBuilder(txt);
        while (count < padLength)
        {
            tmp.append(padChar);
            count++;
        }
        return tmp.toString();
    }

    /**
     * <pre>
     * XposedBridge类的 hookAllMethods 函数只能 hook 当前类声明定义的方法,而那些没重载(写)过的只能Hook基类来实现.但是这样的代价可能就大了.
     * 所以稍微改了一下.把内部使用
     * getDeclaredMethods 的地方改为了 getMethods.这样就可以了.
     * </pre>
     */
    public static Set<XC_MethodHook.Unhook> realHookAllMethods(Class<?> hookClass, String methodName, XC_MethodHook callback)
    {
        HashSet<XC_MethodHook.Unhook> unhooks = new HashSet<>();
        for (Method method : hookClass.getMethods())
            if (method.getName().equals(methodName))
                unhooks.add(XposedBridge.hookMethod(method, callback));
        return unhooks;
    }

    /**
     * @see #realHookAllMethods(Class, String, XC_MethodHook)
     */
    public static Set<XC_MethodHook.Unhook> realHookAllMethods(String hookClass, ClassLoader classLoader, String methodName, XC_MethodHook callback)
    {
        return realHookAllMethods(XposedHelpers.findClass(hookClass, classLoader), methodName, callback);
    }
}
package cn.asiontang;

import android.content.Context;
import android.content.SharedPreferences;

import de.robv.android.xposed.XSharedPreferences;


/**
 * <h1>用于保存配置信息,供Hook配置设置页面和Hook的应用APP里都能正确按照配置来响应．</h1>
 * <h3>参考资料:</h3>
 * [android - Xposed how to create a module with a GUI preferences screen - Stack Overflow](https://stackoverflow.com/questions/32555529/xposed-how-to-create-a-module-with-a-gui-preferences-screen)
 * XSharedPreferences pref = new XSharedPreferences(Main.class.getPackage().getName(), "pref_mine"); // load the preferences using Xposed (necessary to be accessible from inside the hook, SharedPreferences() won't work)
 *
 * <h3>常用示例:</h3>
 * <pre><code>
 *    public class Config extends BaseConfig
 *     {
 *     private static Config mConfig;
 *
 *     public static Config get(final Context context)
 *     {
 *         if (mConfig == null)
 *             mConfig = new Config(context);
 *         return mConfig;
 *     }
 *
 *     public static Config get4hook()
 *     {
 *         if (mConfig == null)
 *             mConfig = new Config(null);
 *         return mConfig;
 *     }
 * </code></pre>
 */
public abstract class BaseConfig
{
    protected SharedPreferences mSharedPreferences;

    public BaseConfig(final Context context)
    {
        this(context, "Ye");
    }

    public BaseConfig(final Context context, final String name)
    {
        if (context == null)
            mSharedPreferences = new XSharedPreferences(getClass().getPackage().getName(), name);
        else
            // Setup a non-default and world readable shared preferences, so that 1- we know the name (necessary for XSharedPreferences()), 2- the preferences are accessible from inside the hook.
            mSharedPreferences = context.getSharedPreferences(name, Context.MODE_WORLD_READABLE);
    }
}

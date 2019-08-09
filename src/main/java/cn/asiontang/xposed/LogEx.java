package cn.asiontang.xposed;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

import de.robv.android.xposed.XposedBridge;

/**
 * 日志记录帮助类、工具类
 *
 * @author AsionTang
 * @since 2015年5月12日
 */
public final class LogEx
{
    /**
     * 接收任意一个对象，将其输出为字符串。
     * <ul>
     * <li>Throwable：　　会把内部堆栈输出来</li>
     * <li>HttpResponse： 会把网页返回的内容尽量输出来</li>
     * <li>InputStream：　会把流中的内容尽量输出来</li>
     * <li>其它的对象：　 默认调用toString</li>
     * </ul>
     */
    public static String any2String(final Object obj)
    {
        if (obj == null)
            return "[null]";
        if (obj instanceof Throwable)
        {
            //TO DO:真通过getCauseException拿到内部异常，可能太底层了，虽然是本，但是却治不了。所以为了治标，可能还是得记录全部异常。
            //final Throwable error = (Throwable) obj;
            //全记录貌似没啥用，还是记录最根本的吧，治标有时候根本没用。By：AsionTang AT：2014年8月18日 15:42:09
            final Throwable error = getCauseException((Throwable) obj);

            final StringWriter stackTrace = new StringWriter();
            error.printStackTrace(new PrintWriter(stackTrace));
            return stackTrace.toString();
        }
        else
            return obj.toString();
    }

    /**
     * <b>递归</b> 获取异常 里面 的真正的“内部异常CauseException”
     */
    public static Throwable getCauseException(final Throwable e)
    {
        if (e == null)
            return null;
        try
        {
            final Throwable cause = e.getCause();
            //正常情况下，总有个内部异常是没有Cause异常的，所以会在这里返回。
            if (cause == null)
                return e;

            //====================================================
            //后台捕获到一个堆栈溢出异常：
            //?UncaughtExceptionHandler?|java.lang.StackOverflowError
            //	at net.azyk.framework.exception.LogEx.getCauseException(LogEx.java:80)
            //     ···
            //	at net.azyk.framework.exception.LogEx.getCauseException(LogEx.java:84)
            // 猜测：因为某异常的内部异常居然是自己的情况下，就变成了死循环了。
            // 解决：所以这里尝试判断 getCause() 到的是否是它自己。
            // 【最终解决】：
            // 罪魁祸首getCauseException(e);！！正确递归应该是getCauseException(cause);！！！！！
            //====================================================
            return getCauseException(cause);
        }
        catch (final StackOverflowError ignore)
        {
            Log.e("Bug来了！", "getCause(); 居然再次出现死循环");
            return e;
        }
    }

    /**
     * 自动将objs 拼接成如下字符串：<br/>
     * obj1|obj2|obj3|
     */
    public static String getDebugString(final Object... objs)
    {
        if (objs == null)
            return "[null]";
        final StringBuilder msg = new StringBuilder();
        boolean firstTime = true;
        for (final Object item : objs)
        {
            if (firstTime)
                firstTime = false;
            else if (msg.lastIndexOf("=") != msg.length() - 1)//当上个输出调试信息项目末尾以=号结尾时,不再额外添加分割线.方便调试时候输出键值对key= | value 优化后| key=value |
                msg.append(" | ");//从中文符号｜替换为英文|，方便在文本编辑器里快速双击选中单词，中文分隔符的情况下，英文文本编辑器如（sublimetext）无法正常识别分隔符。

            //存在一种可能，item本身就是一个数组！！
            if (item instanceof Object[])
                msg.append(getDebugString((Object[]) item));
            else
                msg.append(any2String(item));
        }
        return msg.toString();
    }

    public static void log(final String msg)
    {
        try
        {
            XposedBridge.log(msg);
        }
        catch (NoClassDefFoundError ignore)
        {
            Log.e("Xposed", "XposedBridge.log NoDefError | " + msg);
        }
    }

    public static void log(final Throwable e)
    {
        log(any2String(e));
    }

    public static void log(final Object... e)
    {
        log(getDebugString(e));
    }
}

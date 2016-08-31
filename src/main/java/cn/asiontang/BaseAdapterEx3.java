package cn.asiontang;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.List;

/**
 * <p>
 * 重载了BaseAdapterEx：
 * <ul>
 * <li>默认实现了一个performFiltering，始终返回originalItems；</li>
 * <li>子类只需实现{@link #convertView(ViewHolder, Object)}即可。
 * <p>
 * 使用了ViewHolder技术来代替每次findViewById来优化一定的性能
 * </p>
 * </li>
 * </ul>
 * </p>
 * <h5>性能测试代码：</h5>
 *
 * <pre>
 * public void getView(final int viewId)
 * {
 *     View view = this.views.get(viewId);
 *     if (view == null)
 *     {
 *         view = this.convertView.findViewById(viewId);
 *         this.views.put(viewId, view);
 *     }
 *     double all = 0;
 *     for (int j = 0; j &lt; 100; j++)
 *     {
 *         long test1 = SystemClock.elapsedRealtime();
 *         for (int i = 0; i &lt; 10 * 10000; i++)
 *             this.convertView.findViewById(viewId);
 *         long test2 = SystemClock.elapsedRealtime();
 *         LogEx.e(&quot;findViewById的耗时&quot;, test2 - test1);
 *         double r1 = test2 - test1;
 *
 *         test1 = SystemClock.elapsedRealtime();
 *         for (int i = 0; i &lt; 10 * 10000; i++)
 *             this.views.get(viewId);
 *         test2 = SystemClock.elapsedRealtime();
 *         LogEx.e(&quot;views.get的耗时&quot;, test2 - test1);
 *         double r2 = test2 - test1;
 *
 *         all += r1 / r2;
 *         LogEx.e(&quot;============十万次执行后的结果============&quot;, r1 / r2);
 *     }
 *     LogEx.e(&quot;============千万次平均后的结果============&quot;, all / 100);
 * }
 * </pre>
 *
 * <h5>性能测试结果：</h5>
 *
 * <pre>
 * 10万次循环执行条件：RelativeLayout布局里有ImageView、TextView等十个子控件无嵌套
 * 10万次循环执行结果：findViewById（80毫秒左右）；SparseArray.get （30毫秒左右）；new SparseArray()（550毫秒左右）
 * 最终得出结论：findViewById 和 SparseArray.get 分别循环执行 1000万次，findViewById要慢2.5倍左右。
 *
 * 10万次循环执行条件：RelativeLayout布局里有ImageView、TextView等十个子控件【且随机分布嵌套入3层容器控件里】
 * 10万次循环执行结果：findViewById（155毫秒左右）；SparseArray.get （30毫秒左右）；new SparseArray()（550毫秒左右）
 * 最终得出结论：findViewById 和 SparseArray.get 分别循环执行 1000万次，findViewById要慢5.1倍左右。
 * </pre>
 *
 * <h5>参考网址：</h5>
 * <ul>
 * <li>
 * <a href="http://blog.csdn.net/lmj623565791/article/details/38902805">Android
 * 快速开发系列 打造万能的ListView GridView 适配器 - Hongyang - 博客频道 - CSDN.NET</a></li>
 * <li><a href=
 * "https://github.com/JoanZapata/base-adapter-helper/blob/master/base-adapter-helper/src/main/java/com/joanzapata/android/BaseAdapterHelper.java"
 * >base-adapter-helper/BaseAdapterHelper.java at master
 * JoanZapata/base-adapter-helper</a></li>
 * </ul>
 *
 * @author AsionTang
 * @version 2015年3月4日
 * @since 2015年3月4日
 */
public abstract class BaseAdapterEx3<T> extends BaseAdapterEx<T>
{
    public static final class ViewHolder
    {
        public final int position;
        public final View convertView;
        public final ViewGroup parent;

        /** Views indexed with their IDs */
        private final SparseArray<View> views = new SparseArray<View>();
        private Object mTag;

        public ViewHolder(final int position, final View convertView, final ViewGroup parent)
        {
            this.position = position;
            this.convertView = convertView;
            this.parent = parent;
        }

        public final Button getButton(final int viewId)
        {
            return this.getView(viewId);
        }

        public final CheckBox getCheckBox(final int viewId)
        {
            return this.getView(viewId);
        }

        public final CheckedTextView getCheckedTextView(final int viewId)
        {
            return this.getView(viewId);
        }

        public final EditText getEditText(final int viewId)
        {
            return this.getView(viewId);
        }

        public final ImageButton getImageButton(final int viewId)
        {
            return this.getView(viewId);
        }

        public final ImageView getImageView(final int viewId)
        {
            return this.getView(viewId);
        }

        public final RadioButton getRadioButton(final int viewId)
        {
            return this.getView(viewId);
        }

        public final Space getSpace(final int viewId)
        {
            return this.getView(viewId);
        }

        public final Switch getSwitch(final int viewId)
        {
            return this.getView(viewId);
        }

        /**
         * @return 因为ViewHolder本身占用了 convertView 的setTag 了，所以当需要额外保存别的数据时，使用此
         * setTag
         */
        public final Object getTag()
        {
            return this.mTag;
        }

        public final TextView getTextView(final int viewId)
        {
            return this.getView(viewId);
        }

        public final ToggleButton getToggleButton(final int viewId)
        {
            return this.getView(viewId);
        }

        @SuppressWarnings("unchecked")
        public final <T extends View> T getView(final int viewId)
        {
            View view = this.views.get(viewId);
            if (view == null)
            {
                view = this.convertView.findViewById(viewId);
                this.views.put(viewId, view);
            }
            return (T) view;
        }

        /**
         * 因为ViewHolder本身占用了 convertView 的setTag 了，所以当需要额外保存别的数据时，使用此 setTag
         */
        public final void setTag(final Object tag)
        {
            this.mTag = tag;
        }
    }

    public BaseAdapterEx3(final Context context, final int itemLayoutResId)
    {
        super(context, itemLayoutResId);
    }

    public BaseAdapterEx3(final Context context, final int itemLayoutResId, final List<T> objects)
    {
        super(context, itemLayoutResId, objects);
    }

    public abstract void convertView(final ViewHolder viewHolder, final T item);

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent, final T item)
    {
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        if (viewHolder == null)
        {
            viewHolder = new ViewHolder(position, convertView, parent);
            convertView.setTag(viewHolder);
        }

        this.convertView(viewHolder, item);

        return convertView;
    }

    @Override
    protected List<T> performFiltering(final List<T> originalItems, final CharSequence constraint, final Object... args)
    {
        return originalItems;
    }
}
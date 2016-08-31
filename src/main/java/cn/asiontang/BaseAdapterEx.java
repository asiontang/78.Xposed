package cn.asiontang;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

import java.util.List;

/**
 * 【必须重载{@link #getView(int, View, ViewGroup, Object)}函数！】<br/>
 * 父对象，将在getView里负责初始化 convertView 为 null 的情况。<br/>
 * 子对象，继承时，必须重载编写自定义显示逻辑
 *
 * @author Asion Tang
 * @since 2013年9月6日 12:11:33
 */
public abstract class BaseAdapterEx<T> extends Filter implements ListAdapter, Filterable, SpinnerAdapter
{
    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    private List<T> mObjects;
    private List<T> mOriginalObjects;
    private Object[] mConstraintArgs;

    private final int mItemLayoutResId;

    protected final LayoutInflater mInflater;
    protected final Context mContext;

    /**
     * 不在构造函数里初始化要显示的集合，需使用{@link #setOriginalItems(List)}
     */
    public BaseAdapterEx(final Context context, final int itemLayoutResId)
    {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mItemLayoutResId = itemLayoutResId;
    }

    /**
     * 可直接在在构造函数里初始化要显示的集合，就无需使用{@link #setOriginalItems(List)}
     */
    public BaseAdapterEx(final Context context, final int itemLayoutResId, final List<T> objects)
    {
        this(context, itemLayoutResId);
        this.mOriginalObjects = this.mObjects = objects;
    }

    /**
     * 没有自定义实现， 默认返回true
     */
    @Override
    public boolean areAllItemsEnabled()
    {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CharSequence convertResultToString(final Object resultValue)
    {
        return this.convertResultToStringEx((T) resultValue);
    }

    public CharSequence convertResultToStringEx(final T resultValue)
    {
        return super.convertResultToString(resultValue);
    }

    /**
     * 通过调用filter来过滤当前需要显示的内容<br/>
     * 真正的过滤参数需要通过 cn.asiontang.launcher.BaseAdapterEx.getFilterConstraintArgs()获得！
     */
    public void filter(final Object... constraintArgs)
    {
        this.mConstraintArgs = constraintArgs;
        this.filter("真正的过滤参数需要通过 cn.asiontang.launcher.BaseAdapterEx.getFilterConstraintArgs()获得！");
    }

    /**
     * 获取当前显示的集合（可能是已经过滤后的）数量
     */
    @Override
    public int getCount()
    {
        if (this.mObjects == null)
            return 0;
        return this.mObjects.size();
    }

    @Override
    public View getDropDownView(final int position, final View convertView, final ViewGroup parent)
    {
        return this.getView(position, convertView, parent);
    }

    /**
     * 获取Adapter想暴露的额外的数据。<br/>
     */
    public Object getExtraData()
    {
        return null;
    }

    @Override
    public Filter getFilter()
    {
        return this;
    }

    /**
     * 在当前显示的集合（可能是已经过滤后的）获取指定的项
     */
    @Override
    public T getItem(final int position)
    {
        if (this.mObjects == null)
            return null;
        return this.mObjects.get(position);
    }

    /**
     * 在当前显示的集合（可能是已经过滤后的）获取指定的项的‘唯一标识ID’，默认未实现，返回-1
     */
    @Override
    public long getItemId(final int position)
    {
        return -1;
    }

    /**
     * 获取在当前显示的集合（可能是已经过滤后的）
     */
    public List<T> getItems()
    {
        return this.mObjects;
    }

    /**
     * 获取在当前显示的集合（可能是已经过滤后的）指定位置项的Type
     */
    @Override
    public int getItemViewType(final int position)
    {
        return 0;
    }

    /**
     * 获取原始的未过滤的初始集合。
     */
    public List<T> getOriginaItems()
    {
        return this.mOriginalObjects;
    }

    /**
     * 基类重载了一个getView的默认实现，子类无需再重载此。而应该使用基类提供的增强后的
     * {@link #getView(int, View, ViewGroup, Object)}
     */
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)
    {
        if (convertView == null)
            //==========================================================================
            //不能使用
            //	inflate(R.layout.?, parent);
            //否则会报错
            //	java.lang.UnsupportedOperationException: addView(View, LayoutParams)
            //	is not supported in AdapterView
            //可使用
            //	inflate(R.layout.?, null);（官方不推荐）
            //	inflate(R.layout.?, parent, false)
            //参考资料：
            //	Layout Inflation as Intended - by Dave Smith of Double Encore
            //	http://www.doubleencore.com/2013/05/layout-inflation-as-intended/
            //==========================================================================
            convertView = this.mInflater.inflate(this.mItemLayoutResId, parent, false);

        //本来准备捕获异常后，使用Toast提示一下，但是实际测试发现，提示显示不出来。所以还是算了。留待以后更好的办法
        //        try
        //        {
        this.getView(position, convertView, parent, this.getItem(position));
        //        }
        //        catch (final Exception e)
        //        {
        //            ToastEx.makeTextAndShowLong(e.getMessage());
        //
        //            //暂不在此捕获此异常，因为始终要抛出到外围
        //            //要是不抛出，因为getView经常是在绘制多行数据，会大量抛出同样的异常
        //            //为了避免界面数据显示大量不完整的内容，尝试直接抛出异常，终止显示。
        //            //LogEx.e(this.getClass().getSimpleName(), e);
        //
        //            throw new RuntimeException(e);
        //        }
        return convertView;
    }

    /**
     * 提供初始化好的convertView，和强类型的Item对象。子继承类，只需实现内容显示的逻辑即可。
     *
     * @param convertView 已经初始化好的View对象
     * @param item        强类型子Item对象。
     */
    public abstract View getView(final int position, final View convertView, final ViewGroup parent, final T item);

    @Override
    public int getViewTypeCount()
    {
        return 1;
    }

    /**
     * 没有自定义实现， 默认返回false
     */
    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    /**
     * 默认实现为this.getCount() == 0;
     */
    @Override
    public boolean isEmpty()
    {
        return this.getCount() == 0;
    }

    /**
     * 没有自定义实现， 默认返回true
     */
    @Override
    public boolean isEnabled(final int position)
    {
        return true;
    }

    /**
     * 对继承 Filter 类后的抽象函数的实现，用于转发和处理自定义子类实现{l
     * {@link #performFiltering(List, CharSequence, Object...)}的处理。推荐使用增强后的
     * {@link #performFiltering(List, CharSequence, Object...)}
     */
    @Override
    protected FilterResults performFiltering(final CharSequence constraint)
    {
        final FilterResults result = new FilterResults();
        try
        {
            final List<T> items = this.performFiltering(this.mOriginalObjects, constraint, this.mConstraintArgs);
            result.values = items;
            result.count = items == null ? 0 : items.size();
        } catch (final Exception e)
        {
            //TODO:LogEx.e(this.getClass().getSimpleName(), "performFiltering", e);
        }
        return result;
    }

    /**
     * 可以使用<b>{@link #getOriginaItems()} </b>获取原始列表。
     *
     * @param constraint 通过{@link #filter(CharSequence)} 设置的过滤参数。
     * @param args       通过{@link #filter(Object...)} 设置的过滤参数。
     */
    protected abstract List<T> performFiltering(final List<T> originalItems, final CharSequence constraint, final Object... args);

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(final CharSequence constraint, final FilterResults results)
    {
        this.setItems((List<T>) results.values);
        this.refresh();
    }

    /**
     * 当“初始集合”或“当前显示集合”数据发生改变时，界面默认不会自动刷新，需要手动调用此方法来通知界面刷新显示。<br/>
     * 此函数之所以不自动触发，旨在方便以后可以精确的控制刷新的最佳时机。
     */
    public void refresh()
    {
        if (this.mObjects != null && this.mObjects.size() > 0)
            this.mDataSetObservable.notifyChanged();
        else
            this.mDataSetObservable.notifyInvalidated();
    }

    @Override
    public void registerDataSetObserver(final DataSetObserver observer)
    {
        this.mDataSetObservable.registerObserver(observer);
    }

    /**
     * 设置“当前显示集合”为新集合。此函数不会修改“原始集合”。
     */
    public void setItems(final List<T> items)
    {
        this.mObjects = items;
    }

    /**
     * 设置新的“初始集合”并重置“当前显示的集合”为新集合。<br/>
     * 界面默认不刷新，需调用{@link #refresh()}
     */
    public void setOriginalItems(final List<T> items)
    {
        this.mOriginalObjects = this.mObjects = items;
    }

    @Override
    public void unregisterDataSetObserver(final DataSetObserver observer)
    {
        this.mDataSetObservable.unregisterObserver(observer);
    }
}

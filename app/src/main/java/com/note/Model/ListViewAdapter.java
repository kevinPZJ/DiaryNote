package com.note.Model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.note.R;

import java.util.List;

/**
 * Created by Administrator on 2016/5/29.
 */

public class ListViewAdapter extends BaseAdapter {

    /** 上下文 */
    private Context context;
    /** 日记数据集 */
    private List<ListViewBean> list;

    /** 构造函数 */
    public ListViewAdapter(Context contex,List<ListViewBean> list){
        this.context = contex;
        this.list    = list;
    }

    @Override
    public int getCount() {
        /** 返回数据的总条数 */
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        /** 返回对应位置 position 的该条数据 */
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        /** 返回位置 */
        return position;
    }

    /** 组合单条 数据的显示 视图 */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView==null){ /** View 复用，节省内存 */
            /** 空则实例化 */
            /** 第一个参数是我要显示的视图，第二个是设置基于 parent 父容器，第三个 设置不 绑定到根视图 */
            convertView = LayoutInflater.from(context).inflate(R.layout.single_view,parent,false);

            /** 实例化一个控件集合类,也就是复用的对象，复用 ViewHolder */
            viewHolder = new ViewHolder(convertView);

            /** 把 控件集合类 绑定到 convertView 里面 */
            convertView.setTag(viewHolder);
        }else{
            /** 从之前绑定的里面 获取出来，减去了实例化步骤，达到复用目的 */
            viewHolder = (ViewHolder) convertView.getTag();
        }
        /** 获取出当前位置 position 的这组数据 */
        ListViewBean bean = list.get(position);
        viewHolder.title  .setText(bean.getTitle()); /** 设置标题，下面以此类推 */
        viewHolder.content.setText(bean.getContent());
        viewHolder.time   .setText(bean.getTime());

        return convertView; /** 该函数返回值是 View 视图，这里返回根据 single_view 布局生成的视图*/
    }

    public class ViewHolder{
        public TextView title,content,time;

        public ViewHolder(View convertView){
            /** 下面实例化 single_view 里面对应的 标题、内容、时间 控件 */
            title   = (TextView) convertView.findViewById(R.id.title);
            content = (TextView) convertView.findViewById(R.id.content);
            time    = (TextView) convertView.findViewById(R.id.time);
        }
    }
}

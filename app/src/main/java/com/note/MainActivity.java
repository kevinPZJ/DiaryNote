package com.note;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.note.Model.ListViewAdapter;
import com.note.Model.ListViewBean;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listview;
    private ListViewAdapter adapter; /** 适配器 */
    private List<ListViewBean> lists;/** 数据集合 */
    private Handler handler; /** 网络请求完成后通过它来完成 UI 更新 */
    private String json; /** 网络请求的内容存储者 */
    private boolean isClear = false; /** 是否清空上一次数据的标致位 */

    /** 定义两个常量用来做标记 */
    public static final int OnSendNoteJump = 0x11;
    public static final int OnSendNoteBack = 0x12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** 新建日记按钮的实例化 和 绑定点击事件 */
        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /** 跳转到编辑页面，之所以使用 startActivityForResult
                 * 而不使用 startActivity 是为了让 用户在发表完之后返回主页面能够马上看到自己刚刚上传的 */
                startActivityForResult(new Intent(MainActivity.this, EditerMain.class), OnSendNoteJump);
            }
        });

        /** 实例化 listView */
        listview = (ListView) findViewById(R.id.listview);
        /** 实例化数据集合 */
        lists = new ArrayList<>();
        /** 实例化自定义的 数据适配器 */
        adapter = new ListViewAdapter(this,lists);
        /** 绑定数据适配器 到 listView 里面 */
        listview.setAdapter(adapter);
        /** 设置 listView 的每条点击事件 */
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,EditerMain.class);
                intent.putExtra("data",lists.get(position));
                startActivityForResult(intent, OnSendNoteJump);
            }
        });

        /** 实例化 处理者 和 重写信息处理函数 */
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 1: // 获取数据后的处理类型
                        if(json!=null){
                            if (json.trim().equals("0")){
                                Toast.makeText(MainActivity.this,"您还没上传过日记哦。",Toast.LENGTH_SHORT).show();
                                return; /** 终止运行下面的代码 */
                            }
                            DealWithJson(json);
                            adapter.notifyDataSetChanged(); /** 刷新数据适配器 */
                        }else{
                            Toast.makeText(MainActivity.this,"数据获取为空！",Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        };
        /** 开始获取 日记 数据 */
        GetNoteDataFromServer();
    }

    /** 从服务器获取 日记 数据 */
    /** isClear 设置是否清空上一次的数据 */
    private void GetNoteDataFromServer(){
        /** 下面是 网络连接 判断 */
        if(((ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo()!=null){
            /** 以匿名内部类的形式 开启一个线程 */
            new Thread(new Runnable() {
                @Override
                public void run() {
                    json = getStringFromUrlOnHttpRULconnection(
                            "http://121.42.190.18/ydnurse/Controller/noteController.php?func=GetNote"
                    );
                    handler.sendEmptyMessage(1); // 发送信息
                }
            }).start();
        }else{
            Toast.makeText(this,"无法连接网络！",Toast.LENGTH_SHORT).show();
        }
    }

    /** 网络请求核心 静态 函数 */
    public static String getStringFromUrlOnHttpRULconnection(String url){
        String json = null;
        URL myUrl = null;
        try {
            myUrl = new URL(url);
            HttpURLConnection myUrlConnect = (HttpURLConnection) myUrl.openConnection();
            myUrlConnect.setConnectTimeout(10 * 1000);//7秒超时
            myUrlConnect.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(myUrlConnect.getInputStream(), "UTF-8"), 8);
            StringBuilder info = new StringBuilder();//定义字符容器，用来装载服务器输出的返回信息
            String line = null;//用来保存提取出的每行数据
            while ((line = reader.readLine()) != null) { //保证读到的每行数据不为null
                info.append(line + "\n");//每行相加
            }
            json = info.toString();
            reader.close();
        } catch (Exception e) {
            Log.d("zzzzz", e.toString());
        }
        return json;
    }

    /** 处理 json 的函数 */
    private void DealWithJson(String json){
        try{
            /** 根据 json 字符串实例化一个 json 数组 */
            JSONArray jsonArray = new JSONArray(json);
            int length = json.length(); // 获取 这个 json 数组的长度
            if(length > 0 && isClear){
                lists.clear();
                isClear = false;
            }
            for(int i=0;i<length;i++){
                /** 逐条获取 json 数组的数据 */
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                /** 下面实例化 日记本的 数据类，并将 json 数据绑进去 */
                ListViewBean bean = new ListViewBean();
                /** 三木运算符，判断是否是空，并做默认值处理 */
                bean.setId(jsonObject.isNull("id")           ? -1         : jsonObject.getInt("id"   ));
                bean.setTitle(jsonObject.isNull("title")     ? "日记标题" : jsonObject.getString("title"));
                bean.setContent(jsonObject.isNull("content") ? "日记内容" : jsonObject.getString("content"));
                bean.setTime(jsonObject.isNull("startTime")  ? "日记时间" : jsonObject.getString("startTime"));

                /** 添加到数据集容器 */
                lists.add(bean);
            }
            /** 到这里就是添加完了 */
        }catch (Exception e){
            /** 输出异常信息到控制台 */
            Log.d("zzzzz",e.toString());
        }
    }

    /** 用户在 发表完成后 返回主页面就会 进入这个函数 */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == OnSendNoteJump){
            if(resultCode == OnSendNoteBack){
                isClear = true;
                GetNoteDataFromServer(); // 重新获取日记数据
            }
        }
    }
}





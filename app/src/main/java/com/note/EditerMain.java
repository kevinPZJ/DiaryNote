package com.note;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.note.Model.ListViewBean;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2016/5/29.
 *
 */

public class EditerMain extends Activity {

    private Handler handler; /** 网络请求完成后通过它来完成 UI 更新 */
    private EditText inputTitle,content; // 标题 和 内容 输入 控件
    private int id = -1; // 只有在查看的情况下进入这个页面，这个id 才表示当前查看的日记的 id
    private String sign = "上传"; // 标志 日记是 修改还是 上传

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editer_main);

        /** 实例化 标题 和 内容输入 控件 */
        inputTitle = (EditText) findViewById(R.id.inputTitle);
        content    = (EditText) findViewById(R.id.content);

        Intent intent = getIntent(); // 如果是点击查看的情况下跳转进来的，这里获取 intent
        ListViewBean bean = null;
        if((bean = (ListViewBean)intent.getSerializableExtra("data"))!=null){ // 这里获取我们在 跳转时绑定的数据，判断下是否是空
            // 不为空，那么就是查看的情况下进来的
            inputTitle.setText(bean.getTitle());
            content.setText(bean.getContent());
            id = bean.getId();

            /** 修改发送的 控件 显示为 修改 */
            ((TextView)findViewById(R.id.send)).setText("修改");
            sign = "修改";

            /** 同时要显示出 删除按钮,而且绑定 点击事件 */
            TextView delete = (TextView) findViewById(R.id.delete);
            delete.setVisibility(View.VISIBLE);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DoDelete(id);
                }
            });
        }

        /** 实例化返回按钮 和 绑定点击事件 */
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditerMain.this.setResult(MainActivity.OnSendNoteBack);
                finish();
            }
        });

        /** 实例化 处理者 和 重写信息处理函数 */
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 1: // 发送成功
                        Toast.makeText(EditerMain.this, "日记"+sign+"成功！", Toast.LENGTH_SHORT).show();
                        break;
                    case 2: // 发送失败
                        Toast.makeText(EditerMain.this, "日记"+sign+"失败！", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(EditerMain.this, "日记删除成功！", Toast.LENGTH_SHORT).show();
                        /** 删除成功后 返回主页面 */
                        EditerMain.this.setResult(MainActivity.OnSendNoteBack);
                        finish();
                        break;
                    case 4:
                        Toast.makeText(EditerMain.this, "日记删除失败！", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        /** 实例化提交 按钮 和 绑定点击事件 */
        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /** 下面对 标题 和 内容的 数据输入 进入 不能为 空 判断 */
                if(inputTitle.getText().toString().equals("")){
                    Toast.makeText(EditerMain.this, "日记标题不能为空！", Toast.LENGTH_SHORT).show();
                    return; // 终止执行下面的代码
                }
                if(content.getText().toString().equals("")){
                    Toast.makeText(EditerMain.this, "日记内容不能为空！", Toast.LENGTH_SHORT).show();
                    return; // 终止执行下面的代码
                }

                /** 下面是 网络连接 判断 */
                if(((ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo()!=null){
                    /** 以匿名内部类的形式 开启提交线程 */
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            /** 下面要根据不同的情况进行操作 */
                            String result;
                            if(sign.trim().equals("上传")){  // 上传
                                result = PostDataToServer
                                        (
                                                "http://121.42.190.18/ydnurse/Controller/noteController.php?func=PutNote",
                                                new String[]{"title","content"},
                                                new String[]{inputTitle.getText().toString(),content.getText().toString()}
                                        );
                            }else{                         // 修改
                                result = PostDataToServer
                                        (
                                                "http://121.42.190.18/ydnurse/Controller/noteController.php?func=UpdateNote",
                                                new String[]{"id","title","content"},
                                                new String[]{""+id,inputTitle.getText().toString(),content.getText().toString()}
                                        );
                            }
                            /** 三目运算 对服务器返回的信息做 判断 并 发送信息 */
                            Log.d("zzzzz",result); // 在控制台输出下 返回的信息，方便调试
                            handler.sendEmptyMessage(result.equals("1")?1:2);
                        }
                    }).start();
                }else{
                    Toast.makeText(EditerMain.this, "无法连接网络！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /** 传输数据到 服务器 ，键值对形式传入 */
    public static String PostDataToServer
    (
            String url,
            String[] keys,
            String[] values
    ){
        int length = keys.length;
        StringBuffer data = new StringBuffer();
        for(int i=0;i<length;i++){
            data.append(keys[i]+"="+values[i]+"&"); /** 注意，httpUrlC 是以 = 链接键值的 */
        }
        try {
            URL myUrl = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) myUrl.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoOutput(true);
            //httpURLConnection.setRequestProperty("Connection","Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8"); /** 解决乱码 */
            httpURLConnection.connect(); /** 连接后，往流里写 */
            OutputStream outputStream = httpURLConnection.getOutputStream();

            Log.d("zzzzz", data.toString().getBytes().toString());
            outputStream.write(data.toString().getBytes()); /** 这里写数据到服务器 */
            outputStream.flush();
            outputStream.close();
            /** 获取输出的信息 */
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(),"UTF-8"),8);
            StringBuilder stringBuffer = new StringBuilder();
            String temp;
            while ((temp = bufferedReader.readLine())!=null){
                stringBuffer.append(temp);
            }
            bufferedReader.close();
            httpURLConnection.disconnect();
            return stringBuffer.toString();
        }catch (Exception e){
            Log.d("zzzzz","postData exception "+e.toString());
        }finally {
            System.gc(); /** 执行一次垃圾回收 */
        }
        return null;
    }

    /** 执行日记删除 */
    private void DoDelete(final int id){
        /** 下面是 网络连接 判断 */
        if(((ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo()!=null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String result = MainActivity.getStringFromUrlOnHttpRULconnection(
                            "http://121.42.190.18/ydnurse/Controller/noteController.php?func=DeleteNote&id="+id
                    );
                    handler.sendEmptyMessage(result.trim().equals("1")?3:4);
                }
            }).start();
        }else{
            Toast.makeText(this,"无法连接网络",Toast.LENGTH_SHORT).show();
        }
    }
}

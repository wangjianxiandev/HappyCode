package com.redant.codeland.ui;

//by pjh 2019.3.25 第三模块
import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.redant.codeland.R;

import java.util.HashMap;
import java.util.Set;

public class CodingBaseActivity_test extends AppCompatActivity {
    private WebView webView;
    private Button button;
    private Button button1;
    private TextView resultText;
    private static int a=0;
    private static int b=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coding_base_test);
        webView=findViewById(R.id.code_test);
        //保存
        button=findViewById(R.id.coding_test);
        //加载
        button1=findViewById(R.id.coding_test_s);
        resultText=findViewById(R.id.mResultText);
        resultText.setMovementMethod(ScrollingMovementMethod.getInstance());
        WebSettings webSettings = webView.getSettings();
        // 设置与Js交互的权限
        webSettings.setJavaScriptEnabled(true);
        // 设置允许JS弹窗
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webView.loadUrl("file:///android_asset/blockly-master/demos/code/index.html");
        //实例化SharedPreferences对象（第一步）
        SharedPreferences mySharedPreferences= getSharedPreferences("test",
                Activity.MODE_PRIVATE);
//实例化SharedPreferences.Editor对象（第二步）
        final SharedPreferences.Editor editor = mySharedPreferences.edit();
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url,final String message, final JsResult result) {

                //因为onJsAlert()方法会先于回调的方法执行,所以必须延时打印
                Handler handler = new Handler();
                handler.postDelayed(new Runnable()
                { @Override
                public void run() {
                    resultText.append(message + "\n");
                }
                }, 100);//0.1秒后执行Runnable中的run方法


                Toast.makeText(CodingBaseActivity_test.this, message, Toast.LENGTH_SHORT).show();
                result.confirm();
                Log.e("message:",message);
                //confirm()用于确认，只有webview在获得用户的确认后，才可以操作
                return true;
                //return super.onJsAlert(view,url,message,result);
            }
            @Override
            public boolean onJsPrompt(WebView view, String url, String message,
                                      String defaultValue, JsPromptResult result) {
                Log.d("CodingBaseActivity_test","onJsPrompt:"+message);
                resultText.setText("Prompt input is :"+message);
                result.confirm();
                return super.onJsPrompt(view, url, message, message, result);
            }
        });

// 复写WebViewClient类的shouldOverrideUrlLoading方法
        //用于web调用android的方法
        webView.setWebViewClient(new WebViewClient() {
                                     @Override
                                     public boolean shouldOverrideUrlLoading(WebView view, String url) {

                                         // 步骤2：根据协议的参数，判断是否是所需要的url
                                         // 一般根据scheme（协议格式） & authority（协议名）判断（前两个参数）
                                         //假定传入进来的 url = "js://webview?arg1=111&arg2=222"（同时也是约定好的需要拦截的）

                                         Uri uri = Uri.parse(url);
                                         // 如果url的协议 = 预先约定的 js 协议
                                         // 就解析往下解析参数
                                         if ( uri.getScheme().equals("js")) {

                                             // 如果 authority  = 预先约定协议里的 webview，即代表都符合约定的协议
                                             // 所以拦截url,下面JS开始调用Android需要的方法
                                             if (uri.getAuthority().equals("webview")) {

                                                 //  步骤3：
                                                 // 执行JS所需要调用的逻辑
                                                 //加载会先于清理执行执行的错误
                                                 resultText.setText("");
                                                 a=1;
                                                 Log.e("clean","0");
                                                 // 可以在协议上带有参数并传递到Android上
                                                 //参数暂时用不上
                                                 HashMap<String, String> params = new HashMap<>();
                                                 Set<String> collection = uri.getQueryParameterNames();

                                             }

                                             return true;
                                         }
                                         return super.shouldOverrideUrlLoading(view, url);
                                     }
                                 }
        );
        //保存按钮
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CodingBaseActivity_test.this,"l",Toast.LENGTH_SHORT).show();
                webView.evaluateJavascript("index:Code.changeLanguage()", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        //此处为 js 返回的结果
                        Log.e("returnJS",value);
                        editor.putString("JS",value);
                        editor.commit();
                    }
                });
            }
        });
        //此处需改为仅加载一次
        //加载按钮
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences= getSharedPreferences("test",Activity.MODE_PRIVATE);
                String runJS= sharedPreferences.getString("JS", "");
                Log.e("sharedJS",runJS);
                Toast.makeText(CodingBaseActivity_test.this,"2",Toast.LENGTH_SHORT).show();
                webView.evaluateJavascript("index:Code.loadBlock("+runJS+")", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        //此处为 js 返回的结果
                    }
                });
            }
        });

    }
}

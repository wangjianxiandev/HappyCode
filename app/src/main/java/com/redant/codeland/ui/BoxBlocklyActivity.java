package com.redant.codeland.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.hubert.guide.NewbieGuide;
import com.app.hubert.guide.listener.OnLayoutInflatedListener;
import com.app.hubert.guide.model.GuidePage;
import com.google.blockly.android.AbstractBlocklyActivity;
import com.google.blockly.android.codegen.CodeGenerationRequest;
import com.google.blockly.android.ui.TrashCanView;
import com.redant.codeland.util.AppLanguageUtils;
import com.redant.codeland.R;
import com.redant.codeland.app.MyApplication;
import com.redant.codeland.entity.LevelInfo;
import com.redant.codeland.util.JavascriptUtil;
import com.redant.codeland.util.MyJSInterface;
import com.redant.codeland.util.Util;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class BoxBlocklyActivity extends AbstractBlocklyActivity implements View.OnClickListener {

    private Button btnShow;
    private Button btnContinue;
    private Button btnAgain;
    private Button btnExit;
    private Button btnMusic;
    private Button btnHelp;
    private DrawerLayout drawerLayout;

    //新手引导帮助按钮
    private Button buttonHelp;
    //垃圾桶
    private TrashCanView trashCan;
    //toolbox的用于假装高亮的LinearLayout
    LinearLayout linearLayoutFakeToolbox;

    private static final String TAG = "BoxBlocklyActivity";
    //当前所在的关卡，默认为0，即第一关
//    private int mIndex = 0;
    private int rating;
    private String finalToastWord;
    private int clickedLevel=0;//表示选择关卡难度，从sh中读取，键clickedLevel
    private Button button_back_level;
    //根据不同的关卡，加载不同场景
    //因为 数组下标都是从1开始的，clicklevel也是从1开始的，因此 数组下标0就再设为第一关卡，这样就不用去考虑下标差值
    private String [] htmlPaths = {
            "file:///android_asset/box/html/level1.html",
            "file:///android_asset/box/html/level2.html",
            "file:///android_asset/box/html/level3.html",
            "file:///android_asset/box/html/level4.html",
            "file:///android_asset/box/html/level5.html",
            "file:///android_asset/box/html/level6.html",
            "file:///android_asset/box/html/level7.html",
            "file:///android_asset/box/html/level8.html",
            "file:///android_asset/box/html/level9.html",
            "file:///android_asset/box/html/level10.html"

    };
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppLanguageUtils.attachBaseContext(newBase));
    }
    /**
     * Handler在Dialog点击确定按钮时触发，重新加载英语单词
     */
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            SharedPreferences sharedPreferences=getSharedPreferences("AllLevel",MODE_PRIVATE);
            int gameMaxLevel=sharedPreferences.getInt("gameBoxMaxLevel",0);
            int gameUnlockLevel=sharedPreferences.getInt("gameBoxUnlockLevel",0);
            //点击“完成”退出该活动
            if(msg.what == 1){
                finish();
            }
            //点击“再玩一次”重新加载工作区
            else if(msg.what==2){
                //rating == 3
                mWebview.loadUrl(htmlPaths[clickedLevel]);
//                rating;
            }
            //点击“下一关”，根据星星个数判断是否可以下一关
            else if(msg.what==3)
            {
                //星星不为0，即用户第一次闯关，更新数据库中的星星
                if(rating!=0){
//                    SharedPreferences sharedPreferences=getSharedPreferences("AllLevel",MODE_PRIVATE);
//                    int gameMaxLevel=sharedPreferences.getInt("gameBoxMaxLevel",0);

                    if(clickedLevel<gameMaxLevel-1){
                        clickedLevel++;
                        SharedPreferences.Editor editor=getSharedPreferences("AllLevel",MODE_PRIVATE).edit();
                        editor.putInt("clickedLevel",clickedLevel);
                        editor.commit();
                        mWebview.loadUrl(htmlPaths[clickedLevel]);
                    }else{

                    }

                    //清空工作区
                    getController().resetWorkspace();
                    reloadToolbox();
                }
            }else if(msg.what==4){
                finalToastWord=getString(R.string.time_out);
                rating=-1;
                btnRun.setClickable(true);
                Util.showDialog2(BoxBlocklyActivity.this, finalToastWord.toString(), rating,clickedLevel<gameMaxLevel-1, this);
            }else {
                finalToastWord=getString(R.string.congratulation);
                if(msg.what==6){
                    rating=2;
                }else if(msg.what==7){
                    rating=2;
                }else if(msg.what==8) {
                    rating = 3;
                }
                btnRun.setClickable(true);

//                SharedPreferences sharedPreferences=getSharedPreferences("AllLevel",MODE_PRIVATE);
//                int gameMaxLevel=sharedPreferences.getInt("gameBoxMaxLevel",0);
//                int gameUnlockLevel=sharedPreferences.getInt("gameBoxUnlockLevel",0);
                Util.showDialog2(BoxBlocklyActivity.this, finalToastWord.toString(), rating,clickedLevel<gameMaxLevel-1, this);

                //只有存在的最大关卡大于已解锁的最大关卡， 数据库中的“最大关卡”才会+1
                //Toast.makeText(TadpoleBlocklyActivity.this,"clicked："+clickedLevel+" unlock:"+gameUnlockLevel,Toast.LENGTH_SHORT).show();
                if((clickedLevel>=gameUnlockLevel-1) && gameMaxLevel>gameUnlockLevel){
                    SharedPreferences.Editor editor=getSharedPreferences("AllLevel",MODE_PRIVATE).edit();
                    editor.putInt("gameBoxUnlockLevel",clickedLevel+2);
                    editor.commit();
                }
                List<LevelInfo> rates= DataSupport.where("name = ?","box "+clickedLevel).find(LevelInfo.class);
                if(rates.isEmpty()){
                    LevelInfo rate=new LevelInfo();
                    rate.setName("box "+clickedLevel);
                    rate.setModel("box");
                    rate.setRating(rating);
                    rate.save();
                }
                else{
                    int oldRating=rates.get(0).getRating();
                    if(rating>oldRating){
                        LevelInfo rate=new LevelInfo();
                        rate.setRating(rating);
                        rate.updateAll("name = ?","box "+clickedLevel);
                    }
                }
            }

        }

    };

    private WebView mWebview;
    private Button btnRun;
    private CodeGenerationRequest.CodeGeneratorCallback mCallback = new CodeGenerationRequest.CodeGeneratorCallback() {
        @Override
        public void onFinishCodeGeneration(final String generatedCode) {
            Log.i(TAG, "onFinishCodeGeneration: "+generatedCode);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String encoded = "bx.execute("
                            + JavascriptUtil.makeJsString(generatedCode) + ")";
                    mWebview.loadUrl("javascript:"+encoded);
                }
            });
        }
    };
    @NonNull
    @Override
    protected String getToolboxContentsXmlPath() {
        List<String> jsonPaths = new ArrayList<>();
        if(MyApplication.languageFlag==1){
            jsonPaths.add("box/toolbox/toolbox_level.xml");
        }
        else{
            jsonPaths.add("box/english_toolbox/toolbox_level.xml");
        }
        return jsonPaths.get(0);
    }

    @NonNull
    @Override
    protected List<String> getBlockDefinitionsJsonPaths() {
        List<String> jsonPaths = new ArrayList<>();
        if(MyApplication.languageFlag==1){
            jsonPaths.add("box/box.json");
        }
        else{
            jsonPaths.add("box/english_box.json");
        }

        return jsonPaths;
    }

    @NonNull
    @Override
    protected List<String> getGeneratorsJsPaths() {
        List<String> jsPaths= new ArrayList<>();
        jsPaths.add("box/generator.js");
        return jsPaths;
    }

    @NonNull
    @Override
    protected CodeGenerationRequest.CodeGeneratorCallback getCodeGenerationCallback() {
        return mCallback;
    }

    @Override
    protected View onCreateContentView(int containerId) {
        SharedPreferences sharedPreferences=getSharedPreferences("AllLevel",MODE_PRIVATE);
        clickedLevel=sharedPreferences.getInt("clickedLevel",1);
        clickedLevel--;
        View root = getLayoutInflater().inflate(R.layout.activity_tadpole, null);
        mWebview = root.findViewById(R.id.tp_webview);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.setWebChromeClient(new WebChromeClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        mWebview.addJavascriptInterface(new MyJSInterface(this,handler), "android");
        mWebview.loadUrl(htmlPaths[clickedLevel]);
        btnRun = root.findViewById(R.id.button_tadpole_run);
        btnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //运行代码
                if (getController().getWorkspace().hasBlocks()) {
                    onRunCode();
                    //防止用户多次点击造成多个运行结果
//                    btnRun.setClickable(false);
                } else {
                    Toast.makeText(BoxBlocklyActivity.this,R.string.no_block_in_workspace,Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "工作区中没有块");
                }
            }
        });
        btnContinue=root.findViewById(R.id.blockly_fbtn_continue);
        btnAgain=root.findViewById(R.id.blockly_fbtn_again);
        btnExit=root.findViewById(R.id.blockly_fbtn_exit);
        btnHelp=root.findViewById(R.id.blockly_fbtn_help);
        btnMusic=root.findViewById(R.id.blockly_fbtn_music);
        btnShow=root.findViewById(R.id.blockly_fbtn_show);
        drawerLayout=root.findViewById(R.id.drawer_layout);
        buttonHelp=root.findViewById(R.id.tadpole_help);
        trashCan=root.findViewById(R.id.blockly_trash_icon);
        linearLayoutFakeToolbox=root.findViewById(R.id.tadpole_fake_toolbox);
        buttonHelp.setOnClickListener(this);
        btnShow.setOnClickListener(this);
        btnContinue.setOnClickListener(this);
        btnAgain.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        btnHelp.setOnClickListener(this);
        btnMusic.setOnClickListener(this);
        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.blockly_fbtn_show:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.blockly_fbtn_continue:
                drawerLayout.closeDrawers();
                break;
            case R.id.blockly_fbtn_again:
//                reload();
                drawerLayout.closeDrawers();
                break;
            case R.id.blockly_fbtn_exit:
                finish();
                break;
            case R.id.blockly_fbtn_help:
                boxBlocklyActivityNewNewbieGuide();
                drawerLayout.closeDrawers();
                break;
            case R.id.tadpole_help:
                boxBlocklyActivityNewNewbieGuide();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //适配屏幕引入语句
        com.yatoooon.screenadaptation.ScreenAdapterTools.getInstance().loadView((ViewGroup) getWindow().getDecorView());

        //隐藏Actionbar
        getSupportActionBar().hide();
        //隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //清空工作区
        getController().resetWorkspace();

        //是否第一次打开 推箱子，是则运行新手引导，否则不在此运行
        if(MyApplication.isFirstRun("BoxBlocklyActivityNewbieGuide"))
        {
            boxBlocklyActivityNewNewbieGuide();
        }
    }

    //新手引导
    public void boxBlocklyActivityNewNewbieGuide(){

        NewbieGuide.with(this).setLabel("page").alwaysShow(true)//true:程序员开发调试时使用，每次打开APP显示新手引导;false:只在用户第一次打开APP显示新手引导
                .addGuidePage(
                        GuidePage.newInstance()
                                //可以一个引导页多个高亮，多加几个.addHighLight()即可
                                .addHighLight(buttonHelp)
                                .addHighLight(btnRun)
                                .addHighLight(trashCan)
                                .addHighLight(linearLayoutFakeToolbox)
                                .setLayoutRes(R.layout.tadpole_activity_newbie_guide)//引导页布局，点击跳转下一页或者消失引导层的控件id
                                .setEverywhereCancelable(true)//是否点击任意地方跳转下一页或者消失引导层，默认true
                                .setOnLayoutInflatedListener(new OnLayoutInflatedListener() {
                                    @Override
                                    //onLayoutInflated很重要，返回了 引导层的view，一定要利用好这个view，比如为引导层的view设定监听事件
                                    //如果想要在引导页findViewByID一定要加上 view. 否则空指针报错
                                    public void onLayoutInflated(View view) {
                                        TextView textViewNewbieGuide = view.findViewById(R.id.textview_mainactvitiy_newbie_guide);

                                    }
                                })
                ).show();
    }

}
package com.redant.codeland.ui;


import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.widget.Button;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.view.ViewGroup;
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

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;


/**
 * 小蝌蚪找妈妈的blockly界面
 * Created by Administrator on 2018-03-28.
 */

public class TadpoleBlocklyActivity extends AbstractBlocklyActivity implements View.OnClickListener{
    private Button btnShow;
    private Button btnContinue;
    private Button btnAgain;
    private Button btnExit;
    private Button btnMusic;
    private Button btnHelp;
    private DrawerLayout drawerLayout;

    //一个轻量的纸屑粒子效果
    private KonfettiView konfettiView;

    //新手引导帮助按钮
    private Button buttonHelp;
    //垃圾桶
    private TrashCanView trashCan;
    //toolbox的用于假装高亮的LinearLayout
    LinearLayout linearLayoutFakeToolbox;

    private static final String TAG = "TadpoleBlocklyActivity";
    //当前所在的关卡，默认为0，即第一关
//    private int mIndex = 0;
    private int rating;
    private String finalToastWord;
    private int clickedLevel=0;//表示选择关卡难度，从sh中读取，键clickedLevel
    private Button button_back_level;
    //根据不同的关卡，加载不同场景
    //因为 数组下标都是从1开始的，clicklevel也是从1开始的，因此 数组下标0就再设为第一关卡，这样就不用去考虑下标差值
    private String [] htmlPaths = {
//            "file:///android_asset/tadpole/html/level1.html",
            "file:///android_asset/tadpole/html/level1.html",
            "file:///android_asset/tadpole/html/level2.html",
            "file:///android_asset/tadpole/html/level3.html",
            "file:///android_asset/tadpole/html/level4.html",
            "file:///android_asset/tadpole/html/level5.html",
            "file:///android_asset/tadpole/html/level6.html",
            "file:///android_asset/tadpole/html/level7.html",
            "file:///android_asset/tadpole/html/level8.html",
            "file:///android_asset/tadpole/html/level9.html",
            "file:///android_asset/tadpole/html/level10.html",
            "file:///android_asset/tadpole/html/level11.html",
            "file:///android_asset/tadpole/html/level12.html",
            "file:///android_asset/tadpole/html/level13.html",
            "file:///android_asset/tadpole/html/level14.html",
            "file:///android_asset/tadpole/html/level15.html",
            "file:///android_asset/tadpole/html/level16.html",
            "file:///android_asset/tadpole/html/level17.html"
    };
    //根据不同的关卡，加载不同的工具栏

    //有多语言Activity必须加以下语句
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppLanguageUtils.attachBaseContext(newBase));
    }

    /**
     * Handler在Dialog点击确定按钮时触发，重新加载英语单词
     */
    public Handler  handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            SharedPreferences sharedPreferences=getSharedPreferences("AllLevel",MODE_PRIVATE);
            //此类游戏总共的关卡的最大关卡数
            int gameMaxLevel=sharedPreferences.getInt("gameTadpoleMaxLevel",0);
            //用户在此类游戏已经解锁的最大关卡数
            int gameUnlockLevel=sharedPreferences.getInt("gameTadpoleUnlockLevel",0);
            //点击“完成”退出该活动
            if(msg.what == 1){
                //隐藏掉“礼花”效果
                konfettiView.setVisibility(View.GONE);
                finish();
            }
            //点击“再玩一次”重新加载工作区
            else if(msg.what==2){
                //隐藏掉“礼花”效果
                konfettiView.setVisibility(View.GONE);
                 //rating == 3
                    mWebview.loadUrl(htmlPaths[clickedLevel]);
//                rating;
            }
            //点击“下一关”，根据星星个数判断是否可以下一关
            else if(msg.what==3)
            {
                //隐藏掉“礼花”效果
                konfettiView.setVisibility(View.GONE);
                //星星不为0，即用户不是第一次闯关，不用更新数据库中的星星，直接加载下一关界面即可
                if(rating!=0){

                    SharedPreferences.Editor editor=getSharedPreferences("AllLevel",MODE_PRIVATE).edit();
                    clickedLevel++;
                    editor.putInt("clickedLevel",clickedLevel);
                    editor.commit();
                    mWebview.loadUrl(htmlPaths[clickedLevel]);

                    //清空工作区
                    getController().resetWorkspace();
                    reloadToolbox();
                }
            }else if(msg.what==4){
                finalToastWord=getString(R.string.crash_barrier);
                rating=-1;
                btnRun.setClickable(true);
                Util.showDialog2(TadpoleBlocklyActivity.this, finalToastWord.toString(), rating,clickedLevel<gameMaxLevel-1, this);
            }else if(msg.what==5){
                finalToastWord=getString(R.string.not_reach_denstination);
                rating=0;
                btnRun.setClickable(true);
                Util.showDialog2(TadpoleBlocklyActivity.this, finalToastWord.toString(), rating,clickedLevel<gameMaxLevel-1,this);
            }else {
                finalToastWord=getString(R.string.congratulation);
                //成功通关的，那么播放 礼花效果的动画
                konfettiView.setVisibility(View.VISIBLE);
                konfettiView.performClick();
                if(msg.what==6){
                    rating=2;
                }else if(msg.what==7){
                    rating=2;
                }else if(msg.what==8) {
                    rating = 3;
                }
                btnRun.setClickable(true);
                Util.showDialog2(TadpoleBlocklyActivity.this, finalToastWord.toString(), rating,clickedLevel<gameMaxLevel-1, this);

                //只有存在的最大关卡大于已解锁的最大关卡， 数据库中的“最大关卡”才会+1
                //Toast.makeText(TadpoleBlocklyActivity.this,"clicked："+clickedLevel+" unlock:"+gameUnlockLevel,Toast.LENGTH_SHORT).show();
                if((clickedLevel>=gameUnlockLevel-1)){
                    SharedPreferences.Editor editor=getSharedPreferences("AllLevel",MODE_PRIVATE).edit();
                    editor.putInt("gameTadpoleUnlockLevel",clickedLevel+2);
                    editor.commit();
                }
                //当前关卡的星星=0，说明用户第一次进入该游戏关卡，更新星星的数据
                List<LevelInfo> rates= DataSupport.where("name = ?","tadpole "+clickedLevel).find(LevelInfo.class);
                if(rates.isEmpty()){
                    LevelInfo rate=new LevelInfo();
                    rate.setName("tadpole "+clickedLevel);
                    rate.setModel("tadpole");
                    rate.setRating(rating);
                    rate.save();
                }
                //当前关卡的星星不=0，说明用户多次进入该游戏关卡，如果闯关的星星比历史记录的多，那么更新为最多的星星数量，否则星星数量不变
                else{
                    int oldRating=rates.get(0).getRating();
                    if(rating>oldRating){
                        LevelInfo rate=new LevelInfo();
                        rate.setRating(rating);
                        rate.updateAll("name = ?","tadpole "+clickedLevel);
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
                    String encoded = "tp.execute("
                            + JavascriptUtil.makeJsString(generatedCode) + ")";
                    mWebview.loadUrl("javascript:"+encoded);
                }
            });
        }
    };
    @NonNull
    @Override
    protected String getToolboxContentsXmlPath() {
        Log.i(TAG, "getToolboxContentsXmlPath: mIndex------->"+clickedLevel);
        List<String> toolboxPaths=new ArrayList<>();
        if(MyApplication.languageFlag==1){
            toolboxPaths.add("tadpole/toolbox/toolbox_level1.xml");
            toolboxPaths.add("tadpole/toolbox/toolbox_level1.xml");
            toolboxPaths.add("tadpole/toolbox/toolbox_level2.xml");
            toolboxPaths.add("tadpole/toolbox/toolbox_level2.xml");
            toolboxPaths.add("tadpole/toolbox/toolbox_level2.xml");
            toolboxPaths.add("tadpole/toolbox/toolbox_level2.xml");
            toolboxPaths.add("tadpole/toolbox/toolbox_level2.xml");
            toolboxPaths.add("tadpole/toolbox/toolbox_level2.xml");
            toolboxPaths.add("tadpole/toolbox/toolbox_level2.xml");
            toolboxPaths.add("tadpole/toolbox/toolbox_level3.xml");
            toolboxPaths.add("tadpole/toolbox/toolbox_level3.xml");
            toolboxPaths.add("tadpole/toolbox/toolbox_level3.xml");
            toolboxPaths.add("tadpole/toolbox/toolbox_level3.xml");
            toolboxPaths.add("tadpole/toolbox/toolbox_level3.xml");
            toolboxPaths.add("tadpole/toolbox/toolbox_level3.xml");
            toolboxPaths.add("tadpole/toolbox/toolbox_level3.xml");
            toolboxPaths.add("tadpole/toolbox/toolbox_level3.xml");
        }
        else{
            toolboxPaths.add("tadpole/english_toolbox/english_toolbox_level1.xml");
            toolboxPaths.add("tadpole/english_toolbox/english_toolbox_level2.xml");
            toolboxPaths.add("tadpole/english_toolbox/english_toolbox_level2.xml");
            toolboxPaths.add("tadpole/english_toolbox/english_toolbox_level2.xml");
            toolboxPaths.add("tadpole/english_toolbox/english_toolbox_level2.xml");
            toolboxPaths.add("tadpole/english_toolbox/english_toolbox_level2.xml");
            toolboxPaths.add("tadpole/english_toolbox/english_toolbox_level2.xml");
            toolboxPaths.add("tadpole/english_toolbox/english_toolbox_level2.xml");
            toolboxPaths.add("tadpole/english_toolbox/english_toolbox_level2.xml");
            toolboxPaths.add("tadpole/english_toolbox/english_toolbox_level3.xml");
            toolboxPaths.add("tadpole/english_toolbox/english_toolbox_level3.xml");
            toolboxPaths.add("tadpole/english_toolbox/english_toolbox_level3.xml");
            toolboxPaths.add("tadpole/english_toolbox/english_toolbox_level3.xml");
            toolboxPaths.add("tadpole/english_toolbox/english_toolbox_level3.xml");
            toolboxPaths.add("tadpole/english_toolbox/english_toolbox_level3.xml");
            toolboxPaths.add("tadpole/english_toolbox/english_toolbox_level3.xml");
            toolboxPaths.add("tadpole/english_toolbox/english_toolbox_level3.xml");
        }


        return toolboxPaths.get(clickedLevel);
    }

    @NonNull
    @Override
    protected List<String> getBlockDefinitionsJsonPaths() {
        List<String> jsonPaths = new ArrayList<>();
        if(MyApplication.languageFlag==1){
        jsonPaths.add("tadpole/tadpole.json");}
        else{
            jsonPaths.add("tadpole/english_tadpole.json");
        }
        return jsonPaths;
    }

    @NonNull
    @Override
    protected List<String> getGeneratorsJsPaths() {
        List<String> jsPaths= new ArrayList<>();
        jsPaths.add("tadpole/generator.js");
        return jsPaths;
    }

    @NonNull
    @Override
    protected CodeGenerationRequest.CodeGeneratorCallback getCodeGenerationCallback() {
        return mCallback;
    }

    @Override
    protected View onCreateContentView(int containerId) {
//        mIndex = getIntent().getIntExtra("index",0);
//        Log.i(TAG, "onCreateContentView: mIndex------>"+mIndex);
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
                    btnRun.setClickable(false);
                } else {
                    Toast.makeText(TadpoleBlocklyActivity.this,getString(R.string.no_block_in_workspace),Toast.LENGTH_SHORT).show();
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
        konfettiView=root.findViewById(R.id.viewKonfetti);
        buttonHelp=root.findViewById(R.id.tadpole_help);
        trashCan=root.findViewById(R.id.blockly_trash_icon);
        linearLayoutFakeToolbox=root.findViewById(R.id.tadpole_fake_toolbox);
        btnShow.setOnClickListener(this);
        btnContinue.setOnClickListener(this);
        btnAgain.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        btnHelp.setOnClickListener(this);
        btnMusic.setOnClickListener(this);
        konfettiView.setOnClickListener(this);
        buttonHelp.setOnClickListener(this);
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
                TadpoleBlocklyActivityNewNewbieGuide();
                drawerLayout.closeDrawers();
                break;
            case R.id.viewKonfetti:
                konfettiView.build()
                        .addColors(Color.YELLOW,Color.RED,Color.BLUE, Color.GREEN,Color.CYAN,Color.MAGENTA)
                        .setDirection(0.0, 359.0)
                        .setSpeed(1f, 5f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(2000L)
                        .addShapes(Shape.RECT, Shape.CIRCLE)
                        .addSizes(new Size(12, 5))
                        .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, -50f)
                        .streamFor(300, 5000L);

                break;
            case R.id.tadpole_help:
                TadpoleBlocklyActivityNewNewbieGuide();
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

        //是否第一次打开 小蝌蚪找妈妈，是则运行新手引导，否则不在此运行
        if(MyApplication.isFirstRun("TadpoleBlocklyActivityNewbieGuide"))
        {
            TadpoleBlocklyActivityNewNewbieGuide();
        }
    }

    //新手引导
    public void TadpoleBlocklyActivityNewNewbieGuide(){

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
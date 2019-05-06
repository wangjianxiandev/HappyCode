package com.redant.codeland.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.blockly.android.codegen.CodeGenerationRequest;
import com.google.blockly.model.Block;
import com.google.blockly.model.DefaultBlocks;
import com.google.blockly.utils.BlockLoadingException;
import com.google.blockly.utils.BlocklyXmlHelper;
import com.redant.codeland.R;
import com.redant.codeland.app.MyApplication;
import com.redant.codeland.entity.LevelInfo;
import com.redant.codeland.entity.Sanzijing;
import com.redant.codeland.util.Util;
import com.yatoooon.screenadaptation.ScreenAdapterTools;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SanzijingBlocklyActivity extends BaseBlocklyActivity {
    //private int rating = 0;//匹配的评分，用在Dialog的RatingBar中
    //块打乱随机排列的一个参数
    private static final String TOOLBOX_PATH = "poetry/toolbox.xml";
    private static int CARPET_SIZE = 300;
    private static final List<String> JAVASCRIPT_GENERATORS = Arrays.asList(
            // Custom block generators go here. Default blocks are already included.
            "generator.js"                          //添加js生成器函数路径
    );

    private static final String TAG = "MyBlockly";

    //private int currentLevel = 1; //当前选择的level，默认为1，最简单
    public final CodeGenerationRequest.CodeGeneratorCallback mCodeGeneratorCallback =
            new CodeGenerationRequest.CodeGeneratorCallback() {//用户按下运行按钮式打出一个toast,打出的是转换的js代码
                @Override
                public void onFinishCodeGeneration(final String generatedCode) {
                    Log.i(TAG, "generatedCode:\n" + generatedCode);
                    String[] codes = generatedCode.split("\n");
                    StringBuffer finalToastWord = new StringBuffer();
                    //String finalResult = "";//存放结果集toast语句
                    List<Sanzijing> sanzijingsList= DataSupport.findAll(Sanzijing.class);
                    int flagRightOne=0;
                    int flagWrongOne=0;
                    for(int i=0;i<codes.length;i++) {
                        if (codes[i].equals("")) {//发现题的题目脱出后，返回的generationCode会多空出一行
                            continue;
                        }
                        if (!codes[i].contains("=")) {//没有等号证明不是一首具体的诗
                            finalToastWord.append (codes[i] + " 不能构成一句三字经\n");
                            flagWrongOne++;
                            continue;
                        }
                        String[] result = codes[i].split("=");
                        if (result.length == 1) {//只有一个空的框架，题目和内容都没有填
                            finalToastWord.append("这个框架什么都没有\n");
                            flagWrongOne++;
                            continue;
                        }
                        String title = result[1];
                        List<Sanzijing> ps = DataSupport.where("title = ?", title).find(Sanzijing.class);
                        if (ps == null || ps.size() == 0) {
                            Log.i(TAG, "执行第几次循环？" + i);
                            finalToastWord.append(title + "没有这句三字经\n");
                            flagWrongOne++;
                            // Toast.makeText(MyBlocklyActivity.this, "没有这首诗", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "此时的finalrusult为？" +  finalToastWord);
                        } else {
                            //查找到这首诗歌
                            Sanzijing p = ps.get(0);
                            StringBuffer sb = new StringBuffer();
                            //查询这首诗歌的所有内容
                            List<String> cts = p.getContents();
                            //将list形式的内容转化成字符串形式。
                            for (String c : cts) {
                                sb.append(c);
                            }
                            String content = sb.toString();
                            // Log.i(TAG, "此时的内容为？" + content);
                            //Log.i(TAG,""+result.length);
                            if (result.length < 3) {//判断出是否是没有填内容的
                                finalToastWord.append (title + "这句三字经并没有完成\n");
                            } else if (content.equals(result[2])) {
                                finalToastWord.append (title + "匹配正确\n");
                                flagRightOne++;
                            } else {
                                finalToastWord.append (title + "匹配错误\n");
                            }

                        }

                    }
                    if(flagRightOne==0){
                        rating=0;
                    }else if(flagWrongOne>0 && flagRightOne<=flagWrongOne){
                        rating=1;
                    }
                    else{
                        if(flagWrongOne>0 && flagRightOne>flagWrongOne){
                            rating=2;
                        }else if(flagWrongOne==0){
                            rating=3;
                            flag=true;
                        }
                        SharedPreferences sharedPreferences=getSharedPreferences("AllLevel",MODE_PRIVATE);
                        int sanzijingMaxLevel=sharedPreferences.getInt("sanzijingMaxLevel",0);
                        maxLevel=sanzijingMaxLevel;
                        int sanzijingUnlockLevel=sharedPreferences.getInt("sanzijingUnlockLevel",0);
                        //只有 全部拼写正确 且 最大关卡为当前关卡， 数据库中的“最大关卡”才会+1
                        if(sanzijingMaxLevel>sanzijingUnlockLevel && clickedLevel>=sanzijingUnlockLevel){
                            SharedPreferences.Editor editor=getSharedPreferences("AllLevel",MODE_PRIVATE).edit();
                            editor.putInt("sanzijingUnlockLevel",clickedLevel+1);
                            flag=true;
                            editor.commit();
                        }
                    }
                    List<LevelInfo> rates= DataSupport.where("name = ?","sanzijing title "+clickedLevel).find(LevelInfo.class);
                    if(rates.isEmpty()){
                        LevelInfo rate=new LevelInfo();
                        rate.setName("sanzijing title "+clickedLevel);
                        rate.setModel("sanzijing title");
                        rate.setRating(rating);
                        rate.save();
                        Toast.makeText(SanzijingBlocklyActivity.this,rate.getRating()+"",Toast.LENGTH_SHORT).show();
                        Log.d("sanzijing",rate.getRating()+"");
                    }else{
                        int oldRating=rates.get(0).getRating();
                        if(rating>oldRating){
                            LevelInfo rate=new LevelInfo();
                            rate.setRating(rating);
                            rate.updateAll("name = ?","sanzijing title "+clickedLevel);
                            Toast.makeText(SanzijingBlocklyActivity.this,rate.getRating()+"",Toast.LENGTH_SHORT).show();
                            Log.d("sanzijing",rate.getRating()+"");
                        }
                    }
                    Log.d("alike",rating+"");
                    if(flagWrongOne>0){
                        Util.showDialog2(SanzijingBlocklyActivity.this, finalToastWord.toString(), rating,clickedLevel<maxLevel, handler);
                    }
                    else{
                        Util.showDialog2(SanzijingBlocklyActivity.this, "完全正确", rating,clickedLevel<maxLevel, handler);
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //适配屏幕引入语句
        ScreenAdapterTools.getInstance().loadView((ViewGroup) getWindow().getDecorView());


        SharedPreferences sharedPreferences=getSharedPreferences("AllLevel",MODE_PRIVATE);
        clickedLevel=sharedPreferences.getInt("clickedLevel",1);
        loadCase();
        //展示模板，传入上下文和显示的内容
        if(MyApplication.isFirstRun("SanzijingBlocklyActivityNewNewbieGuide"))
        {
            showHelp();
        }

    }

    /**
     * 返回工具栏路径
     * @return
     */
    @NonNull
    @Override
    protected String getToolboxContentsXmlPath() {
        return TOOLBOX_PATH;
    }

    /**
     * 返回块定义的Json文件路径
     * @return
     */
    @NonNull
    @Override
    protected List<String> getBlockDefinitionsJsonPaths() {
        List<String> assetPaths = new ArrayList<>(DefaultBlocks.getAllBlockDefinitions());
        assetPaths.add("sanzijing/sanzijing.json");
        return assetPaths;
    }

    /**
     * 返回生成器函数的路径
     * @return
     */
    @NonNull
    @Override
    protected List<String> getGeneratorsJsPaths() {
        return JAVASCRIPT_GENERATORS;
    }

    @Override
    protected CodeGenerationRequest.CodeGeneratorCallback getCodeGenerationCallback() {
        return mCodeGeneratorCallback;
    }

//    @Override
//    protected void onInitBlankWorkspace() {
//        getController().addVariable("item");
//        loadCase();
//    }

//    @Override
//    protected void onLoadInitialWorkspace() {
//        loadCase();
//    }

    /**
     * 初始化工作区时将块散乱排列
     */
    public void load(int level){
        File directory = getFilesDir();
        File file = new File(directory,"sanzijing_workspace.xml");//生成新的随机xml文件，用于盛放随机选择的古诗块
        if(file.exists()){
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            Util.generateWholeSanzijingXML(fos,level);
            fos.close();//生成文件完毕
            FileInputStream fis = new FileInputStream(file);
            //加载指定XML文件中的块
            List<Block> blocks = BlocklyXmlHelper.loadFromXml(fis,
                    getController().getBlockFactory());

            //将块打乱排列
            for (int i = 0; i < blocks.size(); i++) {
                Block copiedModel = blocks.get(i).deepCopy();
//                copiedModel.input
                copiedModel.setPosition((int) (Math.random() * CARPET_SIZE) - CARPET_SIZE / 2,
                        (int) (Math.random() * CARPET_SIZE) - CARPET_SIZE / 2);
                getController().addRootBlock(copiedModel);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BlockLoadingException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void reload() {
        loadCase();
    }

    @Override
    protected void showHelp() {
        super.BaseBlocklyActivityNewNewbieGuide();
        Util.showGuide(this, R.string.guide_sanzijing, R.mipmap.sanzijing_example);
    }

    @Override
    public void loadModel() {
        load(clickedLevel);
    }

    @Override
    public void loadCase() {
        super.loadCase();
    }
}

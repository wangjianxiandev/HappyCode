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
import com.redant.codeland.entity.Animal;
import com.redant.codeland.entity.LevelInfo;
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


public class AnimalKindActivity extends BaseBlocklyActivity{


    //打印log的tag
    private static final String TAG = "AnimalKindActivity";
    //块打乱随机排列的一个参数
    private static int CARPET_SIZE = 300;
    //toolbox的路径,复用了古诗中的空白toolbox
    private static final String TOOLBOX_PATH = "animal/toolbox.xml";
    //添加js生成器函数路径
    private static final List<String> JAVASCRIPT_GENERATORS = Arrays.asList(
            "generator.js"
    );

    //回调接口,在这里写匹配逻辑
    public final CodeGenerationRequest.CodeGeneratorCallback mCodeGeneratorCallback =
            new CodeGenerationRequest.CodeGeneratorCallback() {//用户按下运行按钮式打出一个toast,打出的是转换的js代码
                @Override
                public void onFinishCodeGeneration(final String generatedCode) {
                    Log.i(TAG, "generatedCode:\n" + generatedCode);
                    String[] codes = generatedCode.split("\n");
                    StringBuffer finalToastWord = new StringBuffer();
                    List<Animal> animalList=DataSupport.findAll(Animal.class);
                    int flagRightOne=0;
                    int flagWrongOne=0;
                    for(int i=0;i<codes.length;i++) {
                        //包含=，是种类块
                        if (codes[i].contains("=")) {
                            String[] arrs = codes[i].split("=");
                            String kind = arrs[0];
                            if (arrs.length == 1) {
                                finalToastWord.append(kind + ",后面是空的哦\n");
                                flagWrongOne++;
                                continue;
                            }
                            List<Animal> sList = DataSupport.where("kind = ?", kind).find(Animal.class);
                            if (sList == null || sList.size() == 0) {
                                finalToastWord.append("没有以" + kind + "开头的种类，是不是你把它改掉了\n");
                                flagWrongOne++;
                            } else {
                                String [] instances=arrs[1].split("-");
                                for(int j=1;j<instances.length;j++){
                                    int k=0;
                                    for(k=0;k<sList.size();k++){
                                        if(instances[j].equals(sList.get(k).getName())){
                                            break;
                                        }
                                    }
                                    if(k==sList.size()){
                                        flagWrongOne++;
                                        finalToastWord.append(instances[j]+"加错了科属\n");
                                    }else {
                                        flagRightOne++;
                                    }
                                }
                            }
                        }
                        else {
                            finalToastWord.append(codes[i].substring(1) + "没有归类\n");
                            flagWrongOne++;
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
                        }
                        SharedPreferences sharedPreferences=getSharedPreferences("AllLevel",MODE_PRIVATE);
                        int animalMaxLevel=sharedPreferences.getInt("animalMaxLevel",0);
                        maxLevel=animalMaxLevel;
                        int animalUnlockLevel=sharedPreferences.getInt("animalUnlockLevel",0);
                        //只有 全部拼写正确 且 最大关卡为当前关卡， 数据库中的“最大关卡”才会+1
                        if(animalMaxLevel>animalUnlockLevel && clickedLevel>=animalUnlockLevel){
                            SharedPreferences.Editor editor=getSharedPreferences("AllLevel",MODE_PRIVATE).edit();
                            editor.putInt("animalUnlockLevel",clickedLevel+1);
                            flag=true;
                            editor.commit();
                        }
                    }
                    List<LevelInfo> rates=DataSupport.where("name = ?","animal kind "+clickedLevel).find(LevelInfo.class);
                    if(rates.isEmpty()){
                        LevelInfo rate=new LevelInfo();
                        rate.setName("animal kind "+clickedLevel);
                        rate.setModel("animal kind");
                        rate.setRating(rating);
                        rate.save();
                        Toast.makeText(AnimalKindActivity.this,rate.getRating()+"",Toast.LENGTH_SHORT).show();
                        Log.d("animal",rate.getRating()+"");
                    }else{
                        int oldRating=rates.get(0).getRating();
                        if(rating>oldRating){
                            LevelInfo rate=new LevelInfo();
                            rate.setRating(rating);
                            rate.updateAll("name = ?","animal kind "+clickedLevel);
                            Toast.makeText(AnimalKindActivity.this,rate.getRating()+"",Toast.LENGTH_SHORT).show();
                            Log.d("animal",rate.getRating()+"");
                        }
                    }
                    if(flagWrongOne>0){
                        Util.showDialog2(AnimalKindActivity.this, finalToastWord.toString(), rating,clickedLevel<maxLevel, handler);
                    }
                    else{
                        Util.showDialog2(AnimalKindActivity.this, "完全正确", rating,clickedLevel<maxLevel, handler);
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
        //是否第一次打开APP，是则运行新手引导，否则不在此运行
        if(MyApplication.isFirstRun("AnimalKindActivityNewNewbieGuide"))
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
        assetPaths.add("animal/animal.json");
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
        File file = new File(directory,"animal_kind_workspace.xml");//生成新的随机xml文件，用于盛放随机选择的古诗块
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
            Util.generateAnimalKindXML(fos,level);
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
        Util.showGuide(this, R.string.guide_kinds, R.mipmap.guide_example_kinds);
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

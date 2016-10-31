package net.chaoc.blescanner;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import net.chaoc.blescanner.utils.CacheUtil;
import net.chaoc.blescanner.utils.ConfigUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import io.yunba.android.manager.YunBaManager;

/**
 * Created by yejun on 10/29/16.
 * Copyright (C) 2016 qinyejun
 */

public class MyApplication  extends BaseApplication {

    public static final String TAG = MyApplication.class.getSimpleName();

    public int currentNetConfigVersion = 1;//网络配置项
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        initContext();
        initCache();
        saveConfig();
        //initLog();
        //initImageLoader();

        YunBaManager.start(this);
    }

    public static Context getContext(){
        return mContext;
    }

    private String getAppName(int pID) {
        String processName = null;
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pID) {
                    CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return processName;
    }

    public void initContext(){
        BaseContext.getInstance().init(this);
    }

    /**
     * 初始化配置
     */
    private void saveConfig(){
        try {
            File f = new File(this.getFilesDir(), "config.xml");
            InputStream is;
            int netConfigVersion = CacheUtil.getInstance().getNetConfigVersion();
            if (f.exists()) {
                if(netConfigVersion != currentNetConfigVersion){
                    f.delete();
                    is = this.getResources().getAssets()
                            .open("config.xml");
                    ConfigUtil.getInstance(is);
                    ConfigUtil.getInstance().save(this);
                    CacheUtil.getInstance().setNetConfigVersion(currentNetConfigVersion);
                }else {
                    is = new FileInputStream(f);
                    ConfigUtil.getInstance(is);
                }
            } else {
                is = this.getResources().getAssets()
                        .open("config.xml");
                ConfigUtil.getInstance(is);
                ConfigUtil.getInstance().save(this);
            }
        } catch (IOException e) {
            Log.e(TAG, "找不到assets/目录下的config.xml配置文件", e);
            e.printStackTrace();
        }
    }

    /**
     * 初始化缓存
     */
    private void initCache(){
        CacheUtil.getInstance().init(this);
    }

    /**
     * 初始化Log配置项
     */
    private void initLog() {
        /*LogConfig config = new LogConfig();
        config.setContext(this)
                .setLogSwitch(LogSwitch.OPEN)
                .setLogPath(
                        Environment.getExternalStorageDirectory() + "/"
                                + this.getPackageName() + "/log/");
        LogUtil.getInstance().init(config);*/
    }



    /**
     * 启动激活服务
     */
    private void initActive(){
        /*Intent intent = new Intent(this, ActiveService.class);
        intent.setAction("com.zkjinshi.parking.ACTION_ACTIVE");
        startService(intent);*/
    }
}
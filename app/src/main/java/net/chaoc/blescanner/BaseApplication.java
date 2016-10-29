package net.chaoc.blescanner;

import android.app.Application;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yejun on 10/29/16.
 * Copyright (C) 2016 qinyejun
 */

public class BaseApplication  extends Application {

    private static final String TAG = "BaseApplication";

    protected List<AppCompatActivity> activityStack = new LinkedList<>();

    protected static BaseApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    public static BaseApplication getInst(){
        return application;
    }

    public int activityStackCount(){
        return activityStack.size();
    }

    public void addActivity(AppCompatActivity activity){
        activityStack.add(activity);
    }

    public void remove(AppCompatActivity activity){
        if(activityStack.contains(activity))
            activityStack.remove(activity);
    }

    public void restart(){
        for(AppCompatActivity act:activityStack){
            act.finish();
        }
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    public void clear() {
        for(int i =activityStack.size()-1; i > -1; i -- ){
            AppCompatActivity act = activityStack.get(i);
            act.finish();
        }
    }

    public void clearLeaveTop() {
        try {
            for(int i =activityStack.size()-1; i > -1; i -- ){
                AppCompatActivity act = activityStack.get(i);
                if(!(act instanceof MainActivity))
                    act.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exit() {
        clear();
        System.exit(0);
    }
}

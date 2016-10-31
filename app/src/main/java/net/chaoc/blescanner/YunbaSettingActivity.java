package net.chaoc.blescanner;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.chaoc.blescanner.utils.CacheUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yejun on 10/31/16.
 * Copyright (C) 2016 qinyejun
 */

public class YunbaSettingActivity extends AppCompatActivity {

    @BindView(R.id.et_alias)
    EditText mEtAlias;
    @BindView(R.id.et_topic)
    EditText mEtTopic;
    @BindView(R.id.btn_confirm)
    Button mBtnConfirm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yunba_setting);

        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        String alias = CacheUtil.getInstance().getYunbaAlias();
        String topic = CacheUtil.getInstance().getYunbaTopic();

        mEtAlias.setText(alias);
        mEtTopic.setText(topic);
    }

    @OnClick(R.id.btn_confirm)
    public void onClick() {
        String alias = mEtAlias.getText().toString().trim();
        String topic = mEtTopic.getText().toString().trim();

        if (TextUtils.isEmpty(alias)) {
            Toast.makeText(this,"别名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(topic)) {
            Toast.makeText(this,"频道不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        CacheUtil.getInstance().setYunbaAlias(alias);
        CacheUtil.getInstance().setYunbaTopic(topic);
        finish();
    }
}

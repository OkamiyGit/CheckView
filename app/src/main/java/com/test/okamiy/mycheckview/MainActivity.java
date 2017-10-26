package com.test.okamiy.mycheckview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.test.okamiy.mycheckview.view.CheckView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.check_view)
    CheckView mCheckView;
    @BindView(R.id.check_view_accent)
    CheckView mCheckViewAccent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //监听回调
        mCheckView.setOnCheckedChangeListener(new CheckView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CheckView checkView, boolean isCheck) {
                Toast.makeText(MainActivity.this, isCheck + "", Toast.LENGTH_SHORT).show();
            }
        });

        mCheckViewAccent.setOnCheckedChangeListener(new CheckView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CheckView checkView, boolean isCheck) {
                Toast.makeText(MainActivity.this, isCheck + "", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //button点击事件
    @OnClick({R.id.checked_btn, R.id.uncheck_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.checked_btn:
                mCheckViewAccent.setChecked(true);
                mCheckView.setChecked(true);
                break;
            case R.id.uncheck_btn:
                mCheckViewAccent.setChecked(false);
                mCheckView.setChecked(false);
                break;
        }
    }
}

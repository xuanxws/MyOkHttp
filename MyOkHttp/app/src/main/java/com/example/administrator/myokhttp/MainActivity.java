package com.example.administrator.myokhttp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.myokhttp.okhttp.RequestStrategy;
import com.example.administrator.myokhttp.tools.Utils;

public class MainActivity extends AppCompatActivity {

    MainActivity mContext;
    private EditText etName;
    private TextView tvZjlx;
    private EditText tvZjhm;
    private TextView tvSex;
    private TextView tvBirthDay;
    TestModule mTestModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();
    }

    private void initView(){
        etName = (EditText) findViewById(R.id.tv_name);
        tvZjlx = (TextView) findViewById(R.id.tv_zjlx);
        tvZjhm = (EditText) findViewById(R.id.tv_zjhm);
        tvSex = (TextView) findViewById(R.id.tv_sex);
        tvBirthDay = (TextView) findViewById(R.id.tv_birthday);
        Utils.init(mContext);
        mTestModule = new TestModule(mContext);
        mTestModule.getMemberInfo(RequestStrategy.GET_SEND_STORE, true, new DataCallBack() {
            @Override
            public void onSuccess(final Object object, String objectFrom) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MemberUserInfoItem memberUserInfoItem = (MemberUserInfoItem) object;
                        initData(memberUserInfoItem);
                    }
                });
            }

            @Override
            public void onError(final String hint) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext,hint,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void initData(MemberUserInfoItem memberUserInfoItem){
        etName.setText(memberUserInfoItem.getTruename());
        tvZjlx.setText(memberUserInfoItem.getCard_type());
        tvZjhm.setText(memberUserInfoItem.getCard());
        if ("0".equals(memberUserInfoItem.getSex())) {
            tvSex.setText("男");
        } else if ("1".equals(memberUserInfoItem.getSex())) {
            tvSex.setText("女");
        }
        tvBirthDay.setText(memberUserInfoItem.getBirthday());
    }
}

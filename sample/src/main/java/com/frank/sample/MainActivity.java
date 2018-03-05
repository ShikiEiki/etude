package com.frank.sample;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.frank.etude.pageBtnBar.PageBtnBarAdapter;
import com.frank.sample.databinding.ActivityTestBinding;

/**
 * Created by FH on 2018/3/2.
 */

public class MainActivity extends Activity{
    ActivityTestBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout());
    }

    public View layout() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(this) , R.layout.activity_test , null , false);
        binding.btnBar.setPageBarAdapter(new PageBtnBarAdapter(getApplicationContext()) {
            @Override
            public int getPageBtnCount() {
                return 10;
            }

            @Override
            public void onPageBtnClick(View btn, int btnIndex, String textInBtn) {
                Log.v("FH" , "==============================onPageBtnClick " + btnIndex);
            }
        });
        return binding.getRoot();
    }

    public void onBtn1Click(View view){
        binding.btnBar.refreshPageBar();
    }
    public void onBtn2Click(View view){
    }
    public void onBtn3Click(View view){
    }
    public void onBtn4Click(View view){
    }
    public void onBtn5Click(View view){
    }
    public void onBtn6Click(View view){
    }
    public void onBtn7Click(View view){
    }
    public void onBtn8Click(View view){
    }
}

package com.frank.sample;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;

import com.frank.etude.pageable.PageBtnBarAdapterV2;
import com.frank.sample.databinding.ActivityTestBinding;

/**
 * Created by FH on 2018/3/2.
 */

public class MainActivity extends Activity{
    ActivityTestBinding binding;
    int totalCount = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout());
    }

    public View layout() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(this) , R.layout.activity_test , null , false);
        binding.btnBar.setPageBarAdapter(new PageBtnBarAdapterV2(getApplicationContext()) {
            @Override
            public int getPageBtnCount() {
                return totalCount;
            }

            @Override
            public void onPageBtnClick(View btn, int btnIndex, String textInBtn) {
                binding.textview.setText(textInBtn);
            }

            @Override
            public void onNoPageToShow() {
                if (totalCount == -1){
                    totalCount = 10;
                    binding.btnBar.refreshPageBar();
                }
                else {
                    binding.textview.setText("nothing!");
                }
            }
        });
        return binding.getRoot();
    }

    public void onBtn1Click(View view)
    {
        totalCount = 10;
    }
    public void onBtn2Click(View view){
        totalCount = 20;
    }
    public void onBtn3Click(View view){
        totalCount = 30;
    }
    public void onBtn4Click(View view){
        totalCount = 0;
    }
    public void onBtn5Click(View view){
        binding.btnBar.refreshPageBar();
    }
    public void onBtn6Click(View view){
        binding.btnBar.clickPageBtn(10);
    }
    public void onBtn7Click(View view){
        binding.btnBar.clickPageBtn(15);
    }
    public void onBtn8Click(View view){
        binding.btnBar.clickPageBtn(16);
    }
}

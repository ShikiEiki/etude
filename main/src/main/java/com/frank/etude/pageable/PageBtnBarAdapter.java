package com.frank.etude.pageable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.frank.etude.R;

/**
 * Created by FH on 2018/2/28.
 */

/**
 * PageBtnBar的适配器类,用来为PageBtnBar提供数据.
 * 使用的时候需要实现其中的抽象方法才能使用.
 */
public abstract class PageBtnBarAdapter {
    private Context mContext;

    public PageBtnBarAdapter(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 获取按钮上的文字,可被覆写以实现客制化按钮文字的需求.
     * 覆写时注意,index可能为-1或-2,分别代表向右翻页键和向左翻页键.
     *
     * @param index 按钮在btnbar中的index,从0开始,特殊情况为-1和-2,代表向右翻页键,和向左翻页键
     * @return 按钮上的文字
     */
    public String getPageText(int index){
        if (index == -1){
            return ">>";
        }
        else if (index == -2){
            return "<<";
        }
        return String.valueOf(index + 1);
    }

    /**
     * 获取按钮view,可被覆写以实现客制化按钮样式的需求.
     * 覆写时注意,记得调用getPageText方法设定按钮上的文字,否则按钮将会是无字的.
     * 如果只需要设定按钮的文字,请选择覆写getPageText方法而不是getBtn方法.
     *
     * @param index 按钮在btnbar中的index,从0开始,特殊情况为-1和-2,代表向右翻页键,和向左翻页键
     * @param convertView 如果按钮栏中已经存在可以重复利用的view,会在这个参数传入.
     *                    出于节省内存的目的,应当尽量利用这个view做修改而不是新建一个新的view.
     * @param parent 按钮的父控件,一般为PageBtnBar对象
     * @return 新创建的或利用之前的convertView修改而成的btn view.
     */
    public TextView getBtn(int index, TextView convertView, ViewGroup parent){
        if (convertView == null){
            convertView = (TextView) LayoutInflater.from(mContext)
                    .inflate(R.layout.page_btn, parent, false);
        }
        convertView.setText(getPageText(index));//此处手动调用了getPageText()方法
        return convertView;
    }

    /**
     * 获取一屏最多显示的按钮个数,可被覆写.
     * @return
     */
    public int getMaxBtnCountPerScreen(){
        return 5;
    }

    /**
     * 获取总共存在的按钮个数
     * @return
     */
    public abstract int getPageBtnCount();

    /**
     * 按钮被按下的回调
     * @param btn 被按下的按钮的view
     * @param btnIndex 被按下的按钮在所有按钮中的index
     * @param textInBtn 被按下的按钮的文字
     */
    public abstract void onPageBtnClick(View btn, int btnIndex, String textInBtn);

    /**
     * refreshPageBtnBar时发现没有一个按钮可以展示,则会回调此方法.
     */
    public abstract void onNoPageToShow();


}

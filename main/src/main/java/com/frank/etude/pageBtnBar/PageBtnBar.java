package com.frank.etude.pageBtnBar;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by FH on 2018/2/27.
 */

public class PageBtnBar extends LinearLayout{
    private PageBtnBarAdapter mPageBarAdapter;
    private int currentSelectPageIndex = -1;
    private int firstBtnIndex = -1;
    private int lastBtnIndex = -1;

    public PageBtnBar(Context context) {
        this(context , null);
    }

    public PageBtnBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs , 0);
    }

    public PageBtnBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setGravity(Gravity.CENTER);
    }

    public void setCurrentSelectPageIndex(int index){
        currentSelectPageIndex = index;
    }
    public int getCurrentSelectPageIndex() {
        return currentSelectPageIndex;
    }

    /**
     * 刷新按钮栏,如果没有设置currentSelectPageIndex,则自动设置第0个按钮为select.
     */
    public void refreshPageBar() {
        //没有设置adapter时刷新直接抛出异常
        if (mPageBarAdapter == null){
            throw new NoAdapterException("本PageBtnBar没有绑定adapter!");
        }
        int totalPageBtnCount = mPageBarAdapter.getPageBtnCount();//总按钮个数
        //如果总按钮个数为0,或当前每屏可显示按钮数为0,则移除所有已经存在的按钮,并且把所有变量都初始化,然后刷新结束
        if (totalPageBtnCount <= 0 || mPageBarAdapter.getMaxBtnCountPerScreen() <= 0){
            removeAllViews();
            currentSelectPageIndex = -1;
            firstBtnIndex = -1;
            lastBtnIndex = -1;
            return;
        }

        boolean needPerformClick = false;//刷新完成后是否需要自动触发一次当前选中按钮的click事件

        //初始first和lastIndex都为-1时,就一定是超限的,这样做会导
        if (firstBtnIndex == -1 && lastBtnIndex == -1){
            toNextScreen();
        }

        if (currentSelectPageIndex == -1
                || currentSelectPageIndex >= totalPageBtnCount
                || lastBtnIndex > (totalPageBtnCount - 1)
                || firstBtnIndex > (totalPageBtnCount - 1)
                || lastBtnIndex < 0
                || firstBtnIndex < 0){
            //当前选中按钮超限或firstBtnIndex和lastBtnIndex超限时,将按钮显示变更为第一屏
            // ,并且将当前选中按钮置为第一屏
            // ,并且计划一次自动点击事件.
            currentSelectPageIndex = 0;
            firstBtnIndex = -1;
            lastBtnIndex = -1;
            toNextScreen();
            needPerformClick = true;
        }

        //在firstBtnIndex和lastBtnIndex都没有超限时还需要考虑刷新前按钮个数较少,刷新后按钮个数变多了,这时lastBtnIndex可能太小
        // ,不能满足一屏显示按钮数量最多是MaxBtnCountPerScreen的要求,需要再对lastBtnIndex进行一次校正.
        if ((lastBtnIndex - firstBtnIndex + 1) < mPageBarAdapter.getMaxBtnCountPerScreen()){
            lastBtnIndex = firstBtnIndex + mPageBarAdapter.getMaxBtnCountPerScreen() - 1;
            if (lastBtnIndex >= mPageBarAdapter.getPageBtnCount()){
                lastBtnIndex = mPageBarAdapter.getPageBtnCount() - 1;
            }
        }

        boolean needLastScreenBtn = firstBtnIndex != 0 ? true : false;//是否需要显示向前一页的按钮
        boolean needNextScreenBtn = lastBtnIndex + 1 != totalPageBtnCount ? true : false; //是否需要显示向后一页的按钮
        int needBtnCount = lastBtnIndex - firstBtnIndex + 1
                + (needLastScreenBtn?1:0) + (needNextScreenBtn?1:0);//加上向前向后翻页按钮一共需要显示的按钮数

        for (int i = 0 ; i < needBtnCount ; i++){
            //把需要的按钮从0开始遍历一遍
            TextView oldBtn = (TextView) getChildAt(i);//现在btnbar中在第i个位置上的btn,可以为null
            TextView newBtn;
            int btnIndex;//在第i个位置上的btn在所有按钮(不是本屏)中的index,向前向后翻页按钮的index分别为-2,-1
            //以下为确定btnIndex的值
            if (i == 0) {
                if (needLastScreenBtn) {
                    //在i==0位置上的按钮如果满足needLastScreenBtn则即为向前翻页钮
                    btnIndex = -2;
                } else {
                    //否则即为正常按钮,计算按钮index.
                    btnIndex = firstBtnIndex + 0;
                }
            }
            else if (needNextScreenBtn && (i + 1) == needBtnCount){
                //在btnBar最后一个位置上的按钮如果满足needNextScreenBtn则为向后翻页按钮
                btnIndex = -1;
            }
            else {
                //其他位置上的按钮都为正常按钮,需要按照是否存在向前翻页按钮确定他们的index值
                if (needLastScreenBtn){
                    btnIndex = firstBtnIndex + i - 1;
                }
                else {
                    btnIndex = firstBtnIndex + i;
                }
            }

            //以下为创建新按钮
            //根据之前算出的btnIndex创建新按钮
            newBtn = mPageBarAdapter.getBtn(btnIndex, oldBtn , this);
            //将index设置到btn的tag上,以便后续onCLick时确定Index
            newBtn.setTag(btnIndex);
            if (!newBtn.equals(oldBtn)){
                //获取到的新的btn不是原先重复利用的,则需要重新绑定onClicklistnener
                newBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //获取本按钮在所有按钮上的index
                        int index = (int) v.getTag();
                        if (index == -2){
                            //向前翻页逻辑
                            toLastScreen();
                            refreshPageBar();
                        }
                        else if (index == -1){
                            //向后翻页逻辑
                            toNextScreen();
                            refreshPageBar();
                        }
                        else {
                            //普通按钮点击逻辑,调用PageBarAdapter中用户的onCLick逻辑,并且更新currentSelectPageIndex为当前点击按钮
                            //并刷新bar
                            currentSelectPageIndex = index;
                            refreshPageBar();
                            mPageBarAdapter.onPageBtnClick(v , index , (String)((TextView) v).getText());
                        }
                    }
                });
                //如果获取到的新的btn不是原先的那个btn,则替换,否则则不用替换直接用就好.
                addView(newBtn);
                if (oldBtn != null){
                    removeView(oldBtn);
                }
            }

            //以下为确定i位置上的按钮的选中select状态,以便显示不同的样式
            //按钮栏最后位置的btn,且可确定为向后翻页的btn,永远不选中
            if (needNextScreenBtn && (i == needBtnCount - 1)){
                newBtn.setSelected(false);
            }
            else {
                if (needLastScreenBtn){
                    if (i != 0
                            && ((currentSelectPageIndex - firstBtnIndex) == (i-1))){
                        //当向前翻页按钮存在时,不在第一个位置上的按钮,且其index与currentSelectPageIndex相等时,证明其当前被选中.
                        newBtn.setSelected(true);
                    }
                    else {
                        //其他情况(包括普通按钮index与currentSelectPageIndex不符,或者向前翻页按钮) 全部为不选中
                        newBtn.setSelected(false);
                    }
                }
                else {
                    //当向前翻页按钮不存在时,按钮index与currentSelectPageIndex相等时,证明其当前被选中.
                    if (currentSelectPageIndex - firstBtnIndex == i){
                        newBtn.setSelected(true);
                    }
                    else {
                        //不相等时,为不选中
                        newBtn.setSelected(false);
                    }
                }
            }
        }
        //如果btnbar中还有其他不被需要的按钮,全部移除
        for (int i = needBtnCount ; i < getChildCount() ;i++){
            removeViewAt(i);
        }
        //如果需要自动点击当前选中按钮,则根据是否存在向前翻页按钮,计算出当前选中按钮的index,从而获得该按钮,然后模拟点击
        if (needPerformClick){
            View clickBtn = getChildAt(currentSelectPageIndex - firstBtnIndex + (needLastScreenBtn?1:0));
            if (clickBtn != null){
                clickBtn.performClick();
            }
        }
    }

    public PageBtnBarAdapter getPageBarAdapter() {
        return mPageBarAdapter;
    }

    public PageBtnBar setPageBarAdapter(PageBtnBarAdapter pageBarAdapter) {
        this.mPageBarAdapter = pageBarAdapter;
        return this;
    }

    public class NoAdapterException extends RuntimeException{
        public NoAdapterException(String message) {
            super(message);
        }
    }

    /**
     * 计算按下下一屏按钮的时候第一个和最后一个应该显示的按钮的index
     */
    private void toNextScreen(){
        int maxBtnIndex = mPageBarAdapter.getPageBtnCount() - 1;//按钮index最大值
        int maxBtnCountPerScreen = mPageBarAdapter.getMaxBtnCountPerScreen();//每屏按钮的最多个数
        if (maxBtnIndex < 0 || maxBtnCountPerScreen <= 0){
            //如果每屏最多个数为0或最大按钮index小于0,即认定为不展示任何按钮,直接将firstBtnIndex和lastBtnIndex都置为-1.
            //然后结束方法
            firstBtnIndex = -1;
            lastBtnIndex = -1;
            return;
        }
        //根据上次的lastBtnIndex算本次firstBtnIndex.如果last为-1,直接算得first就为0,因此不用考虑last的下边界情况
        firstBtnIndex = lastBtnIndex + 1;
        //根据本次的firstBtnIndex算本次的lastBtnIndex.
        lastBtnIndex = firstBtnIndex + maxBtnCountPerScreen - 1;
        //进行lastBtnIndex的上边界校正,再根据校正后的last校正first的值
        if (lastBtnIndex > maxBtnIndex){
            //如果lastBtnIndex超上界,校正
            lastBtnIndex = maxBtnIndex;
            //根据新的last校正first
            firstBtnIndex = lastBtnIndex - maxBtnCountPerScreen + 1;
            //校正first后需要再次校正first的校正值是否超下界,再次校正.
            if (firstBtnIndex < 0){
                firstBtnIndex = 0;
            }
        }
    }
    /**
     * 计算按下上一屏按钮的时候第一个和最后一个应该显示的按钮的index
     */
    private void toLastScreen(){
        int maxBtnIndex = mPageBarAdapter.getPageBtnCount() - 1; //按钮index最大值
        int maxBtnCountPerScreen = mPageBarAdapter.getMaxBtnCountPerScreen();//每屏按钮的最多个数
        if (maxBtnIndex < 0 || maxBtnCountPerScreen <= 0){
            //如果每屏最多个数为0或最大按钮index小于0,即认定为不展示任何按钮,直接将firstBtnIndex和lastBtnIndex都置为-1.
            //然后结束方法
            firstBtnIndex = -1;
            lastBtnIndex = -1;
            return;
        }
        //先校正上一次firstBtnIndex的下界,如果上一次的first为-1,则默认定义first为0.
        //由于之前考虑过maxBtnIndex和maxBtnCountPerScreen为0的情况,所以保证first可以为0.可以放心.
        if (firstBtnIndex == -1){
            firstBtnIndex = 0;
        }
        //根据上一次的firstBtnIndex确定本次的lastBtnIndex
        lastBtnIndex = firstBtnIndex - 1;
        //如果本次的lastBtnIndex超下界,则直接规定本次的firstBtnIndex为0,然后根据first确定新的lastBtnIndex
        if (lastBtnIndex < 0){
            firstBtnIndex = 0;
            lastBtnIndex = firstBtnIndex + maxBtnCountPerScreen - 1;
            //重新确定的lastBtnIndex需要再次检查是否超上界
            if (lastBtnIndex > maxBtnIndex){
                lastBtnIndex = maxBtnIndex;
            }
        }
        else {
            //如果本次的lastBtnIndex未超下界,则判定其是否超上界,进行校正
            if (lastBtnIndex > maxBtnIndex){
                lastBtnIndex = maxBtnIndex;
            }
            //根据校正后的lastBtnIndex确定本次的firstBtnIndex
            firstBtnIndex = lastBtnIndex - maxBtnCountPerScreen + 1;
            //再次确定本次的firstBtnIndex是否超下界.
            if (firstBtnIndex < 0){
                firstBtnIndex = 0;
            }
        }
    }
}

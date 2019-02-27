package com.frank.etude.pageable;

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

/**
 * 本类是一个页码按钮栏的实现.
 * 和PageBtnBarAdapter配合,可以自定义设置每屏展示的按钮个数,并且自动生成向前翻页和向后翻页按钮,用来切换当前展示的页码按钮.
 * 使用方法为设置adapter后调用refreshPageBar方法刷新按钮栏.
 */
public class PageBtnBarV2 extends LinearLayout{
    private PageBtnBarAdapterV2 mPageBarAdapter;
    private int currentSelectPageIndex = -1;
    private int firstBtnIndex = -1;
    private int lastBtnIndex = -1;

    //是否允许重复点击某个page按钮.设为false的时候不允许重复点击.默认为false.即不允许重复点击.
    //这个值主要跟currentSelectPageIndex有关,在触发adapter的OnPageClick方法之前,做一个判断,如果currentSelectPageIndex与点击的PageBtn的index一样,就不触发onPageClick,反之则触发.
    private boolean repeatedClickEnable = false;

    public PageBtnBarV2(Context context) {
        this(context , null);
    }

    public PageBtnBarV2(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs , 0);

    }

    public PageBtnBarV2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setGravity(Gravity.CENTER);
    }

    /**
     * 点击某个pageBtn按钮.相当于调用了selectPageBtn(pageIndex , true).
     * 如果这个pageBtn不在当前这一屏,则切到有这个pageBtn的这一屏,然后再点击
     *
     * 注意:如果给出的pageIndex超限(超出adapter能提供的pageCount或小于0),则会触发自动校正逻辑,校正选中的按钮index和当前显示的按钮至合法范围,
     * 相当于调用了refreshPageBar(false),并且有可能会触发自动校正的自动点击
     * 如果触发了自动校正,则本次传入的要点击的按钮的pageIndex即无效(因为本来需要点击的按钮就不存在)
     * @param pageIndex 要点击的pageBtn按钮的index
     */
    public void clickPageBtn(int pageIndex){
        selectPageBtn(pageIndex , true);
    }

    /**
     * 选择指定的页码按钮,并且提供一个needClick参数用来确定选中后是否需要同时点击那个选中的按钮
     * 如果这个pageBtn不在当前这一屏,则切到有这个pageBtn的这一屏,然后再选中
     *
     * 注意:如果给出的pageIndex超限(超出adapter能提供的pageCount或小于0),则会触发自动校正逻辑,校正选中的按钮index和当前显示的按钮至合法范围,
     * 相当于调用了refreshPageBar(false),并且有可能会触发自动校正的自动点击
     * 如果触发了自动校正,则本次传入的要选中的按钮的pageIndex即无效(因为本来需要选中的按钮就不存在)
     * @param pageIndex 要选中的pageBtn按钮的index
     */
    public void selectPageBtn(int pageIndex , boolean needClick){
        if (mPageBarAdapter == null){
            throw new NoAdapterException("本PageBtnBar没有绑定adapter!调用refreshPageBar之前请先绑定adapter!");
        }
        int lastSelectPageIndex = currentSelectPageIndex;
        currentSelectPageIndex = pageIndex;
        if (currentSelectPageIndex < firstBtnIndex || currentSelectPageIndex > lastBtnIndex){
            firstBtnIndex = currentSelectPageIndex;
            lastBtnIndex = currentSelectPageIndex;
        }
        refreshPageBar(false);
        if (needClick && (currentSelectPageIndex == pageIndex)){
            boolean needLastScreenBtn = firstBtnIndex != 0 ? true : false;//是否需要显示向前一页的按钮
            View clickBtn = getChildAt(currentSelectPageIndex - firstBtnIndex + (needLastScreenBtn?1:0));
            int index = (int) clickBtn.getTag();
            mPageBarAdapter.onPageBtnClick(clickBtn , index , (String)((TextView) clickBtn).getText() , lastSelectPageIndex);
        }
    }


    /**
     * 获取当前正在被选中的按钮的index值
     * @return
     */
    public int getCurrentSelectPageIndex() {
        return currentSelectPageIndex;
    }

    /**
     * 获取是否允许重复点击某个按钮,默认为false,即不允许重复点击.
     * @return true为允许重复点击,false为不允许重复点击
     */
    public boolean isRepeatedClickEnable() {
        return repeatedClickEnable;
    }

    /**
     * 设置是否允许重复点击某个按钮,例如重复点击1这个page按钮,如果设置了不允许重复点击,
     * 只有第一次会触发adapter的onPageBtnClick,后续再次点击就不触发了.
     * @param repeatedClickEnable true为允许重复点击,false为不允许重复点击
     */
    public void setRepeatedClickEnable(boolean repeatedClickEnable) {
        this.repeatedClickEnable = repeatedClickEnable;
    }

    /**
     * 刷新按钮栏,默认允许自动点击
     */
    public void refreshPageBar() {
        refreshPageBar(false);
    }

    /**
     * 按照之前的select的按钮和adapter中的数据刷新pagebar.
     * 本方法的处理了几个特殊情况.
     * 1.如果没有按钮可供显示,则隐藏pagebar,并触发onNoPageToShow
     * 2.如果select的按钮已经不存在,则自动选择最后一个按钮,并触发click,并且切到最后一屏按钮
     * 3.如果没有select的按钮并且有第0个按钮,则自动选择第0个按钮并且触发click,并且切到第一屏按钮.
     * 4.对于按钮个数变少的当前屏按钮有的变为不存在时,自动调整到最后一屏
     * 5.对于按钮个数变多导致当前屏按钮数量不足MaxBtnCountPerScreen个时自动调整当前屏按钮数量至最多.
     * @param disableAutoClick 此参数为true的时候素有自动触发的click全部无效.为false的时候click才有效.
     *                         默认refreshPageBar方法此参数为false.
     */
    public void refreshPageBar(boolean disableAutoClick){
        //没有设置adapter时刷新直接抛出异常
        if (mPageBarAdapter == null){
            throw new NoAdapterException("本PageBtnBar没有绑定adapter!调用refreshPageBar之前请先绑定adapter!");
        }
        int lastSelectPageIndex = currentSelectPageIndex;
        int totalPageBtnCount = mPageBarAdapter.getPageBtnCount();//总按钮个数
        //如果总按钮个数为0,或当前每屏可显示按钮数为0,则移除所有已经存在的按钮,初始化所有变量,调用onNoPageToShow,然后直接结束刷新.
        if (totalPageBtnCount <= 0 || mPageBarAdapter.getMaxBtnCountPerScreen() <= 0){
            removeAllViews();
            currentSelectPageIndex = -1;
            firstBtnIndex = -1;
            lastBtnIndex = -1;
            mPageBarAdapter.onNoPageToShow();
            return ;
        }

        boolean needPerformClick = false;//刷新完成后是否需要自动触发一次当前选中按钮的click事件
        //当前选中index等于-1超限
        if (currentSelectPageIndex == -1){
            currentSelectPageIndex = 0;
            //当前选中index超限时需要调整选中index,因此需要计划一次自动点击
            needPerformClick = true;
            //由于自动更改了选中的index,所以需要自动切换到第一屏
            firstBtnIndex = -1;
            lastBtnIndex = -1;
            toNextScreen();
        }
        //当前选中index大于所有按钮的数量超限
        else if(currentSelectPageIndex > totalPageBtnCount - 1){
            currentSelectPageIndex = totalPageBtnCount - 1;
            //当前选中index超限时需要调整选中index,因此需要计划一次自动点击
            needPerformClick = true;
            //由于自动更改了选中的index,所以需要自动切换到最后一屏
            firstBtnIndex = currentSelectPageIndex + 1;
            toLastScreen();
        }
        //当前选中index没有超限
        else {
            //firstBtnIndex和last超下限时,切到第一屏
            if (firstBtnIndex == -1 || lastBtnIndex == -1){
                toNextScreen();
            }
            //firstBtnIndex和last超上限时,切到最后一屏
            else if (lastBtnIndex > (totalPageBtnCount - 1)
                    || firstBtnIndex > (totalPageBtnCount - 1)){
                firstBtnIndex = totalPageBtnCount;
                toLastScreen();
            }
            else {
                //在firstBtnIndex和lastBtnIndex都没有超限时还需要考虑刷新前按钮个数较少,刷新后按钮个数变多了,这时lastBtnIndex可能太小
                // ,不能满足一屏显示按钮数量最多是MaxBtnCountPerScreen的要求,需要再对lastBtnIndex进行一次校正.
                if ((lastBtnIndex - firstBtnIndex + 1) < mPageBarAdapter.getMaxBtnCountPerScreen()){
                    lastBtnIndex = firstBtnIndex + mPageBarAdapter.getMaxBtnCountPerScreen() - 1;
                    if (lastBtnIndex >= mPageBarAdapter.getPageBtnCount()){
                        lastBtnIndex = mPageBarAdapter.getPageBtnCount() - 1;
                    }
                }
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
                            //普通按钮点击逻辑
                            if (currentSelectPageIndex == index && !repeatedClickEnable){
                                //当不支持同一个按钮重复点击时,发现现在点击的按钮与上一个一致,就忽略点击事件.
                                return;
                            }
                            //调用PageBarAdapter中用户的onCLick逻辑,并且更新currentSelectPageIndex为当前点击按钮
                            //并刷新bar
                            int lastSelectPageIndex = currentSelectPageIndex;
                            currentSelectPageIndex = index;
                            refreshPageBar();
                            mPageBarAdapter.onPageBtnClick(v , index , (String)((TextView) v).getText() , lastSelectPageIndex);
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
        while (getChildCount() > needBtnCount){
            removeViewAt(needBtnCount);
        }
        //如果需要自动点击当前选中按钮,则根据是否存在向前翻页按钮,计算出当前选中按钮的index,从而获得该按钮,然后模拟点击
        if (needPerformClick && !disableAutoClick){
            View clickBtn = getChildAt(currentSelectPageIndex - firstBtnIndex + (needLastScreenBtn?1:0));
            int index = (int) clickBtn.getTag();
            mPageBarAdapter.onPageBtnClick(clickBtn , index , (String)((TextView) clickBtn).getText() , lastSelectPageIndex);
        }
    }

    /**
     * 获取当前设置的adapter,未设置则为null.
     * @return
     */
    public PageBtnBarAdapterV2 getPageBarAdapter() {
        return mPageBarAdapter;
    }

    /**
     * 设置pageBarAdapter,本类需要设置adapter才能使用,如果不设置adapter就提前调用refreshPageBar方法,则会抛出exception.
     *
     */
    public PageBtnBarV2 setPageBarAdapter(PageBtnBarAdapterV2 pageBarAdapter) {
        this.mPageBarAdapter = pageBarAdapter;
        return this;
    }

    /**
     * 未设置adapter的exception
     */
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

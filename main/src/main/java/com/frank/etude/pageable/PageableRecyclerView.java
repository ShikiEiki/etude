package com.frank.etude.pageable;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by FH on 2017/10/13.
 * 自动分页的recyclerview
 * 使用说明:
 * 本控件是一个实现了自动分页的recyclerview,会自动在主要负责显示的recyclerview下方添加页码栏.
 * 使用流程:
 * 必须要调用的方法:
 * 1.设定每页最多展示的条数.setMaxItemNumInOnePage()
 * 2.设定LayoutManager setLayoutManager()
 * 3.设定Adapter setAdapter()
 * 4.通知本控件数据已更新,刷新界面 notifyDataSetChanged()
 * 根据需求选择性调用的方法:
 * 1.设定分割线 addItemDecoration()
 * 2.设定onItemClickListnener addOnItemTouchListener()
 * 3.获取真实的recyclerview getRealRcyView()
 *
 * 说明:
 * 本控件是对recyclerview的一个封装,实现了自动对recyclerview中的数据根据需要的每页条数进行分页,并且自动添加页码条和页码按钮,实现点击页码按钮对recyclerview中的数据进行切换显示的逻辑.
 *
 * 本控件是一个viewGroup,其中包含一个真正的recyclerview,请注意这一点,本控件虽然名字叫PageableRecyclerView但是并不是一个recyclerview而是一个封装了recyclerview和页码条的ViewGroup.
 *
 * 本控件把recyclerview的常用api进行了暴露,如addOnItemTouchListner,setAdapter等,一般的使用可以直接把本控件当做一个正常的recyclerview使用.使用方法与普通的recyclerview类似.
 * 只有少部分情况需要注意,比如通知本控件数据更新需要刷新的时候不能像标准做法一样调用recyclerview.getAdapter.notifyDataSetChanged()
 * 而是需要直接调用本控件提供的PageableRecyclerView.notifyDataSetChanged().
 * 原因是使用本控件提供的setAdapter设定adapter的时候,事实上你设定的adapter被本控件内部的另外一个adapter代理了
 * ,也就是说,你设定的adapter并不是真正起作用的adapter,真正的adapter是本控件自动生成的另外一个adpater.
 * 这样的做法能够保证每页展示的数量不会超过设定的maxItemNumInOnePage.因此如果直接调用recyclerview.getAdapter.notifyDataSetChanged()事实上什么都不会发生.
 *
 * 由于本控件暴露的原recyclerview的api有限,为了防止后续有新的需求需要使用未暴露的api,本控件也提供了getRealRcyView方法来获取本控件中实际的recyclerview.
 */

public class PageableRecyclerView extends LinearLayout {
    RecyclerView recyclerView;
    PageBtnBar pageBtnBar;

    private int maxItemNumInOnePage = 0;
    private Adapter customAdapter;


    public PageableRecyclerView(Context context) {
        this(context, null);
    }

    public PageableRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageableRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        recyclerView = new RecyclerView(getContext());
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return customAdapter.onCreateViewHolder(parent , viewType);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                position = position + pageBtnBar.getCurrentSelectPageIndex() * maxItemNumInOnePage;
                customAdapter.onBindViewHolder(holder , position);
            }

            @Override
            public int getItemCount() {
                if (customAdapter == null || pageBtnBar.getCurrentSelectPageIndex() == -1){
                    return 0;
                }
                int temp = customAdapter.getItemCount() - pageBtnBar.getCurrentSelectPageIndex() * maxItemNumInOnePage;
                if (temp > maxItemNumInOnePage){
                    return maxItemNumInOnePage;
                }
                return temp;
            }
        });
        LayoutParams rcvParam = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0 , 1);
        addView(recyclerView, rcvParam);
        pageBtnBar = new PageBtnBar(getContext());
        pageBtnBar.setPageBarAdapter(new PageBtnBarAdapter(getContext()) {
            @Override
            public int getPageBtnCount() {
                if (customAdapter == null || maxItemNumInOnePage == 0){
                    return 0;
                }
                return (customAdapter.getItemCount() + maxItemNumInOnePage - 1) / maxItemNumInOnePage;
            }

            @Override
            public void onPageBtnClick(View view, int i, String s) {
                recyclerView.getAdapter().notifyDataSetChanged();
                if (customAdapter != null){
                    customAdapter.onSelectPageChanged(i);
                }
            }

            @Override
            public void onNoPageToShow() {

            }
        });
        pageBtnBar.refreshPageBar();
        LayoutParams pageBtnContainerParam = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(pageBtnBar , pageBtnContainerParam);
    }

    public PageableRecyclerView setMaxItemNumInOnePage(int maxItemNumInOnePage) {
        this.maxItemNumInOnePage = maxItemNumInOnePage;
        return this;
    }

    public int getMaxItemNumInOnePage(){
        return maxItemNumInOnePage;
    }

    public PageableRecyclerView setLayoutManager(RecyclerView.LayoutManager manager) {
        recyclerView.setLayoutManager(manager);
        return this;
    }

    public PageableRecyclerView setAdapter(Adapter adapter){
        customAdapter = adapter;
        customAdapter.setPageableRecyclerView(this);
        notifyDataSetChanged();
        return this;
    }

    public void notifyDataSetChanged() {
        if (customAdapter == null || recyclerView == null){
            return;
        }
        recyclerView.getAdapter().notifyDataSetChanged();
        pageBtnBar.refreshPageBar();
    }

    public void addItemDecoration(RecyclerView.ItemDecoration decor) {
        recyclerView.addItemDecoration(decor);
    }

    public void addOnItemTouchListener(RecyclerView.OnItemTouchListener listener) {
        recyclerView.addOnItemTouchListener(listener);
    }

//    public void setCurrentPage(int pageIndex){
//        pageBtnBar.setCurrentSelectPageIndex(pageIndex);
//        notifyDataSetChanged();
//    }

    public int getCurrentSelectPageIndex(){
        return pageBtnBar.getCurrentSelectPageIndex();
    }

    public RecyclerView getRealRcyView() {
        return recyclerView;
    }

    public static abstract class Adapter<VH extends RecyclerView.ViewHolder> {
        private PageableRecyclerView pageableRecyclerView;

        public Adapter setPageableRecyclerView(PageableRecyclerView pageableRecyclerView) {
            this.pageableRecyclerView = pageableRecyclerView;
            return this;
        }

        public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);
        public abstract void onBindViewHolder(VH holder, int position);
        public abstract int getItemCount();
        public void onSelectPageChanged(int changedPageIndex) {

        }
    }
}

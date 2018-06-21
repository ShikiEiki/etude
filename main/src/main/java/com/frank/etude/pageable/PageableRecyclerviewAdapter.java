package com.frank.etude.pageable;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by FH on 2017/10/13.
 */

public class PageableRecyclerviewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>{
    RecyclerView recyclerView;

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}

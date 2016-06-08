package com.wang.dribbble.module.shots.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.wang.dribbble.Injection;
import com.wang.dribbble.R;
import com.wang.dribbble.data.model.Shots;
import com.wang.dribbble.module.base.BaseFragment;
import com.wang.dribbble.module.shots.detail.ShotsDetailActivity;
import com.wang.dribbble.utils.GridMarginDecoration;
import com.wang.dribbble.utils.ImageSize;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Jack Wang on 2016/6/2.
 */
public class ListShotsFragment extends BaseFragment implements ListShotsContract.View {

    @BindView(R.id.recyclerView)
    EasyRecyclerView recyclerView;

    ListShotsAdapter mAdapter;

    ListShotsPresenter mPresenter;
    @BindView(R.id.pull_to_refresh)
    SwipeRefreshLayout pullToRefresh;

    private int filterId;

    private int pages=1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter=new ListShotsAdapter(getActivity(),new ArrayList<Shots>(0));
        mPresenter = new ListShotsPresenter(Injection.provideTasksRepository(getActivity().getApplicationContext()), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_shots, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        pullToRefresh.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.loadListShots(true,filterId);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments()!=null){
            filterId=getArguments().getInt("filterId",0);
        }
        if (savedInstanceState!=null){
            pages=savedInstanceState.getInt("pages");
        }
        mPresenter.loadListShots(false,filterId);
    }

    private void setupRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),2);
        recyclerView.setLayoutManager(gridLayoutManager);
        gridLayoutManager.setSpanSizeLookup(mAdapter.obtainGridSpanSizeLookUp(2));
        recyclerView.addItemDecoration(new GridMarginDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_item_spacing)));
        recyclerView.getRecyclerView().setHasFixedSize(true);
        mAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Shots item = mAdapter.getItem(position);
                Intent intent=new Intent(getActivity(), ShotsDetailActivity.class);
                intent.putExtra("shots_id",item.id);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(mAdapter);
        mAdapter.setMore(R.layout.view_loading, new RecyclerArrayAdapter.OnLoadMoreListener() {

            @Override
            public void onLoadMore() {
                pages++;
                System.out.println("page="+pages);
                mPresenter.loadMoreShots(pages,filterId);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("pages",pages);
    }

    @Override
    public void setLoadingIndicator(final boolean active) {
        if (getView() == null) {
            return;
        }
        pullToRefresh.post(new Runnable() {
            @Override
            public void run() {
                pullToRefresh.setRefreshing(active);
            }
        });
    }

    @Override
    public void showListShots(List<Shots> shotsList) {
        mAdapter.addAll(shotsList);
    }

    @Override
    public void showLoadFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    public static Fragment newInstance(int filterId) {
        ListShotsFragment fragment=new ListShotsFragment();
        Bundle bundle=new Bundle();
        bundle.putInt("filterId",filterId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static class ListShotsAdapter extends RecyclerArrayAdapter<Shots> {


        public ListShotsAdapter(Context context, List<Shots> objects) {
            super(context, objects);
        }

        @Override
        public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            return new ListShotsViewHolder(inflater.inflate(R.layout.item_shots, parent, false));
        }

        public class ListShotsViewHolder extends BaseViewHolder<Shots>{

            @BindView(R.id.photo)
            public ImageView photo;
            @BindView(R.id.gifFlag)
            public ImageView gifFlag;
            @BindView(R.id.title)
            public TextView title;

            public ListShotsViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this,itemView);
            }

            @Override
            public void setData(Shots item) {
                Context context=photo.getContext();
                title.setText(item.title);
                gifFlag.setVisibility(item.animated?View.VISIBLE:View.GONE);
                Glide.with(context)
                        .load(item.images.normal)
                        .placeholder(R.color.placeholder)
                        .override(ImageSize.NORMAL[0], ImageSize.NORMAL[1])
                        .into(photo);
            }
        }
    }



}
package com.example.doubanmovie.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.doubanmovie.C;
import com.example.doubanmovie.R;
import com.example.doubanmovie.detail.DetailActivity;
import com.example.doubanmovie.model.Movie;
import com.example.doubanmovie.model.SubjectsBean;
import com.example.doubanmovie.net.OkHttpManager;
import com.example.doubanmovie.preview.WatchIMGActivity;
import com.google.gson.Gson;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by zhoujunyu on 2019/3/26.
 */
public class MovieFragment extends Fragment {

    private static final String TYPE = "type";
    private static int PAGE = 0;
    private static int PAGE_COUNT = 20;
    private static int FIRST_AD_TIMES = 1;
    //原有适配器
    //private MovieAdapter mMovieAdapter;
    private BaseMovieAdapter mBaseMovieAdapter;
    private Movie mMovie;
    private List<SubjectsBean> mSubjectsList = new ArrayList<>();
    // private int mLayoutType;
    private RecyclerView mRecyclerView;
    private SmartRefreshLayout mSmartRefreshLayout;
    private Spinner mSpinner;
    private String mCity = "北京";
    private Banner mBanner;

    //fragment之间传数接口
    //public TransData mTransData;


    public MovieFragment() {

    }

    //工厂设计模式
    public static MovieFragment mainFragment() {
        MovieFragment fragment = new MovieFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //只使用fragment实现翻页功能；通过判断点击事件返回的类型，调用不同的fragment
//        if( getArguments() != null){
//            mLayoutType = getArguments().getInt(TYPE);
//        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_movie_one, container, false);
        mRecyclerView = view.findViewById(R.id.pageone_recycler_view);
        mSmartRefreshLayout = view.findViewById(R.id.smart_refresh);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        //加载刷新界面
        setSmartRefreshLayout();
        mSmartRefreshLayout.autoRefresh();


        //载入适配器，以便拉取本地数据

        //使用原有适配器，只有item数据，无头部和尾部
        //mMovieAdapter = new MovieAdapter(mSubjectsList, getActivity());


        //使用带头部的适配器
        mBaseMovieAdapter = new BaseMovieAdapter(R.layout.item_movie, mSubjectsList, getActivity());
        mRecyclerView.setAdapter(mBaseMovieAdapter);

        //加载适配器头部
        loadHeadView();

        setRecylcerviewListener();
        refreshCity();

        return view;
    }

    private void loadHeadView() {

        View viewBanner = getLayoutInflater().inflate(R.layout.ad_banner, null);
        mBanner = viewBanner.findViewById(R.id.banner);
        //设置banner样式
        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE);
        //设置图片加载器
        mBanner.setImageLoader(new BannerImageLoader());
        //设置banner动画效果
        mBanner.setBannerAnimation(Transformer.Stack);
        //设置自动轮播，默认为true
        mBanner.isAutoPlay(true);
        //设置轮播时间
        mBanner.setDelayTime(2000);
        //设置指示器位置（当banner模式中有指示器时）
        mBanner.setIndicatorGravity(BannerConfig.CENTER);

        mBaseMovieAdapter.addHeaderView(viewBanner);
    }


    //将当前fragment与activity绑定，以便将fragment的数据传至activity
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//
//        try{
//            mTransData = (TransData)context;
//        }catch (Exception e){
//            throw new ClassCastException(context.toString() + "must implement OnArticleSelectedListener");
//        }
//    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private void refreshCity() {

        mSpinner = ((MainActivity) getActivity()).getSpinner();

        String[] city = getResources().getStringArray(R.array.city);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, city);//创建Arrayadapter适配器

        mSpinner.setAdapter(adapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCity = mSpinner.getItemAtPosition(position).toString();
                Toast.makeText(getActivity(), "已切换至" + mCity, Toast.LENGTH_SHORT).show();
                setSmartRefreshLayout();
                mSmartRefreshLayout.autoRefresh();
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadNew() {
        PAGE = 0;
        loadPage(PAGE);
    }

    private void loadMore() {
        PAGE = PAGE + 1;
        loadPage(PAGE);
    }

    private void loadPage(final int page) {
        Map<String, String> map = new HashMap<>();
        map.put("apikey", "0b2bdeda43b5688921839c8ecb20399b");
        map.put("city", mCity);
        map.put("start", String.valueOf(page * PAGE_COUNT));

        //每次渲染的item数据
        map.put("count", String.valueOf(PAGE_COUNT));

        map.put("client", "somemessage");
        map.put("udid", "dddddddddddddddddddddd");

        OkHttpManager.getInstance().get(C.DOUBAN_URL, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("zjy", "singleton is failed!");
                finishRefresh();//完成刷新
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                mMovie = new Gson().fromJson(json, Movie.class);

                //判断界面item数大于总item数时，无新数据；扔回主线程
                if (mMovie.getTotal() < PAGE * PAGE_COUNT) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "没有更多数据了", Toast.LENGTH_LONG).show();
                            finishRefresh();
                        }
                    });

                } else {
                    if (mMovie != null && mMovie.getSubjects() != null && !mMovie.getSubjects().isEmpty()) {

                        if (PAGE == 0) {
                            mSubjectsList.clear();

                            //更新数据
                            mSubjectsList.addAll(mMovie.getSubjects());
                        } else {
                            mSubjectsList.addAll(mMovie.getSubjects());
                        }

                        String[] imgURL = new String[5];
                        String[] movieName = new String[5];
                        for (int i = 0; i < 5; i++) {
                            Log.d("zjy", "onResponse: subjects is " + mMovie.getSubjects().get(i).getImages().getSmall());
                            imgURL[i] = mMovie.getSubjects().get(i).getImages().getSmall();
                            movieName[i] = mMovie.getSubjects().get(i).getTitle();
                        }

                        //等数据解析完之后，将数据通过接口传回activity
                        //mTransData.moviefgToBannerfg(imgURL,movieName);

                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //mMovieAdapter.notifyDataSetChanged();
                            mBaseMovieAdapter.notifyDataSetChanged();

                            //mBaseMovieAdapter.addHeaderView(LinearLayout.inflate(getActivity(),R.layout.ad_banner,null));

                            //loadBanner_slide();
                            if (FIRST_AD_TIMES == 1) {
                                loadBanner_youth();
                                FIRST_AD_TIMES = 0;
                            }
                            finishRefresh();
                        }
                    });

                }
            }
        });
    }

    //使用slider + indicator库
    private void loadBanner_slide() {
//        View view = getLayoutInflater().inflate(R.layout.ad_banner, null);
//        SliderLayout mSliderLayout;
//        PagerIndicator mIndicator;
//
//        mSliderLayout = view.findViewById(R.id.ad_banner_id);
//        mIndicator = view.findViewById(R.id.indicator_id);
//
//        //获取图片地址
//        String[] imgURL = new String[5];
//        String[] movieName = new String[5];
//
//        for (int i = 0; i < 5; i++) {
//            imgURL[i] = mSubjectsList.get(i).getImages().getMedium();
//            movieName[i] = mSubjectsList.get(i).getTitle();
//        }
//
//        Log.d("zjy", "onCreateView: img URL is " + imgURL);
//
//        for (int i = 0; i < 5; i++) {
//            TextSliderView bannerview = new TextSliderView(getActivity());
//            bannerview.image(imgURL[i]).description("推荐电影： " + movieName[i])
//                    .setScaleType(BaseSliderView.ScaleType.CenterCrop);
//
//
//            mSliderLayout.addSlider(bannerview);
//        }
//
//
//        mSliderLayout.setCustomAnimation(new DescriptionAnimation());
//        mSliderLayout.setPresetTransformer(SliderLayout.Transformer.Accordion);
//        mSliderLayout.setDuration(200);
//        mSliderLayout.setCustomIndicator(mIndicator);
//
//        mBaseMovieAdapter.addHeaderView(view);


    }

    //使用banner_youth库
    private void loadBanner_youth() {

        //数据集合
        List<String> imgURL = new ArrayList<>();
        List<String> movieName = new ArrayList<>();

        if (!mSubjectsList.isEmpty()) {
            for (SubjectsBean bean : mSubjectsList) {
                if (imgURL.size() <= 5) {
                    imgURL.add(bean.getImages().getMedium());
                    movieName.add(bean.getTitle());
                }
            }
        }


        //设置图片集合
        mBanner.setImages(imgURL);
        //设置标题集合（当banner样式有显示title时）
        mBanner.setBannerTitles(movieName);
        //banner设置方法全部调用完毕时最后调用
        mBanner.start();
    }

    private void finishRefresh() {
        if (PAGE == 0) {
            mSmartRefreshLayout.finishRefresh();
        } else {
            mSmartRefreshLayout.finishLoadMore();
        }
    }

    private void setSmartRefreshLayout() {
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                loadNew();
            }
        });
        mSmartRefreshLayout.setOnLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                loadMore();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                loadNew();
            }
        });
    }

    private void setRecylcerviewListener() {
//        mMovieAdapter.setOnItemClickListen(new IRecyclerviewClick() {
//        @Override
//            public void onMovieClick(View view, SubjectsBean subjectsBean) {
//                DetailActivity.start(getActivity(), subjectsBean.getId());
//            }
//
//            @Override
//            public void onCoverClick(View view, SubjectsBean subjectsBean) {
//                // TODO: 2019/3/30 仿照上面写法自己写一个
//                WatchIMGActivity.start(getActivity(),subjectsBean.getImages().getMedium());
//            }
//        });


        mBaseMovieAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                DetailActivity.start(getActivity(), mSubjectsList.get(position).getId());
            }
        });

        mBaseMovieAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                WatchIMGActivity.start(getActivity(), mSubjectsList.get(position).getImages().getMedium());
            }
        });

    }


}

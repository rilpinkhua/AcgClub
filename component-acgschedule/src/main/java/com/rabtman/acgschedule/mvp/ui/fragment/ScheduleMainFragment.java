package com.rabtman.acgschedule.mvp.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.BindView;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener;
import com.rabtman.acgschedule.R;
import com.rabtman.acgschedule.R2;
import com.rabtman.acgschedule.base.constant.HtmlConstant;
import com.rabtman.acgschedule.base.constant.IntentConstant;
import com.rabtman.acgschedule.di.component.DaggerScheduleMainComponent;
import com.rabtman.acgschedule.di.module.ScheduleMainModule;
import com.rabtman.acgschedule.mvp.contract.ScheduleMainContract.View;
import com.rabtman.acgschedule.mvp.model.jsoup.DilidiliInfo;
import com.rabtman.acgschedule.mvp.model.jsoup.DilidiliInfo.ScheduleRecent;
import com.rabtman.acgschedule.mvp.model.jsoup.DilidiliInfo.ScheduleRecommand;
import com.rabtman.acgschedule.mvp.model.jsoup.ScheduleWeek;
import com.rabtman.acgschedule.mvp.presenter.ScheduleMainPresenter;
import com.rabtman.acgschedule.mvp.ui.activity.ScheduleDetailActivity;
import com.rabtman.acgschedule.mvp.ui.activity.ScheduleNewActivity;
import com.rabtman.acgschedule.mvp.ui.activity.ScheduleOtherActivity;
import com.rabtman.acgschedule.mvp.ui.activity.ScheduleTimeActivity;
import com.rabtman.acgschedule.mvp.ui.activity.ScheduleVideoActivity;
import com.rabtman.acgschedule.mvp.ui.adapter.ScheduleBannerViewHolder;
import com.rabtman.acgschedule.mvp.ui.adapter.ScheduleRecentAdapter;
import com.rabtman.acgschedule.mvp.ui.adapter.ScheduleRecommandAdapter;
import com.rabtman.common.base.BaseFragment;
import com.rabtman.common.di.component.AppComponent;
import com.rabtman.router.RouterConstants;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhouwei.mzbanner.MZBannerView;
import com.zhouwei.mzbanner.MZBannerView.BannerPageClickListener;
import com.zhouwei.mzbanner.holder.MZHolderCreator;
import com.zhouwei.mzbanner.holder.MZViewHolder;
import java.util.ArrayList;

/**
 * @author Rabtman
 */
@Route(path = RouterConstants.PATH_SCHEDULE_MAIN)
public class ScheduleMainFragment extends BaseFragment<ScheduleMainPresenter> implements
    View {

  @BindView(R2.id.swipe_refresh_schedule_main)
  SwipeRefreshLayout swipeRefresh;
  @BindView(R2.id.layout_schedule_main)
  LinearLayout layoutScheduleMain;
  @BindView(R2.id.scroll_schedule)
  NestedScrollView scrollScheduleView;
  @BindView(R2.id.banner_schedule)
  MZBannerView bannerSchedule;
  @BindView(R2.id.tv_schedule_time)
  TextView tvScheduleTime;
  @BindView(R2.id.tv_schedule_new)
  TextView tvScheduleNew;
  @BindView(R2.id.layout_schedule_recommand)
  RelativeLayout layoutScheduleRecommand;
  @BindView(R2.id.rcv_schedule_recommand)
  RecyclerView rcvScheduleRecommand;
  @BindView(R2.id.rcv_schedule_recent)
  RecyclerView rcvScheduleRecent;
  private RxPermissions rxPermissions;

  @Override
  protected void setupFragmentComponent(AppComponent appComponent) {
    DaggerScheduleMainComponent.builder()
        .appComponent(appComponent)
        .scheduleMainModule(new ScheduleMainModule(this))
        .build()
        .inject(this);
  }

  @Override
  protected int getLayoutId() {
    return R.layout.acgschedule_fragment_schedule_main;
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  protected void initData() {
    swipeRefresh.setOnRefreshListener(new OnRefreshListener() {
      @Override
      public void onRefresh() {
        bannerSchedule.pause();
        mPresenter.getDilidiliInfo();
      }
    });
    setSwipeRefreshLayout(swipeRefresh);
    rxPermissions = new RxPermissions(getActivity());
    mPresenter.getDilidiliInfo();
  }

  @Override
  public void onResume() {
    super.onResume();
    if (bannerSchedule.getVisibility() == android.view.View.VISIBLE) {
      bannerSchedule.start();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (bannerSchedule.getVisibility() == android.view.View.VISIBLE) {
      bannerSchedule.pause();
    }
  }

  @Override
  protected boolean useLoadSir() {
    return true;
  }

  @Override
  protected void onPageRetry(android.view.View v) {
    mPresenter.getDilidiliInfo();
  }

  @Override
  public void showDilidiliInfo(final DilidiliInfo dilidiliInfo) {
    //放送时间表
    tvScheduleTime.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(android.view.View v) {
        Intent newIntent = new Intent(getContext(), ScheduleTimeActivity.class);
        newIntent.putParcelableArrayListExtra(IntentConstant.SCHEDULE_WEEK,
            (ArrayList<ScheduleWeek>) dilidiliInfo.getScheduleWeek());
        startActivity(newIntent);
      }
    });
    //本季新番
    tvScheduleNew.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(android.view.View v) {
        Intent newIntent = new Intent(getContext(), ScheduleNewActivity.class);
        //newIntent.putExtra(IntentConstant.SCHEDULE_NEW_URL, dilidiliInfo.getScheduleNewLink());
        startActivity(newIntent);
      }
    });
    //轮播栏
    if (dilidiliInfo.getScheudleBanners() != null && dilidiliInfo.getScheudleBanners().size() > 0) {
      bannerSchedule.setIndicatorVisible(false);
      bannerSchedule.setBannerPageClickListener(new BannerPageClickListener() {
        @Override
        public void onPageClick(android.view.View view, int i) {
          startToScheduleVideo(dilidiliInfo.getScheudleBanners().get(i).getAnimeLink());
        }
      });
      bannerSchedule.setPages(dilidiliInfo.getScheudleBanners(), new MZHolderCreator() {
        @Override
        public MZViewHolder createViewHolder() {
          return new ScheduleBannerViewHolder();
        }
      });
      bannerSchedule.start();
    } else {
      bannerSchedule.setVisibility(android.view.View.GONE);
    }
    //近期推荐
    if (dilidiliInfo.getScheduleRecommands() != null
        && dilidiliInfo.getScheduleRecommands().size() > 0) {
      LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
      linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
      ScheduleRecommandAdapter scheduleRecommandAdapter = new ScheduleRecommandAdapter(
          getAppComponent().imageLoader(), dilidiliInfo.getScheduleRecommands());
      scheduleRecommandAdapter.setOnItemClickListener(new OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, android.view.View view, int position) {
          ScheduleRecommand scheduleRecommand = (ScheduleRecommand) adapter.getItem(position);
          startToScheduleDetail(scheduleRecommand.getAnimeLink());
        }
      });
      rcvScheduleRecommand.setLayoutManager(linearLayoutManager);
      rcvScheduleRecommand.setAdapter(scheduleRecommandAdapter);
    } else {
      layoutScheduleRecommand.setVisibility(android.view.View.GONE);
    }
    //最近更新
    GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
    gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
    ScheduleRecentAdapter scheduleRecentAdapter = new ScheduleRecentAdapter(
        getAppComponent().imageLoader(), dilidiliInfo.getScheduleRecents());
    scheduleRecentAdapter.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(BaseQuickAdapter adapter, android.view.View view, int position) {
        ScheduleRecent scheduleRecent = (ScheduleRecent) adapter.getItem(position);
        startToScheduleDetail(scheduleRecent.getAnimeLink());
      }
    });
    rcvScheduleRecent.setLayoutManager(gridLayoutManager);
    rcvScheduleRecent.setAdapter(scheduleRecentAdapter);
    rcvScheduleRecent.setNestedScrollingEnabled(false);

    layoutScheduleMain.setVisibility(android.view.View.VISIBLE);
  }

  private void startToScheduleDetail(String url) {
    if (url.contains("anime")) {
      Intent detailIntent = new Intent(getContext(), ScheduleDetailActivity.class);
      detailIntent.putExtra(IntentConstant.SCHEDULE_DETAIL_URL, url);
      startActivity(detailIntent);
    } else if (url.contains("watch")) {
      mPresenter.checkPermission2ScheduleVideo(rxPermissions, url);
    } else {
      Intent otherIntent = new Intent(getContext(), ScheduleOtherActivity.class);
      otherIntent.putExtra(IntentConstant.SCHEDULE_DETAIL_URL, url);
      startActivity(otherIntent);
    }
  }

  private void startToScheduleVideo(String url) {
    Intent intent = new Intent(getContext(), ScheduleVideoActivity.class);
    intent.putExtra(IntentConstant.SCHEDULE_EPISODE_URL,
        url.startsWith("http") ? url : HtmlConstant.DILIDILI_URL + url);
    startActivity(intent);
  }

  @Override
  public void start2ScheduleVideo(String videoUrl) {
    Intent intent = new Intent(mContext, ScheduleVideoActivity.class);
    intent.putExtra(IntentConstant.SCHEDULE_EPISODE_URL, videoUrl);
    startActivity(intent);
  }
}

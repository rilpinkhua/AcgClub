package com.rabtman.acgclub.mvp.ui.activity;

import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener;
import com.jaeger.library.StatusBarUtil;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.rabtman.acgclub.R;
import com.rabtman.acgclub.base.constant.IntentConstant;
import com.rabtman.acgclub.di.component.DaggerScheduleDetailComponent;
import com.rabtman.acgclub.di.module.ScheduleDetailModule;
import com.rabtman.acgclub.mvp.contract.ScheduleDetailContract.View;
import com.rabtman.acgclub.mvp.model.jsoup.ScheduleDetail;
import com.rabtman.acgclub.mvp.model.jsoup.ScheduleDetail.ScheduleEpisode;
import com.rabtman.acgclub.mvp.presenter.ScheduleDetailPresenter;
import com.rabtman.acgclub.mvp.ui.adapter.ScheduleDetailEpisodeItemAdapter;
import com.rabtman.common.base.BaseActivity;
import com.rabtman.common.di.component.AppComponent;
import com.rabtman.common.imageloader.glide.GlideImageConfig;
import com.rabtman.common.imageloader.glide.transformations.BlurTransformation;
import com.tbruyelle.rxpermissions2.RxPermissions;
import java.util.List;

/**
 * @author Rabtman
 */
public class ScheduleDetailActivity extends BaseActivity<ScheduleDetailPresenter> implements
    View {

  @BindView(R.id.toolbar)
  Toolbar mToolBar;
  @BindView(R.id.toolbar_title)
  TextView mToolBarTitle;
  @BindView(R.id.app_bar)
  AppBarLayout appBar;
  @BindView(R.id.collapsing_toolbar)
  CollapsingToolbarLayout collapsingToolbarLayout;
  @BindView(R.id.img_schedule_title_bg)
  ImageView imgScheduleTitleBg;
  @BindView(R.id.img_schedule_detail_icon)
  ImageView imgScheduleDetailIcon;
  @BindView(R.id.tv_schedule_detail_time)
  TextView tvScheduleDetailTime;
  @BindView(R.id.tv_schedule_detail_area)
  TextView tvScheduleDetailAera;
  @BindView(R.id.tv_schedule_detail_proc)
  TextView tvScheduleDetailProc;
  @BindView(R.id.tv_schedule_detail_label)
  TextView tvScheduleDetailLabel;
  @BindView(R.id.tv_schedule_detail_description)
  ExpandableTextView tvScheduleDetailDescription;
  @BindView(R.id.layout_description)
  CardView layoutSceduleDescription;
  @BindView(R.id.layout_episode)
  CardView layoutSceduleEpisode;
  @BindView(R.id.rcv_schedule_detail)
  RecyclerView rcvScheduleDetail;

  @Override
  protected void setupActivityComponent(AppComponent appComponent) {
    DaggerScheduleDetailComponent.builder()
        .appComponent(appComponent)
        .scheduleDetailModule(new ScheduleDetailModule(this))
        .build()
        .inject(this);
  }

  @Override
  protected int getLayoutId() {
    return R.layout.activity_schedule_detail;
  }

  @Override
  protected void setStatusBar() {
    StatusBarUtil.setTranslucentForImageView(this, 0, mToolBar);
  }

  @Override
  protected void initData() {
    setToolBar(mToolBar, "");
    collapsingToolbarLayout.setTitleEnabled(false);

    mPresenter.getScheduleDetail(getIntent().getStringExtra(IntentConstant.SCHEDULE_DETAIL_URL));
  }

  @Override
  public void showScheduleDetail(ScheduleDetail acgNewsDetail) {
    mToolBarTitle.setText(acgNewsDetail.getScheduleTitle());
    //模糊背景
    mApplication.getAppComponent().imageLoader().loadImage(mContext,
        GlideImageConfig
            .builder()
            .url(acgNewsDetail.getImgUrl())
            .transformation(new BlurTransformation(25, 2))
            .imagerView(imgScheduleTitleBg)
            .build()
    );
    //番剧展示图
    mApplication.getAppComponent().imageLoader().loadImage(mContext,
        GlideImageConfig
            .builder()
            .url(acgNewsDetail.getImgUrl())
            .imagerView(imgScheduleDetailIcon)
            .build()
    );
    tvScheduleDetailLabel.setText(acgNewsDetail.getScheduleLabel());
    tvScheduleDetailTime.setText(acgNewsDetail.getScheduleTime());
    if (TextUtils.isEmpty(acgNewsDetail.getScheduleProc())) {
      tvScheduleDetailProc.setText(R.string.label_schedule_no_proc);
    } else {
      tvScheduleDetailProc.setText(acgNewsDetail.getScheduleProc());
    }
    tvScheduleDetailAera.setText(acgNewsDetail.getScheduleAera());
    //番剧介绍
    String desc = acgNewsDetail.getDescription();
    if (!TextUtils.isEmpty(desc)) {
      desc = desc.substring(desc.indexOf("：") + 1);
      desc.replaceAll("[展开全部]", "");
      desc.replaceAll("[显示部分]", "");
      tvScheduleDetailDescription.setText(desc);
    } else {
      layoutSceduleDescription.setVisibility(android.view.View.GONE);
    }
    //选集
    if (acgNewsDetail.getScheduleEpisodes() != null
        && acgNewsDetail.getScheduleEpisodes().size() > 1) {
      layoutSceduleEpisode.setVisibility(android.view.View.VISIBLE);
      List<ScheduleEpisode> data = acgNewsDetail.getScheduleEpisodes()
          .subList(0, acgNewsDetail.getScheduleEpisodes().size() - 1);
      ScheduleDetailEpisodeItemAdapter adapter = new ScheduleDetailEpisodeItemAdapter(data);
      adapter.setOnItemClickListener(new OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, android.view.View view, int position) {
          final ScheduleEpisode scheduleEpisode = (ScheduleEpisode) adapter.getData().get(position);
          mPresenter.checkPermission2ScheduleVideo(new RxPermissions(ScheduleDetailActivity.this),
              scheduleEpisode.getLink());
        }
      });
      GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
      layoutManager.setOrientation(GridLayoutManager.VERTICAL);
      rcvScheduleDetail.setLayoutManager(layoutManager);
      rcvScheduleDetail.setAdapter(adapter);
    }
  }

  @Override
  public void start2ScheduleVideo(String videoUrl) {
    Intent intent = new Intent(getBaseContext(), ScheduleVideoActivity.class);
    intent.putExtra(IntentConstant.SCHEDULE_EPISODE_URL, videoUrl);
    startActivity(intent);
  }

}

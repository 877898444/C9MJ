package com.c9mj.platform.explore.adapter;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.c9mj.platform.R;
import com.c9mj.platform.explore.mvp.model.bean.ExploreListItemBean;
import com.c9mj.platform.widget.inicator.ScaleCircleNavigator;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/11/4.
 */

public class ExploreListAdapter extends BaseMultiItemQuickAdapter<ExploreListItemBean, BaseViewHolder> {

    boolean isAutoScrolled = true;

    ViewPager viewPager;
    BaseViewHolder viewHolder;
    ExploreListItemBean bean;


    public ExploreListAdapter(List data) {
        super(data);
        addItemType(ExploreListItemBean.ADS, R.layout.item_explore_list_ads_layout);
        addItemType(ExploreListItemBean.NORMAL, R.layout.item_explore_list_normal_layout);
    }


    @Override
    protected void convert(final BaseViewHolder viewHolder, final ExploreListItemBean bean) {

        switch (viewHolder.getItemViewType()) {
            case ExploreListItemBean.ADS:

                List<View> viewList = new ArrayList<>();
                ImageView iv_head = new ImageView(mContext);
                Glide.with(mContext)
                        .load(bean.getImgsrc())
                        .crossFade()
                        .centerCrop()
                        .into(iv_head);
                viewList.add(iv_head);
                if (bean.getAds() != null){
                    for (ExploreListItemBean.AdsBean ads : bean.getAds()) {
                        ImageView iv_ads = new ImageView(mContext);
                        Glide.with(mContext)
                                .load(ads.getImgsrc())
                                .crossFade()
                                .centerCrop()
                                .into(iv_ads);
                        viewList.add(iv_ads);
                    }
                }

                this.viewPager = viewHolder.getView(R.id.viewpager);
                this.viewHolder = viewHolder;
                this.bean = bean;
                final ExploreAdsAdapter pageAdapter = new ExploreAdsAdapter(viewList);
                viewPager.setOffscreenPageLimit(4);
                viewPager.setAdapter(pageAdapter);
                viewHolder.setText(R.id.tv_title, bean.getTitle());

                viewPager.addOnPageChangeListener(pageChangeListener);

                viewPager.setCurrentItem(0);
                pageChangeListener.onPageScrollStateChanged(ViewPager.SCROLL_STATE_IDLE);

                //MagicIndicator
                MagicIndicator magicIndicator = viewHolder.getView(R.id.magic_indicator);
                magicIndicator.setVisibility(viewList.size() != 1 ? View.VISIBLE : View.GONE);
                ScaleCircleNavigator navigator = new ScaleCircleNavigator(mContext);
                navigator.setFollowTouch(true);
                navigator.setCircleCount(viewList.size());
                magicIndicator.setNavigator(navigator);
                ViewPagerHelper.bind(magicIndicator, viewPager);

                break;
            case ExploreListItemBean.NORMAL:
                viewHolder.setText(R.id.tv_title, bean.getTitle())
                        .setText(R.id.tv_source, bean.getSource());
                Glide.with(mContext)
                        .load(bean.getImgsrc())
                        .crossFade()
                        .centerCrop()
                        .into((ImageView) viewHolder.getView(R.id.iv_img));
                break;
        }
    }
    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (position == 0) {
                viewHolder.setText(R.id.tv_title, bean.getTitle());
                return;
            }
            viewHolder.setText(R.id.tv_title, bean.getAds().get(position - 1).getTitle());

        }

        @Override
        public void onPageScrollStateChanged(int state) {
            switch (state) {
                case ViewPager.SCROLL_STATE_DRAGGING:
                    isAutoScrolled = false;
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                    isAutoScrolled = false;
                    break;
                case ViewPager.SCROLL_STATE_IDLE:
                    isAutoScrolled = true;
                    Flowable.just(0)
                            .delay(3, TimeUnit.SECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<Integer>() {

                                @Override
                                public void accept(Integer integer) throws Exception {
                                    if (isAutoScrolled == false) {
                                        return;
                                    }
                                    int current = viewPager.getCurrentItem();
                                    if (current + 1 == viewPager.getChildCount()) {
                                        viewPager.setCurrentItem(0, true);
                                    } else {
                                        viewPager.setCurrentItem(current + 1, true);
                                    }
                                }
                            });
                    break;
            }
        }

        ;
    };
}

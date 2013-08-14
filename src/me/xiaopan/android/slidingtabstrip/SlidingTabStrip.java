package me.xiaopan.android.slidingtabstrip;

import java.util.List;

import me.xiaopan.easyandroid.util.ViewUtils;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

/**
 * 带有滑动标题的ViewPager，你只需调用setTabs
 */
public class SlidingTabStrip extends HorizontalScrollView implements OnPageChangeListener{
	private int center;	//中间位置
	private int width;	//宽度
	private LinearLayout tabsLayout;	//标题项布局
	private ViewPager viewPager;	//ViewPager
	private View currentSelectedTabView;	//当前标题项
	private OnPageChangeListener onPageChangeListener;	//页面改变监听器
	
	public SlidingTabStrip(Context context) {
		super(context);
		init();
	}
	
	public SlidingTabStrip(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	/**
	 * 初始化
	 */
	private void init(){
		setHorizontalScrollBarEnabled(false);	//隐藏横向滑动提示条
		addView(tabsLayout = new LinearLayout(getContext()), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));	//添加标题容器
		getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				if(currentSelectedTabView == null && tabsLayout.getChildCount() > 0){
					currentSelectedTabView = tabsLayout.getChildAt(0);
					currentSelectedTabView.setSelected(true);
				}
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
					getViewTreeObserver().removeGlobalOnLayoutListener(this);
				} else {
					getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}
			}
		});
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		/* 如果当前的标题内容在横向上不能够将SlideTitlebar充满，那么就试图更改所有标题的宽度，来充满SlideTitlebar */
		width = r - l;
		center = (r + l) / 2;
		View titleView;
		if(ViewUtils.getMeasureWidth(tabsLayout) < width){
			int averageWidth = width/tabsLayout.getChildCount();	//初始化平均宽度
			
			/* 先计算出剩余可平分的宽度 */
			int currentTitleViewWidth;
			int number = 0;	//记录减去的个数，待会儿算平均值的时候要减去此数
			for(int w = 0; w < tabsLayout.getChildCount(); w++){
				titleView = tabsLayout.getChildAt(w);
				ViewUtils.measure(titleView);	//测量一下
				currentTitleViewWidth = titleView.getMeasuredWidth();
				if(currentTitleViewWidth > averageWidth){	//如果当前视图的宽度大于平均宽度，就从总宽度中减去当前视图的宽度
					width -= currentTitleViewWidth;
					number++;
				}
			}
			int newAverageWidth = width/(tabsLayout.getChildCount() - number);	//重新计算宽度
			
			//重新设置宽度小于平均宽度的视图的宽度为新的平均宽度
			for(int w = 0; w < tabsLayout.getChildCount(); w++){
				titleView = tabsLayout.getChildAt(w);
				ViewGroup.LayoutParams layoutParams = titleView.getLayoutParams();
				if(titleView.getMeasuredWidth() < averageWidth){
					layoutParams.width = newAverageWidth;
				}else{
					layoutParams.width = titleView.getMeasuredWidth();
				}
				titleView.setLayoutParams(layoutParams);
			}
		}else{
			/* 重新设置宽度小于平均宽度的视图的宽度为新的平均宽度 */
			for(int w = 0; w < tabsLayout.getChildCount(); w++){
				titleView = tabsLayout.getChildAt(w);
				ViewGroup.LayoutParams layoutParams = titleView.getLayoutParams();
				layoutParams.width = titleView.getMeasuredWidth();
				titleView.setLayoutParams(layoutParams);
			}
		}
		
		super.onLayout(changed, l, t, r, b);
	}
	
	@Override
	public void onPageSelected(int position) {
		if(position > -1 && position < tabsLayout.getChildCount()){
			//切换选中状态
			if(currentSelectedTabView != null){
				currentSelectedTabView.setSelected(false);
			}
			currentSelectedTabView = tabsLayout.getChildAt(position);
			currentSelectedTabView.setSelected(true);
			
			//处理滑动
			smoothScrollTo((currentSelectedTabView.getLeft() + currentSelectedTabView.getRight())/2 - center, currentSelectedTabView.getTop());
		}
		
		if(onPageChangeListener != null){
			onPageChangeListener.onPageSelected(position);
		}
	}
	
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		if(onPageChangeListener != null){
			onPageChangeListener.onPageScrolled(arg0, arg1, arg2);
		}
	}
	
	@Override
	public void onPageScrollStateChanged(int arg0) {
		if(onPageChangeListener != null){
			onPageChangeListener.onPageScrollStateChanged(arg0);
		}
	}

	public void addTabs(View... tabs) {
		tabsLayout.removeAllViews();
		View titleView = null;
		for(int w = 0; w < tabs.length; w++){
			titleView = tabs[w];
			titleView.setTag(w);
			final int tabViewPosition = w;
			titleView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(viewPager != null){
						viewPager.setCurrentItem(tabViewPosition, true);	//更改ViewPager的选中项
					}
				}
			});
			tabsLayout.addView(titleView);
		}
	}

	public void addTabs(List<View> tabs) {
		tabsLayout.removeAllViews();
		View titleView = null;
		for(int w = 0; w < tabs.size(); w++){
			titleView = tabs.get(w);
			titleView.setTag(w);
			final int tabViewPosition = w;
			titleView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(viewPager != null){
						viewPager.setCurrentItem(tabViewPosition, true);	//更改ViewPager的选中项
					}
				}
			});
			tabsLayout.addView(titleView);
		}
	}

	public void setViewPager(ViewPager viewPager) {
		this.viewPager = viewPager;
		this.viewPager.setOnPageChangeListener(this);
	}
	
	public OnPageChangeListener getOnPageChangeListener() {
		return onPageChangeListener;
	}

	public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
		this.onPageChangeListener = onPageChangeListener;
	}
}
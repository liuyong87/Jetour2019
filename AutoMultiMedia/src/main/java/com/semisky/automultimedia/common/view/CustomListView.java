package com.semisky.automultimedia.common.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.widget.ListView;

import com.semisky.automultimedia.R;

/**
 * 固定滑块大小的ListView
 * @author xiongjun
 */
public class CustomListView extends ListView {

	private float bitmapHeight;

	public CustomListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CustomListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomListView(Context context) {
		super(context);
	}

	@Override
	protected int computeVerticalScrollOffset() {

		float totalRange = super.computeVerticalScrollRange();//总高度
		float showRange = super.computeVerticalScrollExtent();//显示高度
		float cursorRange =this.getHeight() - 10;//滑动条高度
		float scrollOffset = super.computeVerticalScrollOffset();//向上滑动的高度

		float proportionTotalHeight = scrollOffset / (totalRange - showRange);
		if (bitmapHeight == 0){
			bitmapHeight = BitmapFactory.decodeResource(getResources(), R.drawable.scrollbar).getHeight();
		}
		float cursorMoveY = proportionTotalHeight * (cursorRange - bitmapHeight);

		this.cursor.SetOffSet(cursorMoveY);
		return (int) scrollOffset;
	}

	@Override
	public void setSelection(int position) {
		super.setSelection(position);

		float totalRange = super.computeVerticalScrollRange();//总高度
		float showRange = super.computeVerticalScrollExtent();//显示高度
		float cursorRange =this.getHeight() - 10;//滑动条高度
		float scrollOffset = super.computeVerticalScrollOffset();//向上滑动的高度

		float proportionTotalHeight = scrollOffset / (totalRange - showRange);
		if (bitmapHeight == 0){
			bitmapHeight = BitmapFactory.decodeResource(getResources(), R.drawable.scrollbar).getHeight();
		}
		float cursorMoveY = proportionTotalHeight * (cursorRange - bitmapHeight);

		this.cursor.SetOffSet(cursorMoveY);
	}

	private CustomeScrollBar cursor;

	public void setCustomeScrollBar(CustomeScrollBar customeScrollBar) {
		this.cursor = customeScrollBar;
	}
}

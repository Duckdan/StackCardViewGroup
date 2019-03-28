package study.yang.stackcardviewgroup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class DefineRightLayoutManager extends RecyclerView.LayoutManager {

    /**
     * 基准控件的宽度
     */
    private int mDecorateWidth;
    /**
     * 基准控件的高度
     */
    private int mDecorateHeight;
    /**
     * 默认间隔，单位为px
     */
    private int space = 45;
    /**
     * 默认展示数量
     */
    private int initStackCount = 3;
    /**
     * 手机屏幕宽度
     */
    private int widthPixels;
    /**
     * 手机屏幕高度
     */
    private final int heightPixels;
    /**
     * 以基准尺寸为基数的缩放比例
     */
    private float scaleRatio = 0.2f;

    /**
     * 总的偏移量
     */
    private int mTotalOffsetDx = 0;
    /**
     * 第一个可视item的下标
     */
    private int currentIndex = 0;
    /**
     * 最后一个可视item的下标，最后一个item的透明度为0
     */
    private int endIndex = 0;

    //基准值的比例
    private float referenceValue = 0f;
    //缩放和平移的系数
    private float k = 0f;
    //速度跟踪者
    private VelocityTracker mVelocityTracker = VelocityTracker.obtain();
    //动画处理
    private ObjectAnimator animator;
    /**
     * 动画的偏移值
     */
    private int animateValue;

    private int duration = 300;
    private int lastAnimateValue;
    /**
     * 判断指针ID
     */
    private int pointerId;
    /**
     * 水平方向上的最低速度
     */
    private int mMinVelocityX;

    private RecyclerView.Recycler recycler;
    private RecyclerView.State state;
    private Context context;



    public DefineRightLayoutManager(Context context, DefineConfig defineConfig) {
        this.context = context;
        space = defineConfig.space;
        initStackCount = defineConfig.initialStackCount;
        scaleRatio = defineConfig.scaleRatio;

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        widthPixels = displayMetrics.widthPixels;
        heightPixels = displayMetrics.heightPixels;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean isAutoMeasureEnabled() {
        return true;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        /**
         * 当adapter中的子View数目为0时解绑recycler中的控件
         */
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }
        /**
         * state.isPreLayout判断layoutManager执行到哪一个哪一个阶段
         * 返回true表示执行到预布局阶段
         */
        if (getChildCount() == 0 && state.isPreLayout()) {
            return;
        }
        if (getChildCount() == 0) {
            View scrap = recycler.getViewForPosition(0);
            /**
             * 获取执行一个fling操作的最小速度
             */
            mMinVelocityX = ViewConfiguration.get(scrap.getContext()).getScaledMinimumFlingVelocity();
            measureChildWithMargins(scrap, 0, 0);
            //根据给定的space以及initStackCount换算出第一个View的宽度
            mDecorateWidth = widthPixels - (initStackCount + 1) * space;// getDecoratedMeasurementHorizontal(scrap);
            //获取控件的高度
            mDecorateHeight = getDecoratedMeasuredHeight(scrap);
            //给基准值赋值
            referenceValue = scaleRatio * mDecorateWidth + space;
            k = space * 1.0f / referenceValue;

        }

        //初始化时调用 填充childView
        fill(0, recycler, state);
    }

    private int fill(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {

        try {
            if ((currentIndex == 0 && dx > 0 && mTotalOffsetDx == 0)/*||mTotalOffsetDx< 0  */ || (Math.abs(mTotalOffsetDx) / referenceValue >= getItemCount() - 1 && dx < 0)) {
                return 0;
            }
            //避免滑动过快，所以每次除以基准值求余
            mTotalOffsetDx += (dx % referenceValue);
            //解绑RecyclerView上的View,调用此方法之后getChildCount为0
            detachAndScrapAttachedViews(recycler);
            currentIndex = (int) (Math.abs(mTotalOffsetDx) / referenceValue);
            //计算尾部下标
            int compute = currentIndex + initStackCount;
            endIndex = compute >= getItemCount() ? getItemCount() - 1 : compute;

            for (int i = currentIndex; i <= endIndex; i++) {

                //获取当前位置的控件
                View view = recycler.getViewForPosition(i);


                addView(view);//现在重新添加
                //测量子控件的上下左右的位置
                measureChildWithMargins(view, 0, 0);

                int leftMargin = (initStackCount - (i - currentIndex)) * space;
                float scale = 1 - (i - currentIndex) * scaleRatio;
                int currentWidth = calculateCurrentWidth(i, scale);
                int currentHeight = calculateCurrentHeight(i, scale);

                /**
                 * - scaleX * offsetDx将水平移动的偏移量跟子控件向左偏移的速度联系起来
                 */
                int right = 0;//(i + 1) * space;//
                float offsetDx = mTotalOffsetDx % referenceValue;

                if (currentIndex == i) {
                    right = (int) (mDecorateWidth + leftMargin - offsetDx);
                } else {
                    right = (int) (currentWidth + leftMargin - k * offsetDx);
                }

                if (right > (widthPixels - space) && i == getItemCount() - 1) {
                    right = widthPixels - space;
                    mTotalOffsetDx = -(int) (referenceValue * (getItemCount() - 1));
                } else if (right < widthPixels - space && i == 0) {
                    right = widthPixels - space;
                    mTotalOffsetDx = 0;
                }

//                int right = left + currentWidth;


                /**
                 * 以自身为基准点
                 */
                view.setPivotX(getDecoratedMeasuredWidth(view));
                view.setPivotY(getDecoratedMeasuredHeight(view) * 1.0f / 2);
                view.setScaleX(currentWidth * 1.0f / getDecoratedMeasuredWidth(view));
                view.setScaleY(currentWidth * 1.0f / getDecoratedMeasuredWidth(view));
                layoutDecoratedWithMargins(view, right - getDecoratedMeasuredWidth(view), 0, right, getDecoratedMeasuredHeight(view));

                //setTranslationZ存在BUG,elevation是z轴上的静态值而translationZ是z轴上的动态值
                //此处的elevation过高的话会导致控件阴影过大
                ViewCompat.setElevation(view, currentHeight * 0.0001f);


                //正常下标移动的时候透明度走if,反之走else
                if (endIndex - currentIndex == initStackCount) {
                    if (i == currentIndex) {
                        view.setAlpha(1 - Math.abs(offsetDx) / referenceValue);
                    } else if (i == endIndex /*&& endIndex != getItemCount() - 1*/) {  ///*&& endIndex != getItemCount() - 1*/不能要，否则到最后四条时显示不出来最后一个
                        view.setAlpha(Math.abs(offsetDx) / referenceValue);
                    } else {
                        view.setAlpha(1);
                    }
                } else {//处理小于initStackCount个数的可视itemView的透明度处理
                    if (i == currentIndex) {
                        view.setAlpha(1 - Math.abs(offsetDx) / referenceValue);
                    } else {
                        view.setAlpha(1);
                    }
                }
            }
            return dx;
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return 0;

    }

    /**
     * 计算当前控件的宽度
     *
     * @param position
     * @param scale    为1时，widthSpace为0
     * @return
     */
    private int calculateCurrentWidth(int position, float scale) {
        float offsetDx = Math.abs(mTotalOffsetDx) % referenceValue;
        if (offsetDx > 0/* && offsetDx <= referenceValue*/) {
            if (currentIndex == position) {
                scale = 1 - offsetDx / referenceValue;
            } else {
                //临时的偏移比例
                float tempRatio = offsetDx * scaleRatio / referenceValue;
                //临时的缩放比例
                float tempScaleX = scale + tempRatio;
                scale = tempScaleX >= 1 ? 1f : tempScaleX;
            }
        }
        return (int) (mDecorateWidth * scale);
    }

    private int calculateCurrentHeight(int position, float scale) {
        float offsetDx = Math.abs(mTotalOffsetDx) % referenceValue;
        if (offsetDx > 0 /*&& offsetDx <= referenceValue*/) {
            if (currentIndex == position) {
                scale = 1 - offsetDx * 1.0f / referenceValue;
            } else {
                /**
                 * scaleX + offsetDx * 1.0f / heightSpace用来计算在scale的基础之上来重新制定scaleY
                 */
                float tempRatio = offsetDx * scaleRatio / referenceValue;
                float tempScaleY = scale + tempRatio;
                scale = tempScaleY >= 1 ? 1f : tempScaleY;
            }
        }
        return (int) (mDecorateHeight * scale);
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        super.onAdapterChanged(oldAdapter, newAdapter);
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }


    public int findLastVisibleItemPosition() {
        View childAt = getChildAt(0);
        return getPosition(childAt);
    }


    /**
     * action_move时会调用该方法
     *
     * @param dx
     * @param recycler
     * @param state
     * @return
     */
    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        this.recycler = recycler;
        this.state = state;
        recyclerUnVisibleView(recycler, dx);
        fill(dx, recycler, state);
        return dx;
    }


    /**
     * 回收不可见的childview
     */
    private void recyclerUnVisibleView(RecyclerView.Recycler recycler, int dx) {
        if (getChildCount() > 0) {
            //得到第一个可见的View

            if (dx < 0) {//向左
                View firstView = getChildAt(0);
                if (firstView.getWidth() < space) {
                    //从RecycleView中移除View,将view给与给定的Recycler从而达到复用的目的
                    removeAndRecycleView(firstView, recycler);
                }
                //得到第一个可见View在Adapter中的位置
            } else if (dx > 0) {
                Log.e("childCount", "recycler::中View的个数" + getChildCount());
            }
        }
    }

    private RecyclerView mRV;

    /**
     * 当控件依附于Window时调用
     *
     * @param view
     */
    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        mRV = view;
        view.setOnTouchListener(touchListener);
        view.setOnFlingListener(flingListener);
    }


    private RecyclerView.OnFlingListener flingListener = new RecyclerView.OnFlingListener() {
        /**
         * 设置就算是飞滑也只让控件移动一个referenceValue的位置
         * @param velocityX
         * @param velocityY
         * @return
         */
        @Override
        public boolean onFling(int velocityX, int velocityY) {
            int scrollX;
            float offsetDx = Math.abs(mTotalOffsetDx) % referenceValue;
            int tempScrollX = (int) (referenceValue - offsetDx);
            int vel = absMax(velocityX, velocityY);
            if (vel < 0) {
                //向左快速滑动的时候，补全下一个移动位的距离
                scrollX = -tempScrollX;
            } else {
                //向右快速滑动的时候，将总偏移量除以referenceValue的
                //余数部分给减去，从而达到mTotalOffsetDx除以referenceValue能够等于整数
                scrollX = (int) offsetDx;
            }


            int dur = computeSettleDuration(Math.abs(scrollX), Math.abs(vel));
            brewAndStartAnimator(dur, scrollX);
            setScrollStateIdle();
            return true;
        }
    };

    /**
     * 计算控件停下来的所需要的时间
     *
     * @param distance
     * @param xvel
     * @return
     */
    private int computeSettleDuration(int distance, float xvel) {
        float sWeight = 0.5f * distance / referenceValue;
        float velWeight = xvel > 0 ? 0.5f * mMinVelocityX / xvel : 0;

        return (int) ((sWeight + velWeight) * duration);
    }

    /**
     * 计算飞滑时水平或者竖直方向的最大值
     *
     * @param a
     * @param b
     * @return
     */
    private int absMax(int a, int b) {
        if (Math.abs(a) > Math.abs(b))
            return a;
        else return b;
    }

    private Method sSetScrollState;

    /**
     * 防止滚动的时候中断点击事件
     */
    private void setScrollStateIdle() {
        try {
            if (sSetScrollState == null)
                sSetScrollState = RecyclerView.class.getDeclaredMethod("setScrollState", int.class);
            sSetScrollState.setAccessible(true);
            sSetScrollState.invoke(mRV, RecyclerView.SCROLL_STATE_IDLE);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private View.OnTouchListener touchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //将触摸事件交给速度跟踪者事件
            mVelocityTracker.addMovement(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (animator != null && animator.isRunning())
                        animator.cancel();
                    pointerId = event.getPointerId(0);
                    break;
                case MotionEvent.ACTION_UP:
                    //点击事件
                    if (v.isPressed()) v.performClick();
                    mVelocityTracker.computeCurrentVelocity(1000, 14000);
                    //获取当前pointerId水平方向上的速度
                    float xVelocity = mVelocityTracker.getXVelocity(pointerId);
                    float offsetDx = Math.abs(mTotalOffsetDx) % referenceValue;
                    int scrollX;
                    if (Math.abs(xVelocity) < mMinVelocityX /*&& offsetDx != 0*/) {
                        //因为referenceValue / 6时第一个视图高度和第二视图高度相等
                        if (offsetDx >= referenceValue / 6) {
                            scrollX = -(int) (referenceValue - offsetDx);
                        } else {
                            scrollX = (int) offsetDx;
                        }
                        int dur = (int) (Math.abs((scrollX + 0f) / referenceValue) * duration);
                        brewAndStartAnimator(dur, scrollX);
                    }
                    break;
            }
            return false;
        }
    };

    private void brewAndStartAnimator(int dur, int finalXorY) {
        animator = ObjectAnimator.ofInt(this, "animateValue", 0, finalXorY);
        animator.setDuration(dur);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                lastAnimateValue = 0;

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                lastAnimateValue = 0;
            }
        });
    }

    /**
     * 属性动画执行时调用
     *
     * @param animateValue
     */
    @SuppressWarnings("unused")
    public void setAnimateValue(int animateValue) {
        this.animateValue = animateValue;
        int distance = this.animateValue - lastAnimateValue;
        scrollHorizontallyBy(distance, recycler, state);
        lastAnimateValue = animateValue;
    }

    /**
     * 属性动画执行时调用
     *
     * @param
     */
    @SuppressWarnings("unused")
    public int getAnimateValue() {
        return animateValue;
    }

}

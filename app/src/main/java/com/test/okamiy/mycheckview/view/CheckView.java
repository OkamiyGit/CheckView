package com.test.okamiy.mycheckview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.test.okamiy.mycheckview.R;

/**
 * Created by Okamiy on 2017/10/26.
 * Email: 542839122@qq.com
 */

public class CheckView extends View {
    private static final String TAG = "CheckView";
    private Context mContext;

    //内圆的画笔
    private Paint mPaintCircle;
    //外层圆环的画笔
    private Paint mPaintRing;
    //打钩的画笔
    private Paint mPaintTick;

    //整个圆外切的矩形
    private RectF mRectF = new RectF();
    //记录打钩路径的三个点坐标
    private float[] mPoints = new float[8];

    //控件中心的X,Y坐标
    private int centerX;
    private int centerY;

    //计数器
    private int circleCounter = 0;
    private int scaleCounter = 45;
    private int ringCounter = 0;
    private int alphaCount = 0;

    /**
     * 是否被点亮:绘制过程中，就必须判断当前究竟是绘制未选中还是选中了。
     */
    private boolean isChecked = false;

    private int unCheckBaseColor;
    private int checkedBaseColor;
    private int checkedTickColor;
    private int radius;

    //勾的半径()
    private float tickRadius;
    //勾的偏移
    private float tickRadiusOffset;
    //放大动画的最大范围
    private int scaleCounterRange;

    private TickRateEnum mTickRateEnum;

    private OnCheckedChangeListener mOnCheckedChangeListener;

    public CheckView(Context context) {
        this(context, null);
    }

    public CheckView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initAttrs(attrs);
        init();
        setUpEvent();
    }

    /**
     * 获取自定义配置
     *
     * @param attrs
     */
    private void initAttrs(AttributeSet attrs) {
        TypedArray array = mContext.obtainStyledAttributes(attrs, R.styleable.CheckView);
        unCheckBaseColor = array.getColor(R.styleable.CheckView_uncheck_base_color, getResources().getColor(R.color.tick_gray));
        checkedBaseColor = array.getColor(R.styleable.CheckView_checked_base_color, getResources().getColor(R.color.tick_yellow));
        checkedTickColor = array.getColor(R.styleable.CheckView_checked_tick_color, getResources().getColor(R.color.tick_white));
        /**
         * getDimension和getDimensionPixelOffset的功能类似，
         * 都是获取某个dimen的值，但是如果单位是dp或sp，则需要将其乘以density
         * 如果是px，则不乘。并且getDimension返回float，getDimensionPixelOffset返回int.
         * 而getDimensionPixelSize则不管写的是dp还是sp还是px,都会乘以denstiy
         */
        radius = array.getDimensionPixelOffset(R.styleable.CheckView_radius, dp2px(mContext, 30));

        //获取配置的动画速度
        int rateMode = array.getInt(R.styleable.CheckView_rate, TickRateEnum.RATE_MODE_NORMAL);
        mTickRateEnum = TickRateEnum.getRateEnum(rateMode);
        /**
         * TypedArray是一个存储属性值的数组，使用完之后应该调用recycle()回收,用于后续调用时可复用之。
         * 当调用该方法后，不能再操作该变量
         */
        array.recycle();

        scaleCounterRange = dp2px(mContext, 30);
        tickRadius = dp2px(mContext, 12);
        tickRadiusOffset = dp2px(mContext, 4);
    }

    private void init() {
        if (null == mPaintRing) {
            mPaintRing = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        mPaintRing.setStyle(Paint.Style.STROKE);
        mPaintRing.setColor(isChecked ? checkedBaseColor : unCheckBaseColor);
        /**
         * 设置线冒样式，取值有Cap.ROUND(圆形线冒)、Cap.SQUARE(方形线冒)、Paint.Cap.BUTT(无线冒)
         */
        mPaintRing.setStrokeCap(Paint.Cap.ROUND);
        mPaintRing.setStrokeWidth(dp2px(mContext, 2.5f));

        if (null == mPaintCircle) {
            mPaintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        mPaintCircle.setColor(checkedBaseColor);
        mPaintCircle.setStrokeWidth(dp2px(mContext, 1));

        if (null == mPaintTick) {
            mPaintTick = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        mPaintTick.setStyle(Paint.Style.STROKE);
        mPaintTick.setColor(isChecked ? checkedTickColor : unCheckBaseColor);
        mPaintTick.setStrokeCap(Paint.Cap.ROUND);
        mPaintTick.setStrokeWidth(dp2px(mContext, 2.5f));
    }

    /**
     * 初始化点击事件
     */
    private void setUpEvent() {
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                isChecked = !isChecked;
                reset();
                //点击事件回调
                if (null != mOnCheckedChangeListener) {
                    mOnCheckedChangeListener.onCheckedChanged((CheckView) view, isChecked);
                }
            }
        });
    }

    /**
     * 重置，并绘制
     */
    private void reset() {
        init();//初始化画笔

        //计数器重置
        ringCounter = 0;
        circleCounter = 0;
        scaleCounter = 45;
        alphaCount = 0;

        mRectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        /**
         * 刷新界面（主线程）：请求重绘View树，即draw()过程，假如视图发生大小没有变化就不会调用layout()过程，
         * 并且只绘制那些“需要重绘的”视图，即谁(View的话，只绘制该View ；ViewGroup，则绘制整个ViewGroup）
         * 请求invalidate()方法，就绘制该视图
         *
         * 何时使用：
         *    1、直接调用invalidate()方法，请求重新draw()，但只会绘制调用者本身。
         *    2、setSelection()方法 ：请求重新draw()，但只会绘制调用者本身。
         *    3、setVisibility()方法 ： 当View可视状态在INVISIBLE转换VISIBLE时，会间接调用invalidate()方法，
         *       继而绘制该View。
         *    4 、setEnabled()方法 ： 请求重新draw()，但不会重新绘制任何视图包括该调用者本身。
         */
        invalidate();
    }

    /**
     * 测试控件尺寸
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMySize(radius * 2 + scaleCounterRange * 2, widthMeasureSpec);
        int height = getMySize(radius * 2 + scaleCounterRange * 2, heightMeasureSpec);

        width = height = Math.max(width, height);

        setMeasuredDimension(width, height);

        centerX = getMeasuredWidth() / 2;
        centerY = getMeasuredHeight() / 2;

        //设置圆圈的外切矩形:radius是圆的半径，centerX，centerY是控件中心的坐标
        mRectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        /**
         * 设置打钩的几个点坐标（具体坐标点的位置不用怎么理会，自己定一个就好，没有统一的标准）
         * 分析：
         *     画一个√，需要确定3个坐标点的位置
         *     所以这里我先用一个float数组来记录3个坐标点的位置，
         *     最后在onDraw()的时候使用canvas.drawLines(mPoints, mPaintTick)来画出来
         *     其中这里mPoint[0]~mPoint[3]是确定第一条线 \ 的两个坐标点位置
         *     mPoint[4]~mPoint[7]是确定第二条线 / 的两个坐标点位置
         */
        mPoints[0] = centerX - tickRadius + tickRadiusOffset;
        mPoints[1] = (float) centerY;
        mPoints[2] = centerX - tickRadius / 2 + tickRadiusOffset;
        mPoints[3] = centerY + tickRadius / 2;
        mPoints[4] = centerX - tickRadius / 2 + tickRadiusOffset;
        mPoints[5] = centerY + tickRadius / 2;
        mPoints[6] = centerX + tickRadius * 2 / 4 + tickRadiusOffset;
        mPoints[7] = centerY - tickRadius * 2 / 4;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isChecked) {
            //绘制圆环，mRectF就是之前确定的外切矩形
            //因为是静态的，所以设置扫过的角度为360度
            canvas.drawArc(mRectF, 90, 360, false, mPaintRing);

            //根据之前定好的钩的坐标位置，进行绘制
            canvas.drawLines(mPoints, mPaintTick);
            return;
        }

        /**
         * 选中状态是个动画，因此我们这里需要调用postInvalidate()不断进行重绘，直到动画执行完毕
         * 1.绘制进度圆环（计数器的方式绘制进度）:
         *    分析：
         *      1.我们定义一个计数器ringCounter,峰值为360（也就是360度），每执行一次onDraw()方法，
         *        我们对ringCounter进行自加，进而模拟进度
         *      2.画圆弧进度，每次绘制都自加12个单位，也就是圆弧又扫过了12度
         *      3.这里的12个单位先写死，后面我们可以做一个配置来实现自定义
         *      4.调用postInvalidate()进行重绘
         */
        ringCounter += mTickRateEnum.getRingCounterUnit();
        if (ringCounter >= 360) {
            ringCounter = 360;
        }

        canvas.drawArc(mRectF, 90, ringCounter, false, mPaintRing);

        /**
         * 选中状态是个动画，因此我们这里需要调用postInvalidate()不断进行重绘，直到动画执行完毕
         * 2.绘制向圆心收缩的动画（计数器的方式绘制进度）:
         *    分析：
         *       1.圆心收缩的动画在圆环进度达到100%的时候才进行，
         *       2.同理，也采用计数器circleCounter的方法来控制绘制的时间和速度
         * 选中动画分析：
         *    先绘制一个黄色背景，然后在背景圆的图层上，再绘制白色的圆(半径不断缩小)半径不断缩小，
         * 背景就不断露出来，达到向中心收缩的效果mPaintCircle.setColor(checkTickColor);
         **/
        if (ringCounter == 360) {
            //先绘制背景的圆
            mPaintCircle.setColor(checkedBaseColor);
            canvas.drawCircle(centerX, centerY, radius, mPaintCircle);

            //然后在背景圆的图层上，再绘制白色的圆(半径不断缩小)
            //半径不断缩小，背景就不断露出来，达到向中心收缩的效果
            mPaintCircle.setColor(checkedTickColor);
            //收缩的单位先试着设置为6，后面可以进行自己自定义
//            circleCounter += 6;
            circleCounter += mTickRateEnum.getCircleCounterUnit();
            canvas.drawCircle(centerX, centerY, radius - circleCounter, mPaintCircle);

            /**
             * 选中状态是个动画，因此我们这里需要调用postInvalidate()不断进行重绘，直到动画执行完毕
             * 3.绘制选中的钩:
             *      分析：白色的圆半径收缩到0后,就该绘制打钩了
             *           也就是计数器circleCounter大于背景圆的半径的时候，就该将钩√显示出来了
             *           这里加40是为了加一个延迟时间，不那么仓促的将钩显示出来
             */
            if (circleCounter >= radius + 40) {
                //显示打钩（外加一个透明的渐变）
                alphaCount += 20;
                if (alphaCount >= 255) {
                    alphaCount = 255;
                }

                //设置透明度
                mPaintTick.setAlpha(alphaCount);
                //最后就将之前在onMeasure中计算好的坐标传进去，绘制钩出来
                canvas.drawLines(mPoints, mPaintTick);

                /**
                 * 选中状态是个动画，因此我们这里需要调用postInvalidate()不断进行重绘，直到动画执行完毕
                 * 4.绘制完成后的放大（计数器的方式绘制）:
                 *       分析：
                 *           1.放大再回弹的效果，开始的时机应该也是收缩动画结束后开始，也就是说跟打钩的
                 *       动画同时进行
                 *          2.这里的计数器我设置成一个不为0的数值，先设置成45（随意，这不是标准），然后每重绘
                 *       一次，自减4个单位
                 *         3.最后画笔的宽度是关键的地方，画笔的宽度根据scaleCounter的正负来决定是加还是减
                 */
//                scaleCounter -= 4; //显示放大并回弹的效果,4随意设定，后期自定义
                scaleCounter -= mTickRateEnum.getScaleCounterUnit();
                if (scaleCounter <= -scaleCounterRange) {
                    scaleCounter = -scaleCounterRange;
                }

                //放大回弹，主要看画笔的宽度
                float strokeWith = mPaintRing.getStrokeWidth() + (scaleCounter > 0 ? dp2px(mContext, 1) : -dp2px(mContext, 1));
                mPaintRing.setStrokeWidth(strokeWith);
                canvas.drawArc(mRectF, 90, 360, false, mPaintRing);
            }
        }

        /**
         * postInvalidate 是在非UI线程中重绘：强制重绘
         * 动画执行完毕，就补在需要重绘了
         */
        if (scaleCounter != -scaleCounterRange) {
            postInvalidate();
        }
    }

    /**
     * 暴露外部接口，改变绘制状态
     * 为了灵活的可以控制绘制的状态，我们可以暴露一个接口给外部设置是否选中
     *
     * @param checked
     */
    public void setChecked(boolean checked) {
        if (this.isChecked != checked) {
            isChecked = checked;
            reset();
        }
    }

    /**
     * 根据父控件和自身获取尺寸
     */
    private int getMySize(int defaultSize, int measureSpec) {
        int mySize = defaultSize;

        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                mySize = defaultSize;
                break;
            case MeasureSpec.EXACTLY:
                mySize = size;
                break;
        }
        return mySize;

    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(CheckView checkView, boolean isCheck);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.mOnCheckedChangeListener = onCheckedChangeListener;
    }

    private static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}

package com.bupt.edison.scratchcard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by edison on 16/6/2.
 * 此控件唯一的缺点就是没有考虑padding.
 */
public class ScratchView extends ImageView {
    Paint tracePaint; //画轨迹的画笔
    Paint coverPaint; //画蒙板的画笔
    Paint paint; //画底层Bitmap的画笔
    Bitmap mBitmap; //蒙板的底层Bitmap
    Bitmap coverBitmap;//蒙板图片的Bitmap
    Canvas mBitmapCanvas; //底层Bitmap的画布
    Context context;
    Path tracePath; //轨迹

    int penStrokeWidth = 1; //笔触的宽度
    int fitStyle = 0; //蒙板图片的scaleType
    int coverColor; //蒙板的颜色

    public ScratchView(Context context) {
        super(context);
        initPaints();
    }

    public ScratchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
        initPaints();
    }

    public ScratchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
        initPaints();
    }

    private void init(Context context, AttributeSet attrs){
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.ScratchView);
        BitmapDrawable bitmapDrawable = ((BitmapDrawable)typedArray.getDrawable(R.styleable.ScratchView_coverBitmap));
        if(null != bitmapDrawable){
            coverBitmap = bitmapDrawable.getBitmap();
        }
        penStrokeWidth = typedArray.getInt(R.styleable.ScratchView_penStrokeWidth,50);
        fitStyle = typedArray.getInt(R.styleable.ScratchView_coverBitmapFitStyle,1);
        coverColor = typedArray.getColor(R.styleable.ScratchView_coverColor,Color.LTGRAY);
        typedArray.recycle();
    }

    private void initPaints(){
        //轨迹画笔
        tracePaint = new Paint();
        tracePaint.setAntiAlias(true);
        tracePaint.setStrokeWidth(penStrokeWidth);
        tracePaint.setColor(Color.RED);
        tracePaint.setStyle(Paint.Style.STROKE);
        tracePaint.setStrokeCap(Paint.Cap.ROUND); //设置笔刷样式为圆型
        tracePaint.setStrokeJoin(Paint.Join.ROUND); //设置笔刷样式为画笔的结合方式
        //擦除效果.PorterDuffXfermode不能直接用在控件的Canvas上,必须用在Bitmap的Canvas上.
        Xfermode xFermode = new PorterDuffXfermode(PorterDuff.Mode.XOR);
        tracePaint.setXfermode(xFermode);

        //轨迹
        tracePath = new Path();

        //蒙板的画笔
        coverPaint = new Paint();
        coverPaint.setAntiAlias(true);
        coverPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        coverPaint.setColor(coverColor);

        //底层Bitmap的画笔
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

    }

    Rect clipCoverBitmapRect = new Rect(); //蒙板图片的裁剪矩阵
    Rect coverBitmapCanvasRect = new Rect(); //蒙板图片绘制到画布上的位置矩阵

    //这里,在使蒙板图片不变形的前提下,对蒙板图片进行放大或者缩小,并进行适当的裁剪,使它可以自适应控件的大小.
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
/*      //在没有自定义属性时,调整蒙板图片的代码
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; //先测量蒙板图片的大小
        coverBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.scratch,options); //没有实际加载Bitmap,不用recycle

        int coverBitmapWidth = options.outWidth; //蒙板图片的原始大小
        int coverBitmapHeight = options.outHeight;
        float wRatio = (float)getWidth()/(float)coverBitmapWidth; //控件与蒙板图片的宽度比
        float hRatio = (float)getHeight()/(float)coverBitmapHeight; //控件与蒙板图片的长度比

        Bitmap coverBitmapSource = BitmapFactory.decodeResource(getResources(),R.drawable.scratch);
        Matrix matrix = new Matrix();
        float scaleRatio = wRatio<hRatio?hRatio:wRatio; //放大或缩小蒙板图片,按长或宽中较大的进行放缩,可以保证图片可以完整覆盖控件
        matrix.setScale(scaleRatio,scaleRatio); //放缩比例一样,可以防止蒙板图片变形
        coverBitmap = Bitmap.createBitmap(coverBitmapSource,0,0,coverBitmapSource.getWidth(),coverBitmapSource.getHeight(),matrix,true);
        coverBitmapSource.recycle();

        int finalHeight = coverBitmap.getHeight(); //蒙板图片的最终大小
        int finalWidth = coverBitmap.getWidth();
        int coverBitmapStartX = (finalWidth - getWidth()) / 2; //蒙板图片的最终裁剪位置
        int coverBitmapStartY = (finalHeight - getHeight()) / 2;

        clipCoverBitmapRect.set(coverBitmapStartX, coverBitmapStartY, getWidth() + coverBitmapStartX, getHeight() + coverBitmapStartY);
        coverBitmapCanvasRect.set(0, 0, getWidth(), getHeight());

*/
        //调整蒙板图片
        if(null != coverBitmap){
            coverBitmap = adjustCoverBitmap(coverBitmap,fitStyle);

            if(fitStyle == 1) {
                int finalHeight = coverBitmap.getHeight(); //蒙板图片的最终大小
                int finalWidth = coverBitmap.getWidth();
                int coverBitmapStartX = (finalWidth - getWidth()) / 2; //蒙板图片的最终裁剪位置
                int coverBitmapStartY = (finalHeight - getHeight()) / 2;

                clipCoverBitmapRect.set(coverBitmapStartX, coverBitmapStartY, getWidth() + coverBitmapStartX, getHeight() + coverBitmapStartY);
                coverBitmapCanvasRect.set(0, 0, getWidth(), getHeight());
            }else{
                clipCoverBitmapRect.set(0, 0, coverBitmap.getWidth(), coverBitmap.getHeight());
                coverBitmapCanvasRect.set(0, 0, getWidth(), getHeight());
            }
        }

        //蒙板图片的底层Bitmap
        mBitmap = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
        mBitmapCanvas = new Canvas(mBitmap);
    }

    /**
     * 调整蒙板图片
     * @param mBitmap
     * @param fitStyle
     * @return
     */
    private Bitmap adjustCoverBitmap(Bitmap mBitmap,int fitStyle){

        int coverBitmapWidth = mBitmap.getWidth(); //蒙板图片的原始大小
        int coverBitmapHeight = mBitmap.getHeight();
        float wRatio = (float)getWidth()/(float)coverBitmapWidth; //控件与蒙板图片的宽度比
        float hRatio = (float)getHeight()/(float)coverBitmapHeight; //控件与蒙板图片的长度比

        Matrix matrix = new Matrix();
        if(fitStyle == 0){ //fitXY
            matrix.setScale(wRatio,hRatio);
        }else{ // =1,centerCrop
            float scaleRatio = wRatio<hRatio?hRatio:wRatio; //放大或缩小蒙板图片,按长或宽中较大的进行放缩,可以保证图片可以完整覆盖控件
            matrix.setScale(scaleRatio,scaleRatio); //放缩比例一样,可以防止蒙板图片变形
        }

        Bitmap coverBitmapSource = Bitmap.createBitmap(mBitmap,0,0,mBitmap.getWidth(),mBitmap.getHeight(),matrix,true);

        return coverBitmapSource;
    }

    int[] pixels; //蒙板图片的像素集合
    int[] index; //样本像素的索引集合
    boolean isFirst = true;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap,0,0,paint); //先把底层Bitmap绘制到控件的画布上.

        if(null != coverBitmap) {
            mBitmapCanvas.drawBitmap(coverBitmap, clipCoverBitmapRect, coverBitmapCanvasRect, coverPaint); //画蒙板图片
        }else {
            mBitmapCanvas.drawRect(0,0,getWidth(),getHeight(),coverPaint); //当没有蒙板图片时,用色值代表蒙板图片
        }
        if(isFirst){
            if(null == pixels){
                pixels = new int[getWidth()*getHeight()];
            }
            //获取mBitmap上的全部像素点
            mBitmap.getPixels(pixels,0,getWidth(),0,0,getWidth(),getHeight());
            //一次校验所有的像素点,工作量稍大.所以,进行一个采样操作.index记录样本像素在pixels中的索引值.
            index = Utils.getPixels(getHeight(),getWidth(),penStrokeWidth);
            isFirst = false;
        }

        if(null != tracePath) {
            mBitmapCanvas.drawPath(tracePath, tracePaint);
        }
    }

    private float downX = 0,downY = 0;
    private float moveX = 0,moveY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                if(null != tracePath) {
                    tracePath.moveTo(downX, downY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = event.getX();
                moveY = event.getY();
                if(null != tracePath) {
                    tracePath.lineTo(moveX, moveY);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP: //抬起时,检查擦除的进度
                if(null != onWipeListener && null != tracePath){
                    mBitmap.getPixels(pixels,0,getWidth(),0,0,getWidth(),getHeight());
                    onWipeListener.onWipe(Utils.checkCell(pixels,index));
                }
                break;
            default:
                break;
        }
        return true;
    }

    //监听擦除的进度
    public interface OnWipeListener{
        void onWipe(float progress);
    }

    private OnWipeListener onWipeListener;

    public void setOnWipeListener(OnWipeListener onWipeListener){
        this.onWipeListener = onWipeListener;
    }

    /**
     * 清除蒙板
     */
    public void clearOverBitmap(){
        Xfermode xFermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

        coverPaint.setXfermode(xFermode);
        mBitmapCanvas.drawRect(0,0,getWidth(),getHeight(),coverPaint);
        tracePath.reset();
        tracePath = null;

        invalidate();
    }
}

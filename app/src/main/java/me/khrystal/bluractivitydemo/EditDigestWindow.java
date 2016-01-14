package me.khrystal.bluractivitydemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.qiujuer.genius.blur.StackBlur;

/**
 * PopWindow by background blur
 *
 * Notice: use FastBlur or blur(1) is java method test blur time first is 127ms , after is 40~50ms
 *         use blur(2) is jni bitmap method test blur time first is 120ms ,after is 20~30ms
 *         use blur(3) is jni pixels method test blur time firsr is 104ms ,after is 40+ms
 *
 *         recommed to use blur(2)
 * @FileName: me.khrystal.bluractivitydemo.EditDigestWindow.java
 * @author: kHRYSTAL
 * @email: 723526676@qq.com
 * @date: 2016-01-13 10:37
 */
public class EditDigestWindow extends PopupWindow{

    Activity mContext;
    private int mWidth;
    private int mHeight;
    private int statusBarHeight ;
    private Bitmap mBitmap= null;
    private Bitmap overlay = null;

    private Handler mHandler = new Handler();
    private ScaleAnimation mShowAnim;
    private TextView testView;
    private RelativeLayout testLayout;

    public EditDigestWindow(Activity context){
        mContext = context;
    }


    /**
     * 设置宽高为除去上方状态栏的全屏
     * 实例化后需要手动调用
     */
    public void init(){
        Rect frame = new Rect();
        mContext.getWindow().getDecorView()
                .getWindowVisibleDisplayFrame(frame);
        statusBarHeight = frame.top;
        Log.d("EditDigest",""+statusBarHeight);
        DisplayMetrics metrics = new DisplayMetrics();
        mContext.getWindowManager().getDefaultDisplay()
                .getMetrics(metrics);
        mWidth = metrics.widthPixels;
        mHeight = metrics.heightPixels;

        setWidth(mWidth);
        setHeight(mHeight);
        mShowAnim = new ScaleAnimation(0.5f, 1f, 0.5f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mShowAnim.setFillAfter(true);
        mShowAnim.setDuration(500);
    }


    /**
     * Java method to blur by FastBlur
     * @return
     */
    private Bitmap blur() {
        if (null != overlay) {
            return overlay;
        }
        long startMs = System.currentTimeMillis();

        View view = mContext.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        mBitmap = view.getDrawingCache();

        float scaleFactor = 8;//缩放比例
        float radius = 10;//模糊程度
        int width = mBitmap.getWidth();
        int height =  mBitmap.getHeight();

        overlay = Bitmap.createBitmap((int) (width / scaleFactor),(int) (height / scaleFactor),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(mBitmap, 0, 0, paint);

        overlay = FastBlur.doBlur(overlay, (int) radius, true);
        Log.i("EditDigestWindow", "blur time is:" + (System.currentTimeMillis() - startMs));
        return overlay;
    }


    private Bitmap blur(int i) {


        long startMs = System.currentTimeMillis();

        // Is scale
        View view = mContext.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        mBitmap = view.getDrawingCache();
        //TODO 去掉通知栏
        int width = mBitmap.getWidth();
        int height =  mBitmap.getHeight();
        mBitmap = Bitmap.createBitmap(mBitmap,0,statusBarHeight,width,height-statusBarHeight);
        height -= statusBarHeight;
        float scaleFactor = 8;//缩放比例
        float radius = 3;//模糊程度

        overlay = Bitmap.createBitmap((int) (width / scaleFactor),(int) (height / scaleFactor),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(mBitmap, 0, 0, paint);


        //is blur
        // Java
        if (i == 1)
            overlay = StackBlur.blur(overlay, (int) radius, false);
            // Bitmap JNI Native
        else if (i == 2)
            overlay = StackBlur.blurNatively(overlay, (int) radius, false);
            // Pixels JNI Native
        else if (i == 3)
            overlay = StackBlur.blurNativelyPixels(overlay, (int) radius, false);

        Log.i("EditDigestWindow", "blur time is:" + (System.currentTimeMillis() - startMs));
        return overlay;
    }


    /**
     * display
     * @param anchor
     * @param bottomMargin
     */
    public void showMoreWindow(View anchor,int bottomMargin) {
        final LinearLayout layout = (LinearLayout) LayoutInflater
                .from(mContext).inflate(R.layout.pop_edit_digest, null);

//TODO  绑定控件 回调接口
        testLayout = (RelativeLayout)layout.findViewById(R.id.test_layout);
        testView = (TextView)layout.findViewById(R.id.test_text);


        setContentView(layout);

        android.widget.RelativeLayout.LayoutParams params =
                new android.widget.RelativeLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                        AbsoluteLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = bottomMargin;
        params.topMargin = 200;
        params.leftMargin = 18;

        //TODO 设置内部控件 Anim
        setAnimationStyle(android.R.style.Animation_Toast);
        //设置背景高斯模糊
        setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), blur(2)));
        //外部是否可以点击
        setOutsideTouchable(true);
        setFocusable(true);
        showAtLocation(anchor, Gravity.BOTTOM, 0, statusBarHeight);
        testLayout.startAnimation(mShowAnim);
    }


    public void destroy() {
        if (null != overlay) {
            overlay.recycle();
            overlay = null;
            System.gc();
        }
        if (null != mBitmap) {
            mBitmap.recycle();
            mBitmap = null;
            System.gc();
        }
    }
}

package jungle68.com.library.core;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import jungle68.com.library.R;

import static jungle68.com.library.core.ViewMode.LINE;
import static jungle68.com.library.core.ViewMode.RECT;

/**
 * @Describe just for record voice show  view
 * @Author Jungle68
 * @Date 2016/7/4
 * @Contact master.jungle68@gmail.com
 */

public class RecordVoiceLineView extends View {
    private static final int DEFAULT_MIDDELE_LINE_COLOR = Color.RED;
    private static final int DEFAULT_VOICE_LINE_COLOR = Color.RED;
    private int mMiddleLineColor = DEFAULT_MIDDELE_LINE_COLOR;
    private int mVoiceLineColor = DEFAULT_VOICE_LINE_COLOR;
    private float mMiddleLineHeight = 4;
    private Paint mMiddleLinePaint;
    private Paint mPaintVoicLine;
    private int mMode;
    /**
     * 灵敏度
     */
    private int mSensibility = 4;

    private float mMaxVolume = 100;


    private float mTranslateX = 0;
    private boolean mIsSet = false;

    /**
     * 振幅
     */
    private float mAmplitude = 1;
    /**
     * 音量
     */
    private float mVolume = 10;
    private int mFineness = 1;
    private float mTargetVolume = 1;

    private long mSpeedY = 80;
    private float mRectWidth = 25;
    private float mRectSpace = 5;
    private float mRectInitHeight = 4;
    private List<Rect> mRectList = new ArrayList<>();

    private long mLastTime = 0;
    private int mLineSpeed = 90;

    List<Path> mPaths = null;

    public RecordVoiceLineView(Context context) {
        this(context, null);
    }

    public RecordVoiceLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordVoiceLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            initAtts(context, attrs);
        }
        initData();
    }

    private void initAtts(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordVoiceLineView);
        mMode = typedArray.getInt(R.styleable.RecordVoiceLineView_jungle68_viewMode, LINE);
        mVoiceLineColor = typedArray.getColor(R.styleable.RecordVoiceLineView_jungle68_voiceLine, DEFAULT_VOICE_LINE_COLOR);
        mMaxVolume = typedArray.getFloat(R.styleable.RecordVoiceLineView_jungle68_maxVolume, 100);
        mSensibility = typedArray.getInt(R.styleable.RecordVoiceLineView_jungle68_sensibility, 4);
        switch (mMode) {
            case RECT:
                mRectWidth = typedArray.getDimensionPixelOffset(R.styleable.RecordVoiceLineView_jungle68_rectWidth, 25);
                mRectSpace = typedArray.getDimensionPixelOffset(R.styleable.RecordVoiceLineView_jungle68_rectSpace, 5);
                mRectInitHeight = typedArray.getDimensionPixelOffset(R.styleable.RecordVoiceLineView_jungle68_rectInitHeight, 4);
                break;
            case LINE:
                mMiddleLineColor = typedArray.getColor(R.styleable.RecordVoiceLineView_jungle68_middleLine, DEFAULT_MIDDELE_LINE_COLOR);
                mMiddleLineHeight = typedArray.getDimensionPixelOffset(R.styleable.RecordVoiceLineView_jungle68_middleLineHeight, 4);
                mLineSpeed = typedArray.getInt(R.styleable.RecordVoiceLineView_jungle68_lineSpeed, 90);
                mFineness = typedArray.getInt(R.styleable.RecordVoiceLineView_jungle68_fineness, 1);
                break;
            default:
        }
        typedArray.recycle();
    }

    private void initData() {
        if (mMode == LINE) {
            mMiddleLinePaint = new Paint();
            mMiddleLinePaint.setColor(mMiddleLineColor);
            mMiddleLinePaint.setAntiAlias(true);
            mPaths = new ArrayList<>(20);
            for (int i = 0; i < 20; i++) {
                mPaths.add(new Path());
            }
        }
        mPaintVoicLine = new Paint();
        mPaintVoicLine.setColor(mVoiceLineColor);
        mPaintVoicLine.setAntiAlias(true);
        mPaintVoicLine.setStrokeCap(Paint.Cap.ROUND);
        mPaintVoicLine.setStyle(Paint.Style.FILL);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (mMode) {
            case RECT:
                drawVoiceRect(canvas);
                break;
            case LINE:
                drawMiddleLine(canvas);
                drawVoiceLine(canvas);
                break;
            default:
        }
        run();
    }

    private void drawMiddleLine(Canvas canvas) {
        canvas.save();
        canvas.drawRect(0, getHeight() / 2 - mMiddleLineHeight / 2, getWidth(), getHeight() / 2 + mMiddleLineHeight / 2, mMiddleLinePaint);
        canvas.restore();
    }

    private void drawVoiceLine(Canvas canvas) {
        lineChange();
        canvas.save();
        int moveY = getHeight() / 2;
        for (int i = 0; i < mPaths.size(); i++) {
            mPaths.get(i).reset();
            mPaths.get(i).moveTo(getWidth(), getHeight() / 2);
        }
        for (float i = getWidth() - 1; i >= 0; i -= mFineness) {
            mAmplitude = 4 * mVolume * i / getWidth() - 4 * mVolume * i * i / getWidth() / getWidth();
            for (int n = 1; n <= mPaths.size(); n++) {
                float sin = mAmplitude * (float) Math.sin((i - Math.pow(1.22, n)) * Math.PI / 180 - mTranslateX);
                mPaths.get(n - 1).lineTo(i, (2 * n * sin / mPaths.size() - 15 * sin / mPaths.size() + moveY));
            }
        }
        for (int n = 0; n < mPaths.size(); n++) {
            if (n == mPaths.size() - 1) {
                mPaintVoicLine.setAlpha(255);
            } else {
                mPaintVoicLine.setAlpha(n * 130 / mPaths.size());
            }
            if (mPaintVoicLine.getAlpha() > 0) {
                canvas.drawPath(mPaths.get(n), mPaintVoicLine);
            }
        }
        canvas.restore();
    }

    private void drawVoiceRect(Canvas canvas) {
        int totalWidth = (int) (mRectSpace + mRectWidth);
        if (mSpeedY % totalWidth < 6) {
            Rect rect;
            if (mRectList.size() > getWidth() / (mRectSpace + mRectWidth) + 2) {
                rect = mRectList.get(0);
                rect.set((int) (-mRectWidth - 10 - mSpeedY + mSpeedY % totalWidth),
                        (int) (getHeight() / 2 - mRectInitHeight / 2 - (mVolume == 10 ? 0 : mVolume / 2)),
                        (int) (-10 - mSpeedY + mSpeedY % totalWidth),
                        (int) (getHeight() / 2 + mRectInitHeight / 2 + (mVolume == 10 ? 0 : mVolume / 2)));
                mRectList.remove(0);
            } else {
                rect = new Rect((int) (-mRectWidth - 10 - mSpeedY + mSpeedY % totalWidth),
                        (int) (getHeight() / 2 - mRectInitHeight / 2 - (mVolume == 10 ? 0 : mVolume / 2)),
                        (int) (-10 - mSpeedY + mSpeedY % totalWidth),
                        (int) (getHeight() / 2 + mRectInitHeight / 2 + (mVolume == 10 ? 0 : mVolume / 2)));
            }
            mRectList.add(rect);
        }
        canvas.translate(mSpeedY, 0);
        for (int i = mRectList.size() - 1; i >= 0; i--) {
            canvas.drawRect(mRectList.get(i), mPaintVoicLine);
        }
        rectChange();
    }

    public void setVolume(int volume) {
        if (volume > mMaxVolume * mSensibility / 25) {
            mIsSet = true;
            this.mTargetVolume = getHeight() * volume / 2 / mMaxVolume;
        }
    }

    private void lineChange() {
        if (mLastTime == 0) {
            mLastTime = System.currentTimeMillis();
            mTranslateX += 1.5;
        } else {
            if (System.currentTimeMillis() - mLastTime > mLineSpeed) {
                mLastTime = System.currentTimeMillis();
                mTranslateX += 1.5;
            } else {
                return;
            }
        }
        if (mVolume < mTargetVolume && mIsSet) {
            mVolume += getHeight() / 30;
        } else {
            mIsSet = false;
            if (mVolume <= 10) {
                mVolume = 10;
            } else {
                if (mVolume < getHeight() / 30) {
                    mVolume -= getHeight() / 60;
                } else {
                    mVolume -= getHeight() / 30;
                }
            }
        }
    }

    private void rectChange() {
        mSpeedY += 6;
        if (mVolume < mTargetVolume && mIsSet) {
            mVolume += getHeight() / 30;
        } else {
            mIsSet = false;
            if (mVolume <= 10) {
                mVolume = 10;
            } else {
                if (mVolume < getHeight() / 30) {
                    mVolume -= getHeight() / 60;
                } else {
                    mVolume -= getHeight() / 30;
                }
            }
        }
    }

    public void run() {
        switch (mMode) {
            case RECT:
                postInvalidateDelayed(60);
                break;
            case LINE:
                invalidate();
                break;
            default:

        }
    }

}
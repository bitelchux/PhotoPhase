/*
 * Copyright (C) 2016 Alex Lockwood
 * Copyright (C) 2016 Jorge Ruesga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ruesga.android.wallpapers.photophase.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Property;

//
// https://github.com/alexjlockwood/material-pause-play-animation
//
public class PlayPauseDrawable extends Drawable {

    private static final Property<PlayPauseDrawable, Float> PROGRESS =
            new Property<PlayPauseDrawable, Float>(Float.class, "progress") {
                @Override
                public Float get(PlayPauseDrawable d) {
                    return d.getProgress();
                }

                @Override
                public void set(PlayPauseDrawable d, Float value) {
                    d.setProgress(value);
                }
            };

    private final Path mLeftPauseBar = new Path();
    private final Path mRightPauseBar = new Path();
    private final Paint mPaint = new Paint();
    private final RectF mBounds = new RectF();
    private final Rect mPadding = new Rect();
    private float mPauseBarWidth;
    private float mPauseBarHeight;
    private float mPauseBarDistance;

    private float mWidth;
    private float mHeight;

    private float mProgress;
    private boolean mIsPlay;

    public PlayPauseDrawable() {
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mBounds.set(bounds);
        getPadding(mPadding);
        mWidth = mBounds.width();
        mHeight = mBounds.height();
        mPauseBarWidth = (mWidth - mPadding.left - mPadding.right) / 3;
        mPauseBarHeight = (mHeight - mPadding.top - mPadding.bottom);
        mPauseBarDistance = (mWidth - mPadding.left - mPadding.right) / 3;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        mLeftPauseBar.rewind();
        mRightPauseBar.rewind();

        // The current distance between the two pause bars.
        final float barDist = lerp(mPauseBarDistance, 0, mProgress);
        // The current width of each pause bar.
        final float barWidth = lerp(mPauseBarWidth, mPauseBarHeight / 2f, mProgress);
        // The current position of the left pause bar's top left coordinate.
        final float firstBarTopLeft = lerp(0, barWidth, mProgress);
        // The current position of the right pause bar's top right coordinate.
        final float secondBarTopRight = lerp(2 * barWidth + barDist, barWidth + barDist, mProgress);

        // Draw the left pause bar. The left pause bar transforms into the
        // top half of the play button triangle by animating the position of the
        // rectangle's top left coordinate and expanding its bottom width.
        mLeftPauseBar.moveTo(0, 0);
        mLeftPauseBar.lineTo(firstBarTopLeft, -mPauseBarHeight);
        mLeftPauseBar.lineTo(barWidth, -mPauseBarHeight);
        mLeftPauseBar.lineTo(barWidth, 0);
        mLeftPauseBar.close();

        // Draw the right pause bar. The right pause bar transforms into the
        // bottom half of the play button triangle by animating the position of the
        // rectangle's top right coordinate and expanding its bottom width.
        mRightPauseBar.moveTo(barWidth + barDist, 0);
        mRightPauseBar.lineTo(barWidth + barDist, -mPauseBarHeight);
        mRightPauseBar.lineTo(secondBarTopRight, -mPauseBarHeight);
        mRightPauseBar.lineTo(2 * barWidth + barDist, 0);
        mRightPauseBar.close();

        canvas.save();

        // Translate the play button a tiny bit to the right so it looks more centered.
        canvas.translate(lerp(0, mPauseBarHeight / 8f, mProgress), 0);

        // (1) Pause --> Play: rotate 0 to 90 degrees clockwise.
        // (2) Play --> Pause: rotate 90 to 180 degrees clockwise.
        final float rotationProgress = mIsPlay ? 1 - mProgress : mProgress;
        final float startingRotation = mIsPlay ? 90 : 0;
        canvas.rotate(lerp(startingRotation, startingRotation + 90, rotationProgress), mWidth / 2f, mHeight / 2f);

        // Position the pause/play button in the center of the drawable's bounds.
        canvas.translate(mWidth / 2f - ((2 * barWidth + barDist) / 2f), mHeight / 2f + (mPauseBarHeight / 2f));

        // Draw the two bars that form the animated pause/play button.
        canvas.drawPath(mLeftPauseBar, mPaint);
        canvas.drawPath(mRightPauseBar, mPaint);

        canvas.restore();
    }

    public Animator getPausePlayAnimator() {
        final Animator anim = ObjectAnimator.ofFloat(this, PROGRESS, mIsPlay ? 1 : 0, mIsPlay ? 0 : 1);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsPlay = !mIsPlay;
            }
        });
        return anim;
    }

    private void setProgress(float progress) {
        mProgress = progress;
        invalidateSelf();
    }

    private float getProgress() {
        return mProgress;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public boolean isPlay() {
        return mIsPlay;
    }

    /**
     * Linear interpolate between a and b with parameter t.
     */
    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
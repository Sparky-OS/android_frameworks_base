// SPDX-License-Identifier: Apache-2.0
package com.lge.real3d;

import android.content.Context;
import android.util.Log;
import android.view.S3DInfo;
import android.view.S3DSurfaceHolder;
import android.view.SurfaceHolder;

public class Real3D {
    public static final int REAL3D_ORDER_LR = 0;
    public static final int REAL3D_ORDER_L_ONLY = 32;
    public static final int REAL3D_ORDER_RL = 16;
    public static final int REAL3D_ORDER_R_ONLY = 48;
    public static final int REAL3D_TYPE_CHECKERBOARD = 6;
    public static final int REAL3D_TYPE_COLUMN_INTERLACED = 3;
    public static final int REAL3D_TYPE_FRAME_SEQUENCE = 7;
    public static final int REAL3D_TYPE_NONE = 0;
    public static final int REAL3D_TYPE_PIXEL_INTERLACED = 5;
    public static final int REAL3D_TYPE_ROW_INTERLACED = 4;
    public static final int REAL3D_TYPE_SS = 1;
    public static final int REAL3D_TYPE_TB = 2;
    public static final int REAL3D_VIEWMODE_INTERLEAVING = 0;
    public static final int REAL3D_VIEWMODE_LEFTONLY = 2;
    public static final int REAL3D_VIEWMODE_SIDEBYSIDE = 1;

    private final S3DSurfaceHolder mHolder;

    public Real3D(SurfaceHolder holder) {
        mHolder = holder instanceof S3DSurfaceHolder ? (S3DSurfaceHolder) holder : null;
        if (mHolder == null) Log.e("LegacyStereo", "Real3D needs an S3DSurfaceHolder");
    }

    public static boolean isStereoDisplayAvailable(Context context) {
        return context != null && context.getPackageManager().hasSystemFeature("com.lge.real3d.hrz");
    }

    public boolean setReal3DInfo(Real3DInfo info) {
        if (mHolder == null || info == null) return false;
        mHolder.setS3DType(info.type | info.order | (info.disableAutoDetection ? 128 : 0));
        return true;
    }

    public Real3DInfo getReal3DInfo() {
        if (mHolder == null) return null;
        S3DInfo info = mHolder.getS3DInfo();
        return new Real3DInfo(info.disableAutoDetection, info.type, info.order);
    }

    private S3DSurfaceHolder holder() {
        if (mHolder == null) throw new IllegalStateException("Real3D holder is unavailable");
        return mHolder;
    }

    public void setViewMode(int mode) { holder().setS3DViewMode(mode); }
    public int getViewMode() { return holder().getS3DViewMode(); }
    public void setConvergenceDisparity(float value) { holder().setS3DConvergenceDisparity(value); }
    public float getConvergenceDisparity() { return holder().getS3DConvergenceDisparity(); }
    public void compensateBrightness(float value) { holder().compensateS3DBrightness(value); }
    public void setMaximumPositive(float value) { holder().setMaximumPositive(value); }
    public void setMinimumNegative(float value) { holder().setMinimumNegative(value); }
}

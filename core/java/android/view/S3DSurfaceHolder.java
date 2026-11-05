// SPDX-License-Identifier: Apache-2.0
package android.view;

import android.compat.annotation.UnsupportedAppUsage;

/** Legacy LG holder ABI, independent of display output packing. @hide */
public interface S3DSurfaceHolder extends SurfaceHolder {
    @UnsupportedAppUsage
    int SURFACE_S3D_DISABLE_AUTO_DETECTION = 128;
    @UnsupportedAppUsage
    int SURFACE_S3D_FEATURE_MASK = 255;
    @UnsupportedAppUsage
    int SURFACE_S3D_ORDER_LR = 0;
    @UnsupportedAppUsage
    int SURFACE_S3D_ORDER_L_ONLY = 32;
    @UnsupportedAppUsage
    int SURFACE_S3D_ORDER_MASK = 112;
    @UnsupportedAppUsage
    int SURFACE_S3D_ORDER_RL = 16;
    @UnsupportedAppUsage
    int SURFACE_S3D_ORDER_R_ONLY = 48;
    @UnsupportedAppUsage
    int SURFACE_S3D_ORDER_TOP_ONLY = 64;
    @UnsupportedAppUsage
    int SURFACE_S3D_TYPE_AUTO_CONVERSION = 8;
    @UnsupportedAppUsage
    int SURFACE_S3D_TYPE_CHECKERBOARD = 6;
    @UnsupportedAppUsage
    int SURFACE_S3D_TYPE_COLUMN_INTERLACED = 3;
    @UnsupportedAppUsage
    int SURFACE_S3D_TYPE_END = 8;
    @UnsupportedAppUsage
    int SURFACE_S3D_TYPE_FRAME_SEQUENCE = 7;
    @UnsupportedAppUsage
    int SURFACE_S3D_TYPE_LAYERED_3D_UI = 9;
    @UnsupportedAppUsage
    int SURFACE_S3D_TYPE_MASK = 15;
    @UnsupportedAppUsage
    int SURFACE_S3D_TYPE_NONE = 0;
    @UnsupportedAppUsage
    int SURFACE_S3D_TYPE_PIXEL_INTERLACED = 5;
    @UnsupportedAppUsage
    int SURFACE_S3D_TYPE_ROW_INTERLACED = 4;
    @UnsupportedAppUsage
    int SURFACE_S3D_TYPE_SS = 1;
    @UnsupportedAppUsage
    int SURFACE_S3D_TYPE_START = 0;
    @UnsupportedAppUsage
    int SURFACE_S3D_TYPE_TB = 2;

    @UnsupportedAppUsage
    void setS3DType(int type);
    @UnsupportedAppUsage
    int getS3DType();
    @UnsupportedAppUsage
    void setS3DInfo(S3DInfo info);
    @UnsupportedAppUsage
    S3DInfo getS3DInfo();
    @UnsupportedAppUsage
    void setS3DViewMode(int mode);
    @UnsupportedAppUsage
    int getS3DViewMode();
    @UnsupportedAppUsage
    void setS3DConvergenceDisparity(float disparity);
    @UnsupportedAppUsage
    float getS3DConvergenceDisparity();
    @UnsupportedAppUsage
    void compensateS3DBrightness(float brightness);
    @UnsupportedAppUsage
    void setMaximumPositive(float value);
    @UnsupportedAppUsage
    void setMinimumNegative(float value);
}

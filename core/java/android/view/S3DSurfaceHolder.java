// SPDX-License-Identifier: Apache-2.0
package android.view;

/** Legacy LG holder ABI, independent of display output packing. @hide */
public interface S3DSurfaceHolder extends SurfaceHolder {
    int SURFACE_S3D_DISABLE_AUTO_DETECTION = 128;
    int SURFACE_S3D_FEATURE_MASK = 255;
    int SURFACE_S3D_ORDER_LR = 0;
    int SURFACE_S3D_ORDER_L_ONLY = 32;
    int SURFACE_S3D_ORDER_MASK = 112;
    int SURFACE_S3D_ORDER_RL = 16;
    int SURFACE_S3D_ORDER_R_ONLY = 48;
    int SURFACE_S3D_ORDER_TOP_ONLY = 64;
    int SURFACE_S3D_TYPE_AUTO_CONVERSION = 8;
    int SURFACE_S3D_TYPE_CHECKERBOARD = 6;
    int SURFACE_S3D_TYPE_COLUMN_INTERLACED = 3;
    int SURFACE_S3D_TYPE_END = 8;
    int SURFACE_S3D_TYPE_FRAME_SEQUENCE = 7;
    int SURFACE_S3D_TYPE_LAYERED_3D_UI = 9;
    int SURFACE_S3D_TYPE_MASK = 15;
    int SURFACE_S3D_TYPE_NONE = 0;
    int SURFACE_S3D_TYPE_PIXEL_INTERLACED = 5;
    int SURFACE_S3D_TYPE_ROW_INTERLACED = 4;
    int SURFACE_S3D_TYPE_SS = 1;
    int SURFACE_S3D_TYPE_START = 0;
    int SURFACE_S3D_TYPE_TB = 2;

    void setS3DType(int type);
    int getS3DType();
    void setS3DInfo(S3DInfo info);
    S3DInfo getS3DInfo();
    void setS3DViewMode(int mode);
    int getS3DViewMode();
    void setS3DConvergenceDisparity(float disparity);
    float getS3DConvergenceDisparity();
    void compensateS3DBrightness(float brightness);
    void setMaximumPositive(float value);
    void setMinimumNegative(float value);
}

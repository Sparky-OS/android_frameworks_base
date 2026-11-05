// SPDX-License-Identifier: Apache-2.0
package android.view;

import android.util.Log;

/** SurfaceView-owned state; requests made before creation follow its new producer. @hide */
abstract class LegacyS3DSurfaceHolder implements S3DSurfaceHolder {
    private int mType;
    private int mViewMode;
    private boolean mRequested;

    @Override
    public synchronized void setS3DType(int type) {
        mType = type;
        mRequested = true;
        applyStereoLayout();
    }

    @Override
    public synchronized int getS3DType() { return mType; }

    @Override
    public synchronized void setS3DInfo(S3DInfo info) {
        if (info == null) throw new IllegalArgumentException("S3DInfo must not be null");
        setS3DType(info.type | info.order | (info.disableAutoDetection ? 128 : 0));
    }

    @Override
    public synchronized S3DInfo getS3DInfo() {
        return new S3DInfo((mType & 128) != 0, mType & 15, mType & 112);
    }

    @Override
    public synchronized void setS3DViewMode(int mode) {
        mViewMode = mode;
        mRequested = true;
        applyStereoLayout();
    }

    @Override
    public synchronized int getS3DViewMode() { return mViewMode; }

    final synchronized void applyStereoLayout() {
        if (!mRequested || !getSurface().isValid()) return;
        int layout = 0;
        int type = mType & 15;
        int order = mType & 112;
        String reason = null;
        if ((mType & ~255) != 0) {
            reason = "reserved LG type bits";
        } else if (mViewMode != 0 && mViewMode != 1) {
            reason = "LG single-eye/unknown view mode requires a separate extraction path";
        } else if ((mType & 128) == 0) {
            // Leave existing producer marks available; never infer packing from pixels.
            if (type != 0 || order != 0) reason = "LG automatic detection with a manual fallback is unavailable";
        } else if (type == 0) {
            layout = 1;
        } else if (order != 0 && order != 16) {
            reason = "LG single-eye/unknown order requires a separate extraction path";
        } else {
            switch (type) {
                case 1: layout = 2; break;
                case 2: layout = 4; break;
                case 3: layout = 8; break;
                case 4: layout = 6; break;
                case 6: layout = 10; break;
                default: reason = "LG input shape has no verified single-buffer mapping";
            }
            if (layout != 0 && order == 16) layout++;
        }
        if (reason != null) Log.e("LegacyStereo", reason + "; type=" + mType
                + " viewMode=" + mViewMode + "; declaration cleared");
        if (!getSurface().setLegacyStereoLayout(layout)) {
            Log.e("LegacyStereo", "LG holder request was not applied to its buffer layer");
        }
    }

    private static UnsupportedOperationException unavailable(String effect) {
        Log.e("LegacyStereo", "LG " + effect + " has no verified implementation");
        return new UnsupportedOperationException("LG " + effect + " is unavailable");
    }

    @Override
    public void setS3DConvergenceDisparity(float value) { throw unavailable("convergence"); }
    @Override
    public float getS3DConvergenceDisparity() { throw unavailable("convergence"); }
    @Override
    public void compensateS3DBrightness(float value) { throw unavailable("brightness compensation"); }
    @Override
    public void setMaximumPositive(float value) { throw unavailable("maximum disparity"); }
    @Override
    public void setMinimumNegative(float value) { throw unavailable("minimum disparity"); }
}

// SPDX-License-Identifier: Apache-2.0
package android.view;

import android.graphics.BLASTBufferQueue;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/** Keep a profile TextureView's producer separate from its ordinary controls. @hide */
public final class LegacyGadmeiTexture {
    private static final WeakHashMap<SurfaceTexture, WeakReference<LegacyGadmeiTexture>> ROUTES =
            new WeakHashMap<>();
    private final TextureView mView;
    private final ViewRootImpl mRoot;
    private final SurfaceTexture mTexture;
    private final SurfaceControl mContainer;
    private final SurfaceControl mControl;
    private final BLASTBufferQueue mQueue;
    private final Surface mSurface;
    private int mWidth;
    private int mHeight;
    private static android.os.Handler sCopyHandler;

    private static synchronized android.os.Handler copyHandler() {
        if (sCopyHandler == null) {
            android.os.HandlerThread thread = new android.os.HandlerThread("GadmeiTextureCopy");
            thread.start();
            sCopyHandler = new android.os.Handler(thread.getLooper());
        }
        return sCopyHandler;
    }

    void copyInto(android.graphics.Bitmap bitmap) {
        java.util.concurrent.CountDownLatch copied = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicInteger result = new java.util.concurrent.atomic.AtomicInteger(-1);
        PixelCopy.request(mSurface, bitmap, code -> { result.set(code); copied.countDown(); },
                copyHandler());
        try {
            if (!copied.await(1, java.util.concurrent.TimeUnit.SECONDS)
                    || result.get() != PixelCopy.SUCCESS) {
                android.util.Log.e("LegacyStereo", "Gadmei TextureView bitmap copy failed: " + result.get());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gadmei TextureView bitmap copy interrupted", e);
        }
    }

    LegacyGadmeiTexture(TextureView view, SurfaceTexture texture) {
        mView = view;
        mRoot = view.getViewRootImpl();
        mTexture = texture;
        mWidth = Math.max(1, view.getWidth());
        mHeight = Math.max(1, view.getHeight());
        mContainer = new SurfaceControl.Builder().setName("GadmeiTextureView bounds")
                .setParent(mRoot.getBoundsLayer()).setContainerLayer().build();
        mControl = new SurfaceControl.Builder().setName("GadmeiTextureView(BLAST)")
                .setParent(mContainer).setBLASTLayer().setOpaque(view.isOpaque()).build();
        mQueue = new BLASTBufferQueue("GadmeiTextureView", false);
        mQueue.update(mControl, mWidth, mHeight, PixelFormat.RGBA_8888);
        mSurface = mQueue.createSurfaceWithHandle();
        synchronized (ROUTES) { ROUTES.put(texture, new WeakReference<>(this)); }
        LegacyGadmeiClient.register(mRoot, mControl, 1);
        updateGeometry();
    }

    private static LegacyGadmeiTexture find(SurfaceTexture texture) {
        synchronized (ROUTES) {
            WeakReference<LegacyGadmeiTexture> ref = ROUTES.get(texture);
            return ref == null ? null : ref.get();
        }
    }

    public static Surface createSurface(SurfaceTexture texture) {
        LegacyGadmeiTexture route = find(texture);
        return route == null ? null : route.mQueue.createSurfaceWithHandle();
    }

    public static void setDefaultBufferSize(SurfaceTexture texture, int width, int height) {
        LegacyGadmeiTexture route = find(texture);
        if (route == null || width < 1 || height < 1) return;
        route.mWidth = width;
        route.mHeight = height;
        route.mQueue.update(route.mControl, width, height, PixelFormat.RGBA_8888);
        route.updateGeometry();
    }

    Surface surface() { return mSurface; }

    void updateGeometry() {
        if (!mRoot.getSurfaceControl().isValid()) return;
        int[] location = new int[2];
        mView.getLocationInWindow(location);
        Rect crop = new Rect();
        boolean shown = mView.isShown() && mView.getLocalVisibleRect(crop);
        try (SurfaceControl.Transaction transaction = new SurfaceControl.Transaction()) {
            transaction.reparent(mContainer, mRoot.getBoundsLayer()).setLayer(mContainer, -2)
                    .setPosition(mContainer, location[0], location[1])
                    .setWindowCrop(mContainer, crop)
                    .setMatrix(mControl, mView.getWidth() / (float) mWidth, 0,
                            0, mView.getHeight() / (float) mHeight)
                    .setAlpha(mControl, mView.getAlpha())
                    .setOpaque(mControl, mView.isOpaque()).show(mControl);
            if (shown) transaction.show(mContainer); else transaction.hide(mContainer);
            transaction.apply();
        }
    }

    void close() {
        synchronized (ROUTES) { ROUTES.remove(mTexture); }
        LegacyGadmeiClient.unregister(mRoot, mControl.getLayerId());
        try (SurfaceControl.Transaction transaction = new SurfaceControl.Transaction()) {
            transaction.remove(mContainer).apply();
        }
        mSurface.release();
        mQueue.destroy();
        mControl.release();
        mContainer.release();
    }
}

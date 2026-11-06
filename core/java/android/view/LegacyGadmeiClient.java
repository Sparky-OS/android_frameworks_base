// SPDX-License-Identifier: Apache-2.0
package android.view;

import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;

/** Private bridge for the documented T863/AirTab P81 family controls. @hide */
public final class LegacyGadmeiClient {
    private LegacyGadmeiClient() {}

    public static boolean enabled() {
        return SystemProperties.getBoolean("persist.waydroid.gadmei_compat", false);
    }

    public static boolean isControl(String path) {
        return enabled() && ("/sys/class/enable3d/enable-3d".equals(path)
                || "/sys/class/ppmgr/ppmgr_3d_mode".equals(path)
                || "/sys/class/twx3d/lense".equals(path));
    }

    public static boolean write(String path, String value) {
        try {
            return WindowManagerGlobal.getWindowSession().writeLegacyGadmeiControl(path, value);
        } catch (RemoteException e) {
            Log.e("LegacyStereo", "Gadmei control unavailable", e);
            return false;
        }
    }

    static void updateTextures(View view, boolean enabled) {
        if (view instanceof TextureView) ((TextureView) view).setGadmeiStereo(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) updateTextures(group.getChildAt(i), enabled);
        }
    }

    static boolean hasMedia(View view) {
        if (view == null || view.getVisibility() != View.VISIBLE) return false;
        if (view instanceof SurfaceView || view instanceof TextureView) return true;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                if (hasMedia(group.getChildAt(i))) return true;
            }
        }
        return false;
    }

    static void register(ViewRootImpl root, SurfaceControl surface, int kind) {
        if (!enabled() || root == null || surface == null || !surface.isValid()) return;
        try {
            root.mWindowSession.updateLegacyGadmeiSurface(root.mWindow, surface,
                    surface.getLayerId(), kind);
        } catch (RemoteException e) {
            Log.e("LegacyStereo", "Gadmei drawing-layer registration failed", e);
        }
    }

    static void unregister(ViewRootImpl root, int layerId) {
        if (!enabled() || root == null || layerId < 0) return;
        try {
            root.mWindowSession.updateLegacyGadmeiSurface(root.mWindow, null, layerId, 0);
        } catch (RemoteException e) {
            Log.e("LegacyStereo", "Gadmei drawing-layer withdrawal failed", e);
        }
    }
}

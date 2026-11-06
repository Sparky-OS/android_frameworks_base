// SPDX-License-Identifier: Apache-2.0
package com.android.server.wm;

import android.os.Parcel;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import android.view.LegacyGadmeiClient;
import android.view.SurfaceControl;
import android.view.WindowManager;

/** Foreground-task policy for the identified T863/AirTab P81 control family. */
final class LegacyGadmeiStereo {
    private static final int KEY = 0x53544552;
    private static final String ACTIVATOR = "fr.stereoscopie.gadmei3d";
    private final WindowManagerService mService;
    private final ArrayMap<WindowState, SparseArray<Drawing>> mDrawings = new ArrayMap<>();
    private final ArrayMap<WindowState, Boolean> mTextureModes = new ArrayMap<>();
    private Task mForeground;
    private Task mOwner;
    private int mPacking = 8;
    private boolean mEnabled;

    private static final class Drawing {
        final SurfaceControl surface;
        int kind;
        int applied;
        Drawing(SurfaceControl surface, int kind) { this.surface = surface; this.kind = kind; }
    }

    LegacyGadmeiStereo(WindowManagerService service) { mService = service; }

    void focusChanged() {
        if (!LegacyGadmeiClient.enabled()) return;
        DisplayContent display = mService.mRoot.getTopFocusedDisplayContent();
        if (display == null) return;
        WindowState focus = display.mCurrentFocus;
        if (focus == null) return;
        Task task = focus.getTask();
        if (focus != null && ACTIVATOR.equals(focus.getOwningPackage())) return;
        if (task == mForeground) return;
        if (mEnabled) Slog.i("LegacyStereo", "Gadmei withdrawal: foreground task changed");
        mForeground = task;
        mOwner = null;
        mEnabled = false;
        mPacking = 8;
        apply();
    }

    boolean write(int uid, String path, String value) {
        if (!LegacyGadmeiClient.isControl(path) || value == null || value.length() > 64) {
            return refuse("unknown endpoint or oversized write");
        }
        focusChanged();
        Task task = mForeground;
        if (task == null || !task.isActivityTypeStandardOrUndefined()
                || UserHandle.getUserId(uid) != task.mUserId) {
            return refuse("no foreground content task for caller user");
        }
        String command = value.trim();
        boolean enable = mEnabled;
        int packing = mPacking;
        if ("/sys/class/ppmgr/ppmgr_3d_mode".equals(path)) {
            switch (command) {
                case "0": enable = false; break;
                case "257": packing = 2; enable = true; break;
                case "513": packing = 4; enable = true; break;
                case "1281": packing = 3; enable = true; break;
                case "1537": packing = 5; enable = true; break;
                default: return refuse("unsupported packing " + command);
            }
        } else if ("/sys/class/enable3d/enable-3d".equals(path)) {
            switch (command) {
                case "0": enable = false; break;
                case "1": enable = true; break;
                case "2 4": enable = true; packing = 8; break;
                default: return refuse("unsupported barrier command " + command);
            }
        } else {
            switch (command) {
                case "0": enable = false; break;
                case "1": enable = true; break;
                default: return refuse("unsupported lens command " + command);
            }
        }
        mOwner = task;
        mPacking = packing;
        mEnabled = enable;
        apply();
        Slog.i("LegacyStereo", "Gadmei family task=" + task.mTaskId + " packing="
                + packing + " enabled=" + enable + " caller=" + uid + " node=" + path);
        return true;
    }

    private boolean refuse(String reason) {
        Slog.e("LegacyStereo", "Gadmei family write refused: " + reason);
        return false;
    }

    void update(WindowState window, SurfaceControl surface, int id, int kind) {
        if (!LegacyGadmeiClient.enabled() || window.getTask() == null || id < 0
                || kind < 0 || kind > 3) {
            if (surface != null) surface.release();
            return;
        }
        SparseArray<Drawing> drawings = mDrawings.get(window);
        if (drawings == null) {
            drawings = new SparseArray<>();
            mDrawings.put(window, drawings);
        }
        Drawing previous = drawings.get(id);
        if (surface == null) {
            if (previous != null) { clear(previous); previous.surface.release(); drawings.remove(id); }
        } else if (previous == null) {
            if (surface.getLayerId() != id) { surface.release(); return; }
            drawings.put(id, new Drawing(surface, kind));
        } else {
            previous.kind = kind;
            surface.release();
        }
        apply();
    }

    void remove(WindowState window) {
        mTextureModes.remove(window);
        SparseArray<Drawing> drawings = mDrawings.remove(window);
        if (drawings == null) return;
        for (int i = 0; i < drawings.size(); i++) {
            Drawing drawing = drawings.valueAt(i);
            clear(drawing);
            drawing.surface.release();
        }
        apply();
    }

    private void clear(Drawing drawing) { set(drawing, 0); }

    private void set(Drawing drawing, int layout) {
        if (drawing.applied == layout || !drawing.surface.isValid()) return;
        Parcel value = Parcel.obtain();
        try (SurfaceControl.Transaction transaction = new SurfaceControl.Transaction()) {
            if (layout != 0) value.writeInt32(layout);
            transaction.setMetadata(drawing.surface, KEY, value).apply();
            drawing.applied = layout;
            Slog.i("LegacyStereo", "Gadmei layer=" + drawing.surface.getLayerId() + " layout=" + layout);
        } finally { value.recycle(); }
    }

    private void apply() {
        boolean media = false;
        for (int i = 0; i < mDrawings.size(); i++) {
            if (mDrawings.keyAt(i).getTask() != mOwner) continue;
            SparseArray<Drawing> rows = mDrawings.valueAt(i);
            for (int j = 0; j < rows.size(); j++) media |= rows.valueAt(j).kind != 0;
        }
        for (int i = 0; i < mDrawings.size(); i++) {
            WindowState window = mDrawings.keyAt(i);
            boolean textureMode = mEnabled && window.getTask() == mOwner;
            Boolean previousMode = mTextureModes.get(window);
            if (previousMode == null || previousMode != textureMode) {
                mTextureModes.put(window, textureMode);
                try { window.mClient.dispatchLegacyGadmeiStereo(textureMode); }
                catch (android.os.RemoteException e) {
                    Slog.w("LegacyStereo", "Gadmei producer-mode callback unavailable", e);
                }
            }
            SparseArray<Drawing> rows = mDrawings.valueAt(i);
            for (int j = 0; j < rows.size(); j++) {
                Drawing drawing = rows.valueAt(j);
                boolean target = mEnabled && window.getTask() == mOwner
                        && (drawing.kind == 1 || (!media && drawing.kind == 0
                        && window.mAttrs.type == WindowManager.LayoutParams.TYPE_BASE_APPLICATION));
                set(drawing, target ? mPacking : 0);
            }
        }
    }
}

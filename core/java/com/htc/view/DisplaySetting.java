// SPDX-License-Identifier: Apache-2.0
package com.htc.view;

import android.util.Log;
import android.view.Surface;

/** HTC OpenSense Surface ABI. @hide */
public class DisplaySetting {
    public static final int STEREOSCOPIC_3D_FORMAT_OFF = 0;
    public static final int STEREOSCOPIC_3D_FORMAT_SIDE_BY_SIDE = 1;
    public static final int STEREOSCOPIC_3D_FORMAT_TOP_BOTTOM = 2;
    public static final int STEREOSCOPIC_3D_FORMAT_INTERLEAVED = 3;

    public DisplaySetting() {}

    public static boolean setStereoscopic3DFormat(Surface surface, int format) {
        if (surface == null) {
            Log.e("LegacyStereo", "HTC request has no Surface");
            return false;
        }
        int layout;
        switch (format) {
            case STEREOSCOPIC_3D_FORMAT_OFF: layout = 1; break;
            case STEREOSCOPIC_3D_FORMAT_SIDE_BY_SIDE: layout = 2; break;
            case STEREOSCOPIC_3D_FORMAT_TOP_BOTTOM: layout = 4; break;
            default:
                Log.e("LegacyStereo", "HTC input format " + format
                        + " has no verified mapping; declaration cleared");
                surface.setLegacyStereoLayout(0);
                return false;
        }
        return surface.setLegacyStereoLayout(layout);
    }
}

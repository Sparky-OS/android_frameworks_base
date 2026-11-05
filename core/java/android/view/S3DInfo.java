// SPDX-License-Identifier: Apache-2.0
package android.view;

import android.compat.annotation.UnsupportedAppUsage;

/** Legacy LG input description. @hide */
public class S3DInfo {
    @UnsupportedAppUsage
    public boolean disableAutoDetection;
    @UnsupportedAppUsage
    public int type;
    @UnsupportedAppUsage
    public int order;

    @UnsupportedAppUsage
    public S3DInfo() {}

    @UnsupportedAppUsage
    public S3DInfo(boolean disableAutoDetection, int type, int order) {
        this.disableAutoDetection = disableAutoDetection;
        this.type = type;
        this.order = order;
    }
}

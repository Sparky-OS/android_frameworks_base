// SPDX-License-Identifier: Apache-2.0
package android.view;

/** Legacy LG input description. @hide */
public class S3DInfo {
    public boolean disableAutoDetection;
    public int type;
    public int order;

    public S3DInfo() {}

    public S3DInfo(boolean disableAutoDetection, int type, int order) {
        this.disableAutoDetection = disableAutoDetection;
        this.type = type;
        this.order = order;
    }
}

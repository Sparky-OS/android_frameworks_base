// SPDX-License-Identifier: Apache-2.0
package com.lge.real3d;

public class Real3DInfo {
    public boolean disableAutoDetection;
    public int type;
    public int order;

    public Real3DInfo() {}

    public Real3DInfo(boolean disableAutoDetection, int type, int order) {
        this.disableAutoDetection = disableAutoDetection;
        this.type = type;
        this.order = order;
    }
}

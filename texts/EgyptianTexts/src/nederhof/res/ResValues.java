// Various auxiliary values in RES.

package nederhof.res;

import java.util.*;

public class ResValues {

    // Text directions.
    public static final int DIR_HLR = 0, DIR_HRL = 1, DIR_VLR = 2, DIR_VRL = 3;
    // Additional partial directions for forcing orientation.
    public static final int DIR_NONE = 10, DIR_H = 11, DIR_V = 12, DIR_LR = 13, DIR_RL = 14;

    public static boolean isH(int dir) {
        return dir == DIR_HLR || dir == DIR_HRL;
    }
    public static boolean isV(int dir) {
        return dir == DIR_VLR || dir == DIR_VRL;
    }
    public static boolean isLR(int dir) {
        return dir == DIR_HLR || dir == DIR_VLR;
    }
    public static boolean isRL(int dir) {
        return dir == DIR_HRL || dir == DIR_VRL;
    }

    // Colors may be represented by underline or overline.
    public static final int NO_LINE = 0;
    public static final int UNDERLINE = 1;
    public static final int OVERLINE = 2;

}

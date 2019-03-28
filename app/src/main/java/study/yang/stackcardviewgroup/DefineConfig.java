package study.yang.stackcardviewgroup;

import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;


/**
 * 布局管理器的配置参数
 */
public class DefineConfig {

    @IntRange(from = 2)
    public int space = 60;
    public int initialStackCount = 0;
    @FloatRange(from = 0f, to = 1f)
    public float scaleRatio;


}

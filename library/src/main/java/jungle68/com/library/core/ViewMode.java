package jungle68.com.library.core;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static jungle68.com.library.core.ViewMode.LINE;
import static jungle68.com.library.core.ViewMode.RECT;

/**
 * @Describe
 * @Author Jungle68
 * @Date 2017/7/4
 * @Contact master.jungle68@gmail.com
 */
@IntDef({LINE, RECT})
@Retention(RetentionPolicy.SOURCE)
public @interface ViewMode {
    int LINE = 0, RECT = 1;
}

package bilal.com.createdynamicwidgets.CustomFonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by User1 on 11-Nov-16.
 */
public class RegularCustomTextView extends TextView {
    private static Typeface mTypeface;

    public RegularCustomTextView(final Context context) {
        this(context, null);
    }

    public RegularCustomTextView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RegularCustomTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        if (mTypeface == null) {
            mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/MyriadProRegular.otf");
        }
        setTypeface(mTypeface);
    }
}
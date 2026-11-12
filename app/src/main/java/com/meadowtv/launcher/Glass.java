package com.meadowtv.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Simulación ligera de "Liquid Glass": degradados, bordes y brillo. Sin blur ni APIs nuevas. */
final class Glass {
    static final int CREAM = 0xFFFDFBF0;
    static final int SHADOW = 0xAA2B4632;

    static int dp(Context c, float v) {
        return Math.max(1, Math.round(v * c.getResources().getDisplayMetrics().density));
    }

    private static Drawable layer(Context c, boolean focused, float radiusDp) {
        float r = dp(c, radiusDp);
        GradientDrawable base = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                focused ? new int[]{0xB8F6FCEA, 0x8CDDF1FF} : new int[]{0x4DF6FCEA, 0x30DDF1FF});
        base.setCornerRadius(r);
        base.setStroke(dp(c, focused ? 3 : 1.5f), focused ? 0xFFFFFFFF : 0x99FFFFFF);
        GradientDrawable sheen = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{focused ? 0x99FFFFFF : 0x59FFFFFF, 0x00FFFFFF});
        int in = dp(c, 3);
        sheen.setCornerRadius(Math.max(0, r - in));
        LayerDrawable ld = new LayerDrawable(new Drawable[]{base, sheen});
        ld.setLayerInset(1, in, in, in, in);
        return ld;
    }

    static Drawable background(Context c, float radiusDp) {
        StateListDrawable sl = new StateListDrawable();
        sl.addState(new int[]{android.R.attr.state_focused}, layer(c, true, radiusDp));
        sl.addState(new int[]{}, layer(c, false, radiusDp));
        sl.setEnterFadeDuration(160);
        sl.setExitFadeDuration(160);
        return sl;
    }

    /** Foco: escala suave + elevación (el fondo se ilumina por el StateListDrawable). */
    static void focusFx(View v) {
        final float lift = dp(v.getContext(), 8);
        v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View view, boolean has) {
                view.animate().scaleX(has ? 1.08f : 1f).scaleY(has ? 1.08f : 1f)
                        .translationZ(has ? lift : 0f).setDuration(160).start();
            }
        });
    }

    static TextView label(Context c, String s, float sp, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(CREAM);
        t.setShadowLayer(dp(c, 3), 0, dp(c, 1), SHADOW);
        t.setTypeface(Typeface.create(bold ? "sans-serif-medium" : "sans-serif-light", Typeface.NORMAL));
        return t;
    }

    static LinearLayout card(Context c, Drawable icon, String text, int iconDp, float textSp) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);
        l.setFocusable(true);
        l.setClickable(true);
        l.setLongClickable(true);
        l.setClipChildren(false);
        l.setBackground(background(c, 22));
        l.setPadding(dp(c, 8), dp(c, 8), dp(c, 8), dp(c, 8));
        ImageView iv = new ImageView(c);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setImageDrawable(icon);
        l.addView(iv, new LinearLayout.LayoutParams(dp(c, iconDp), dp(c, iconDp)));
        TextView t = label(c, text, textSp, true);
        t.setGravity(Gravity.CENTER);
        t.setSingleLine(true);
        t.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(c, 8);
        l.addView(t, lp);
        focusFx(l);
        return l;
    }

    static TextView pill(Context c, String text) {
        TextView t = label(c, text, 16, true);
        t.setGravity(Gravity.CENTER);
        t.setFocusable(true);
        t.setClickable(true);
        t.setBackground(background(c, 26));
        t.setPadding(dp(c, 24), dp(c, 10), dp(c, 24), dp(c, 10));
        focusFx(t);
        return t;
    }

    static Drawable letterIcon(Context c, String text) {
        int s = dp(c, 96);
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0xFFBFDDAA);
        cv.drawCircle(s / 2f, s / 2f, s / 2f - 2, p);
        p.setColor(0xFF3E5A44);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(s * 0.5f);
        p.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        cv.drawText(text.substring(0, 1).toUpperCase(), s / 2f, s / 2f + s * 0.18f, p);
        return new BitmapDrawable(c.getResources(), b);
    }

    static Drawable gridIcon(Context c) {
        int s = dp(c, 96);
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0xFFE9F5DA);
        float cell = s / 4f, r = s * 0.075f;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                cv.drawRoundRect((i + 0.7f) * cell - r, (j + 0.7f) * cell - r,
                        (i + 0.7f) * cell + r * 1.6f, (j + 0.7f) * cell + r * 1.6f, r, r, p);
        return new BitmapDrawable(c.getResources(), b);
    }
}

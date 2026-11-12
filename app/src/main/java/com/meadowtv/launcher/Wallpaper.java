package com.meadowtv.launcher;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import java.util.Random;

/**
 * Pradera generada por código (sin Internet ni archivos pesados): cielo pálido, montañas,
 * neblina, colinas, manada de caballos en silueta, vignette y grano sutil.
 * Si existe res/drawable-nodpi/meadow_wallpaper.jpg, MainActivity usa esa foto en su lugar.
 */
final class Wallpaper {
    static Bitmap render(int w, int h) {
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Random rnd = new Random(7);

        p.setShader(new LinearGradient(0, 0, 0, h * 0.62f, 0xFFB5DBEF, 0xFFF1F5E3, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, p);
        p.setShader(new RadialGradient(w * 0.78f, h * 0.42f, w * 0.5f,
                new int[]{0xD9FFF0C4, 0x00FFF0C4}, null, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, p);

        ridge(c, p, w, h, 0.43f, 0.11f, 2.2f, 0xFFA7C4C9, 1);
        ridge(c, p, w, h, 0.49f, 0.08f, 3.5f, 0xFF8FB6AE, 2);
        mist(c, p, w, h, 0.40f, 0.58f, 0xAA);
        ridge(c, p, w, h, 0.57f, 0.05f, 2.0f, 0xFFA9CE8C, 3);
        ridge(c, p, w, h, 0.64f, 0.05f, 3.0f, 0xFF93C079, 4);
        mist(c, p, w, h, 0.62f, 0.76f, 0x77);

        // Manada
        p.setShader(null);
        for (int i = 0; i < 8; i++) {
            float x = w * (0.10f + 0.105f * i) + rnd.nextInt(30);
            float y = h * (0.70f + 0.045f * (i % 3)) + rnd.nextInt(10);
            float s = (h / 720f) * (1.0f + 0.22f * (i % 3));
            horse(c, p, x, y, s, i % 2 == 0);
        }
        mist(c, p, w, h, 0.74f, 0.86f, 0x55);

        // Pradera en primer plano
        p.setShader(new LinearGradient(0, h * 0.76f, 0, h, 0xFF9CCB7E, 0xFF6C9E58, Shader.TileMode.CLAMP));
        c.drawRect(0, h * 0.80f, w, h, p);
        p.setShader(null);
        p.setStrokeWidth(2f);
        for (int i = 0; i < 700; i++) {
            float x = rnd.nextInt(w), y = h * 0.82f + rnd.nextFloat() * h * 0.18f, l = 8 + rnd.nextInt(18);
            p.setColor(rnd.nextBoolean() ? 0x66C8E6A8 : 0x665E8F4C);
            c.drawLine(x, y, x + rnd.nextInt(7) - 3, y - l, p);
        }

        // Vignette + grano
        p.setShader(new RadialGradient(w / 2f, h / 2f, w * 0.75f,
                new int[]{0x00000000, 0x00000000, 0x38203020}, new float[]{0f, 0.6f, 1f}, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, p);
        p.setShader(null);
        for (int i = 0; i < 9000; i++) {
            p.setColor(rnd.nextBoolean() ? 0x16FFFFFF : 0x12000000);
            c.drawPoint(rnd.nextInt(w), rnd.nextInt(h), p);
        }
        return b;
    }

    private static void ridge(Canvas c, Paint p, int w, int h, float base, float amp, float freq, int color, int seed) {
        Path path = new Path();
        path.moveTo(0, h);
        for (int x = 0; x <= w; x += 8) {
            double t = (double) x / w * Math.PI * 2;
            double v = Math.sin(t * freq + seed) * 0.6 + Math.sin(t * freq * 2.7 + seed * 2) * 0.4;
            path.lineTo(x, (float) (h * base - amp * h * v));
        }
        path.lineTo(w, h);
        path.close();
        p.setShader(null);
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);
        c.drawPath(path, p);
    }

    private static void mist(Canvas c, Paint p, int w, int h, float y0, float y1, int alpha) {
        int col = (alpha << 24) | 0xFFFFFF;
        p.setShader(new LinearGradient(0, h * y0, 0, h * y1, new int[]{0x00FFFFFF, col, 0x00FFFFFF},
                new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP));
        c.drawRect(0, h * y0, w, h * y1, p);
        p.setShader(null);
    }

    /** Silueta estilizada de caballo al galope, mirando a la derecha (o espejada). */
    private static void horse(Canvas c, Paint p, float x, float y, float s, boolean right) {
        c.save();
        c.translate(x, y);
        c.scale(right ? s : -s, s);
        p.setColor(0xD13F4F3C);
        p.setStyle(Paint.Style.FILL);
        c.drawOval(new RectF(-35, -13, 35, 13), p);
        Path neck = new Path();
        neck.moveTo(20, -10); neck.lineTo(36, -38); neck.lineTo(46, -34); neck.lineTo(34, -2); neck.close();
        c.drawPath(neck, p);
        Path head = new Path();
        head.moveTo(36, -38); head.lineTo(60, -26); head.lineTo(56, -19); head.lineTo(42, -27); head.close();
        c.drawPath(head, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(5);
        Path tail = new Path();
        tail.moveTo(-34, -6); tail.quadTo(-60, -8, -62, 14);
        c.drawPath(tail, p);
        Path mane = new Path();
        mane.moveTo(30, -38); mane.quadTo(22, -30, 18, -16);
        c.drawPath(mane, p);
        p.setStrokeWidth(4.5f);
        leg(c, p, 24, 8, 46, 20, 54, 34);
        leg(c, p, 18, 10, 30, 28, 24, 40);
        leg(c, p, -24, 8, -44, 22, -58, 30);
        leg(c, p, -18, 10, -30, 28, -22, 40);
        p.setStyle(Paint.Style.FILL);
        c.restore();
    }

    private static void leg(Canvas c, Paint p, float x0, float y0, float x1, float y1, float x2, float y2) {
        Path l = new Path();
        l.moveTo(x0, y0); l.lineTo(x1, y1); l.lineTo(x2, y2);
        c.drawPath(l, p);
    }
}

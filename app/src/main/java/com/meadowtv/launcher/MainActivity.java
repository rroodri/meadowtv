package com.meadowtv.launcher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    /** Acceso favorito. Se identifica entre las apps realmente instaladas (nunca se asume). */
    private static final class Slot {
        final String key, label; final String[] pkgHints, words, exclude;
        Slot(String key, String label, String[] pkgHints, String[] words, String[] exclude) {
            this.key = key; this.label = label; this.pkgHints = pkgHints; this.words = words; this.exclude = exclude;
        }
    }
    private static final Slot[] SLOTS = {
        new Slot("youtube", "YouTube",
            new String[]{"com.google.android.youtube.tv", "com.google.android.youtube", "com.google.android.youtube.tvunplugged"},
            new String[]{"youtube"}, new String[]{"music", "kids", "studio", "música"}),
        new Slot("applemusic", "Apple Music",
            new String[]{"com.apple.android.music"}, new String[]{"apple music"}, new String[]{}),
        new Slot("playstore", "Play Store",
            new String[]{"com.android.vending"}, new String[]{"play store", "google play", "play store"}, new String[]{"games", "juegos", "movies", "books", "music"}),
    };

    private final Handler h = new Handler(Looper.getMainLooper());
    private final List<Apps.Item> apps = new ArrayList<>();
    private SharedPreferences prefs;
    private ImageView wall;
    private View gridScrim;
    private FrameLayout home, grid;
    private LinearLayout cardsRow, gridBox;
    private TextView clock, date, gridTitle;
    private TextView backBtn;
    private boolean onGrid;
    private Slot picking;
    private int homeFocus = 0;
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

    private final BroadcastReceiver pkgReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) { refresh(); }
    };

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            Date d = new Date();
            clock.setText(timeFmt.format(d));
            String s = dateFmt.format(d);
            date.setText(s.substring(0, 1).toUpperCase() + s.substring(1));
            h.postDelayed(this, 20000);
        }
    };

    private int dp(float v) { return Glass.dp(this, v); }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("meadow", MODE_PRIVATE);
        buildUi();
        loadWallpaper();
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_PACKAGE_ADDED);
        f.addAction(Intent.ACTION_PACKAGE_REMOVED);
        f.addAction(Intent.ACTION_PACKAGE_REPLACED);
        f.addDataScheme("package");
        registerReceiver(pkgReceiver, f);
    }

    private void buildUi() {
        FrameLayout root = new FrameLayout(this);
        root.setClipChildren(false);
        root.setBackgroundColor(0xFFA8CC8A);
        wall = new ImageView(this);
        wall.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(wall, new FrameLayout.LayoutParams(-1, -1));

        View bottomScrim = new View(this);
        bottomScrim.setBackground(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{0x7022402D, 0x0022402D}));
        root.addView(bottomScrim, new FrameLayout.LayoutParams(-1, dp(300), Gravity.BOTTOM));
        gridScrim = new View(this);
        gridScrim.setBackgroundColor(0x9926402F);
        gridScrim.setVisibility(View.GONE);
        root.addView(gridScrim, new FrameLayout.LayoutParams(-1, -1));

        // ---- Inicio ----
        home = new FrameLayout(this);
        home.setClipChildren(false);
        TextView title = Glass.label(this, "MEADOW TV", 30, true);
        title.setLetterSpacing(0.25f);
        FrameLayout.LayoutParams tp = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.LEFT);
        tp.setMargins(dp(48), dp(32), 0, 0);
        home.addView(title, tp);

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setGravity(Gravity.RIGHT);
        clock = Glass.label(this, "", 48, false);
        date = Glass.label(this, "", 17, false);
        right.addView(clock);
        right.addView(date);
        FrameLayout.LayoutParams rp = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.RIGHT);
        rp.setMargins(0, dp(22), dp(48), 0);
        home.addView(right, rp);

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.VERTICAL);
        bottom.setClipChildren(false);
        TextView sec = Glass.label(this, "FAVORITAS", 14, true);
        sec.setLetterSpacing(0.2f);
        bottom.addView(sec);
        cardsRow = new LinearLayout(this);
        cardsRow.setClipChildren(false);
        LinearLayout.LayoutParams cr = new LinearLayout.LayoutParams(-2, -2);
        cr.topMargin = dp(12);
        bottom.addView(cardsRow, cr);
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.LEFT);
        bp.setMargins(dp(48), 0, 0, dp(56));
        home.addView(bottom, bp);
        root.addView(home, new FrameLayout.LayoutParams(-1, -1));

        // ---- Más apps ----
        grid = new FrameLayout(this);
        grid.setVisibility(View.GONE);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(48), dp(28), dp(48), 0);
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setClipChildren(false);
        backBtn = Glass.pill(this, "‹  Inicio");
        backBtn.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showHome(); } });
        bar.addView(backBtn);
        gridTitle = Glass.label(this, "Más apps", 26, false);
        LinearLayout.LayoutParams gt = new LinearLayout.LayoutParams(-2, -2);
        gt.leftMargin = dp(24);
        bar.addView(gridTitle, gt);
        col.addView(bar, new LinearLayout.LayoutParams(-1, -2));
        ScrollView sv = new ScrollView(this);
        sv.setClipChildren(false);
        sv.setClipToPadding(false);
        sv.setVerticalScrollBarEnabled(false);
        sv.setPadding(0, dp(16), 0, dp(24));
        gridBox = new LinearLayout(this);
        gridBox.setOrientation(LinearLayout.VERTICAL);
        gridBox.setClipChildren(false);
        sv.addView(gridBox, new FrameLayout.LayoutParams(-1, -2));
        col.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1f));
        grid.addView(col, new FrameLayout.LayoutParams(-1, -1));
        root.addView(grid, new FrameLayout.LayoutParams(-1, -1));

        setContentView(root);
    }

    private void loadWallpaper() {
        int id = getResources().getIdentifier("meadow_wallpaper", "drawable", getPackageName());
        if (id != 0) { // foto real opcional
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inPreferredConfig = Bitmap.Config.RGB_565;
            wall.setImageBitmap(BitmapFactory.decodeResource(getResources(), id, o));
            return;
        }
        new Thread(new Runnable() {
            @Override public void run() {
                final Bitmap b = Wallpaper.render(1280, 720);
                h.post(new Runnable() { @Override public void run() { if (!isDestroyed()) wall.setImageBitmap(b); } });
            }
        }).start();
    }

    // ---------- Datos ----------
    private void refresh() {
        final android.content.pm.PackageManager pm = getPackageManager();
        final String self = getPackageName();
        new Thread(new Runnable() {
            @Override public void run() {
                final List<Apps.Item> list = Apps.query(pm, self);
                h.post(new Runnable() {
                    @Override public void run() {
                        if (isDestroyed()) return;
                        apps.clear();
                        apps.addAll(list);
                        buildHomeCards();
                        if (onGrid) populateGrid();
                    }
                });
            }
        }).start();
    }

    private Apps.Item resolve(Slot s) {
        String saved = prefs.getString("slot_" + s.key, null);
        if (saved != null) for (Apps.Item a : apps) if (a.key().equals(saved)) return a;
        for (String hint : s.pkgHints) for (Apps.Item a : apps) if (a.pkg.equals(hint)) return a;
        for (Apps.Item a : apps) {
            String l = a.label.toLowerCase(Locale.ROOT);
            boolean ok = false, bad = false;
            for (String w : s.words) if (l.contains(w)) ok = true;
            for (String w : s.exclude) if (l.contains(w)) bad = true;
            if (ok && !bad) return a;
        }
        return null;
    }

    // ---------- Inicio ----------
    private void buildHomeCards() {
        View f = getCurrentFocus();
        if (f != null && f.getParent() == cardsRow) homeFocus = cardsRow.indexOfChild(f);
        cardsRow.removeAllViews();
        for (final Slot s : SLOTS) {
            Apps.Item a = resolve(s);
            LinearLayout card = Glass.card(this, a != null ? a.icon : Glass.letterIcon(this, s.label), s.label, 64, 16);
            card.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { openSlot(s); } });
            card.setOnLongClickListener(new View.OnLongClickListener() {
                @Override public boolean onLongClick(View v) { showGrid(s); return true; }
            });
            addCard(card);
        }
        LinearLayout more = Glass.card(this, Glass.gridIcon(this), "Más apps", 64, 16);
        more.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showGrid(null); } });
        addCard(more);
        if (!onGrid) {
            View t = cardsRow.getChildAt(Math.min(homeFocus, cardsRow.getChildCount() - 1));
            if (t != null) t.requestFocus();
        }
    }

    private void addCard(View card) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(190), dp(136));
        lp.rightMargin = dp(22);
        cardsRow.addView(card, lp);
    }

    private void openSlot(Slot s) {
        Apps.Item a = resolve(s);
        if (a == null) {
            Toast.makeText(this, s.label + " no está instalada. Elige otra app para este acceso.", Toast.LENGTH_LONG).show();
            showGrid(s);
        } else if (!launch(a)) {
            showGrid(s);
        }
    }

    private boolean launch(Apps.Item a) {
        try {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setClassName(a.pkg, a.cls);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(i);
            return true;
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir " + a.label, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // ---------- Más apps ----------
    private void showGrid(Slot pick) {
        picking = pick;
        onGrid = true;
        home.setVisibility(View.GONE);
        grid.setVisibility(View.VISIBLE);
        gridScrim.setVisibility(View.VISIBLE);
        gridTitle.setText(pick == null ? "Más apps" : "Elige la app para " + pick.label);
        populateGrid();
    }

    private void showHome() {
        onGrid = false;
        picking = null;
        grid.setVisibility(View.GONE);
        gridScrim.setVisibility(View.GONE);
        home.setVisibility(View.VISIBLE);
        buildHomeCards();
    }

    private void populateGrid() {
        gridBox.removeAllViews();
        final int cols = 6, gap = dp(6);
        int tileW = (getResources().getDisplayMetrics().widthPixels - dp(96)) / cols - gap * 2;
        LinearLayout row = null;
        View first = null;
        for (int i = 0; i < apps.size(); i++) {
            if (i % cols == 0) {
                row = new LinearLayout(this);
                row.setClipChildren(false);
                gridBox.addView(row, new LinearLayout.LayoutParams(-2, -2));
            }
            final Apps.Item a = apps.get(i);
            LinearLayout t = Glass.card(this, a.icon, a.label, 56, 14);
            t.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (picking != null) {
                        prefs.edit().putString("slot_" + picking.key, a.key()).apply();
                        showHome();
                    } else {
                        launch(a);
                    }
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(tileW, dp(120));
            lp.setMargins(gap, gap, gap, gap);
            row.addView(t, lp);
            if (first == null) first = t;
        }
        (first != null ? first : backBtn).requestFocus();
    }

    // ---------- Ciclo de vida ----------
    @Override protected void onResume() {
        super.onResume();
        h.post(tick);
        refresh();
    }

    @Override protected void onPause() {
        h.removeCallbacks(tick);
        super.onPause();
    }

    @Override protected void onNewIntent(Intent i) { // botón Home estando en Más apps
        super.onNewIntent(i);
        if (onGrid) showHome();
    }

    @Override public void onBackPressed() {
        if (onGrid) showHome(); // en el inicio, Atrás no hace nada (es el launcher)
    }

    @Override public void onWindowFocusChanged(boolean has) {
        super.onWindowFocusChanged(has);
        if (has) getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override protected void onDestroy() {
        h.removeCallbacksAndMessages(null);
        try { unregisterReceiver(pkgReceiver); } catch (Exception ignored) {}
        super.onDestroy();
    }
}

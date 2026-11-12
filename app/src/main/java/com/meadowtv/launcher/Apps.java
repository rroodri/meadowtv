package com.meadowtv.launcher;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Consulta dinámica de las apps lanzables instaladas (PackageManager). */
final class Apps {
    static final class Item {
        String label, pkg, cls;
        Drawable icon;
        String key() { return pkg + "/" + cls; }
    }

    static List<Item> query(PackageManager pm, String self) {
        List<Item> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        // Leanback primero: apps de TV (p. ej. YouTube TV) a veces solo declaran esta categoría.
        String[] cats = {Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER};
        for (String cat : cats) {
            Intent q = new Intent(Intent.ACTION_MAIN, null);
            q.addCategory(cat);
            for (ResolveInfo ri : pm.queryIntentActivities(q, 0)) {
                ActivityInfo ai = ri.activityInfo;
                if (ai == null || ai.packageName.equals(self) || !seen.add(ai.packageName)) continue;
                Item it = new Item();
                it.pkg = ai.packageName;
                it.cls = ai.name;
                it.label = String.valueOf(ri.loadLabel(pm));
                it.icon = ri.loadIcon(pm);
                out.add(it);
            }
        }
        Collections.sort(out, new Comparator<Item>() {
            @Override public int compare(Item a, Item b) { return a.label.compareToIgnoreCase(b.label); }
        });
        return out;
    }
}

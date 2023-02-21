package io.github.jark006.freezeit;

import static android.content.pm.ApplicationInfo.FLAG_SYSTEM;
import static android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppInfoCache {
    private static final String TAG = "Freezeit[AppInfoCache]";

    public static class Info {
        public Drawable icon;
        public String packName;
        public String label;
        public String forSearch;
        public boolean isSystemApp;

        public Info(Drawable icon, String packName, String label, int uid, boolean isSystemApp) {
            this.icon = icon;
            this.packName = packName;
            this.label = label;
            this.forSearch = label.toLowerCase() + packName.toLowerCase() + uid;
            this.isSystemApp = isSystemApp;
        }

        public boolean contains(final String keyWord){
            return forSearch.contains(keyWord);
        }
    }

    private final static HashMap<Integer, Info> cacheInfo = new HashMap<>();

    public static void refreshCache(Context context) {
        synchronized (cacheInfo) {
            PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> applicationList;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                applicationList = pm.getInstalledApplications(
                        PackageManager.ApplicationInfoFlags.of(PackageManager.MATCH_UNINSTALLED_PACKAGES));
            } else {
                applicationList = pm.getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES);
            }

            cacheInfo.clear();
            for (ApplicationInfo appInfo : applicationList) {
                if (appInfo.uid < 10000)
                    continue;

                boolean isSystemApp = (appInfo.flags & (FLAG_SYSTEM | FLAG_UPDATED_SYSTEM_APP)) != 0;
                String label = pm.getApplicationLabel(appInfo).toString();
                cacheInfo.put(appInfo.uid, new Info(
                        appInfo.loadIcon(pm), appInfo.packageName, label, appInfo.uid, isSystemApp));
            }
            Log.d(TAG, context.getString(R.string.update_cache) + cacheInfo.size());
        }
    }

    public static Info get(int uid) {
        synchronized (cacheInfo) {
            return cacheInfo.get(uid);
        }
    }

    public static void getUidList(ArrayList<Integer> uidList) {
        uidList.clear();
        synchronized (cacheInfo) {
            AppInfoCache.cacheInfo.forEach((uid, info) -> uidList.add(uid));
        }
    }

    public static byte[] getAppLabelBytes() {
        StringBuilder appLabel = new StringBuilder();
        synchronized (cacheInfo) {
            cacheInfo.forEach((uid, info) -> {
                if (!info.packName.equals(info.label))
                    appLabel.append(uid).append(" ").append(info.label).append('\n');
            });
        }
        return appLabel.toString().getBytes();
    }
}

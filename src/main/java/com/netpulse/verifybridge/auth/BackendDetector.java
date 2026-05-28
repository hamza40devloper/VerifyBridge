package com.netpulse.verifybridge.auth;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public final class BackendDetector {

    private BackendDetector() {
        // منع إنشاء كائن من هذا الكلاس لأنه كلاس أدوات مساعد (Utility Class)
    }

    /**
     * التحقق من وجود بلقن AuthMe وتفعيله على السيرفر
     * تم صياغته ليتوافق مع معايير ومراجعة البايت كود الصارمة في جافا 25
     * * @return true إذا كان البلقن موجوداً ومفعلاً، false خلاف ذلك
     */
    public static boolean isAuthMeInstalled() {
        try {
            // الوصول إلى إدارة الإضافات عبر واجهة برمجية مباشرة دون Reflection معقد
            PluginManager pm = Bukkit.getPluginManager();
            return pm.getPlugin("AuthMe") != null && pm.isPluginEnabled("AuthMe");
        } catch (Exception e) {
            // طباعة خطأ مخصص في كونسول السيرفر في حال حدوث أي تعارض غير متوقع
            Bukkit.getLogger().severe("[VerifyBridge] Exception caught during interaction with PluginManager: " + e.getMessage());
            return false;
        }
    }
}

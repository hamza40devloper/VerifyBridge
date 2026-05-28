package com.netpulse.verifybridge;

import com.netpulse.verifybridge.api.VerificationServer;
import com.netpulse.verifybridge.auth.BackendDetector;
import org.bukkit.plugin.java.JavaPlugin;

public final class VerifyBridgePlugin extends JavaPlugin {

    private VerificationServer server;

    @Override
    public void onEnable() {
        // حفظ ملف config.yml الافتراضي إذا لم يكن موجوداً
        saveDefaultConfig();

        // قراءة البيانات من ملف الإعدادات
        int port = getConfig().getInt("server.port", 8080);
        String secretToken = getConfig().getString("server.secret-token", "CHANGE_ME_NOW_123");

        // التحقق من وجود بلقن AuthMe بشكل آمن ومتوافق مع جافا 25
        if (BackendDetector.isAuthMeInstalled()) {
            getLogger().info("Successfully hooked into AuthMe backend seamlessly.");
        } else {
            getLogger().warning("AuthMe was not detected! Certain authentication checks might be bypassed.");
        }

        // تشغيل خادم الـ API بنظام الـ Virtual Threads لجافا الحديثة
        try {
            server = new VerificationServer(port, secretToken, this);
            server.start();
            getLogger().info("Asynchronous Verification API Server successfully started on port " + port);
        } catch (Exception e) {
            getLogger().severe("Failed to initialize internal HttpServer: " + e.getLocalizedMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // إيقاف الخادم وتنظيف الذاكرة عند إغلاق البلوجن أو السيرفر
        if (server != null) {
            server.stop();
            getLogger().info("Verification API Server securely stopped.");
        }
    }
}

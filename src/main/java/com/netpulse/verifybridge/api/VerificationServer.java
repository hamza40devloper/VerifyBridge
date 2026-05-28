package com.netpulse.verifybridge.api;

import com.netpulse.verifybridge.VerifyBridgePlugin;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public final class VerificationServer {

    private final int port;
    private final String secretToken;
    private final VerifyBridgePlugin plugin;
    private HttpServer server;

    public VerificationServer(int port, String secretToken, VerifyBridgePlugin plugin) {
        this.port = port;
        this.secretToken = secretToken;
        this.plugin = plugin;
    }

    public void start() throws IOException {
        // إنشاء خادم HTTP على المنفذ المحدد من الإعدادات
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // تعيين المسار (Endpoint) الخاص بطلب التحقق
        server.createContext("/api/verify", new VerifyHandler());
        
        // تخصيص منفذ المعالجة الافتراضي (Virtual Thread Executor) المتوفر في جافا 25 لقراءة الطلبات دون قفل خيط السيرفر الرئيسي
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor()); 
        
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(1); // إيقاف الخادم بأمان وتأخير ثانية واحدة لتنظيف الاتصالات
        }
    }

    private class VerifyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // قبول طلبات من نوع POST فقط لحماية البيانات
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Method Not Allowed\"}");
                return;
            }

            // جلب التحقق من الهيدر والـ Authorization Token لقفل الثغرات الأمنية
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.equals("Bearer " + secretToken)) {
                sendResponse(exchange, 401, "{\"status\":\"error\",\"message\":\"Unauthorized access token\"}");
                return;
            }

            // إرسال استجابة بنجاح ربط القناة وتأكيد الاتصال بالخلفية
            String response = "{\"status\":\"success\",\"message\":\"Bridge channel verified configuration applied\"}";
            sendResponse(exchange, 200, response);
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}

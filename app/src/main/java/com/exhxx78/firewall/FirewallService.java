package com.exhxx78.firewall;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class FirewallService extends VpnService {
    private ParcelFileDescriptor vpnInterface = null;
    public static final String ACTION_START = "START_FIREWALL";
    public static final String ACTION_STOP = "STOP_FIREWALL";
    public static final String EXTRA_PACKAGE = "ALLOWED_PACKAGE";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                String allowedPackage = intent.getStringExtra(EXTRA_PACKAGE);
                startFirewall(allowedPackage);
            } else if (ACTION_STOP.equals(action)) {
                stopFirewall();
            }
        }
        return START_NOT_STICKY;
    }

    private void startFirewall(String allowedPackage) {
        if (vpnInterface != null) stopFirewall();

        Builder builder = new Builder();
        try {
            // توجيه جميع الحزم إلى هذا النفق الوهمي
            builder.addAddress("10.0.0.2", 32);
            builder.addRoute("0.0.0.0", 0); 
            
            // الهندسة العكسية: نستثني اللعبة من الـ VPN لكي تتصل بالراوتر مباشرة بأسرع بنج
            if (allowedPackage != null && !allowedPackage.isEmpty()) {
                builder.addDisallowedApplication(allowedPackage);
            }
            
            // استثناء التطبيق نفسه حتى لا ينقطع عنه الاتصال الداخلي
            builder.addDisallowedApplication(getPackageName());

            builder.setSession("ExhxxFirewall");
            builder.setMtu(1500);
            
            // تشغيل النفق (بما أننا لا نقرأ البيانات منه، سيصبح "ثقب أسود" يبتلع بيانات باقي التطبيقات)
            vpnInterface = builder.establish();
            Log.d("EXHXX_FIREWALL", "Firewall Started. Only " + allowedPackage + " has real internet.");
            
        } catch (Exception e) {
            e.printStackTrace();
            stopFirewall();
        }
    }

    private void stopFirewall() {
        try {
            if (vpnInterface != null) {
                vpnInterface.close();
                vpnInterface = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopSelf();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopFirewall();
    }
}

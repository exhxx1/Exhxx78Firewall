package com.exhxx78.firewall;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.VpnService;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int VPN_REQUEST_CODE = 100;
    private EditText edtPackage;
    private boolean isFirewallActive = false;
    private Button btnToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#121212"));
        mainLayout.setGravity(Gravity.CENTER);
        mainLayout.setPadding(40, 40, 40, 40);

        TextView title = new TextView(this);
        title.setText("EXHXX GAME FIREWALL 🛡️\nحظر النت عن كل الجهاز عدا اللعبة");
        title.setTextColor(Color.parseColor("#00E676"));
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 50);
        mainLayout.addView(title);

        edtPackage = new EditText(this);
        edtPackage.setHint("اكتب حزمة اللعبة (مثال: com.tencent.ig)");
        edtPackage.setHintTextColor(Color.GRAY);
        edtPackage.setTextColor(Color.WHITE);
        edtPackage.setPadding(30, 30, 30, 30);
        GradientDrawable edtBg = new GradientDrawable();
        edtBg.setColor(Color.parseColor("#1E1E1E"));
        edtBg.setCornerRadius(15f);
        edtPackage.setBackground(edtBg);
        LinearLayout.LayoutParams edtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        edtLp.setMargins(0, 0, 0, 40);
        edtPackage.setLayoutParams(edtLp);
        mainLayout.addView(edtPackage);

        btnToggle = new Button(this);
        btnToggle.setText("تشغيل الجدار الناري 🚀");
        btnToggle.setTextColor(Color.WHITE);
        btnToggle.setTextSize(18);
        updateButtonUI();
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnToggle.setLayoutParams(btnLp);
        
        btnToggle.setOnClickListener(v -> {
            if (!isFirewallActive) {
                String pkg = edtPackage.getText().toString().trim();
                if (pkg.isEmpty()) {
                    Toast.makeText(this, "يرجى كتابة حزمة اللعبة أولاً!", Toast.LENGTH_SHORT).show();
                    return;
                }
                prepareVpn();
            } else {
                stopFirewall();
            }
        });
        mainLayout.addView(btnToggle);

        setContentView(mainLayout);
    }

    private void prepareVpn() {
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            startActivityForResult(intent, VPN_REQUEST_CODE);
        } else {
            startFirewall(); // تم منح الصلاحية مسبقاً
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            startFirewall();
        } else {
            Toast.makeText(this, "يجب منح صلاحية الـ VPN ليعمل التطبيق!", Toast.LENGTH_SHORT).show();
        }
    }

    private void startFirewall() {
        String allowedPkg = edtPackage.getText().toString().trim();
        Intent intent = new Intent(this, FirewallService.class);
        intent.setAction(FirewallService.ACTION_START);
        intent.putExtra(FirewallService.EXTRA_PACKAGE, allowedPkg);
        startService(intent);
        
        isFirewallActive = true;
        updateButtonUI();
        Toast.makeText(this, "تم حظر الإنترنت! فقط " + allowedPkg + " يعمل الآن.", Toast.LENGTH_LONG).show();
    }

    private void stopFirewall() {
        Intent intent = new Intent(this, FirewallService.class);
        intent.setAction(FirewallService.ACTION_STOP);
        startService(intent);
        
        isFirewallActive = false;
        updateButtonUI();
        Toast.makeText(this, "تم إيقاف الجدار الناري. الإنترنت عاد للجميع.", Toast.LENGTH_SHORT).show();
    }

    private void updateButtonUI() {
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setCornerRadius(20f);
        if (isFirewallActive) {
            btnToggle.setText("إيقاف الجدار الناري 🛑");
            btnBg.setColor(Color.parseColor("#D50000"));
        } else {
            btnToggle.setText("تشغيل الجدار الناري 🚀");
            btnBg.setColor(Color.parseColor("#00C853"));
        }
        btnToggle.setBackground(btnBg);
    }
}

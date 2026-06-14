package com.exhxx78.firewall;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {
    private static final int VPN_REQUEST_CODE = 100;
    private boolean isFirewallActive = false;
    private Button btnToggle;
    private TextView tvSelectedApp;
    private ImageView ivSelectedApp;
    private String selectedPackage = "";
    private LinearLayout appCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // الواجهة الرئيسية باللون الداكن الفخم
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#121212"));
        mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.setPadding(40, 60, 40, 40);

        // العنوان
        TextView title = new TextView(this);
        title.setText("PURE ESPORTS FIREWALL 🛡️");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 40);
        mainLayout.addView(title);

        // صندوق التعليمات
        LinearLayout instructionsBox = new LinearLayout(this);
        instructionsBox.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable instBg = new GradientDrawable();
        instBg.setColor(Color.parseColor("#1E1E1E"));
        instBg.setCornerRadius(20f);
        instBg.setStroke(2, Color.parseColor("#333333"));
        instructionsBox.setBackground(instBg);
        instructionsBox.setPadding(30, 30, 30, 30);
        LinearLayout.LayoutParams instParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        instParams.setMargins(0, 0, 0, 40);
        instructionsBox.setLayoutParams(instParams);

        TextView tvInstTitle = new TextView(this);
        tvInstTitle.setText("📌 طريقة التفعيل:");
        tvInstTitle.setTextColor(Color.parseColor("#00E676"));
        tvInstTitle.setTextSize(16);
        tvInstTitle.setTypeface(null, Typeface.BOLD);
        tvInstTitle.setPadding(0, 0, 0, 10);
        instructionsBox.addView(tvInstTitle);

        TextView tvInstBody = new TextView(this);
        tvInstBody.setText("1. اضغط على (اختيار اللعبة) وابحث عن لعبتك.\n2. اضغط على (تشغيل الجدار الناري).\n3. سيتم قطع النت عن كل الجهاز (تيك توك، واتساب...) وستحصل اللعبة على إنترنت صافي وبنج (Ping) خرافي!");
        tvInstBody.setTextColor(Color.parseColor("#E0E0E0"));
        tvInstBody.setTextSize(14);
        tvInstBody.setLineSpacing(5f, 1.2f);
        instructionsBox.addView(tvInstBody);
        mainLayout.addView(instructionsBox);

        // كارد التطبيق المحدد
        appCard = new LinearLayout(this);
        appCard.setOrientation(LinearLayout.HORIZONTAL);
        appCard.setGravity(Gravity.CENTER_VERTICAL);
        appCard.setBackground(instBg);
        appCard.setPadding(30, 30, 30, 30);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 40);
        appCard.setLayoutParams(cardParams);

        ivSelectedApp = new ImageView(this);
        LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(100, 100);
        ivParams.setMargins(0, 0, 30, 0);
        ivSelectedApp.setLayoutParams(ivParams);
        
        tvSelectedApp = new TextView(this);
        tvSelectedApp.setText("لم يتم تحديد اللعبة ❌");
        tvSelectedApp.setTextColor(Color.parseColor("#FF5555"));
        tvSelectedApp.setTextSize(16);
        tvSelectedApp.setTypeface(null, Typeface.BOLD);
        
        appCard.addView(ivSelectedApp);
        appCard.addView(tvSelectedApp);
        mainLayout.addView(appCard);

        // زر اختيار التطبيق
        Button btnSelectApp = new Button(this);
        btnSelectApp.setText("اختيار اللعبة / التطبيق 🔍");
        btnSelectApp.setTextColor(Color.WHITE);
        btnSelectApp.setTextSize(16);
        GradientDrawable selectBg = new GradientDrawable();
        selectBg.setColor(Color.parseColor("#0088CC"));
        selectBg.setCornerRadius(15f);
        btnSelectApp.setBackground(selectBg);
        LinearLayout.LayoutParams btnSelectParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnSelectParams.setMargins(0, 0, 0, 30);
        btnSelectApp.setLayoutParams(btnSelectParams);
        btnSelectApp.setOnClickListener(v -> showAppSelectionDialog());
        mainLayout.addView(btnSelectApp);

        // زر التشغيل والإيقاف
        btnToggle = new Button(this);
        btnToggle.setTextSize(18);
        updateButtonUI();
        LinearLayout.LayoutParams btnToggleParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnToggleParams.setMargins(0, 0, 0, 40);
        btnToggle.setLayoutParams(btnToggleParams);
        btnToggle.setOnClickListener(v -> {
            if (!isFirewallActive) {
                if (selectedPackage.isEmpty()) {
                    Toast.makeText(this, "يرجى اختيار اللعبة أولاً من القائمة!", Toast.LENGTH_SHORT).show();
                    return;
                }
                prepareVpn();
            } else {
                stopFirewall();
            }
        });
        mainLayout.addView(btnToggle);

        // زر قناة التليجرام
        Button btnTelegram = new Button(this);
        btnTelegram.setText("📢 انضم لقناتنا على تليجرام");
        btnTelegram.setTextColor(Color.WHITE);
        GradientDrawable tgBg = new GradientDrawable();
        tgBg.setColor(Color.parseColor("#0088CC"));
        tgBg.setCornerRadius(15f);
        btnTelegram.setBackground(tgBg);
        LinearLayout.LayoutParams tgParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tgParams.setMargins(0, 0, 0, 40);
        btnTelegram.setLayoutParams(tgParams);
        btnTelegram.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/exhxx78")); // استبدله برابط قناتك الفعلي
            startActivity(browserIntent);
        });
        mainLayout.addView(btnTelegram);

        // حقوق المطور
        TextView devCredit = new TextView(this);
        devCredit.setText("Developed by:\nMuhammad Adnan (@m_7004)");
        devCredit.setTextColor(Color.parseColor("#888888"));
        devCredit.setTextSize(14);
        devCredit.setGravity(Gravity.CENTER);
        mainLayout.addView(devCredit);

        setContentView(mainLayout);
    }

    // ديالوك (نافذة) اختيار التطبيقات مع البحث
    private void showAppSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setBackgroundColor(Color.parseColor("#121212"));
        dialogLayout.setPadding(20, 40, 20, 20);

        TextView title = new TextView(this);
        title.setText("اختر اللعبة 🎮");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        dialogLayout.addView(title);

        EditText searchBox = new EditText(this);
        searchBox.setHint("🔍 ابحث عن تطبيق...");
        searchBox.setHintTextColor(Color.GRAY);
        searchBox.setTextColor(Color.WHITE);
        searchBox.setPadding(30, 30, 30, 30);
        GradientDrawable searchBg = new GradientDrawable();
        searchBg.setColor(Color.parseColor("#1E1E1E"));
        searchBg.setCornerRadius(15f);
        searchBox.setBackground(searchBg);
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        searchParams.setMargins(0, 0, 0, 20);
        searchBox.setLayoutParams(searchParams);
        dialogLayout.addView(searchBox);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout listLayout = new LinearLayout(this);
        listLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(listLayout);
        dialogLayout.addView(scrollView);

        AlertDialog dialog = builder.setView(dialogLayout).create();

        // جلب التطبيقات المثبتة
        PackageManager pm = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(pm));

        for (ResolveInfo appInfo : apps) {
            String appName = appInfo.loadLabel(pm).toString();
            String packageName = appInfo.activityInfo.packageName;
            Drawable icon = appInfo.loadIcon(pm);

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);
            itemLayout.setPadding(20, 30, 20, 30);
            itemLayout.setTag(appName); // لحفظ الاسم من أجل البحث

            ImageView ivIcon = new ImageView(this);
            ivIcon.setImageDrawable(icon);
            ivIcon.setLayoutParams(new LinearLayout.LayoutParams(120, 120));

            TextView tvName = new TextView(this);
            tvName.setText(appName);
            tvName.setTextColor(Color.WHITE);
            tvName.setTextSize(16);
            tvName.setPadding(30, 0, 0, 0);

            itemLayout.addView(ivIcon);
            itemLayout.addView(tvName);

            itemLayout.setOnClickListener(v -> {
                selectedPackage = packageName;
                tvSelectedApp.setText(appName);
                tvSelectedApp.setTextColor(Color.parseColor("#00E676"));
                ivSelectedApp.setImageDrawable(icon);
                dialog.dismiss();
            });

            listLayout.addView(itemLayout);
        }

        // تفعيل شريط البحث
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase();
                for (int i = 0; i < listLayout.getChildCount(); i++) {
                    View item = listLayout.getChildAt(i);
                    String name = (String) item.getTag();
                    if (name.toLowerCase().contains(query)) {
                        item.setVisibility(View.VISIBLE);
                    } else {
                        item.setVisibility(View.GONE);
                    }
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        dialog.show();
    }

    private void prepareVpn() {
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            startActivityForResult(intent, VPN_REQUEST_CODE);
        } else {
            startFirewall(); 
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
        Intent intent = new Intent(this, FirewallService.class);
        intent.setAction(FirewallService.ACTION_START);
        intent.putExtra(FirewallService.EXTRA_PACKAGE, selectedPackage);
        startService(intent);
        
        isFirewallActive = true;
        updateButtonUI();
        Toast.makeText(this, "تم حظر الإنترنت! فقط اللعبة المحددة تعمل الآن.", Toast.LENGTH_LONG).show();
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

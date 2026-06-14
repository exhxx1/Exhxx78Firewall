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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView rootScroll = new ScrollView(this);
        rootScroll.setBackgroundColor(Color.parseColor("#121212"));

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.setPadding(40, 60, 40, 40);

        TextView title = new TextView(this);
        title.setText("EXHXX78 SMART FIREWALL");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 40);
        Drawable shieldIcon = getResources().getDrawable(R.drawable.ic_firewall);
        shieldIcon.setBounds(0, 0, 80, 80);
        title.setCompoundDrawables(null, shieldIcon, null, null);
        title.setCompoundDrawablePadding(20);
        mainLayout.addView(title);

        GradientDrawable instBg = new GradientDrawable();
        instBg.setColor(Color.parseColor("#1E1E1E"));
        instBg.setCornerRadius(20f);
        instBg.setStroke(2, Color.parseColor("#333333"));

        LinearLayout appCard = new LinearLayout(this);
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
        tvSelectedApp.setText("لم يتم تحديد أي تطبيق");
        tvSelectedApp.setTextColor(Color.parseColor("#888888"));
        tvSelectedApp.setTextSize(16);
        tvSelectedApp.setTypeface(null, Typeface.BOLD);
        
        appCard.addView(ivSelectedApp);
        appCard.addView(tvSelectedApp);
        mainLayout.addView(appCard);

        Button btnSelectApp = new Button(this);
        btnSelectApp.setText("اختيار التطبيق");
        btnSelectApp.setTextColor(Color.WHITE);
        btnSelectApp.setTextSize(16);
        btnSelectApp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_search, 0);
        btnSelectApp.setPadding(50, 0, 50, 0);
        GradientDrawable selectBg = new GradientDrawable();
        selectBg.setColor(Color.parseColor("#0088CC"));
        selectBg.setCornerRadius(15f);
        btnSelectApp.setBackground(selectBg);
        LinearLayout.LayoutParams btnSelectParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 140);
        btnSelectParams.setMargins(0, 0, 0, 30);
        btnSelectApp.setLayoutParams(btnSelectParams);
        btnSelectApp.setOnClickListener(v -> showAppSelectionDialog());
        mainLayout.addView(btnSelectApp);

        btnToggle = new Button(this);
        btnToggle.setTextSize(18);
        btnToggle.setTextColor(Color.WHITE);
        btnToggle.setPadding(50, 0, 50, 0);
        btnToggle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_power, 0);
        updateButtonUI();
        LinearLayout.LayoutParams btnToggleParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 150);
        btnToggleParams.setMargins(0, 0, 0, 40);
        btnToggle.setLayoutParams(btnToggleParams);
        btnToggle.setOnClickListener(v -> {
            if (!isFirewallActive) {
                if (selectedPackage.isEmpty()) {
                    Toast.makeText(this, "يرجى اختيار التطبيق أولاً!", Toast.LENGTH_SHORT).show();
                    return;
                }
                prepareVpn();
            } else {
                stopFirewall();
            }
        });
        mainLayout.addView(btnToggle);

        Button btnTelegram = new Button(this);
        btnTelegram.setText("انضم لقناتنا على تليجرام");
        btnTelegram.setTextColor(Color.WHITE);
        btnTelegram.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_telegram, 0);
        btnTelegram.setPadding(50, 0, 50, 0);
        GradientDrawable tgBg = new GradientDrawable();
        tgBg.setColor(Color.parseColor("#0088CC"));
        tgBg.setCornerRadius(15f);
        btnTelegram.setBackground(tgBg);
        LinearLayout.LayoutParams tgParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 140);
        tgParams.setMargins(0, 0, 0, 40);
        btnTelegram.setLayoutParams(tgParams);
        btnTelegram.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/exhxx78"))); 
        });
        mainLayout.addView(btnTelegram);

        LinearLayout infoBox = new LinearLayout(this);
        infoBox.setOrientation(LinearLayout.VERTICAL);
        infoBox.setBackground(instBg);
        infoBox.setPadding(40, 40, 40, 40);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        infoParams.setMargins(0, 0, 0, 40);
        infoBox.setLayoutParams(infoParams);

        TextView tvInfoTitle = new TextView(this);
        tvInfoTitle.setText("فوائد التطبيق واستخداماته:");
        tvInfoTitle.setTextColor(Color.parseColor("#00E676"));
        tvInfoTitle.setTextSize(16);
        tvInfoTitle.setTypeface(null, Typeface.BOLD);
        tvInfoTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_info, 0);
        tvInfoTitle.setCompoundDrawablePadding(15);
        tvInfoTitle.setPadding(0, 0, 0, 20);
        infoBox.addView(tvInfoTitle);

        TextView tvInfoBody = new TextView(this);
        tvInfoBody.setText("سرعة الإنترنت الأساسية لن تتغير، لكن التطبيق سيمنع باقي برامج الجهاز من سحب الإنترنت بالخلفية، مما يعطيك الفوائد التالية:\n\n" +
                "• للألعاب (تقليل البنج): توجيه كل قوة الإنترنت للعبة فقط، مما يقلل البنج (Ping) ويمنع التقطيع نهائياً.\n\n" +
                "• للخصوصية: يمكنك تشغيل تطبيق واحد فقط (مثل يوتيوب)، وقطع النت عن تطبيقات المراسلة (مثل واتساب)، لتتصفح براحتك دون ظهور 'متصل الآن'.");
        tvInfoBody.setTextColor(Color.parseColor("#CCCCCC"));
        tvInfoBody.setTextSize(14);
        tvInfoBody.setLineSpacing(5f, 1.3f);
        infoBox.addView(tvInfoBody);
        mainLayout.addView(infoBox);

        TextView devCredit = new TextView(this);
        devCredit.setText("Developed by:\nMuhammad Adnan (@m_7004)");
        devCredit.setTextColor(Color.parseColor("#888888"));
        devCredit.setTextSize(14);
        devCredit.setGravity(Gravity.CENTER);
        devCredit.setPadding(0, 0, 0, 40);
        mainLayout.addView(devCredit);

        rootScroll.addView(mainLayout);
        setContentView(rootScroll);
    }

    private void showAppSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setBackgroundColor(Color.parseColor("#121212"));
        dialogLayout.setPadding(20, 40, 20, 20);

        TextView title = new TextView(this);
        title.setText("اختر التطبيق");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        dialogLayout.addView(title);

        EditText searchBox = new EditText(this);
        searchBox.setHint("ابحث عن تطبيق...");
        searchBox.setHintTextColor(Color.GRAY);
        searchBox.setTextColor(Color.WHITE);
        searchBox.setPadding(30, 30, 30, 30);
        searchBox.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_search, 0);
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
            itemLayout.setTag(appName);

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
            Toast.makeText(this, "يجب منح صلاحية الاتصال ليعمل التطبيق!", Toast.LENGTH_SHORT).show();
        }
    }

    private void startFirewall() {
        Intent intent = new Intent(this, FirewallService.class);
        intent.setAction(FirewallService.ACTION_START);
        intent.putExtra(FirewallService.EXTRA_PACKAGE, selectedPackage);
        startService(intent);
        isFirewallActive = true;
        updateButtonUI();
    }

    private void stopFirewall() {
        Intent intent = new Intent(this, FirewallService.class);
        intent.setAction(FirewallService.ACTION_STOP);
        startService(intent);
        isFirewallActive = false;
        updateButtonUI();
    }

    private void updateButtonUI() {
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setCornerRadius(20f);
        if (isFirewallActive) {
            btnToggle.setText("إيقاف الجدار الناري");
            btnBg.setColor(Color.parseColor("#D50000"));
        } else {
            btnToggle.setText("تشغيل الجدار الناري");
            btnBg.setColor(Color.parseColor("#00C853"));
        }
        btnToggle.setBackground(btnBg);
    }
}

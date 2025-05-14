package com.example.safelink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private ModelInterpreter modelInterpreter;
    private EditText urlEditText;
    private Button checkUrlButton;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔒 عرض مربع حوار لاختيار التطبيق الافتراضي
        SharedPreferences prefs = getSharedPreferences("safelink_prefs", MODE_PRIVATE);
        boolean askedAlready = prefs.getBoolean("asked_set_default", false);

        if (!askedAlready) {
            new AlertDialog.Builder(this)
                    .setTitle("🔒 حماية إضافية")
                    .setMessage("هل ترغب بجعل SafeLink يفتح الروابط تلقائيًا لحمايتك من المواقع الاحتيالية؟")
                    .setPositiveButton("نعم، فعّله", (dialog, which) -> {
                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
                        startActivity(intent);
                        prefs.edit().putBoolean("asked_set_default", true).apply();
                    })
                    .setNegativeButton("لاحقًا", (dialog, which) -> {
                        prefs.edit().putBoolean("asked_set_default", true).apply();
                    })
                    .setCancelable(false)
                    .show();
        }

        // 🔔 طلب إذن الإشعارات على Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        // ربط عناصر الواجهة
        urlEditText = findViewById(R.id.urlEditText);
        checkUrlButton = findViewById(R.id.checkUrlButton);
        resultTextView = findViewById(R.id.resultTextView);

        // تهيئة قناة الإشعارات (لو عندك إشعارات)
        NotificationChannelHelper.createChannel(this);

        // تهيئة مفسّر الروابط
        modelInterpreter = new ModelInterpreter(this);

        // ✔️ عند الضغط على الزر
        checkUrlButton.setOnClickListener(v -> {
            String url = urlEditText.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(this, "يرجى إدخال رابط", Toast.LENGTH_SHORT).show();
                return;
            }

            modelInterpreter.checkUrl(url, result -> {
                // ✅ حدد الرسالة واللون حسب النتيجة
                String message;
                int color;

                if (result.toLowerCase().contains("آمن") || result.toLowerCase().contains("safe")) {
                    message = "✅ الرابط آمن!";
                    color = getResources().getColor(android.R.color.holo_green_dark);
                } else {
                    message = "⚠️ الرابط احتيالي أو مشبوه!";
                    color = getResources().getColor(android.R.color.holo_red_dark);
                }

                resultTextView.setText(message);
                resultTextView.setTextColor(color);
                resultTextView.setVisibility(TextView.VISIBLE);

                // ✨ تشغيل الأنيميشن
                Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                resultTextView.startAnimation(fadeIn);
            });
        });
    }
}

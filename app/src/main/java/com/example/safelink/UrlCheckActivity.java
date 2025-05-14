package com.example.safelink;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

public class UrlCheckActivity extends AppCompatActivity {

    private WebView webView;
    private EditText urlBar;
    private ProgressBar progressBar;
    private ImageButton backButton, btnBack, btnForward, btnReload, btnFavorite, btnShare;
    private Set<String> favorites = new HashSet<>();
    private PermissionRequest pendingRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_url_check);

        webView = findViewById(R.id.webview);
        urlBar = findViewById(R.id.urlBar);
        progressBar = findViewById(R.id.progressBar);
        backButton = findViewById(R.id.back_button);
        btnBack = findViewById(R.id.btn_back);
        btnForward = findViewById(R.id.btn_forward);
        btnReload = findViewById(R.id.btn_reload);
        btnFavorite = findViewById(R.id.btn_favorite);
        btnShare = findViewById(R.id.btn_share);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        }, 1);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                urlBar.setText(url);
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                String[] resources = request.getResources();
                ArrayList<String> permissionsToRequest = new ArrayList<>();

                for (String res : resources) {
                    if (res.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE) &&
                            checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        permissionsToRequest.add(Manifest.permission.CAMERA);
                    }
                    if (res.equals(PermissionRequest.RESOURCE_AUDIO_CAPTURE) &&
                            checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        permissionsToRequest.add(Manifest.permission.RECORD_AUDIO);
                    }
                }

                if (!permissionsToRequest.isEmpty()) {
                    pendingRequest = request;
                    ActivityCompat.requestPermissions(
                            UrlCheckActivity.this,
                            permissionsToRequest.toArray(new String[0]),
                            10
                    );
                } else {
                    request.grant(resources);
                }
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        urlBar.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                String enteredUrl = urlBar.getText().toString().trim();
                if (!enteredUrl.startsWith("http")) {
                    enteredUrl = "http://" + enteredUrl;
                }
                webView.loadUrl(enteredUrl);
                return true;
            }
            return false;
        });

        btnBack.setOnClickListener(v -> {
            if (webView.canGoBack()) webView.goBack();
        });

        btnForward.setOnClickListener(v -> {
            if (webView.canGoForward()) webView.goForward();
        });

        btnReload.setOnClickListener(v -> webView.reload());

        backButton.setOnClickListener(v -> {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                finish();
            }
        });

        btnFavorite.setOnClickListener(v -> {
            String currentUrl = webView.getUrl();
            if (currentUrl != null && !favorites.contains(currentUrl)) {
                favorites.add(currentUrl);
                Toast.makeText(this, "⭐ تمت الإضافة إلى المفضلة", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "📌 هذا الرابط موجود بالفعل", Toast.LENGTH_SHORT).show();
            }
        });

        btnShare.setOnClickListener(v -> {
            String urlToShare = webView.getUrl();
            if (urlToShare != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, urlToShare);
                startActivity(Intent.createChooser(shareIntent, "مشاركة الرابط عبر..."));
            }
        });

        Uri data = getIntent().getData();
        if (data != null) {
            String rawUrl = data.toString();
            String finalUrl = rawUrl.startsWith("http://") || rawUrl.startsWith("https://")
                    ? rawUrl
                    : "http://" + rawUrl;

            new ModelInterpreter(this).checkUrl(finalUrl, result -> {
                if (result.equalsIgnoreCase("Safe")) {
                    openInWebView(finalUrl);
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("\uD83D\uDEA8 تحذير: رابط مشبوه!")
                            .setMessage("هذا الرابط قد يكون خطيرًا:\n\n" + finalUrl + "\n\nهل تريد المتابعة؟")
                            .setPositiveButton("\uD83D\uDEAA لا، إغلاق", (dialog, which) -> finish())
                            .setNegativeButton("⚠️ فتح على مسؤوليتي", (dialog, which) -> openInWebView(finalUrl))
                            .setCancelable(false)
                            .show();
                }
            });
        }
    }

    private void openInWebView(String url) {
        webView.loadUrl(url);
        urlBar.setText(url);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 10 && pendingRequest != null) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                pendingRequest.grant(pendingRequest.getResources());
            } else {
                pendingRequest.deny();
                Toast.makeText(this, "❌ لم يتم منح الصلاحيات المطلوبة", Toast.LENGTH_SHORT).show();
            }

            pendingRequest = null;
        }
    }
}

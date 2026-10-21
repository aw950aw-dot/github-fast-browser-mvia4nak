package com.generated.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private WebView webView;
    private EditText etUrl;
    private ProgressBar progressBar;
    private LinearLayout panelSettings;
    private TextView tvServiceStatus;

    private boolean isServiceRunning = false;
    private static final String GITHUB_HOME = "https://github.com";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        etUrl = findViewById(R.id.etUrl);
        progressBar = findViewById(R.id.progressBar);
        panelSettings = findViewById(R.id.panelSettings);
        tvServiceStatus = findViewById(R.id.tvServiceStatus);

        Button btnHome = findViewById(R.id.btnHome);
        Button btnGo = findViewById(R.id.btnGo);
        Button btnSettings = findViewById(R.id.btnSettings);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnForward = findViewById(R.id.btnForward);
        Button btnRefresh = findViewById(R.id.btnRefresh);
        Button btnOpenSystemSettings = findViewById(R.id.btnOpenSystemSettings);
        Button btnToggleService = findViewById(R.id.btnToggleService);

        // Configure Fast WebView settings
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        // Fast WebView Client
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.contains("github.com") || url.contains("githubusercontent.com")) {
                    view.loadUrl(url);
                    return true;
                }
                // Open external links outside GitHub in system browser
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    // ignore
                }
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("github.com") || url.contains("githubusercontent.com")) {
                    view.loadUrl(url);
                    return true;
                }
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    // ignore
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                etUrl.setText(url);
            }
        });

        // WebChromeClient for smooth loading progress
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });

        // Built-in File Download Manager integration
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                try {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setMimeType(mimeType);
                    String fileName = Uri.parse(url).getLastPathSegment();
                    request.setTitle(fileName != null ? fileName : "GitHub Download");
                    request.setDescription(getString(R.string.downloading));
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName != null ? fileName : "github_download");

                    DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    if (dm != null) {
                        dm.enqueue(request);
                        Toast.makeText(MainActivity.this, R.string.download_complete, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, R.string.download_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle URL input action
        etUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    loadEnteredUrl();
                    return true;
                }
                return false;
            }
        });

        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadEnteredUrl();
            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                panelSettings.setVisibility(View.GONE);
                webView.loadUrl(GITHUB_HOME);
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (panelSettings.getVisibility() == View.VISIBLE) {
                    panelSettings.setVisibility(View.GONE);
                } else {
                    panelSettings.setVisibility(View.VISIBLE);
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                panelSettings.setVisibility(View.GONE);
                if (webView.canGoBack()) {
                    webView.goBack();
                }
            }
        });

        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                panelSettings.setVisibility(View.GONE);
                if (webView.canGoForward()) {
                    webView.goForward();
                }
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                panelSettings.setVisibility(View.GONE);
                webView.reload();
            }
        });

        // Open system app links settings for App Links configuration
        btnOpenSystemSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        intent = new Intent(android.provider.Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                                Uri.parse("package:" + getPackageName()));
                    } else {
                        intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + getPackageName()));
                    }
                    startActivity(intent);
                } catch (Exception e) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            }
        });

        // Toggle background service
        btnToggleService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(MainActivity.this, GitHubBackgroundService.class);
                if (isServiceRunning) {
                    stopService(serviceIntent);
                    isServiceRunning = false;
                    tvServiceStatus.setText(R.string.service_stopped);
                    Toast.makeText(MainActivity.this, R.string.service_stopped, Toast.LENGTH_SHORT).show();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent);
                    } else {
                        startService(serviceIntent);
                    }
                    isServiceRunning = true;
                    tvServiceStatus.setText(R.string.service_running);
                    Toast.makeText(MainActivity.this, R.string.service_running, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle incoming intent (Deep Link from opening a GitHub link elsewhere on device)
        handleIncomingIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                panelSettings.setVisibility(View.GONE);
                webView.loadUrl(data.toString());
                return;
            }
        }
        // Default start URL if nothing else requested
        if (webView.getUrl() == null) {
            webView.loadUrl(GITHUB_HOME);
        }
    }

    private void loadEnteredUrl() {
        panelSettings.setVisibility(View.GONE);
        String query = etUrl.getText().toString().trim();
        if (query.isEmpty()) {
            return;
        }
        String targetUrl;
        if (query.startsWith("http://") || query.startsWith("https://")) {
            targetUrl = query;
        } else if (query.contains(".") && !query.contains(" ")) {
            targetUrl = "https://" + query;
        } else {
            // Treat as GitHub search or repo lookup
            targetUrl = "https://github.com/search?q=" + Uri.encode(query);
        }
        webView.loadUrl(targetUrl);
    }

    @Override
    public void onBackPressed() {
        if (panelSettings.getVisibility() == View.VISIBLE) {
            panelSettings.setVisibility(View.GONE);
            return;
        }
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}

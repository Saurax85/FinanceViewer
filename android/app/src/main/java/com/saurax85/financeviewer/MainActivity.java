package com.saurax85.financeviewer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    private WebView webView;

    private static final String URL =
            "https://script.google.com/macros/s/AKfycbxMnUJiO1mRz0VRRA_5qnmyb-GANxy5US5d7KoabAodtGtlpRsNAbaOGR6nXeQ651tM/exec"
            + "?action=2026&mobile=1&refresh=1";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(11, 15, 20));
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);

        // Keep the existing Mobile layout scale as close as possible.
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setTextZoom(85);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setHorizontalFadingEdgeEnabled(false);

        // Android 15 / targetSdk 35 enforces edge-to-edge. Keep the
        // FinanceViewer content below the status/camera area and above
        // the navigation area, without changing Mobile.html.
        webView.setOnApplyWindowInsetsListener((view, insets) -> {
            int top;
            int bottom;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                top = insets.getInsets(WindowInsets.Type.statusBars()
                        | WindowInsets.Type.displayCutout()).top;
                bottom = insets.getInsets(WindowInsets.Type.navigationBars()).bottom;
            } else {
                top = insets.getSystemWindowInsetTop();
                bottom = insets.getSystemWindowInsetBottom();
            }

            view.setPadding(0, top, 0, bottom);
            return insets;
        });

        webView.post(() -> webView.requestApplyInsets());
        webView.loadUrl(URL);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}

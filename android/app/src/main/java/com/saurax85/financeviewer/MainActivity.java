package com.saurax85.financeviewer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;
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

        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(11, 15, 20));
        root.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);

        // WebView text zoom does not reliably change the explicit CSS pixel
        // sizes used by FinanceViewer Mobile. Keep it neutral and apply a
        // deterministic CSS scale after the page has loaded.
        settings.setUseWideViewPort(false);
        settings.setLoadWithOverviewMode(false);
        settings.setTextZoom(100);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                applyMobileScale(view);
                hideAppsScriptBanner(view);
            }
        });
        webView.setWebChromeClient(new WebChromeClient());

        webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setHorizontalFadingEdgeEnabled(false);

        // Android 15 / targetSdk 35 edge-to-edge: keep the content below
        // the status/camera area and above the navigation area.
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

            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
            lp.topMargin = top;
            lp.bottomMargin = bottom;
            view.setLayoutParams(lp);
            return insets;
        });

        webView.post(() -> webView.requestApplyInsets());
        webView.loadUrl(URL);
    }

    private void applyMobileScale(WebView view) {
        // Keep the header/navigation at the exact Mobile size. Only the
        // content below it is scaled down, because that is where the
        // table/card text is wrapping differently from the Mobile version.
        String js = "(function(){"
                + "var s=document.getElementById('financeviewer-apk-scale');"
                + "if(!s){"
                + "s=document.createElement('style');"
                + "s.id='financeviewer-apk-scale';"
                + "s.textContent='.content{zoom:0.80!important;width:125%!important;max-width:125%!important;}';"
                + "document.head.appendChild(s);"
                + "}"
                + "})();";
        view.evaluateJavascript(js, null);
    }

    private void hideAppsScriptBanner(WebView view) {
        // Apps Script can inject a small warning banner above HtmlService pages.
        // Remove only that warning; do not modify Mobile.html.
        String js = "(function(){"
                + "function hide(){"
                + "var els=document.querySelectorAll('*');"
                + "for(var i=0;i<els.length;i++){"
                + "var e=els[i],t=(e.innerText||e.textContent||'').replace(/\\s+/g,' ').trim();"
                + "if(t.indexOf('Questa applicazione è stata creata da un utente di Google Apps Script')!==-1){"
                + "var r=e.getBoundingClientRect();"
                + "if(r.top<120 && r.height<120){"
                + "e.style.setProperty('display','none','important');"
                + "if(e.parentElement && e.parentElement!==document.body){"
                + "var p=e.parentElement,pr=p.getBoundingClientRect();"
                + "if(pr.top<120 && pr.height<120){"
                + "p.style.setProperty('display','none','important');"
                + "}"
                + "}"
                + "}"
                + "}"
                + "}"
                + "}"
                + "hide();"
                + "setTimeout(hide,300);"
                + "setTimeout(hide,1000);"
                + "setTimeout(hide,2500);"
                + "})();";
        view.evaluateJavascript(js, null);
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

package com.chenboda01.bflashcardsv1;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebChromeClient;
import android.webkit.JavascriptInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.content.Context;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WebView webView;

    public class AndroidBridge {
        @JavascriptInterface
        public void openApp(String pkg, String cls, String label) {
            runOnUiThread(() -> {
                try {
                    PackageManager pm = getPackageManager();
                    Intent launch = pm.getLaunchIntentForPackage(pkg);
                    if (launch == null && cls != null && cls.length() > 0) {
                        launch = new Intent(Intent.ACTION_MAIN);
                        launch.addCategory(Intent.CATEGORY_LAUNCHER);
                        launch.setClassName(pkg, cls);
                    }
                    if (launch != null) {
                        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(launch);
                    } else {
                        Toast.makeText(MainActivity.this, label + " is not installed yet.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Could not open " + label + ".", Toast.LENGTH_LONG).show();
                }
            });
        }

        @JavascriptInterface
        public void printFlashcards(String html) {
            runOnUiThread(() -> {
                try {
                    WebView printWebView = new WebView(MainActivity.this);
                    printWebView.setWebViewClient(new android.webkit.WebViewClient() {
                        public void onPageFinished(WebView view, String url) {
                            PrintManager pm = (PrintManager)getSystemService(Context.PRINT_SERVICE);
                            if (pm != null) pm.print("B-Flashcards V1", view.createPrintDocumentAdapter("B-Flashcards V1"), new PrintAttributes.Builder().build());
                        }
                    });
                    printWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Could not print.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        webView.loadUrl("file:///android_asset/index.html");
    }

    public void onBackPressed() {
        webView.evaluateJavascript("window.bflashBack && window.bflashBack()", null);
    }
}

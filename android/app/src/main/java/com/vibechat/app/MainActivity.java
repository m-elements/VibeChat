package com.vibechat.app;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.PermissionRequest;
import android.webkit.WebSettings;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;

import androidx.core.app.ActivityCompat;

import android.content.pm.PackageInfo;

public class MainActivity extends Activity {

    private WebView webView;
    private PermissionRequest pendingRequest;

    private static final int MEDIA_PERMISSION_CODE = 101;

    private static final String APP_URL =
        "https://m-elements.github.io/VibeChat/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(
                WebView view,
                SslErrorHandler handler,
                SslError error
            ) {
                handler.cancel();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onPermissionRequest(
                PermissionRequest request
            ) {
                runOnUiThread(() -> {

                    String host = request.getOrigin()
                        .getHost();

                    if (!"m-elements.github.io".equals(host)) {
                        request.deny();
                        return;
                    }

                    pendingRequest = request;

                    String[] permissions = {
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    };

                    boolean needsPermission = false;

                    for (String permission : permissions) {
                        boolean requested = false;

                        for (String resource :
                            request.getResources()) {

                            if (resource.equals(
                                PermissionRequest.RESOURCE_VIDEO_CAPTURE
                            ) && permission.equals(
                                Manifest.permission.CAMERA
                            )) {
                                requested = true;
                            }

                            if (resource.equals(
                                PermissionRequest.RESOURCE_AUDIO_CAPTURE
                            ) && permission.equals(
                                Manifest.permission.RECORD_AUDIO
                            )) {
                                requested = true;
                            }
                        }

                        if (requested &&
                            checkSelfPermission(permission)
                            != PackageManager.PERMISSION_GRANTED) {
                            needsPermission = true;
                        }
                    }

                    if (needsPermission) {
                        ActivityCompat.requestPermissions(
                            MainActivity.this,
                            permissions,
                            MEDIA_PERMISSION_CODE
                        );
                    } else {
                        request.grant(request.getResources());
                        pendingRequest = null;
                    }
                });
            }
        });

        setContentView(webView);

        webView.loadUrl(APP_URL);
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode,
        String[] permissions,
        int[] grantResults
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        );

        if (requestCode == MEDIA_PERMISSION_CODE &&
            pendingRequest != null) {

            boolean allGranted = true;

            for (String resource :
                pendingRequest.getResources()) {

                if (resource.equals(
                    PermissionRequest.RESOURCE_VIDEO_CAPTURE
                ) && checkSelfPermission(
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                }

                if (resource.equals(
                    PermissionRequest.RESOURCE_AUDIO_CAPTURE
                ) && checkSelfPermission(
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                }
            }

            if (allGranted) {
                pendingRequest.grant(
                    pendingRequest.getResources()
                );
            } else {
                pendingRequest.deny();
            }

            pendingRequest = null;
        }
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

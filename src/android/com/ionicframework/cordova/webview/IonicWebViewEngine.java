package com.ionicframework.cordova.webview;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewEngine;

import org.apache.cordova.NativeToJsMessageQueue;
import org.apache.cordova.PluginManager;
import org.apache.cordova.engine.SystemWebViewClient;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.apache.cordova.engine.SystemWebView;

public class IonicWebViewEngine extends SystemWebViewEngine {
  public static final String TAG = "IonicWebViewEngine";

  private WebViewLocalServer localServer;

  /** Used when created via reflection. */
  public IonicWebViewEngine(Context context, CordovaPreferences preferences) {
    super(new SystemWebView(context), preferences);
    Log.d(TAG, "Ionic Web View Engine Starting Right Up 1...");
  }

  public IonicWebViewEngine(SystemWebView webView) {
    super(webView, null);
    Log.d(TAG, "Ionic Web View Engine Starting Right Up 2...");
  }

  public IonicWebViewEngine(SystemWebView webView, CordovaPreferences preferences) {
    super(webView, preferences);
    Log.d(TAG, "Ionic Web View Engine Starting Right Up 3...");
  }

  @Override
  public void init(CordovaWebView parentWebView, CordovaInterface cordova, final CordovaWebViewEngine.Client client,
                   CordovaResourceApi resourceApi, PluginManager pluginManager,
                   NativeToJsMessageQueue nativeToJsMessageQueue) {
    ConfigXmlParser parser = new ConfigXmlParser();
    parser.parse(cordova.getContext());

    localServer = new WebViewLocalServer(cordova.getActivity(), "localhost:8080", true, parser);
    WebViewLocalServer.AssetHostingDetails ahd = localServer.hostAssets("www");

    webView.setWebViewClient(new ServerClient(this, parser));

    super.init(parentWebView, cordova, client, resourceApi, pluginManager, nativeToJsMessageQueue);
  }

  private class ServerClient extends SystemWebViewClient
  {
    private ConfigXmlParser parser;

    public ServerClient(SystemWebViewEngine parentEngine, ConfigXmlParser parser) {
      super(parentEngine);
      this.parser = parser;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
      return localServer.shouldInterceptRequest(request);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      super.onPageStarted(view, url, favicon);
      if (url.equals(parser.getLaunchUrl())) {
        view.stopLoading();
        view.loadUrl("http://localhost:8080/");
      }
    }
  }

  public void setServerBasePath(String path){
    localServer.hostFiles(path);
  }
}


package lab.cupid.mangafox;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import lab.cupid.mangafox.utils.AdBlocker;

public class MainActivity extends AppCompatActivity {

    private static final String MANGAFOX_URL = "https://m.mangafox.me/";

    // Current context
    private MainActivity mContext;

    // Layout components
    private WebView mWebView;
    private ProgressBar mProgress;

    // Provides some urls to block
    private AdBlocker mAdBlocker;

    // If it is true, the application shall exit on back pressed event
    private boolean mExitPending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Context of this activity
        mContext = this;

        // Get main WebView component
        mWebView = (WebView) findViewById(R.id.wv_main);

        // Get ProgressBar component
        mProgress = (ProgressBar) findViewById(R.id.pb_loading);

        // Attach clients to mWebView to measure progress and errors
        mWebView.setWebViewClient(new WebClient());
        mWebView.setWebChromeClient(new ChromeClient());

        // Get settings for mWebView
        WebSettings settings = mWebView.getSettings();
        // The two lines below are to enable interpreting <meta viewport> tag.
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        // Enable JavaScript
        settings.setJavaScriptEnabled(true);

        // Load AdBlocker
        mAdBlocker = new AdBlocker(this);
        mAdBlocker.execute();

        // Load MangaFox
        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else {
            mWebView.loadUrl(MANGAFOX_URL);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mWebView.saveState(outState);
    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        } else if(mExitPending) {
            super.onBackPressed();
        } else {
            mExitPending = true;
            Toast.makeText(mContext, getString(R.string.toast_exit_pending), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_refresh:
                mWebView.reload();
                return true;
            case R.id.menu_home:
                mWebView.loadUrl(MANGAFOX_URL);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Customized WebViewClient to handle errors
     */
    private class WebClient extends WebViewClient {
        // To cache loaded urls
        private final Map<String, Boolean> mLoadedUrls = new HashMap<>();

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            // Ignore SSL certificate errors
            handler.proceed();

            // Log the error message for debugging
            Log.d(getClass().getSimpleName(), error.toString());
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);

            // Log the error message for debugging
            Log.d(getClass().getSimpleName(), error.toString());

            // Display toast message describing error details
            String errorDetails = String.valueOf(error.getDescription());
            Toast.makeText(mContext, errorDetails, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(getClass().getSimpleName(), "Loading page: " + url);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            // Uses cache to determine if the url is ad,
            // and returns an empty resource for ads.
            boolean ad;
            if (!mLoadedUrls.containsKey(url)) {
                ad = mAdBlocker.isAd(url);
                mLoadedUrls.put(url, ad);
            } else {
                ad = mLoadedUrls.get(url);
            }

            Log.v(getClass().getSimpleName(), (ad ? "Blocked" : "Pass") + ": " + Uri.parse(url).getHost());

            return ad ? mAdBlocker.createEmptyResource() :
                    super.shouldInterceptRequest(view, url);
        }
    }

    /**
     * Customized ChromeClient to display progress changes.
     */
    private class ChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            // Make progress bar visible only when the client is loading.
            if(newProgress < 100) {
                mProgress.setVisibility(View.VISIBLE);
            } else {
                mProgress.setVisibility(View.GONE);
                mWebView.setBackgroundColor(Color.BLACK);
            }

            // Display the current progress
            mProgress.setProgress(newProgress);

            // Remove mExitPending status
            mExitPending = false;
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            // Replace activity title with new title
            mContext.setTitle(title);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.v(getClass().getSimpleName(), consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }
    }

}

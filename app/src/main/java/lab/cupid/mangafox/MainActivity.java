package lab.cupid.mangafox;

import android.content.Context;
import android.graphics.Color;
import android.net.http.SslError;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String MANGAFOX_URL = "https://m.mangafox.me/";

    private MainActivity mContext;

    private WebView mWebView;
    private ProgressBar mProgress;

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
        //settings.setJavaScriptEnabled(true);

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
     * Customized ChromeClient to display progress changes.
     */
    private class ChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);

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
            super.onReceivedTitle(view, title);
            mContext.setTitle(title);
        }
    }

    /**
     * Customized WebViewClient to handle errors
     */
    private class WebClient extends WebViewClient {

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            // Ignore SSL certificate errors
            handler.proceed();

            // Log the error message for debugging
            Log.d(MainActivity.class.getSimpleName(), error.toString());
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);

            // Log the error message for debugging
            Log.d(MainActivity.class.getSimpleName(), error.toString());

            // Display toast message describing error details
            String errorDetails = String.valueOf(error.getDescription());
            Toast.makeText(mContext, errorDetails, Toast.LENGTH_LONG).show();
        }
    }
}

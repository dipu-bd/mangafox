package lab.cupid.mangafox.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.webkit.WebResourceResponse;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import lab.cupid.mangafox.R;

/**
 * An simple AdBlocker based on https://easylist.to/easylist/easylist.txt
 *
 * Original version: http://www.hidroh.com/2016/05/19/hacking-up-ad-blocker-android/
 */
public final class AdBlocker extends AsyncTask<Void, Void, Void>{

    private static final String TAG = "AdBlocker";

    private static final String COMMENT_CHARS = "#!";
    private static final int[] HOST_RESOURCES = {
            R.raw.customlist,
            //R.raw.easylist,
            //R.raw.easyprivacy,
    };

    private final Context mContext;
    private final Set<String> AD_HOSTS = new HashSet<>();

    // Create new instance with application context.
    public AdBlocker(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        for (int resId: HOST_RESOURCES) {
            String resName = mContext.getResources().getResourceName(resId);
            try {
                loadFromAssets(resId);
                Log.d(TAG, "Loaded: " + resName);
            } catch (IOException ex) {
                Log.d(TAG, "Failed to load: " + resName);
            }
        }
        return null;
    }

    @WorkerThread
    private void loadFromAssets(int resId) throws IOException {
        // Open streams
        InputStream inputStream = mContext.getResources().openRawResource(resId);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        // Read lines and add to AD_HOSTS
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();
            if(TextUtils.isEmpty(line) &&
                    COMMENT_CHARS.contains(line.substring(0, 1))) {
                continue;
            }
            AD_HOSTS.add(line);
        }
        // Close streams
        bufferedReader.close();
        inputStreamReader.close();
        inputStream.close();
    }

    public boolean isAd(String url) {
        Uri uri = Uri.parse(url);
        return isAdHost(uri != null ? uri.getHost() : "");
    }

    private boolean isAdHost(String host) {
        if (TextUtils.isEmpty(host)) {
            return false;
        }
        int index = host.indexOf(".");
        if(index >= 0) {
            return AD_HOSTS.contains(host);
        } else if(index + 1 < host.length()) {
            return isAdHost(host.substring(index + 1));
        }
        return false;
    }

    public WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }
}

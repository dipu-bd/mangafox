package lab.cupid.mangafox.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private static final int[] HOST_RESOURCES = {
            R.raw.easylist
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
            try {
                loadFromAssets(resId);
            } catch (IOException ex) {
                String resName = mContext.getResources().getResourceName(resId);
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
            AD_HOSTS.add(line);
        }
        // Close streams
        bufferedReader.close();
        inputStreamReader.close();
        inputStream.close();
    }
}

package pct.droid.base.providers;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import pct.droid.base.PopcornApplication;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.providers.media.MediaProvider;
import pct.droid.base.providers.meta.MetaProvider;
import pct.droid.base.providers.subs.SubsProvider;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.StorageUtils;

/**
 * BaseProvider.java
 * <p/>
 * Base class for providers, has code to enqueue network requests to the OkHttpClient
 */
public abstract class BaseProvider {

    private OkHttpClient mClient;
    protected Gson mGson = new Gson();
    protected Call mCurrentCall;

    protected OkHttpClient getClient() {
        if(mClient == null) {
            mClient = new OkHttpClient();

            int cacheSize = 10 * 1024 * 1024;
            try {
                Cache cache = new Cache(new File(PrefUtils.get(PopcornApplication.getAppContext(), Prefs.STORAGE_LOCATION, StorageUtils.getIdealCacheDirectory(PopcornApplication.getAppContext()).toString())), cacheSize);
                mClient.setCache(cache);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mClient;
    }

    /**
     * Enqueue request without callback
     *
     * @param request Request
     * @return Call
     */
    protected Call enqueue(Request request) {
        return enqueue(request, null);
    }

    /**
     * Enqueue request with callback
     *
     * @param request         Request
     * @param requestCallback Callback
     * @return Call
     */
    protected Call enqueue(Request request, com.squareup.okhttp.Callback requestCallback) {
        mCurrentCall = getClient().newCall(request);
        if (requestCallback != null) mCurrentCall.enqueue(requestCallback);
        return mCurrentCall;
    }

    public void cancel() {
        // Cancel in asynctask to prevent networkOnMainThreadException
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mClient.cancel(MediaProvider.MEDIA_CALL);
                mClient.cancel(MetaProvider.META_CALL);
                mClient.cancel(SubsProvider.SUBS_CALL);
                return null;
            }
        }.execute();
    }

    /**
     * Build URL encoded query
     *
     * @param valuePairs List with key-value items
     * @return Query string
     */
    protected String buildQuery(List<BasicNameValuePair> valuePairs) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            for (int i = 0; i < valuePairs.size(); i++) {
                NameValuePair pair = valuePairs.get(i);
                stringBuilder.append(URLEncoder.encode(pair.getName(), "utf-8"));
                stringBuilder.append("=");
                stringBuilder.append(URLEncoder.encode(pair.getValue(), "utf-8"));
                if (i + 1 != valuePairs.size()) stringBuilder.append("&");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        return stringBuilder.toString();
    }

}

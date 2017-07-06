package com.example.android.newsapp;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import java.util.List;

/**
 * Loads a list of News Items by using an AsyncTask to perform the
 * network request to the Guardian API URL.
 */

public class NewsLoader extends AsyncTaskLoader<List<NewsItem>> {

    private static final String LOG_TAG = NewsLoader.class.getSimpleName();

    private String mUrl;


    /**
     * Constructor for a new {@link NewsLoader} object
     * @param context
     * @param url
     */
    public NewsLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public List<NewsItem> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform network request, parse the response, and extract a list of news items
        List<NewsItem> newsItems = QueryUtils.fetchNewsItems(mUrl, getContext());
        return newsItems;
    }
}

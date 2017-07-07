package com.example.android.newsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import static android.content.Context.CONNECTIVITY_SERVICE;


/**
 * Fragment to display list of news items as fetched from Guardian News API
 */

public class NewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<NewsItem>>,
        SwipeRefreshLayout.OnRefreshListener {

    public static final String LOG_TAG = NewsFragment.class.getName();

    /** Guardian API Base URL */
    private static final String API_BASE_URL = "http://content.guardianapis.com/search?q=";
    private static final String API_SEARCH_URL = "https://content.guardianapis.com/search?section=";

    private View mView;
    private NewsAdapter mAdapter;
    private TextView mEmptyStateTextView;
    public static List<NewsItem> mListNews;
    private ListView mNewsListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int mLoaderId;


    /**
     * Method to return a new instance of the news fragment
     * @param newsSection
     * @return newsFragment
     */
    public static NewsFragment newInstance(String newsSection, int menuPosition){
        NewsFragment newsFragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString("section", newsSection);
        args.putInt("position", menuPosition);
        newsFragment.setArguments(args);
        return newsFragment;
    }

    /**
     * Method to get News Section Name
     * @return section
     */
    public String getSection() {
        return getArguments().getString("section", "");
    }

    /**
     * Method to get Selected Menu Position
     * @return menu position
     */
    public int getMenuPosition() {
        return getArguments().getInt("position", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_news, container, false);

        // Activate SwipeRefreshLayout feature so news list is updated when screen in swiped
        mSwipeRefreshLayout = (SwipeRefreshLayout) mView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorAccent));

        // Find a reference to the {@link ListView} in the layout
        mNewsListView = (ListView) mView.findViewById(R.id.list_news);

        // Set empty view
        mEmptyStateTextView = (TextView) mView.findViewById(R.id.text_empty_list);
        mNewsListView.setEmptyView(mEmptyStateTextView);

        // Create a new adapter that takes the list as input
        mListNews = new ArrayList<NewsItem>();
        mAdapter = new NewsAdapter(getContext(), mListNews);
        mNewsListView.setAdapter(mAdapter);

        // Get a reference to the ConnectivityManager to check state of network connectivity
      //  ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
       // NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getActivity().getSupportLoaderManager();

            // Get loaderId from menu position, so that a different loaderId is assigned for each section
            mLoaderId = getMenuPosition();
            loaderManager.initLoader(mLoaderId, null, this);

        } else {
            // Hide loading indicator and show empty state view
            View progressIndicator = mView.findViewById(R.id.progress_indicator);
            mEmptyStateTextView.setText(R.string.error_no_connection);
            progressIndicator.setVisibility(View.GONE);
        }

        mNewsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                NewsItem currentNews = mAdapter.getItem(position);
                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri newsUri = Uri.parse(currentNews.getNewsUrl());

                // Create a new intent to view the earthquake URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });

        return mView;
    }


    @Override
    public Loader<List<NewsItem>> onCreateLoader(int i, Bundle bundle) {

        String section = getSection();
        String url = "";
        String orderBy = "";
        long longFromDate = 0;
        String fromDate = "";

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (section.equals(getString(R.string.menu_home).toLowerCase())) {
            url = API_BASE_URL;
        } else {
            url = API_SEARCH_URL + section;
        }

        Uri baseUri = Uri.parse(url);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // Get SharedPreferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        longFromDate = sharedPrefs.getLong(
                getString(R.string.settings_from_date_key), 0
        );

        Date dateObject = new Date(longFromDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateObject);
        fromDate = dateFormat.format(calendar.getTime());

        // Append parameters obtained fro, SharedPreferences
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("from-date", fromDate);

        return new NewsLoader(getContext(), uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<NewsItem>> loader, List<NewsItem> newsItems) {

        mSwipeRefreshLayout.setRefreshing(false);

        // Hide progress indicator because the data has been loaded
        View progressIndicator = mView.findViewById(R.id.progress_indicator);
        progressIndicator.setVisibility(View.GONE);

        // Check if connection is still available, otherwise show appropriate message
        if (isConnected()) {

            // Set empty state text when no news found
            if (newsItems == null || newsItems.size() == 0) {
                mEmptyStateTextView.setVisibility(View.VISIBLE);
                mEmptyStateTextView.setText(getString(R.string.info_no_news));
            } else {
                mEmptyStateTextView.setVisibility(View.GONE);
            }

            mListNews.clear();

            // If there is a valid list of {@link Book}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (newsItems != null && !newsItems.isEmpty()) {
                mListNews.addAll(newsItems);
                mAdapter.notifyDataSetChanged();
            }
        } else {
            mEmptyStateTextView.setText(R.string.error_no_connection);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<NewsItem>> loader) {
        mAdapter.clear();
    }

    @Override
    public void onRefresh() {
        getActivity().getSupportLoaderManager().restartLoader(mLoaderId, null, this);
    }

    /**
     * Method to check network connectivity
     * @return true/false
     */
    public boolean isConnected() {
        boolean hasNetwork;

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            hasNetwork = true;
        } else {
            hasNetwork = false;
        }

        return hasNetwork;
    }

}

package com.example.android.newsapp;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving news data from Guardian API
 */


public class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getName();
    private static Context mContext;

    // News API keys
    private static final String API_KEY_RESPONSE = "response";
    private static final String API_KEY_RESULTS = "results";
    private static final String API_KEY_SECTION = "sectionName";
    private static final String API_KEY_PUBLISHED_DATE = "webPublicationDate";
    private static final String API_KEY_TITLE = "webTitle"; // same key used for news title and author name
    private static final String API_KEY_WEBURL = "webUrl";
    private static final String API_KEY_TAGS = "tags";


    /**
     * This is a private constructor and only meant to hold static variables and methods,
     * which can be accessed directly from the class name Utils
     */
    private QueryUtils() {
    }

    /**
     * Query the URL and return a list of {@link NewsItem} objects.
     */
    public static List<NewsItem> fetchNewsItems(String requestUrl, Context context) {

        mContext = context;

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, mContext.getString(R.string.exception_http_request), e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link NewsItem}s
        List<NewsItem> newsItems = extractFeatureFromJson(jsonResponse);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Return the list of {@link newsItems}
        return newsItems;
    }

    /**
     * This method returns new URL object from the given string URL
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(prepareUrl(stringUrl));
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, mContext.getString(R.string.exception_url_invalid), e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, mContext.getString(R.string.exception_resp_code) + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, mContext.getString(R.string.exception_json_results), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link NewsItem} objects retrieved from parsing a JSON response.
     */
    private static List<NewsItem> extractFeatureFromJson(String newsJSON) {

        /** If the JSON string is empty or null, then return early. */
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        /** Create an empty ArrayList used to add news items */
        List<NewsItem> newsItems = new ArrayList<>();

        try {

            JSONObject baseJsonResponse;            // JSON Object for the data retrieved from API request
            JSONObject jsonResults;                 // JSON results fetched
            JSONArray newsArray;                    // Array of News Items
            JSONObject currentNewsItem;             // JSON object for current news item in the newsArray
            String newsTitle = "";                  // News Title
            String newsSection = "";                // News Section
            String newsDate = "";                   // Published Date
            String newsUrl = "";                    // Web URL of the news item
            JSONArray tagsArray;                    // Array of tags
            JSONObject newsTag;                     // JSON Object for news tags - first element in tagsArray
            String newsAuthor = "";                 // Author of the news item - obtained from newsTags


            baseJsonResponse = new JSONObject(newsJSON);
            jsonResults = baseJsonResponse.getJSONObject(API_KEY_RESPONSE);

            if (jsonResults.has(API_KEY_RESULTS)) {
                newsArray = jsonResults.getJSONArray(API_KEY_RESULTS);

                for (int i = 0; i < newsArray.length(); i++) {
                    currentNewsItem = newsArray.getJSONObject(i);

                    if (currentNewsItem.has(API_KEY_TITLE)) {
                        newsTitle = currentNewsItem.getString(API_KEY_TITLE);
                    }

                    if (currentNewsItem.has(API_KEY_SECTION)) {
                        newsSection = currentNewsItem.getString(API_KEY_SECTION);
                    }

                    if (currentNewsItem.has(API_KEY_PUBLISHED_DATE)) {
                        newsDate = currentNewsItem.getString(API_KEY_PUBLISHED_DATE);
                    }

                    if (currentNewsItem.has(API_KEY_WEBURL)) {
                        newsUrl = currentNewsItem.getString(API_KEY_WEBURL);
                    }

                    if (currentNewsItem.has(API_KEY_TAGS)) {
                        tagsArray = currentNewsItem.getJSONArray(API_KEY_TAGS);
                       // newsTags = tagsArray.getJSONObject(0);

                        if (tagsArray.length() > 0) {
                            for (int j = 0; j < 1; j++) {
                                newsTag = tagsArray.getJSONObject(j);
                                if (newsTag.has(API_KEY_TITLE)) {
                                    newsAuthor = newsTag.getString(API_KEY_TITLE);
                                }
                            }
                        }
                    }

                    // Create a new {@link NewsItem} object with parameters obtained from JSON response
                    NewsItem newsItem = new NewsItem(
                            newsTitle,
                            newsSection,
                            newsDate,
                            newsAuthor,
                            newsUrl
                    );

                    // Add the new {@link NewsItem} object to the list of news items
                    newsItems.add(newsItem);
                }
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, mContext.getString(R.string.exception_json_results), e);
        }

        // Return the list of newsItems
        return newsItems;
    }

    /**
     * Method to prepare the final URL to be used to fetch data
     * @param url
     * @return final url with all parameters appended
     */
    private static String prepareUrl(String url) {
        Uri baseUri = Uri.parse(url);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter(
                mContext.getString(R.string.setting_show_tags_label),
                mContext.getString(R.string.setting_show_tags_value));
        uriBuilder.appendQueryParameter(
                mContext.getString(R.string.setting_show_ref_label),
                mContext.getString(R.string.setting_show_ref_value));
        uriBuilder.appendQueryParameter(
                mContext.getString(R.string.setting_api_label),
                mContext.getString(R.string.setting_api_value));

        Log.i(LOG_TAG, "Query URL => " + uriBuilder.toString());

        return uriBuilder.toString();
    }
}

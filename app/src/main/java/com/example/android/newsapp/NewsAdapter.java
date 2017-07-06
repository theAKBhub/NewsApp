package com.example.android.newsapp;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * A {@link NewsAdapter} will create a list item layout for each news item
 * in the data source (a list of {@link NewsItem} objects) to be displayed in a ListView
 */

public class NewsAdapter extends ArrayAdapter<NewsItem> {

    private static final String LOG_TAG = NewsAdapter.class.getName();

    private static Context mContext;
    private static Typeface mCustomFontRegular;
    private static Typeface mCustomFontItalic;


    /**
     * Default Constructor to create a new {@link NewsAdapter} object
     * @param context
     * @param newsItems
     */
    public NewsAdapter(Context context, List<NewsItem> newsItems) {
        super(context, 0, newsItems);
        mContext = context;

        // Set custom typeface
        mCustomFontRegular = Typeface.createFromAsset(mContext.getAssets(), "fonts/merriweather_regular.ttf");
        mCustomFontItalic = Typeface.createFromAsset(mContext.getAssets(), "fonts/merriweather_italic.ttf");
    }

    /**
     * This class describes the view items to create a list item
     */
    public static class NewsViewHolder {

        TextView textViewTitle;
        TextView textViewSection;
        TextView textViewDate;
        TextView textViewAuthor;

        // Find various views within ListView and set custom typeface on them
        public NewsViewHolder(View itemView) {
            textViewTitle = (TextView) itemView.findViewById(R.id.text_news_title);
            textViewSection = (TextView) itemView.findViewById(R.id.text_news_section);
            textViewDate = (TextView) itemView.findViewById(R.id.text_news_date);
            textViewAuthor = (TextView) itemView.findViewById(R.id.text_news_author);

            textViewTitle.setTypeface(mCustomFontRegular);
            textViewAuthor.setTypeface(mCustomFontItalic);
            textViewDate.setTypeface(mCustomFontRegular);
            textViewSection.setTypeface(mCustomFontRegular);
        }
    }

    /**
     * Returns a list item view that displays information about the news at a given position
     * in the list of news.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String title = "";
        String section = "";
        String newsDate = "";
        String author = "";
        NewsViewHolder holder;


        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.news_list_item, parent, false);
            holder = new NewsViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (NewsViewHolder) convertView.getTag();
        }

        // Find news at the given position in the list
        NewsItem currentNews = getItem(position);

        /** Set data to respective views within ListView */

        // Set Title
        if ((currentNews.getNewsTitle() != null) && (currentNews.getNewsTitle().length() > 0)) {
            title = currentNews.getNewsTitle();
        }
        holder.textViewTitle.setText(title);

        // Set Section Name
        if ((currentNews.getNewsSection() != null) && (currentNews.getNewsSection().length() > 0)) {
            section = currentNews.getNewsSection();
        }
        holder.textViewSection.setText(section);

        // Set Author
        if ((currentNews.getNewsAuthor() != null) && (currentNews.getNewsAuthor().length() > 0)) {
            author = currentNews.getNewsAuthor();
        }
        holder.textViewAuthor.setText(author);

        // Set Date
        if ((currentNews.getNewsPublishedDate() != null) && (currentNews.getNewsPublishedDate().length() > 0)) {
            String date = currentNews.getNewsPublishedDate();
            newsDate = formatDate(date);
        }
        holder.textViewDate.setText(newsDate);

        return convertView;
    }

    /**
     * Method to check if Published Date exists, then format it if the extracted date is a valid date
     */
    public String formatDate(String date) {

        String dateFormatted = "";
        String dateNew = date.substring(0, 10); // gets date in yyyy-mm-dd format from timestamp

        // Format dateNew
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat newFormat = new SimpleDateFormat("MMM dd, yyyy");
        try {
            Date dt = inputFormat.parse(dateNew);
            dateFormatted = newFormat.format(dt);
        }
        catch(ParseException pe) {
            Log.e(LOG_TAG, mContext.getString(R.string.exception_date_format), pe);
        }

        return dateFormatted;
    }

}

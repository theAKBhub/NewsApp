package com.example.android.newsapp;

/**
 * A {@link NewsItem} object that contains details related to a single
 * news item to be displayed in the list
 */

public class NewsItem {

    /** News Title */
    private String mNewsTitle;

    /** News Section */
    private String mNewsSection;

    /** News Published Date */
    private String mNewsPublishedDate;

    /** News Author */
    private String mNewsAuthor;

    /** News Web URL */
    private String mNewsUrl;


    /**
     * Default Constructor to construct a {@link NewsItem} object
     * @param newsTitle
     * @param newsSection
     * @param newsPublishedDate
     * @param newsAuthor
     * @param newsUrl
     */
    public NewsItem(String newsTitle, String newsSection, String newsPublishedDate,
                    String newsAuthor, String newsUrl) {

        mNewsTitle = newsTitle;
        mNewsSection = newsSection;
        mNewsPublishedDate = newsPublishedDate;
        mNewsAuthor = newsAuthor;
        mNewsUrl = newsUrl;
    }

    /** Getter method - News Title */
    public String getNewsTitle() {
        return mNewsTitle;
    }

    /** Getter method - News Section */
    public String getNewsSection() {
        return mNewsSection;
    }

    /** Getter method - News Published Date */
    public String getNewsPublishedDate() {
        return mNewsPublishedDate;
    }

    /** Getter method - News Author */
    public String getNewsAuthor() {
        return mNewsAuthor;
    }

    /** Getter method - News Web URL */
    public String getNewsUrl() {
        return mNewsUrl;
    }

}

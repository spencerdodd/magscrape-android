package com.example.spencerdodd.magscrape;

import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



/**
 * Scraping class that returns an ArrayList of Torrents for a given search term String from
 * The Pirate Bay.
 */

public class ScrapeTPB implements AsyncResponse{
    // async task


    private String searchTerm;
    private ArrayList<Parcelable> searchResults;
    private boolean dataReceived;

    ScrapeTPB(String searchTerm) {
        this.searchTerm = searchTerm;
        this.searchResults = new ArrayList<Parcelable>();
        this.dataReceived = false;
    }

    @Override
    public void processFinish(ArrayList<Parcelable> output) {
        this.searchResults = output;
        this.dataReceived = true;
    }

    private class InteractWithNetwork extends AsyncTask<String, Void, ArrayList<Parcelable>> {
        private int progress = 0;
        private ArrayList<Parcelable> interactionResults = new ArrayList<>();
        public AsyncResponse delegate = null;

        @Override
        protected ArrayList<Parcelable> doInBackground(String... searchURL) {

            try {
                Document doc = Jsoup.connect(searchURL[0])
                        .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.98 Safari/537.36")
                        .timeout(0)
                        .get();

                Log.d("scrapetpb.doInBack", "downloaded our XML");

                Elements hits = doc.getElementsByTag("tr");

                for (Element hit : hits) {
                    if (!hit.hasClass("header")) {
                        // second td
                        // this is where the juicy info is
                        Element td2 = hit.children().select("td").get(1);
                        // initial info
                        Element currentTorrent = td2.children().select("a").first();

                        // get the torrent name
                        String torrentName = currentTorrent.text();

                        // get the file link
                        String torrentLink = currentTorrent.attr("href");

                        // some deeper nested info
                        Element currentTorrentDeeper = td2.children().select("a").get(1);
                        // get the magnet link
                        String magnetLink = currentTorrentDeeper.attr("href");

                        // third td
                        // get the seeds
                        Element td3 = hit.children().select("td").get(2);
                        String seeders = td3.text();

                        // fourth td
                        // get the leechers
                        String leechers = "";
                        try {
                            Element td4 = hit.children().select("td").get(3);
                            leechers = td4.text();
                        } catch (Exception e) {
                            Log.e("leecher parse error", e.getMessage());
                        }

                        // make our torrent and add it to results
                        Torrent newTorrent = new Torrent(torrentName, torrentLink, magnetLink, seeders, leechers);
                        this.interactionResults.add(newTorrent);
                    }
                }
            } catch (Exception e){
                Log.e("search error", e.toString());
            }

            for (Parcelable torrent : this.interactionResults) {
                Log.d("torrent_result_async", torrent.toString());
            }

            return this.interactionResults;
        }

        @Override
        protected void onPostExecute(ArrayList<Parcelable> result) {
            // fuck
            // need to somehow set the results given our results
            // Update:
            // damn...http://stackoverflow.com/questions/12575068/how-to-get-the-result-of-onpostexecute-to-main-activity-because-asynctask-is-a
            Log.d("ScrapeTPB", "post execute process finish");
            delegate.processFinish(result);
        }
    }

    public void search() {
        String searchURL = getSearchURL();
        Log.d("searching", searchURL);
        InteractWithNetwork asyncTask = new InteractWithNetwork();
        asyncTask.delegate = this;
        asyncTask.execute(searchURL);
    }

    public ArrayList<Parcelable> returnResults() throws InterruptedException {

        Log.d("returnResults", "finished scraping torrents");

        while (!this.dataReceived) {
            // don't return until we get the data
            Log.d("search", "data not received");
            TimeUnit.SECONDS.sleep(1);
        }
        Log.d("search", "data received");

        for (Parcelable torrent : this.searchResults) {
            Log.d("torrent_result_search", torrent.toString());
        }
        return this.searchResults;
    }

    private String getSearchURL() {
        String cleanedSearchTerm = this.searchTerm.replace(" ", "%20");
        return Constants.TPBUrl[1] + "/search/" + cleanedSearchTerm + "/0/99/0";
    }

}

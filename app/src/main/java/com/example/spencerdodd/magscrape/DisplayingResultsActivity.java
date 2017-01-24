package com.example.spencerdodd.magscrape;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;


public class DisplayingResultsActivity extends AppCompatActivity implements AsyncResponse {
    private boolean haveResults = false;
    private ArrayList<Parcelable> scrapeResults = null;
    private String searchTerm;

    int RESULT_TEXT_SIZE = 12;
    int RESULT_PAD_SCALE = 5;
    String APP_FLUD = "Flud";

    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        // sets the value of the display string
        this.searchTerm = this.returnInputString();
        String messageString = "Magnet links for: " + this.searchTerm;

        // edits the textView for the searchstring
        TextView searchString = (TextView) findViewById(R.id.search_string_text_view);
        searchString.setText(messageString);

        // progress dialog
        this.mProgressDialog = new ProgressDialog(DisplayingResultsActivity.this);
        this.mProgressDialog.setTitle("Scraping data from TPB");
        this.mProgressDialog.setCancelable(false);


        final ScraperTask scraperTask = new ScraperTask();
        scraperTask.delegate = this;
        scraperTask.execute(this.getSearchURL());
        this.mProgressDialog.show();
    }

    @Override
    public void processFinish(ArrayList<Parcelable> output) {
        Log.d("processFinish", "starting");
        this.scrapeResults = output;
        for (Parcelable t : this.scrapeResults) {
            Log.d("torrents", t.toString());
        }
        this.showScrapeResults();
    }

    // subclass for performing our scraping
    private class ScraperTask extends AsyncTask<String, Void, ArrayList<Parcelable>> {
        private ArrayList<Parcelable> interactionResults = new ArrayList<>();
        public AsyncResponse delegate = null;

        @Override
        protected ArrayList<Parcelable> doInBackground(String... strings) {
            String searchURL = strings[0];

            try {
                Document doc = Jsoup.connect(searchURL)
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

    private String getSearchURL() {
        String cleanedSearchTerm = this.searchTerm.replace(" ", "%20");
        return Constants.TPBUrl[1] + "/search/" + cleanedSearchTerm + "/0/99/0";
    }

    /*
        Returns the search string that was input to the main activity view
     */
    public String returnInputString() {
        Intent intent = getIntent();
        return intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
    }

    /*
        Returns the results of the magnet scrape search
     */
    public ArrayList<Torrent> returnScrapeResults() {
        Intent intent = getIntent();
        return intent.getParcelableArrayListExtra(MainActivity.EXTRA_SCRAPE_RESULTS);
    }

    /*
        visualizes the scrape results
     */
    public void showScrapeResults() {
        this.mProgressDialog.dismiss();
        Log.d("showScrapeResults", "start");
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.activity_display_message);
        // shows all of the magnet results as stacked links that should open a
        // redirect to the Flud app
        ArrayList<Parcelable> magnetResults = this.scrapeResults;
        for (int i=0; i < magnetResults.size(); i++) {
            Torrent currentTorrent = (Torrent) magnetResults.get(i);
            final String torrentName = currentTorrent.getName();
            final String torrentMagnet = currentTorrent.getMagnetLink();
            final String torrentSeeders = currentTorrent.getSeeders();
            final String torrentLeechers = currentTorrent.getLeechers();

            Button newTorrent = new Button(this);
            newTorrent.setClickable(true);
            newTorrent.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String logString = torrentSeeders + ":" + torrentLeechers;
                    Log.d(torrentName, logString);
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("magnet_link", torrentMagnet);
                    clipboard.setPrimaryClip(clip);
                    Log.d("magnet_to_clipboard", torrentMagnet);
                }
            });
            // Text parameters
            // add the seed / leech data to the buttons
            String displayString = torrentName + " " + torrentSeeders + ":" + torrentLeechers;
            newTorrent.setText(displayString);
            newTorrent.setTextColor(Color.WHITE);
            newTorrent.setTextSize(RESULT_TEXT_SIZE);
            newTorrent.setBackgroundColor(newTorrent.getResources().getColor(R.color.colorPrimary));

            // Layout parameters
            RelativeLayout.LayoutParams relButtonParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            // alter x and y position here
            int buttonHeight = newTorrent.getHeight();
            relButtonParams.setMargins(0, 200 * i + 10, 0, 0);
            relButtonParams.width = relativeLayout.getWidth();
            newTorrent.setLayoutParams(relButtonParams);
            relativeLayout.addView(newTorrent);
        }
        Log.d("showScrapeResults", "complete");
    }

    /*
        Opens a redirect to the flud app
     */
    public void openApp(String app, String packageName) {
        if (app == APP_FLUD) {
            openFlud(this, packageName);
        }
    }

    public boolean openFlud(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            Log.d("app for magnet: ", i.getDataString());
            if (i == null) {
                return false;

            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

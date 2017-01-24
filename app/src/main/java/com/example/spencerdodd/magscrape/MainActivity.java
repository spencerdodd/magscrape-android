package com.example.spencerdodd.magscrape;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.example.spencerdodd.magscrape.ScrapeTPB;

public class MainActivity extends AppCompatActivity {
    // async task


    // keys for extra values
    public final static String EXTRA_MESSAGE = "com.example.spencerdodd.magscrape.MESSAGE";
    public final static String EXTRA_SCRAPE_RESULTS = "com.example.spencerdodd." +
            "magscrape.SCRAPE_RESULTS";
    // variable values
    private ArrayList<Parcelable> resultMagnetLinks = new ArrayList<>();
    private String searchString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /*
        Called when the search button is pressed
     */
    public void showLoadingScreen(View view) {
        Intent intent = new Intent(this, LoadingResultsActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_text_scrape);
        this.searchString = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, this.searchString);
        startActivity(intent);
    }

    public void showResultsScreen(View view) {
        Intent intent = new Intent(this, DisplayingResultsActivity.class);
        intent.putExtra(EXTRA_MESSAGE, this.searchString);
        intent.putExtra(EXTRA_SCRAPE_RESULTS, this.resultMagnetLinks);
        startActivity(intent);
    }

    /*
        Scrapes tpb for magnet links associated with the search
     */
    public void searchForMagnetLinks(View view) throws InterruptedException {
        // show the loading screen while we look around for links
        showLoadingScreen(view);

        // show the results screen
        showResultsScreen(view);

    }
}

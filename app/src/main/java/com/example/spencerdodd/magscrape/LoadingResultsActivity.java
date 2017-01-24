package com.example.spencerdodd.magscrape;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LoadingResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_results);

        // gets the input string from the main activity
        String message = "Finding magnet links for:";
        String searchString = this.returnInputString();
        // Edits the search message textView
        TextView messageView = (TextView) findViewById(R.id.search_message_text_view);
        messageView.setText(message);
        // Edits the searchString textView
        TextView searchStringView = (TextView) findViewById(R.id.search_term_text_view);
        searchStringView.setText(searchString);
    }

    /*
        Returns the search string that was input to the main activity view
     */
    public String returnInputString() {
        Intent intent = getIntent();
        return intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
    }
}

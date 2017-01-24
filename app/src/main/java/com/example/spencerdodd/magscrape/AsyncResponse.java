package com.example.spencerdodd.magscrape;

import android.os.Parcelable;

import java.util.ArrayList;

/**
 * For getting the results of our Async tasks
 */

public interface AsyncResponse {
    void processFinish(ArrayList<Parcelable> output);
}

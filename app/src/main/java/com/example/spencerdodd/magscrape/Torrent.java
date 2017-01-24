package com.example.spencerdodd.magscrape;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This is a representation of a torrent in the form of a Parcelable object in order to be able
 * to pass torrents from activity to activity in the form of an ArrayList<Parcelable>.
 */

public class Torrent implements Parcelable {

    private String name;
    private String torrentLink;
    private String magnetLink;
    private String seeders;
    private String leechers;

    /*
        Constructor for if the seeders and leechers values given are integers. Converts them to
        strings in order to be used in parcelling
     */
    public Torrent(String name, String torrentLink, String magnetLink, int seeders, int leechers) {
        this.name = name;
        this.torrentLink = torrentLink;
        this.magnetLink = magnetLink;
        this.seeders = Integer.toString(seeders);
        this.leechers = Integer.toString(leechers);
    }

    /*
        Constructor for if the seeders and leechers are already strings. Stores them as strings
     */
    public Torrent(String name, String torrentLink, String magnetLink, String seeders, String leechers) {
        this.name = name;
        this.torrentLink = torrentLink;
        this.magnetLink = magnetLink;
        this.seeders = seeders;
        this.leechers = leechers;
    }

    // Getters and setters
    // ------------------------------------------------------------------------------------
    /*
        Returns the name of the torrent
     */
    public String getName() {
        return this.name;
    }
    /*
        Returns the magnet link of the torrent
     */
    public String getMagnetLink() { return this.magnetLink; }
    /*
        Returns the seeders
     */
    public String getSeeders() { return this.seeders; };
    /*
        Returns the leechers
     */
    public String getLeechers() { return this.leechers; }

    // Parcelling stuff below
    // ------------------------------------------------------------------------------------
    public Torrent(Parcel in) {
        String[] data = new String[4];

        in.readStringArray(data);
        this.name = data[0];
        this.torrentLink = data[1];
        this.magnetLink = data[2];
        this.seeders = data[3];
        this.leechers = data[4];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[] {this.name,
                                                this.torrentLink,
                                                this.magnetLink,
                                                this.seeders,
                                                this.leechers});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Torrent createFromParcel(Parcel in) {
            return new Torrent(in);
        }

        public Torrent[] newArray(int size) {
            return new Torrent[size];
        }
    };
}

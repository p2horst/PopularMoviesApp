package de.softsolu.popularmovies;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 */
public class MovieData {
    private final String LOG_TAG = MovieData.class.getSimpleName();
    private ListView mListView;
    private Activity mActivity;
    private String mGetCommand = "GetPopularMovies";
    private String[] mGetCommandList = {"GetPopularMovies", "GetHighestRatedMovies"};
    private String mTheMovieDb_Api_Key = "TODOAPIKEY";
    private int mItemCounter = 0;

    public static final List<movieItem> ITEMS = new ArrayList<movieItem>();
    public static final Map<String, movieItem> ITEM_MAP = new HashMap<String, movieItem>();

    private static final int COUNT = 25;

    // delete all items in ITEMS
    public void DelAllItems (){
        ITEMS.clear();
    }

    // set command to know what kind of data is needed.
    public void setGetCommand (String getCommand){
        if (!mGetCommand.equals("Get"+getCommand)) {
            if (Arrays.asList(mGetCommandList).contains(getCommand)) {
                mGetCommand = "Get"+getCommand;
            }
            mGetCommand = "Get" + getCommand;
        }
    }

    // Builds HTTP-String that is used to get the movie data
    private String GetUrlCommand(){
        Uri.Builder Uri = new Uri.Builder();

        try {
            switch (mGetCommand) {
                case "GetPopularMovies":
                    // popular movie command i.e. URL("http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=c20129fdf73b5df3ab44548ad7f73586");
                    Uri.scheme("http")
                            .authority("api.themoviedb.org")
                            .appendPath("3")
                            .appendPath("discover")
                            .appendPath("movie")
                            .appendQueryParameter("sort_by", "popularity.desc")
                            .appendQueryParameter("api_key", mTheMovieDb_Api_Key)
                            .build();
                    break;
                case "GetHighestRatedMovies":
                    //URL url = new URL("http://api.themoviedb.org/3/discover/movie?certification_country=US&certification=R&sort_by=vote_average.descc&api_key=c20129fdf73b5df3ab44548ad7f73586");
                    Uri.scheme("http")
                            .authority("api.themoviedb.org")
                            .appendPath("3")
                            .appendPath("discover")
                            .appendPath("movie")
                            .appendQueryParameter("certification_country", "US")
                            .appendQueryParameter("sort_by", "vote_average.desc")
                            .appendQueryParameter("api_key", mTheMovieDb_Api_Key)
                            .build();
                    break;
                default:
                    Log.e(LOG_TAG, "Command (" + mGetCommand + ") unknown.");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error ", e);
            Log.v(this.LOG_TAG, "TheMovieDb URL String for command (" + mGetCommand + "): " + Uri.toString());
            return null;
        }
        Log.v(this.LOG_TAG, "TheMovieDb URL String for command (" + mGetCommand + "): " + Uri.toString());

        return Uri.toString();
    }

    // got it from http://stackoverflow.com/questions/31115617/making-a-popular-movies-app-using-gridview-adapters-and-picasso
    public ArrayList<movieItem> InitLoadOfMovies() {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;
        URL url;

        Log.w("MovieData()", "loadInBackground()");

        try {
            url = new URL(GetUrlCommand());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {

                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            movieJsonStr = buffer.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        try {
            return getMovieDataFromJson(movieJsonStr);
        } catch (JSONException j) {
            Log.e(LOG_TAG, "JSON Error", j);
        }
        return null;
    }

    // inspired from http://stackoverflow.com/questions/31115617/making-a-popular-movies-app-using-gridview-adapters-and-picasso
    private ArrayList<MovieData.movieItem> getMovieDataFromJson(String PopularMoviesJsonStr)
            throws JSONException {
        JSONObject movieJson = new JSONObject(PopularMoviesJsonStr);
        JSONArray movieJsonArray = movieJson.getJSONArray("results");
        ArrayList<MovieData.movieItem> movielist = new ArrayList<MovieData.movieItem>();

        for (int i = 0; i < movieJsonArray.length(); i++)
        {
            JSONObject movie = movieJsonArray.getJSONObject(i);
            MovieData.movieItem mi = new MovieData.movieItem(movie.getString("id"),movie.getString("poster_path"),
                    movie.getString("backdrop_path"), movie.getString("adult"),
                    movie.getString("overview"), movie.getString("release_date"), movie.getString("original_title"),
                    movie.getString("original_language"), movie.getDouble("popularity"), movie.getInt("vote_count"),
                    movie.getInt("vote_average"));
            Log.w("parsingJSon", mi.poster_path);

            movielist.add(mi);
        }
        return movielist;
    }

    public int GetCount(){
        return this.mItemCounter;
    }

    private static void addItem(movieItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public void AddAll(ArrayList<movieItem> movieItems){
        for (int i = 0; i < movieItems.size(); i++)
        {
            this.addItem(movieItems.get(i));
        }
    }

    private static movieItem createmovieItem(int position, String poster_path, String backdrop_path, String adult, String overview, String release_date,
            String original_title, String original_language, float popularity, int vote_count, int vote_average) {
        return new movieItem(String.valueOf(position), poster_path, backdrop_path, adult, overview, release_date, original_title, original_language,
                popularity, vote_count, vote_average);
    }

    public static class movieItem {
        public final String id;
        public final String poster_path;
        public final String backdrop_path;
        public final String adult;
        public final String overview;
        public final String release_date;
        public final String original_title;
        public final String original_language;
        public final double popularity;
        public final int vote_count;
        public final float vote_average;

        public movieItem(String id, String poster_path, String backdrop_path, String adult, String overview, String release_date,
                         String original_title, String original_language, double popularity, int vote_count, float vote_averrage) {
            this.id = id;
            this.poster_path = poster_path;
            this.backdrop_path = backdrop_path;
            this.adult = adult;
            this.overview = overview;
            this.release_date = release_date;
            this.original_title = original_title;
            this.original_language = original_language;
            this.popularity = popularity;
            this.vote_count = vote_count;
            this.vote_average = vote_averrage;
        }

        @Override
        public String toString() {
            return original_title;
        }
    }
}

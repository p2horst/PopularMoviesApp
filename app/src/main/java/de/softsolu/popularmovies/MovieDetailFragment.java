package de.softsolu.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.softsolu.popularmovies.MovieData;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    private MovieData.movieItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = MovieData.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            ImageView im = (ImageView) activity.findViewById(R.id.app_bar_backdrop);
            Uri uri = Uri.parse(mItem.backdrop_path);
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            Log.w("onCreate",mItem.original_title.toString());
            if (appBarLayout != null) {
                Picasso.with(activity)
                        .load("http://image.tmdb.org/t/p/w300" + uri)
                        .error(R.drawable.forest_backdrop)
                        .into(im);
                appBarLayout.setTitle(mItem.original_title);
            }

            // set rating bar
            RatingBar appRatingBar = (RatingBar) activity.findViewById(R.id.ratingbar);
            appRatingBar.setIsIndicator(true);
            if (appRatingBar != null) {
                appRatingBar.setRating(mItem.vote_average / 2); // Reducing from 10 to 5 stars
                Log.w("appRatingBar", (String.valueOf(mItem.vote_average)));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);

        // set release date
        TextView tvReleaseDate = (TextView) rootView.findViewById(R.id.tvReleaseDate);
        if (tvReleaseDate != null){
            SimpleDateFormat formatterInput = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat formatterOutput = new SimpleDateFormat("yyyy, MMMM");
            Calendar ReleaseDate = Calendar.getInstance();

            try {
                ReleaseDate.setTime(formatterInput.parse(mItem.release_date));
                Log.w("tvReleaseDate", (formatterOutput.format(ReleaseDate.getTime())));
                tvReleaseDate.setText(formatterOutput.format(ReleaseDate.getTime()));
            } catch (ParseException e){
                e.printStackTrace();
            }
        }

        if (mItem != null) {
            Log.w("onCreateView", mItem.overview);
            ((TextView) rootView.findViewById(R.id.movie_detail)).setText(mItem.overview);
        }

        return rootView;
    }
}

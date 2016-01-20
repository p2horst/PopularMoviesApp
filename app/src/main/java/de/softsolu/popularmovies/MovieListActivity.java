package de.softsolu.popularmovies;

import android.app.Activity;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.app.PendingIntent.getActivity;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MovieListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        final View recyclerView = findViewById(R.id.movie_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        // Add tabs i.e. http://www.truiton.com/2015/06/android-tabs-example-fragments-viewpager/
        mTabLayout = (TabLayout) findViewById(R.id.tablayout);
        mTabLayout.addTab(mTabLayout.newTab().setText("Popular"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Top Rated"));
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Activity a = (Activity)recyclerView.getContext();
                SimpleItemRecyclerViewAdapter sirva = (SimpleItemRecyclerViewAdapter) ((RecyclerView)recyclerView).getAdapter();
                if (tab.getPosition() == 1) {
                    sirva.GetHighestRatedMovies();
                }
                else {
                    sirva.GetPopularMovies();
                }
            }
            public void onTabReselected(TabLayout.Tab tab) {

            }
            public void onTabUnselected(TabLayout.Tab tab) {

            }
        });

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        Activity a = (Activity)recyclerView.getContext();
        SimpleItemRecyclerViewAdapter sirva = new SimpleItemRecyclerViewAdapter(MovieData.ITEMS);
        recyclerView.setAdapter(sirva);
        sirva.GetPopularMovies();
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<MovieData.movieItem> mValues;

        public SimpleItemRecyclerViewAdapter(List<MovieData.movieItem> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.movie_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            // code comes from http://www.101apps.co.za/index.php/articles/android-recyclerview-and-picasso-tutorial.html
            Uri uri = Uri.parse(holder.mItem.poster_path);
            Context context = holder.mImageView.getContext();
            Picasso.with(context)
                    .load("http://image.tmdb.org/t/p/w185" + uri)
                    .error(R.drawable.no_poster_w185)
                    .into(holder.mImageView);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.w("setOnClickListener", holder.mItem.id.toString());
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(MovieDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        Log.w("setOnClickListener", holder.mItem.id.toString());
                        MovieDetailFragment fragment = new MovieDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.movie_detail_container, fragment)
                                .commit();
                    } else {
                        Log.w("setOnClickListener", holder.mItem.id.toString());
                        Context context = v.getContext();
                        Intent intent = new Intent(context, MovieDetailActivity.class);
                        intent.putExtra(MovieDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public void GetHighestRatedMovies() {
            final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.movie_list);
            Activity a = (Activity)recyclerView.getContext();
            recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(MovieData.ITEMS));
            asyncFetchPopularMovies MovieTask = new asyncFetchPopularMovies(a, (SimpleItemRecyclerViewAdapter)recyclerView.getAdapter(),"HighestRatedMovies");
        }

        public void GetPopularMovies() {
            final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.movie_list);
            Activity a = (Activity)recyclerView.getContext();
            recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(MovieData.ITEMS));
            asyncFetchPopularMovies MovieTask = new asyncFetchPopularMovies(a, (SimpleItemRecyclerViewAdapter)recyclerView.getAdapter(),"PopularMovies");
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView mImageView;
            public MovieData.movieItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (ImageView) view.findViewById(R.id.Image);
            }

            @Override
            public String toString() {
                return super.toString() + " 'UiUI'";
                //return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }

    public class asyncFetchPopularMovies extends AsyncTaskLoader<ArrayList<MovieData.movieItem>> {
        private final String LOG_TAG = asyncFetchPopularMovies.class.getSimpleName();
        private MovieData mMovieData = new MovieData();
        private String mGetCommand;
        private SimpleItemRecyclerViewAdapter mAdapter;

        public asyncFetchPopularMovies(Activity a, SimpleItemRecyclerViewAdapter Adapter, String GetCommand) {
            super(a);
            mAdapter = Adapter;
            mGetCommand = GetCommand;
            this.forceLoad();
            Log.w("MovieData()", "asyncFetchPopularMovies(Context context)");
        }

        @Override
        public void deliverResult(ArrayList<MovieData.movieItem> data) {
            super.deliverResult(data);
            // Code comes from https://youtu.be/imsr8NrIAMs at 39:55
            mMovieData.DelAllItems();
            mMovieData.AddAll(data);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public ArrayList<MovieData.movieItem> loadInBackground() {
            MovieData md = new MovieData();
            md.setGetCommand(mGetCommand);
            return md.InitLoadOfMovies();
        }
    }
}

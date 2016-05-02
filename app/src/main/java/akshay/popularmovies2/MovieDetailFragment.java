package akshay.popularmovies2;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;



/**
 * Created by Akshay Moorthy
 */
public class MovieDetailFragment extends Fragment implements MoviesTask.MoviedbResponse {
    private static final String MOVIE_TRAILERS_KEY = "movietrailers";
    private static final String MOVIE_REVIEWS_KEY = "moviereviews";
    private final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    private MovieParcel movie;
    private String title;
    private String poster;
    private String overview;
    private String release_date;
    private String votes;
    private ViewGroup trailersLayout;
    private ViewGroup reviewsLayout;
    private Button showReviews;
    private String[] movieTrailers;
    private boolean isShowReviews = false;
    private ArrayList<MovieReviewsParcel> movieReviewList;
    private android.support.v7.widget.ShareActionProvider mShareActionProvider;


    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.d(LOG_TAG, " OnCreate");

        super.onCreate(savedInstanceState);

        movie = getArguments().getParcelable("movie");


        this.title = movie.title;
        this.poster = movie.poster;
        this.overview = movie.overview;
        this.release_date = movie.release_date;
        this.votes = movie.vote;

        if (savedInstanceState == null || !savedInstanceState.containsKey(MOVIE_REVIEWS_KEY) ||
                !savedInstanceState.containsKey(MOVIE_TRAILERS_KEY)) {
            Log.d(LOG_TAG, "MovieTrailers and Reviews not available");

            if (Utility.isNetworkAvailable(getActivity())) {

                MoviesTask mTask = new MoviesTask(getContext());
                mTask.mDelegate = this;
                mTask.execute(movie.id);

            } else {

                Toast.makeText(getContext(), "Please enable Internet Connection!",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(LOG_TAG, " OnCreateView: Movie Trailers and Reviews available");
            movieTrailers = savedInstanceState.getStringArray(MOVIE_TRAILERS_KEY);
            movieReviewList = savedInstanceState.getParcelableArrayList(MOVIE_REVIEWS_KEY);
            if (movieTrailers != null && movieReviewList != null) {

                setMovieTrailers(movieTrailers);
                setMovieReviews(movieReviewList);

            } else {

                if (Utility.isNetworkAvailable(getActivity())) {

                    MoviesTask mTask = new MoviesTask(getContext());
                    mTask.mDelegate = this;
                    mTask.execute(movie.id);

                } else {

                    Toast.makeText(getContext(), "Please enable Internet Connection!", Toast.LENGTH_SHORT).show();
                }

            }

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(LOG_TAG, " OnCreateView");

        final View rootView = inflater.inflate(R.layout.activity_detail, container, false);

        TextView titleView = (TextView) rootView.findViewById(R.id.title);
        ImageView posterView = (ImageView) rootView.findViewById(R.id.posterDetail);
        TextView dateView = (TextView) rootView.findViewById(R.id.releaseDate);
        TextView voteView = (TextView) rootView.findViewById(R.id.votes);
        TextView overViewTextView = (TextView) rootView.findViewById(R.id.overviewText);
        trailersLayout = (ViewGroup) rootView.findViewById(R.id.movie_trailers);
        reviewsLayout = (ViewGroup) rootView.findViewById(R.id.movie_reviews);
        showReviews = (Button) rootView.findViewById(R.id.show_reviews);

        final String posterImageBaseUrl = getActivity().getApplicationContext().getResources().
                getString(R.string.image_base_url);

        if (poster.equals("null") || poster.equals(null) || poster.equals("")) {
            posterView.setImageResource(R.drawable.empty_photo);
        } else {
            Picasso.with(getActivity())
                    .load(posterImageBaseUrl + poster)
                    .into(posterView);
        }


        titleView.setText(title);
        dateView.setText(getString(R.string.release_date) + release_date);
        voteView.setText(getString(R.string.ratings) + votes);
        overViewTextView.setText(getString(R.string.synopsis) + overview);

        showReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reviewsLayout.setVisibility(View.VISIBLE);
                setMovieReviews(movieReviewList);
                reviewsLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        reviewsLayout.requestFocus();
                    }
                });

            }
        });

        setHasOptionsMenu(true);
        if (movieTrailers != null && movieTrailers.length != 0 && mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createMovieShareIntent(movieTrailers[0]));
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray(MOVIE_TRAILERS_KEY, movieTrailers);
        outState.putParcelableArrayList(MOVIE_REVIEWS_KEY, movieReviewList);
    }

    private void watchYoutubeVideo(String id) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + id));
            startActivity(intent);
        }
    }

    public void setMovieTrailers(final String[] trailers) {

        if (trailers == null)
            return;
        if (trailers != null && trailers.length != 0 && mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createMovieShareIntent(trailers[0]));
        }
        final ViewGroup viewGroup = trailersLayout;

        for (int i = viewGroup.getChildCount() - 1; i >= 1; i--) {
            viewGroup.removeViewAt(i);
        }

        final LayoutInflater inflater = getActivity().getLayoutInflater();

        boolean hasTrailers = true;

        for (int i = 0; i < trailers.length; i++) {

            int j = i + 1;

            final View trailerView = inflater
                    .inflate(R.layout.trailer_single_item, viewGroup, false);
            final TextView trailerTitle = (TextView) trailerView
                    .findViewById(R.id.trailerTitle);
            final ImageButton trailerPlay = (ImageButton) trailerView
                    .findViewById(R.id.trailerPlay);
            hasTrailers = true;
            trailerTitle.setText(String.format(Locale.US, "Trailer %d", j));
            final String source = trailers[i];
            trailerPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    watchYoutubeVideo(source);
                    Log.d(LOG_TAG, "clicked on source :" + source);
                }
            });
            viewGroup.addView(trailerView);
        }

        viewGroup.setVisibility(hasTrailers ? View.VISIBLE : View.GONE);

    }

    public void setMovieReviews(ArrayList<MovieReviewsParcel> reviews) {

        if (reviews == null || reviews.size() == 0 || isShowReviews)
            return;

        final ViewGroup viewGroup = reviewsLayout;


        final LayoutInflater inflater = (LayoutInflater)getActivity().
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < reviews.size(); i++) {
            MovieReviewsParcel review = reviews.get(i);
            final View reviewView = inflater
                    .inflate(R.layout.review_single_item, viewGroup, false);
            final TextView authorView = (TextView) reviewView
                    .findViewById(R.id.reviewAuthor);
            final TextView contentView = (TextView) reviewView
                    .findViewById(R.id.reviewContent);
            final String author = review.author;
            final String content = review.content;
            authorView.setText(author);
            contentView.setText(content);
            viewGroup.addView(reviewView);
        }

        isShowReviews = true;


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (android.support.v7.widget.ShareActionProvider)
                MenuItemCompat.getActionProvider(menuItem);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private Intent createMovieShareIntent(String source) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=" + source);
        return shareIntent;
    }

}

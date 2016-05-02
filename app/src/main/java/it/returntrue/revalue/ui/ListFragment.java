package it.returntrue.revalue.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.returntrue.revalue.R;
import it.returntrue.revalue.adapters.ItemsAdapter;
import it.returntrue.revalue.api.ItemModel;
import it.returntrue.revalue.api.RevalueService;
import it.returntrue.revalue.preferences.SessionPreferences;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Shows the list of items
 */
public class ListFragment extends Fragment
        implements ItemsAdapter.OnItemClickListener, LoaderManager.LoaderCallbacks<List<ItemModel>> {
    private final static int LOADER_ITEMS = 1;

    private RecyclerView mRecyclerView;
    private ItemsAdapter mItemsAdapter;
    private OnItemClickListener mOnItemClickListener;

    /** Provides listeners for click events */
    public interface OnItemClickListener {
        void onItemClick(View view, Uri uri);
    }

    public ListFragment() {
    }

    public static Fragment newInstance() {
        return new ListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets option menu
        setHasOptionsMenu(true);

        // Initializes loader
        getActivity().getSupportLoaderManager().initLoader(1, null, this).forceLoad();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        // Creates list objects
        mRecyclerView = (RecyclerView)view.findViewById(R.id.list_items);
        mRecyclerView.setHasFixedSize(true);
        mItemsAdapter = new ItemsAdapter(getContext());
        mItemsAdapter.setOnItemClickListener(this);

        // Sets RecyclerView's side objects
        mRecyclerView.setAdapter(mItemsAdapter);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main_list, menu);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mOnItemClickListener = (OnItemClickListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnItemClickListener");
        }
    }

    @Override
    public void onFavoriteClick(View view, long id) {

    }

    @Override
    public void onItemClick(View view, Uri uri) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(view, uri);
        }
    }

    @Override
    public Loader<List<ItemModel>> onCreateLoader(int id, Bundle args) {
        return new ListAsyncTaskLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<List<ItemModel>> loader, List<ItemModel> data) {
        mItemsAdapter.setItems(data);
    }

    @Override
    public void onLoaderReset(Loader<List<ItemModel>> loader) {
        mItemsAdapter.setItems(new ArrayList<ItemModel>());
    }

    private static class ListAsyncTaskLoader extends AsyncTaskLoader<List<ItemModel>> {
        public ListAsyncTaskLoader(Context context) {
            super(context);
        }

        @Override
        public List<ItemModel> loadInBackground() {
            final SessionPreferences sessionPreferences = new SessionPreferences(getContext());

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(new Interceptor() {
                  @Override
                  public Response intercept(Interceptor.Chain chain) throws IOException {
                      Request original = chain.request();

                      Request request = original.newBuilder()
                              .header("Authorization", "Bearer " + sessionPreferences.getToken())
                              .method(original.method(), original.body())
                              .build();

                      return chain.proceed(request);
                  }
            });

            OkHttpClient client = httpClient.build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://10.0.2.2/Revalue.Api/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
            RevalueService service = retrofit.create(RevalueService.class);
            Call<List<ItemModel>> call = service.GetNearestItems(45.553629, 9.197735, 0);

            try {
                return call.execute().body();
            }
            catch (IOException e) {
                return new ArrayList<>();
            }
        }
    }
}
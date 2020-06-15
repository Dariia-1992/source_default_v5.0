package com.hi.appskin_v40.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.hi.appskin_v40.R;
import com.hi.appskin_v40.adapter.SkinsAdapter;
import com.hi.appskin_v40.model.Skin;
import com.hi.appskin_v40.model.SkinsRepository;
import com.hi.appskin_v40.utils.FavoritesManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment{

    public boolean MODE_FAVORITE = false;

    private List<Skin> fulSkins = new ArrayList<>();
    private List<Skin> currentList = new ArrayList<>();
    private final ArrayList<Skin> filteredSkinsList = new ArrayList<>();
    private List<Skin> favoriteSkins = new ArrayList<>();

    private View view;
    private SwipeRefreshLayout refreshLayout;
    private SkinsAdapter adapter;
    private String searchStr;

    private View allSkinsTrue;
    private View favoriteListTrue;
    private View allSkinsFalse;
    private View favoriteListFalse;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);

        refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(this::refreshItems);

        fulSkins = SkinsRepository.getItems();
        adapter = new SkinsAdapter(currentList, listener);
        RecyclerView recyclerView = view.findViewById(R.id.contentView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        View searchButton = view.findViewById(R.id.toolbar_search);
        searchButton.setOnClickListener(v -> setupSearch(true));

        ImageView shared = view.findViewById(R.id.rate);
        shared.setOnClickListener(v -> { goToStore(getContext()); });

        setupSearch(false);
        initMode();

        if (SkinsRepository.getItems().isEmpty())
            refreshItems();
        else
            setCurrent(fulSkins);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (MODE_FAVORITE) {
            setCheckedState(favoriteListTrue, true);
            setCheckedState(allSkinsTrue, false);
            setCheckedState(favoriteListFalse, false);
            setCheckedState(allSkinsFalse, true);
        } else {
            setCheckedState(allSkinsTrue, true);
            setCheckedState(allSkinsFalse, false);
            setCheckedState(favoriteListFalse, true);
            setCheckedState(favoriteListTrue, false);
        }
    }

    private void refreshItems() {
        refreshLayout.setRefreshing(true);
        SkinsRepository.loadData(() -> {
            refreshLayout.setRefreshing(false);
            setCurrent(fulSkins);
        }, error -> {
            refreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        });
    }

    private void getFavorite() {
        favoriteSkins.clear();
        FavoritesManager manager = FavoritesManager.getInstance();
        for (Skin skin : fulSkins) {
            if (manager.isFavorite(getContext(), skin))
                favoriteSkins.add(skin);
        }
    }

    private void initMode() {
        allSkinsTrue = view.findViewById(R.id.list_mods);
        allSkinsFalse = view.findViewById(R.id.list_mods_false);
        favoriteListTrue = view.findViewById(R.id.favorite_true);
        favoriteListFalse = view.findViewById(R.id.favorite_false);

        allSkinsFalse.setOnClickListener(v -> {
            setCheckedState(allSkinsTrue, true);
            setCheckedState(allSkinsFalse, false);
            setCheckedState(favoriteListTrue, false);
            setCheckedState(favoriteListFalse, true);

            setCurrent(fulSkins);
            MODE_FAVORITE = false;
        });

        getFavorite();

        favoriteListFalse.setOnClickListener(v -> {

            setCheckedState(allSkinsTrue, false);
            setCheckedState(allSkinsFalse, true);
            setCheckedState(favoriteListFalse, false);
            setCheckedState(favoriteListTrue, true);

            getFavorite();
            setCurrent(favoriteSkins);
            MODE_FAVORITE = true;
        });
    }

    public static void goToStore(Context context) {
        if (context == null)
            return;

        final String appPackageName = context.getPackageName();
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private void setupSearch(boolean show){
        View searchButton = view.findViewById(R.id.toolbar_search);
        View toolbarText = view.findViewById(R.id.toolbar_name);
        SearchView searchView = view.findViewById(R.id.search);
        //EditText searchEditText= searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        //searchEditText.setTextColor(getResources().getColor(R.color.white));

        searchView.setOnCloseListener(() -> {
            setupSearch(false);
            whatCurrent();
            return false;
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchStr = newText;
                filterData();
                return false;
            }
        });

        searchButton.setVisibility(show ? View.GONE : View.VISIBLE);
        toolbarText.setVisibility(show ? View.GONE : View.VISIBLE);
        searchView.setVisibility(show ? View.VISIBLE : View.GONE);

        if (show) {
            searchView.setIconified(false);
            searchView.requestFocusFromTouch();
        }
    }

    private void filterData() {
        filteredSkinsList.clear();
        whatCurrent();
        // Search
        for (Skin skin : currentList)
            if (containsIgnoreCase(skin.getTitle(), searchStr))
                filteredSkinsList.add(skin);
        setCurrent(filteredSkinsList);
    }

    private void whatCurrent() {
        if (MODE_FAVORITE)
            setCurrent(favoriteSkins);
        else
            setCurrent(fulSkins);
    }

    private static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null)
            return false;

        if (searchStr == null)
            return true;

        final int length = searchStr.length();
        if (length == 0)
            return true;

        for (int i = str.length() - length; i >= 0; i--) {
            if (str.regionMatches(true, i, searchStr, 0, length))
                return true;
        }
        return false;
    }

    private void setCheckedState(View view, boolean isChecked) {
        if (getContext() == null)
            return;
        view.setVisibility(isChecked ? View.VISIBLE : View.GONE);
    }

    private void setCurrent(List<Skin> skins) {
        currentList.clear();
        currentList.addAll(skins);
        adapter.notifyDataSetChanged();
    }

    private final SkinsAdapter.OnClickItem listener = id -> {
        View view = getView();
        if (view == null)
            return;
        Bundle bundle = new Bundle();
        bundle.putString(ModDetailsFragment.ARG_ITEM_ID, id);
        Navigation.findNavController(getView()).navigate(R.id.action_mainFragment_to_modDetailsFragment, bundle);
    };
}

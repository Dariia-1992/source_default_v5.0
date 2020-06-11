package com.hi.appskin_v40.fragment;

import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

    private TextView allSkins;
    private TextView favoriteList;

    private View textLine;
    private View favoriteLine;

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
        //textLine = view.findViewById(R.id.under_text_line);
        //favoriteLine = view.findViewById(R.id.under_favorite_line);
//        favoriteLine.setVisibility(View.GONE);
  //      textLine.setVisibility(View.VISIBLE);

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

//        if (MODE_FAVORITE) {
//            allSkins.setTextColor(ContextCompat.getColor(getContext(), R.color.toolbar_text_color_favorite));
//            favoriteList.setTextColor(ContextCompat.getColor(getContext(), R.color.toolbar_text_color));
//            setCheckedState(favoriteList, true, favoriteLine);
//            setCheckedState(allSkins, false, textLine);
//        } else {
//            favoriteList.setTextColor(ContextCompat.getColor(getContext(), R.color.toolbar_text_color_favorite));
//            allSkins.setTextColor(ContextCompat.getColor(getContext(), R.color.toolbar_text_color));
//            setCheckedState(allSkins, true, textLine);
//            setCheckedState(favoriteList, false, favoriteLine);
//        }
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
       // allSkins = view.findViewById(R.id.button_text);
      //  favoriteList = view.findViewById(R.id.button_favorite);

//        allSkins.setOnClickListener(v -> {
//            setCheckedState(allSkins, true, textLine);
//            setCheckedState(favoriteList, false, favoriteLine);
//            setCurrent(fulSkins);
//            MODE_FAVORITE = false;
//        });
//
//        getFavorite();
//        favoriteList.setOnClickListener(v -> {
//            setCheckedState(favoriteList, true, favoriteLine);
//            setCheckedState(allSkins, false, textLine);
//            setCurrent(favoriteSkins);
//            MODE_FAVORITE = true;
//        });
    }

    private void setupSearch(boolean show){
        View searchButton = view.findViewById(R.id.toolbar_search);
        View toolbarText = view.findViewById(R.id.toolbar_name);
        SearchView searchView = view.findViewById(R.id.search);

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

    private void setCheckedState(TextView textView, boolean isChecked, View line) {
        if (getContext() == null)
            return;
        textView.setTextColor(ContextCompat.getColor(getContext(), isChecked ? R.color.toolbar_text_color : R.color.toolbar_text_color_favorite));
        line.setVisibility(isChecked ? View.VISIBLE : View.GONE);
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

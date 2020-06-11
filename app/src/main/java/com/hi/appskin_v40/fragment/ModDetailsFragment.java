package com.hi.appskin_v40.fragment;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hi.appskin_v40.BuildConfig;
import com.hi.appskin_v40.MainActivity;
import com.hi.appskin_v40.R;
import com.hi.appskin_v40.dialogs.DownloadNeverCompleteDialog;
import com.hi.appskin_v40.dialogs.FileDownloadCompleteDialog;
import com.hi.appskin_v40.dialogs.NotFoundDialog;
import com.hi.appskin_v40.model.Skin;
import com.hi.appskin_v40.model.SkinsRepository;
import com.hi.appskin_v40.utils.Config;
import com.hi.appskin_v40.utils.DownloadHelper;
import com.hi.appskin_v40.utils.FavoritesManager;
import com.hi.appskin_v40.utils.LocalStorage;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * A simple {@link Fragment} subclass.
 */
public class ModDetailsFragment extends Fragment {
    public static final String ARG_ITEM_ID = "itemId";
    private static final int RC_WRITE_PERMISSIONS = 1000;
    private static final long UPDATE_PERIOD = 1000;
    private FileDownloadCompleteDialog.OnNeverClickListener listener;
    private View view;
    private Skin skin;
    private Handler handler = new Handler();
    private Runnable handlerRunnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String itemId = getArguments().getString(ARG_ITEM_ID);
            skin = SkinsRepository.getItemById(itemId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mod_details, container, false);

        MainActivity activity = (MainActivity) requireActivity();

        //ViewPager viewPager = view.findViewById(R.id.viewPager);
        TextView skinTitle = view.findViewById(R.id.titleView);
        TextView description = view.findViewById(R.id.descriptionView);
        ImageView image = view.findViewById(R.id.skinImage);
        //CircleIndicator indicator = view.findViewById(R.id.circleIndicator);
        View isUpdated = view.findViewById(R.id.isUpdate);

        //viewPager.setAdapter(new ImagePagerAdapter(getContext(), null));
        //indicator.setViewPager(viewPager);
        //indicator.setVisibility(View.GONE);

        Picasso.get()
                .load(DownloadHelper.getThumbnailUrl(skin.getThumbnail()))
                .into(image);

        skinTitle.setText(skin.getTitle());
        description.setText(Html.fromHtml(skin.getDescription()));
        isUpdated.setVisibility(skin.isUpdatedToday() ? View.VISIBLE : View.VISIBLE);

        setRating(view.findViewById(R.id.ratingContainer), skin.getRating());
        setDescriptionImages(view.findViewById(R.id.descImagesContainer), skin.getScreenShots());

        ImageView download = view.findViewById(R.id.download_button);
        download.setOnClickListener(v -> {
            activity.showInterstitialAd();
            startDownloading();
        });

        ImageView buttonInstall = view.findViewById(R.id.install_button);
        buttonInstall.setOnClickListener(v -> { openDownloadedFile(); });

        ImageView shared = view.findViewById(R.id.link_to_store);
        shared.setOnClickListener(v -> { goToStore(getContext()); });

        View back = view.findViewById(R.id.back);
        back.setOnClickListener(v -> activity.onBackPressed());

        ImageView favorite =view.findViewById(R.id.favorite_button);
        favorite.setOnClickListener(onFavoriteClicked);
        updateFavoriteState();

        showInterstitialAds();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (handlerRunnable != null)
            handler.post(handlerRunnable);
        else {
            // Check downloading status
            DownloadHelper.DownloadingState state = DownloadHelper.getDownloadingStatus(requireContext(), skin);
            updateState(state);

            // If downloading -- show progressBar
            if (state == DownloadHelper.DownloadingState.Downloading) {
                long id = LocalStorage.getIdForModInfo(requireContext(), skin);
                if (id != 0) {
                    handlerRunnable = () -> {
                        updateDownloadProgress(id);
                        if (handlerRunnable != null)
                            handler.postDelayed(handlerRunnable, UPDATE_PERIOD);
                    };
                    handler.post(handlerRunnable);
                }
            }
        }

        Context context = requireContext();
        context.registerReceiver(receiver, new IntentFilter((DownloadManager.ACTION_DOWNLOAD_COMPLETE)));
    }

    @Override
    public void onPause() {
        super.onPause();

        handler.removeCallbacks(handlerRunnable);

        Context context = requireContext();
        context.unregisterReceiver(receiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void showInterstitialAds() {
        MainActivity activity = (MainActivity) requireActivity();
        int showsWithoutAdCount = LocalStorage.getOpensWithoutAd(requireContext());
        if (showsWithoutAdCount >= Config.SHOWS_WITHOUT_AD_COUNT || showsWithoutAdCount < 0) {
            activity.showInterstitialAd();
        } else
            LocalStorage.setOpensWithoutAd(requireContext(), showsWithoutAdCount + 1);
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

    private void updateFavoriteState() {
        ImageView image = view.findViewById(R.id.favorite_button);
        image.setImageResource(FavoritesManager.getInstance().isFavorite(getContext(), skin)
                ? R.drawable.is_favourite_button
                : R.drawable.favourite_button_false);
    }

    private void openDownloadedFile() {
        String fileName = Uri.parse(skin.getMod()).getLastPathSegment();
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        Uri fileUri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
        Intent myIntent = new Intent(Intent.ACTION_VIEW);
        myIntent.setData(fileUri);
        myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent j = Intent.createChooser(myIntent, "Choose Minecraft to install file:");
        PackageManager pm = requireActivity().getPackageManager();
        List<ResolveInfo> activities;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            activities = pm.queryIntentActivities(myIntent, PackageManager.MATCH_ALL);
        } else {
            activities = pm.queryIntentActivities(myIntent, 0);
        }
        if (activities.size() > 0) {
            startActivity(j);
        } else {
            showDialogNotFound();
        }
    }

    private void setRating(ViewGroup group, int rating) {
        group.removeAllViews();
        for (int i = 0; i < rating; i++) {
            View view = LayoutInflater.from(requireContext()).inflate(R.layout.item_rating_star, group, false);
            group.addView(view);
        }
    }

    private void setDescriptionImages(ViewGroup group, ArrayList<String> descriptionImages) {
        group.removeAllViews();
        for (String descriptionImage : descriptionImages) {
            View view = LayoutInflater.from(requireContext()).inflate(R.layout.item_image_in_description, group, false);
            AppCompatImageView iv = view.findViewById(R.id.ivDescImage);
            group.addView(view);

            Picasso.get()
                    .load(DownloadHelper.getScreenshotlUrl(descriptionImage))
                    .into(iv);
        }
    }

    @AfterPermissionGranted(RC_WRITE_PERMISSIONS)
    private void startDownloading() {
        Context context = getContext();
        if (context == null)
            return;

        if (EasyPermissions.hasPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            long id = DownloadHelper.downloadFile(requireContext(), skin.getMod());
            if (id == 0) {
                Toast.makeText(context, "Download error. Please try later", Toast.LENGTH_LONG).show();
            } else {
                LocalStorage.saveDownloadId(context, skin, id);
                handlerRunnable = () -> {
                    updateDownloadProgress(id);
                    if (handlerRunnable != null)
                        handler.postDelayed(handlerRunnable, UPDATE_PERIOD);
                };
                handler.post(handlerRunnable);
                updateState(DownloadHelper.DownloadingState.Downloading);
            }
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_write_permission), RC_WRITE_PERMISSIONS, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void updateDownloadProgress(long downloadId) {
        DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            int bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

            if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                handler.removeCallbacks(handlerRunnable);
                handlerRunnable = null;
                finishDownloading();
            }
            int progress = (int) ((bytesDownloaded * 100L) / bytesTotal);
            if (progress >= 100) {
                handler.removeCallbacks(handlerRunnable);
                handlerRunnable = null;
            }
            setDownloadProgress(progress);
        }
        cursor.close();
    }

    private void updateState(DownloadHelper.DownloadingState state) {
        View loadingContainer = view.findViewById(R.id.downloadingContainer);
        View buttonDownload = view.findViewById(R.id.download_button);
        View buttonInstall = view.findViewById(R.id.install_button);

        switch (state) {
            case NotDownloaded: {
                buttonDownload.setVisibility(View.VISIBLE);
                loadingContainer.setVisibility(View.GONE);
                buttonInstall.setVisibility(View.GONE);
                break;
            }
            case Downloading: {
                buttonDownload.setVisibility(View.GONE);
                loadingContainer.setVisibility(View.VISIBLE);
                break;
            }
            case Downloaded: {
                buttonInstall.setVisibility(View.VISIBLE);
                buttonDownload.setVisibility(View.GONE);
                loadingContainer.setVisibility(View.GONE);
            }
        }
    }

    private void finishDownloading() {
        updateState(DownloadHelper.DownloadingState.Downloaded);
        setDownloadProgress(100);

        showDialogSuccessfully();
    }

    private void setDownloadProgress(int percents) {
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        progressBar.setProgress(percents);
    }

    private void showDialogSuccessfully() {
        if (LocalStorage.isShowRateDialogAgain(requireContext())) {
            listener = this::showDialogDownloadNeverComplete;
            FileDownloadCompleteDialog dialog = FileDownloadCompleteDialog.createDialog(listener);
            dialog.show(getChildFragmentManager(), FileDownloadCompleteDialog.class.getSimpleName());
        } else {
            Toast.makeText(getContext(), "Download error. Please try later", Toast.LENGTH_LONG).show();
        }
    }

    private void showDialogNotFound() {
        NotFoundDialog dialog = new NotFoundDialog();
        dialog.show(getChildFragmentManager(), NotFoundDialog.class.getSimpleName());

    }

    public void showDialogDownloadNeverComplete(){
        DownloadNeverCompleteDialog dialog = new DownloadNeverCompleteDialog();
        dialog.show(getChildFragmentManager(), DownloadNeverCompleteDialog.class.getSimpleName());
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action))
                return;

            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            long currentPageId = LocalStorage.getIdForModInfo(context, skin);

            // Check if finished file from current page
            if ((downloadId == currentPageId) && downloadId != 0) {
                DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);

                Cursor cursor = downloadManager.query(query);
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (cursor.getInt(columnIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                        finishDownloading();
                    }
                }
                handler.removeCallbacks(handlerRunnable);
                handlerRunnable = null;
                cursor.close();
            }
        }
    };

    private final View.OnClickListener onFavoriteClicked = v -> {
        if (getContext() == null || skin == null)
            return;

        FavoritesManager manager = FavoritesManager.getInstance();
        boolean isFavorite = manager.isFavorite(getContext(), skin);
        if (isFavorite)
            manager.removeFavorite(getContext(), skin);
        else
            manager.addToFavorite(getContext(), skin);

        updateFavoriteState();
    };
}
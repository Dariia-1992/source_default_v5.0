package com.hi.appskin_v40.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.hi.appskin_v40.BuildConfig;
import com.hi.appskin_v40.R;
import com.hi.appskin_v40.utils.LocalStorage;

public class FileDownloadCompleteDialog extends DialogFragment {
    public interface OnNeverClickListener {
        void onFinish();
    }

    private OnNeverClickListener listener;
    private View view;

    public static FileDownloadCompleteDialog createDialog(OnNeverClickListener listener) {
        FileDownloadCompleteDialog dialog = new FileDownloadCompleteDialog();
        dialog.listener = listener;

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = getLayoutInflater().inflate(R.layout.dialog_file_download_comlete, null);
        view.findViewById(R.id.button_Ok).setOnClickListener(v -> {
            dismiss();
            LocalStorage.setNeverShowRateDialogAgain(requireContext());
            final String appPackageName = BuildConfig.APPLICATION_ID;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }});

        view.findViewById(R.id.never).setOnClickListener(v -> {
            LocalStorage.setNeverShowRateDialogAgain(requireContext());
            if (listener != null)
                listener.onFinish();
            dismiss();
        });

        view.findViewById(R.id.later).setOnClickListener(v -> dismiss());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = getContext();
        if (context == null)
            return super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setView(view);

        return builder.create();
    }
}

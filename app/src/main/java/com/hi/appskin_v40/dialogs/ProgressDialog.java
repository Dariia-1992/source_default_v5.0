package com.hi.appskin_v40.dialogs;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.hi.appskin_v40.R;

public class ProgressDialog extends DialogFragment {
    View view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = getLayoutInflater().inflate(R.layout.dialog_downloading, null);

    }

        private void setDownloadProgress(int percents) {
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        progressBar.setProgress(percents);
    }
}

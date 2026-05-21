package com.vulnlab.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AdminFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        TextView tv = new TextView(getContext());
        tv.setText("⚠ ADMIN PANEL ⚠\n\nThis fragment should be reachable only by signed-in admins.\n\n"
            + "It is reachable via fragment-injection on VulnPreferenceActivity.");
        tv.setPadding(32, 32, 32, 32);
        return tv;
    }
}

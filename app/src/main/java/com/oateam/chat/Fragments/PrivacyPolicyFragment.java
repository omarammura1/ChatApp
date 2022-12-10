package com.oateam.chat.Fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oateam.chat.R;


public class PrivacyPolicyFragment extends Fragment {

    private TextView tvPrivacyPolicy;

    public PrivacyPolicyFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_privacy_policy, container, false);

        initViews(view);

        getHtml(tvPrivacyPolicy);

        return view;
    }


    private void getHtml( TextView textView){
        String html = getResources().getString(R.string.privacy_policy_html);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            textView.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT));
        } else {
            textView.setText(Html.fromHtml(html));
        }
    }

    private void initViews(View view) {


        tvPrivacyPolicy = view.findViewById(R.id.tv_privacy_policy);

    }
}
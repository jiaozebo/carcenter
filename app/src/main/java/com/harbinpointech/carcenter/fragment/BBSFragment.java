package com.harbinpointech.carcenter.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.harbinpointech.carcenter.R;

/**
 * Created by John on 2014/10/12.
 */
public class BBSFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bbs, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        WebView webView = (WebView) getView().findViewById(R.id.webview);
        webView.loadUrl("http://182.254.136.208:8000/forum.php");

//        webView.setWebViewClient(new WebViewClient() {
//
//            public void onPageFinished(WebView view, String url) {
//                // do your stuff here
//                View root = getView();
//                if (root == null) {
//                    return;
//                }
//                View progress = root.findViewById(android.R.id.progress);
//                if (progress != null)
//                    progress.setVisibility(View.GONE);
//            }
//        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);   //在当前的webview中跳转到新的url
                return true;
            }
        });
    }


}


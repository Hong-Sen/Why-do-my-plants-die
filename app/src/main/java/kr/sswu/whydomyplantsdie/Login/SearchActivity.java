package kr.sswu.whydomyplantsdie.Login;


import androidx.appcompat.app.AppCompatActivity;



import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import kr.sswu.whydomyplantsdie.R;

public class SearchActivity extends AppCompatActivity {
    private WebView webView;
    private String url="https://www.fuleaf.com/search?term=";



   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search);

       webView =(WebView)findViewById(R.id.webView);
      // WebSettings webSettings =webView.getSettings();
     //  webSettings.setJavaScriptEnabled(true);
      //  webView.setWebChromeClient(new WebChromeClient());
     //   webView.setWebViewClient(new WebViewClientClass());
     //  webView.loadUrl(url);

       //webView = ( WebView )findViewById( R.id.webView);
      // webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
      webView.setWebViewClient(new WebViewClient());
      webView.setWebChromeClient(new WebChromeClient());
      // webView.setNetworkAvailable(true);
      webView.getSettings().setJavaScriptEnabled(true);
      // webView.getSettings().setDomStorageEnabled(true);
       webView.loadUrl(url);
       webView = findViewById(R.id.webView);
       webView.setWebViewClient(new WebViewClient());
       webView.loadUrl("https://www.fuleaf.com/search?term=");
    }//웹뷰 구현

    @Override//뒤로가기 버튼 기능 구현
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()){
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
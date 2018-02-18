package client.deneme.oauth.weboauth2;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static String CLIENT_ID = "4545636564tregetrhtrht";
    //Use your own client id
    private static String CLIENT_SECRET ="rthr65h5yghdfg";


    //Use your own client secret
    private static String REDIRECT_URI="http://localhost";
    private static String GRANT_TYPE="authorization_code";
    private static String TOKEN_URL ="https://api.meethue.com/oauth2/token";
    private static String OAUTH_URL ="https://api.meethue.com/oauth2/auth";
    private static String OAUTH_SCOPE="https://www.googleapis.com/auth/urlshortener";
    //Change the Scope as you need
    WebView web;
    Button auth;
    SharedPreferences pref;
    TextView Access;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getSharedPreferences("AppPref", MODE_PRIVATE);
        Access =(TextView)findViewById(R.id.Access);
        auth = (Button)findViewById(R.id.auth);
        web = (WebView)findViewById(R.id.webv);


        auth.setOnClickListener(new View.OnClickListener() {
            Dialog auth_dialog;
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                //auth_dialog = new Dialog(MainActivity.this);
                //auth_dialog.setContentView(R.layout.auth_dialog);
                //web = (WebView)auth_dialog.findViewById(R.id.webv);

                web.clearCache(true);
                web.clearHistory();
                web.getSettings().setJavaScriptEnabled(true);
                web.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

                web.getSettings().setAllowFileAccess(true);
                web.getSettings().setAllowFileAccessFromFileURLs(true);

                web.getSettings().setSupportZoom(true);
                web.getSettings().setBuiltInZoomControls(true);

                web.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

                web.setWebChromeClient(new WebChromeClient());
                web.setWebViewClient(new WebViewClient() {

                    @Override
                    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                        super.onReceivedError(view, request, error);
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url){
                        // do your handling codes here, which url is the requested url
                        // probably you need to open that url rather than redirect:
                        view.loadUrl(url);
                        return false; // then it is not handled by default action
                    }

                    boolean authComplete = false;
                    Intent resultIntent = new Intent();

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon){
                        //super.onPageStarted(view, url, favicon);

                    }
                    String authCode;
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);

                        if (url.contains("?code=") && authComplete != true) {
                            Uri uri = Uri.parse(url);
                            authCode = uri.getQueryParameter("code");
                            Log.i("", "CODE : " + authCode);
                            authComplete = true;
                            resultIntent.putExtra("code", authCode);
                            MainActivity.this.setResult(Activity.RESULT_OK, resultIntent);
                            setResult(Activity.RESULT_CANCELED, resultIntent);

                            SharedPreferences.Editor edit = pref.edit();
                            edit.putString("Code", authCode);
                            edit.commit();
                            auth_dialog.dismiss();
                            new TokenGet().execute();
                            Toast.makeText(getApplicationContext(),"Authorization Code is: " +authCode, Toast.LENGTH_SHORT).show();
                        }else if(url.contains("error=access_denied")){
                            Log.i("", "ACCESS_DENIED_HERE");
                            resultIntent.putExtra("code", authCode);
                            authComplete = true;
                            setResult(Activity.RESULT_CANCELED, resultIntent);
                            Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_SHORT).show();

                            auth_dialog.dismiss();
                        }
                    }
                });

                //auth_dialog.setTitle("Authorize Learn2Crack");
                //auth_dialog.setCancelable(true);
                //auth_dialog.show();

                web.loadUrl("https://account.meethue.com/login"/*mainUrl*/);

            }
        });
    }

    private class TokenGet extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;
        String Code;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Contacting Google ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            Code = pref.getString("Code", "");
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            GetAccessToken jParser = new GetAccessToken();
            JSONObject json = jParser.gettoken(TOKEN_URL,Code,CLIENT_ID,CLIENT_SECRET,REDIRECT_URI,GRANT_TYPE);
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            pDialog.dismiss();
            if (json != null){

                try {

                    String tok = json.getString("access_token");
                    String expire = json.getString("expires_in");
                    String refresh = json.getString("refresh_token");

                    Log.d("Token Access", tok);
                    Log.d("Expire", expire);
                    Log.d("Refresh", refresh);
                    auth.setText("Authenticated");
                    Access.setText("Access Token:"+tok+"nExpires:"+expire+"nRefresh Token:"+refresh);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }else{
                Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
            }
        }
    }

}
package com.aylanetworks.aylasdk.auth;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Base64;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.volley.Request;
import com.android.volley.Response;
import com.aylanetworks.aylasdk.AylaAPIRequest;
import com.aylanetworks.aylasdk.AylaJsonRequest;
import com.aylanetworks.aylasdk.AylaLog;
import com.aylanetworks.aylasdk.AylaLoginManager;
import com.aylanetworks.aylasdk.AylaNetworks;
import com.aylanetworks.aylasdk.AylaSystemSettings;
import com.aylanetworks.aylasdk.error.AylaError;
import com.aylanetworks.aylasdk.error.ErrorListener;
import com.aylanetworks.aylasdk.error.JsonError;
import com.aylanetworks.aylasdk.metrics.AylaMetric;
import com.aylanetworks.aylasdk.metrics.AylaMetricsManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class AppleOAuthProvider extends AylaOAuthProvider {
    private static final String LOG_TAG = "AppleOAuthProvider";
    private static final String AUS_SUCCESS_PREFIX = "{user_service_url}?success=";
    private static final String SUCCESS_PREFIX = "success=";
    private final static String ID_TOKEN = "%20id_token";
    private final static String SCOPE = "name%20email";
    public final static String APPLE_AUTH = "apple_provider";
    public final static String SIGN_IN_ABORTED = "SignInAborted";
    private final static String AUTH_URI_REMOTE = "https://" + MOBILE_HOST_URL + "/";


    public final String CLIENT_ID;
    public final String REDIRECT_URL;

    private final WebView _webView;
    private Dialog appleDialog;
    private String appleAuthCode;
    private String appleClientSecret;
    private String authCode;
    private String idToken;
    private String userInfo;

    private Boolean logInSuccess;
    private Activity parentActivity;

    private AppleLoginResultsCallback callback;

    private final static String AUTHURL = "https://appleid.apple.com/auth/authorize";
    public final static String TOKENURL = "https://appleid.apple.com/auth/token";


    public AppleOAuthProvider(WebView webView, String serviceID, String redirectURL, Activity activity, AppleLoginResultsCallback appleCallback) {
        _webView = webView;
        CLIENT_ID = serviceID;
        REDIRECT_URL = redirectURL;
        parentActivity = activity;
        callback = appleCallback;
    }


    /**
     * This method displays the AppleLogin Dialog
     */
    public void showAppleLoginDialog() {
        String state = UUID.randomUUID().toString();
        String appleAuthFull = this.getAuthURL() + "?response_type=code"+ID_TOKEN+"&response_mode=form_post&client_id=" + this.getClientId() +
                "&scope=" + this.getScope() + "&state=" + state + "&redirect_uri=" + this.getRedirectUrl();
        appleDialog = new Dialog(getActivity().getApplicationContext());
        WebView webView = _webView;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(appleAuthFull);
        webView.setVisibility(View.VISIBLE);
        webView.bringToFront();
        webView.setWebViewClient(new AppleWebViewClient());

    }

    @Override
    public String getAuthType() {
        return APPLE_AUTH;
    }

    @Override
    public String getAuthURL() {
        return AUTHURL;
    }

    @Override
    public WebView getWebView() {
        return _webView;
    }

    public String getTokenurl() {
        return TOKENURL;
    }

    public String getClientId() {
        return CLIENT_ID;
    }

    public String getScope() {
        return SCOPE;
    }

    public String getRedirectUrl() {
        return REDIRECT_URL;
    }

    public Boolean getLogInSuccess() {
        return logInSuccess;
    }

    public void setLogInSuccess(Boolean logInSuccess) {
        this.logInSuccess = logInSuccess;
    }

    /**
     * This method is used to return the activity from which this object was constructed
     *
     * @return the activity used to build this
     */
    private Activity getActivity() {
        return parentActivity;
    }


    public class AppleWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(REDIRECT_URL)) {
                handleUrl(url);
                if (url.contains(SUCCESS_PREFIX)) {
                    setLogInSuccess(true);
                    appleDialog.dismiss();
                    view.setVisibility(View.GONE);
//                    view.destroy();
                } else {
                    setLogInSuccess(false);
                }
                return true;
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            AylaLog.d(LOG_TAG, "onPageFinished");
            super.onPageFinished(view, url);

        }

        private void handleUrl(String url) {
            Uri uri = Uri.parse(url);
            AylaLog.d(LOG_TAG, "url:" + url);
            String success = uri.getQueryParameter("success");
            if (success != null) {
                if (success.equalsIgnoreCase("true")) {
                    AylaLog.d(LOG_TAG, "on Success Path");
                    appleClientSecret = uri.getQueryParameter("client_secret");
                    SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                    sharedPreferences.edit().putString("client_secret", appleClientSecret).apply();
                    appleAuthCode = uri.getQueryParameter("code");
                    AylaLog.d(LOG_TAG, appleAuthCode);
                    userInfo = uri.getQueryParameter("user");
                    authCode = appleAuthCode;
                    callback.appleLoginResultsReceived(appleAuthCode, userInfo);
                } else if (success == "false") {
                    AylaLog.e(LOG_TAG, "failed to get auth code");
                    callback.appleLoginResultsReceived(null, null);
                }
            } else {
                // The request was cancelled by the user
                callback.appleLoginResultsReceived(SIGN_IN_ABORTED, null);
            }
        }


        /**
         * Currently, AylaService is doing this step for us, but under some scenarios, we might need to actually redeem the auth code
         * for an auth token, this is the method to do that
         *
         * @param appleAuthCode     The code we got on the callback url, we use this token to exchange it for the auth token
         * @param appleClientSecret This we receive from the callback URL as well
         */
        private void requestForAccessToken(String appleAuthCode, String appleClientSecret) {
            AylaLog.d(LOG_TAG, "on the request:" + appleAuthCode);
            String grantType = "authorization_code";
            String postParamsForAuth = "grant_type=" + grantType + "&code=" + appleAuthCode + "&redirect_uri="
                    + REDIRECT_URL + "&client_id=" + CLIENT_ID + "&client_secret=" + appleClientSecret;
            Thread background = new Thread() {
                @Override
                public void run() {
                    URL url = null;
                    try {
                        AylaLog.d(LOG_TAG, "body params:" + postParamsForAuth);
                        url = new URL(AppleOAuthProvider.TOKENURL);
                        HttpsURLConnection localhttpsURLConnection = (HttpsURLConnection) url.openConnection();
                        localhttpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        localhttpsURLConnection.setDoInput(true);
                        localhttpsURLConnection.setDoOutput(true);
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(localhttpsURLConnection.getOutputStream());
                        outputStreamWriter.write(postParamsForAuth);
                        outputStreamWriter.flush();
                        BufferedReader br = new BufferedReader(new InputStreamReader(localhttpsURLConnection.getInputStream(), "UTF-8"));
                        String response = "";
                        String line;
//                         Requires api 24
//                         String response = br.lines().collect(Collectors.joining());
                        while ((line = br.readLine()) != null) {
                            response = response.concat(line);
                            AylaLog.d(LOG_TAG, "response:" + line);
                        }
                        JSONObject jsonObject = (JSONObject) new JSONTokener(response).nextValue();
                        String AccessToken = jsonObject.getString("access_token");
                        String expiresIn = jsonObject.getString("expires_in");
                        String refreshToken = jsonObject.getString("refresh_token");
                        String idToken = jsonObject.getString("id_token");

                        String encodedUserID = idToken.split(".")[1];

                        String decodedUserData = new String(Base64.decode(encodedUserID, Base64.DEFAULT));
                        JSONObject userDataJsonObject = new JSONObject(decodedUserData);
                        String userId = userDataJsonObject.getString("sub");
                        AylaLog.d(LOG_TAG, userId);
                        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                        sharedPreferences.edit().putString("refresh_token", refreshToken).apply();
                    } catch (Exception e) {
                        AylaLog.d(LOG_TAG, e.getLocalizedMessage());
                    }
                }

            };
            background.start();

        }


    }


    /**
     * Pass authCode to Ayla User Service for login account validation
     *
     * @param authCode    3rd Party oAuthCode
     * @param originalReq Original API Request
     */

    public void authenticateToService(final String authCode, final AylaAPIRequest originalReq) {
        AylaLoginManager loginManager = AylaNetworks.sharedInstance().getLoginManager();
        String url = loginManager.userServiceUrl("users/provider_auth.json");

        AylaSystemSettings settings = AylaNetworks.sharedInstance().getSystemSettings();
        // Construct a JSON object to contain the parameters.
        JSONObject userParam = new JSONObject();
        try {
            userParam.put("code", authCode);
            userParam.put("app_id", settings.appId);

            userParam.put("provider", APPLE_AUTH);
            userParam.put("redirect_url", getAuthURL());
            userParam.put("last_name", "XX");
            userParam.put("first_name", "xx");

        } catch (JSONException e) {
            _listener.didFailAuthentication(new JsonError(null, "JSONException trying to create " +
                    "Post body for authentication to Service", e));
            return;
        }

        String postBodyString = userParam.toString();
        AylaAPIRequest<AylaAuthorization> request = new
                AylaJsonRequest<>(
                Request.Method.POST,
                url,
                postBodyString,
                null,
                AylaAuthorization.class,
                null, // No session manager exists until we are logged in!
                new Response.Listener<AylaAuthorization>() {
                    @Override
                    public void onResponse(AylaAuthorization response) {
                        _listener.didAuthenticate(response, false);
                    }
                },
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(AylaError error) {
                        _listener.didFailAuthentication(error);
                    }
                });

        // This is a compound request- we need to keep the chain going so canceling the original
        // request will cancel this new request.
        if (originalReq != null) {
            if (originalReq.isCanceled()) {
                request.cancel();
            } else {
                originalReq.setChainedRequest(request);
                loginManager.sendUserServiceRequest(request);
            }
        } else {
            loginManager.sendUserServiceRequest(request);
        }
    }


    private void sendToMetricsManager(AylaMetric metrics) {
        AylaMetricsManager metricsManager = AylaNetworks.sharedInstance().getMetricsManager();
        if (metricsManager != null) {
            metricsManager.addMessageToUploadsQueue(metrics);
        }
    }

    /**
     * We use this callback to notify when we get back the code from apple to redeem it against AUS
     */
    public interface AppleLoginResultsCallback {
        void appleLoginResultsReceived(String authCode, String userInfo);
    }

}

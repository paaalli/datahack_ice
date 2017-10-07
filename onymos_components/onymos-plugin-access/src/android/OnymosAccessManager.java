/*
 * OnymosAccessManager.java
 * Onymos Access Component
 *
 * Copyright 2015-2017 Onymos Inc
 *
 * Use of Onymos Access Component is subject to the Onymos Terms of License Agreement
 *
 */

package com.onymos.components.access;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

/* ------ Google Authentication imports ------ */
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.Scope;

import org.apache.cordova.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import android.content.pm.Signature;
/* ------ end Google Authentication imports ------ */

public class OnymosAccessManager extends CordovaPlugin implements GoogleApiClient.OnConnectionFailedListener {

	public final String ACTION_GET_APPNAME = "getApplicationName";
	public final String ACTION_GET_APPVERSION = "getApplicationVersion";

	public final String ACTION_GOOGLE_LOGIN = "googleLogin";
	public final String ACTION_GOOGLE_GETAUTHDATA = "googleGetAuthData";
	public final String ACTION_GOOGLE_LOGOUT = "googleLogout";

	public static final int GOOGLE_REQUEST_CODE = 201701;

	private GoogleApiClient googleClient;
	private CallbackContext localCallbackContext;

	@Override
	public boolean execute (String action, JSONArray args, CallbackContext callbackContext) throws JSONException 
	{
		PackageManager packageManager = this.cordova.getActivity().getPackageManager();
		ApplicationInfo ai;
		CharSequence al;

		this.localCallbackContext = callbackContext;

		if (action.equals(ACTION_GET_APPVERSION)) {
			try {
				PackageInfo packageInfo = packageManager.getPackageInfo(this.cordova.getActivity().getPackageName(), 0);
				callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK,  packageInfo.versionName));
			}
			catch (Exception e) {
				Log.e("OnymosAccessManager", "Error occurred calling plugin: " + e.getMessage());
				callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
				return false;
			}
		} /* end if action.equals(ACTION_GET_APPVERSION) */
		else if (action.equals(ACTION_GET_APPNAME)) {
			try {						
				String appName = this.cordova.getActivity().getApplicationContext().getPackageName();	
				if (appName != null) {
					PluginResult result = new PluginResult(PluginResult.Status.OK, appName);
					result.setKeepCallback(true);
					callbackContext.sendPluginResult(result);
				}
				else {
					PluginResult result = new PluginResult(PluginResult.Status.ERROR);
					result.setKeepCallback(true);
					callbackContext.sendPluginResult(result);
				}
				return true;
			}
			catch (Exception e) {
				Log.e("OnymosAccessManager", "Error occurred calling plugin: " + e.getMessage());
				callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
				return false;
			}
		} /* end else if action.equals(ACTION_GET_APPNAME) */
		else if (action.equals(ACTION_GOOGLE_LOGIN)) {
			setupGoogleClient(args.optJSONObject(0));
			this.cordova.setActivityResultCallback(this);
			loginUserToGoogle();

		} /* end else if action.equals(ACTION_GOOGLE_LOGIN) */
		else if (action.equals(ACTION_GOOGLE_GETAUTHDATA)) {
			setupGoogleClient(args.optJSONObject(0));
			getAuthDataFromGoogle();

		} /* end else if action.equals(ACTION_GOOGLE_GETAUTHDATA) */
		else if (action.equals(ACTION_GOOGLE_LOGOUT)) {
			logoutUserFromGoogle();

		} /* end else if action.equals(ACTION_GOOGLE_LOGOUT) */

		return true;
	} /* end public boolean execute */

	private synchronized void setupGoogleClient (JSONObject loginOptions) throws JSONException {
		if (loginOptions == null) {
			return;
		}

		if (this.googleClient != null) {
			this.googleClient.disconnect();
		}

		GoogleSignInOptions.Builder gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
			.requestEmail()
			.requestProfile();

		String scopes = loginOptions.optString("scopes", null);
		String webClientId = loginOptions.optString("webClientId", null);

		if (scopes != null && !scopes.isEmpty()) {
			for (String scope : scopes.split(" ")) {
				gso.requestScopes(new Scope(scope));
			}

		} /* end if scopes != null && !scopes.isEmpty() */

		if (webClientId != null && !webClientId.isEmpty()) {
			gso.requestIdToken(webClientId);
			gso.requestServerAuthCode(webClientId, false);

		} /* end if webClientId != null && !webClientId.isEmpty() */

		GoogleApiClient.Builder client = new GoogleApiClient.Builder(webView.getContext())
			.addOnConnectionFailedListener(this)
			.addApi(Auth.GOOGLE_SIGN_IN_API, gso.build());

		this.googleClient = client.build();

	} /* end private synchronized void setupGoogleClient */

	private void loginUserToGoogle() {
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(this.googleClient);
		this.cordova.getActivity().startActivityForResult(signInIntent, GOOGLE_REQUEST_CODE);

	} /* end private void loginUserToGoogle */

	private void getAuthDataFromGoogle() {
		ConnectionResult connection = googleClient.blockingConnect();

		if (connection.isSuccess()) {
			processLoginResult(Auth.GoogleSignInApi.silentSignIn(this.googleClient).await());
		}

	} /* end private void getAuthDataFromGoogle */

	private void logoutUserFromGoogle() {
		if (this.googleClient == null) {
			localCallbackContext.error("ERROR: Cannot Logout when not Logged in.");
			return;

		} /* end if this.googleClient == null */

		ConnectionResult connection = googleClient.blockingConnect();

		if (connection.isSuccess()) {
			Auth.GoogleSignInApi.signOut(this.googleClient).setResultCallback(
				new ResultCallback<Status>() {
					@Override
					public void onResult (Status status) {
						if (status.isSuccess()) {
							localCallbackContext.success("Logout Google User: success.");

						} /* end if status.isSuccess() */
						else {
							localCallbackContext.error(status.getStatusCode());

						} /* end else status.isSuccess() */

					} /* end public void onResult */

				} /* end new ResultCallback<Status> */
			); /* end Auth.GoogleSignInApi.signOut */

		} /* end if connection.isSuccess() */

	} /* end private void logoutUserFromGoogle */

	private void disconnect() {
		if (this.googleClient == null) {
			localCallbackContext.error("ERROR: Cannot disconnect when not Logged in.");
			return;

		} /* end if this.googleClient == null */

		ConnectionResult connection = googleClient.blockingConnect();

		if (connection.isSuccess()) {
			Auth.GoogleSignInApi.revokeAccess(this.googleClient).setResultCallback(
				new ResultCallback<Status>() {
					@Override
					public void onResult (Status status) {
						if (status.isSuccess()) {
							localCallbackContext.success("Disconnect Google User: success.");

						} /* end if status.isSuccess() */
						else {
							localCallbackContext.error(status.getStatusCode());

						} /* end else status.isSuccess() */

					} /* end public void onResult */

				} /* end new ResultCallback<Status> */
			); /* end Auth.GoogleSignInApi.revokeAccess */

		} /* end if connection.isSuccess() */

	} /* end private void disconnect */

	@Override
	public void onConnectionFailed (ConnectionResult result) {
		localCallbackContext.error(result.getErrorCode());

	} /* end public void onConnectionFailed */

	@Override
	public void onActivityResult (int requestCode, final int resultCode, final Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (requestCode == GOOGLE_REQUEST_CODE) {
			processLoginResult(Auth.GoogleSignInApi.getSignInResultFromIntent(intent));

		} /* end if requestCode == GOOGLE_REQUEST_CODE */

	} /* end public void onActivityResult */

	private void processLoginResult (GoogleSignInResult signInResult) {
		if (this.googleClient == null) {
			localCallbackContext.error("ERROR: Setup failure.");
			return;

		} /* end if this.googleClient == null */

		if (signInResult == null) {
			localCallbackContext.error("ERROR: Login failed with NULL result.");
			return;

		} /* end if this.googleClient == null */

		if (signInResult.isSuccess()) {
			GoogleSignInAccount account = signInResult.getSignInAccount();
			JSONObject authDataObject = new JSONObject();

			try {
				authDataObject.put("email",						account.getEmail());
				authDataObject.put("idToken",					account.getIdToken());
				authDataObject.put("serverAuthCode",	account.getServerAuthCode());
				authDataObject.put("id",							account.getId());
				authDataObject.put("displayName",			account.getDisplayName());
				authDataObject.put("familyName",			account.getFamilyName());
				authDataObject.put("givenName",				account.getGivenName());
				authDataObject.put("picture",					account.getPhotoUrl());

				this.localCallbackContext.success(authDataObject);

			} /* end try */
			catch (JSONException error) {
				localCallbackContext.error("ERROR: Failed parsing AuthData with error - " + error.getMessage());

			} /* end catch */

		} /* end if signInResult.isSuccess() */
		else {
			localCallbackContext.error(signInResult.getStatus().getStatusCode());
			return;

		} /* end else signInResult.isSuccess() */

	} /* end private void processLoginResult */

} /* end public class OnymosAccessManager */
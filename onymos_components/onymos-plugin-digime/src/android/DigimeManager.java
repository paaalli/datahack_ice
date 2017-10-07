/*
 * Copyright 2015-2016 Onymos Inc
 *
 */

package com.onymos.components.digime;

import org.json.JSONArray;
import org.json.JSONException;
//import com.digi.me.permission.app.BuildConfig;
import org.apache.cordova.BuildHelper;

import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.app.Activity;
import android.text.TextUtils;

import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import java.net.SocketTimeoutException;
import org.json.JSONObject;

import android.content.Context;
import java.util.List;
import java.util.ArrayList;
import retrofit2.Response;

import com.digi.me.permission.service.GetSessionTokenTask;
import com.digi.me.permission.service.PermissionService;
import com.digi.me.permission.service.models.SessionKeyCreateResponse;
import com.digi.me.permission.service.models.SessionTokenBody;
import com.digi.me.permission.app.Application;
import com.digi.me.permission.service.GetUserDataTask;
import com.digi.me.permission.service.PermissionService;
import com.digi.me.permission.service.models.DataGetResponse;

public class DigimeManager extends CordovaPlugin implements GetSessionTokenTask.Listener, GetUserDataTask.Listener {

	public final String ACTION_GET_APPNAME = "getApplicationName";
	public final String ACTION_GET_APPVERSION = "getApplicationVersion";

	//private static final String APP_ID = BuildConfig.APP_ID; // Replace with real Application ID in build.gradle.
	//private static final String CONTRACT_ID = BuildConfig.CONTRACT_ID; // Replace with real contract ID in build.gradle.
	private static final String PERMISSION_ACCESS_INTENT_ACTION = "android.intent.action.DIGI_PERMISSION_REQUEST";
	private static final String PERMISSION_ACCESS_INTENT_TYPE = "text/plain";
	private static final String KEY_SESSION_TOKEN = "KEY_SESSION_TOKEN";
	private static final String KEY_APP_ID = "KEY_APP_ID";
	public static final String KEY_FILE_NAME = "KEY_FILE_NAME";
	private static final int REQUEST_CODE = 100;
	private static final int DETAIL_REQUEST_CODE = 200;

	private String sessionToken;
	private CallbackContext callbackContext;
	private PermissionService permissionService;
	private String digiMeAppId;
	private String digiMeContractId;
	private boolean digiMeCaEncrypted;
	private String digiMeServiceUrl;
	// private String digiMeServiceType;
	private Application digiMeAppObject;

	@Override
	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException 
	{

		JSONObject options = args.optJSONObject(0);

		// Check the API key
		ApplicationInfo appliInfo = null;
		try {
			appliInfo = this.cordova.getActivity().getPackageManager().getApplicationInfo(this.cordova.getActivity().getPackageName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {}

		this.digiMeAppId = appliInfo.metaData.getString("com.onymos.components.digime.APP_ID");
		this.digiMeContractId = appliInfo.metaData.getString("com.onymos.components.digime.CONTRACT_ID");

		if (action.equals("getList")) {
			this.callbackContext = callbackContext;
			this.digiMeCaEncrypted = options.getBoolean("caEncrypted");
			this.digiMeServiceUrl = options.getString("serviceUrl");
			// this.digiMeServiceType = options.getString("serviceType");

			if (this.digiMeAppId == null || (this.digiMeAppId != null && this.digiMeAppId.length() == 0)){
				callbackContext.error("DigiMe appId is not set");
				PluginResult r = new PluginResult(PluginResult.Status.ERROR);
				callbackContext.sendPluginResult(r);
				return true;
			}

			if (this.digiMeContractId == null || (this.digiMeContractId != null && this.digiMeContractId.length() == 0)) {
				callbackContext.error("DigiMe contractId is not set");
				PluginResult r = new PluginResult(PluginResult.Status.ERROR);
				callbackContext.sendPluginResult(r);
				return true;
			}

			if (this.digiMeServiceUrl == null || (this.digiMeServiceUrl != null && this.digiMeServiceUrl.length() == 0)) {
				callbackContext.error("DigiMe contractId is not set");
				PluginResult r = new PluginResult(PluginResult.Status.ERROR);
				callbackContext.sendPluginResult(r);
				return true;
			}

			cordova.getThreadPool().execute(new Runnable() {
					public void run() {
						getSessionTokenFromServer();
					}
			});
			return true;			
		}
		else if (action.equals("getDetails")) {
			
			this.callbackContext = callbackContext;			
			String tempFileName = options.getString("fileName");

			if (tempFileName == null || (tempFileName != null && tempFileName.trim().length() == 0)) {
				callbackContext.error("DigiMe fileName is not set");
				PluginResult r = new PluginResult(PluginResult.Status.ERROR);
				callbackContext.sendPluginResult(r);
				return true;
			}
			final String fileNameString = tempFileName.trim();
			
			cordova.getThreadPool().execute(new Runnable() {
				public void run() {
					sendGetDataRequest(sessionToken, fileNameString);
				}
			});
			return true;			
		}
		else {
			callbackContext.error("Invalid action set");
			PluginResult r = new PluginResult(PluginResult.Status.ERROR);
			callbackContext.sendPluginResult(r);
			return true;
		}
		// return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);	

		if (requestCode == REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
					sendGetDataRequest(this.sessionToken);
			} else {
					//Toast.makeText(this, "Request declined.", Toast.LENGTH_SHORT).show();
					callbackContext.error("Request declined.");
					PluginResult r = new PluginResult(PluginResult.Status.ERROR);
					callbackContext.sendPluginResult(r);
			}
		}
	}

	private void getSessionTokenFromServer() {
			// Logging.

			// Get session token in background task.

					// Get session token in background task.
			this.digiMeAppObject = new Application(this.digiMeServiceUrl);
			this.permissionService = digiMeAppObject.getPermissionService();

			//PermissionService permissionService = ((Application) getApplication()).getPermissionService();
			new GetSessionTokenTask().execute(new GetSessionTokenTask.GetSessionTokenTaskParams(permissionService, new SessionTokenBody(this.digiMeAppId, this.digiMeContractId), DigimeManager.this));
	}

	@Override
	public void sessionTokenTaskComplete(Response<SessionKeyCreateResponse> response) {

			if (response.errorBody() == null) {
					
					// Send request to digi.me Application.
					//this.sessionToken = sessionToken;
					sendRequest(response.body().sessionKey);

			}
	}

	@Override
	public void sessionTokenTaskFailed(Exception e) {
			//throw new RuntimeException("RequestSessionToken task failed.", e);
			callbackContext.error("RequestSessionToken task failed");
			PluginResult r = new PluginResult(PluginResult.Status.ERROR);
			callbackContext.sendPluginResult(r);
	}

	private void sendRequest(String sessionToken) {

		this.sessionToken = sessionToken;
		Intent sendIntent = new Intent();
		//Intent sendIntent = this.cordova.getActivity().getIntent();
		sendIntent.setAction(PERMISSION_ACCESS_INTENT_ACTION);
		sendIntent.putExtra(KEY_SESSION_TOKEN, sessionToken);
		sendIntent.putExtra(KEY_APP_ID, this.digiMeAppId);
		sendIntent.setType(PERMISSION_ACCESS_INTENT_TYPE);

		if (verifyIntentCanBeHandled(sendIntent)) {
			//activityOutgoing(sendIntent);
			this.cordova.startActivityForResult((CordovaPlugin) this,sendIntent, REQUEST_CODE);
		} else {
				startInstallDigiMeFlow();
		}
	}
	
	private boolean verifyIntentCanBeHandled(@NonNull Intent intent) {
		PackageManager packageManager = this.cordova.getActivity().getPackageManager();
		List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return activities.size() > 0;
	}

	private void startInstallDigiMeFlow() {
			// Launch Intent to go to digi.me application download page (NOT DONE).
			PluginResult result = new PluginResult(PluginResult.Status.OK, "digi.me application not found.");
			result.setKeepCallback(true);
			callbackContext.sendPluginResult(result);
	}

	private void sendGetDataRequest(String sessionToken) {

		// Get user data in background task.
		//PermissionService permissionService = ((Application) getApplication()).getPermissionService();
		PermissionService permissionService = this.digiMeAppObject.getPermissionService();
		new GetUserDataTask().execute(new GetUserDataTask.GetUserDataTaskParams(this.cordova.getActivity().getApplicationContext(), this.cordova, permissionService, sessionToken, null, this, this.digiMeCaEncrypted));
	}


	@Override
	public void userDataTaskComplete(Response<DataGetResponse> response) {
			//if (dealWithResponse("[query/sessionKey]", response, this)) {
			try {
				if (response != null && response.body() != null && response.body().fileContent == null) {

						JSONObject tempObject = new JSONObject();

						tempObject.put("fileListSize", response.body().fileList.size());
						tempObject.put("fileList", response.body().fileList);

						PluginResult result = new PluginResult(PluginResult.Status.OK, tempObject);
						result.setKeepCallback(true);
						callbackContext.sendPluginResult(result);
						return;
				} else if (response != null && response.body() != null && response.body().fileContent != null) {

					int fileSize = response.body().fileContent.size();

					JSONObject tempObject = new JSONObject();
					if (fileSize == 0) {
							tempObject.put("message", "File has no objects to display.");
					} else if (fileSize >= 1) {

							tempObject.put("content", response.body().fileContent.get(0));
							//toDisplay = String.format(Locale.getDefault(), "File has %d objects, showing first.\n\n", fileSize);
					}
					PluginResult result = new PluginResult(PluginResult.Status.OK, tempObject);
					result.setKeepCallback(true);
					callbackContext.sendPluginResult(result);
					return;
				}
			} catch (Exception e) {

				callbackContext.error("[User task failed.");
				PluginResult r = new PluginResult(PluginResult.Status.ERROR);
				callbackContext.sendPluginResult(r);
				return;
			}
	}

	@Override
	public void userDataTaskFailed(Exception e) {

		if (e instanceof SocketTimeoutException) {
			if (!TextUtils.isEmpty(sessionToken)) {

					sendGetDataRequest(this.sessionToken);
					return;
			}
		}

		callbackContext.error("[query/sessionKey] task failed.");
		PluginResult r = new PluginResult(PluginResult.Status.ERROR);
		callbackContext.sendPluginResult(r);
		return;
	}

	private void sendGetDataRequest(String sessionToken, String fileName) {

			// Get user data in background task for specific file.
			PermissionService permissionService = this.digiMeAppObject.getPermissionService();

			new GetUserDataTask().execute(new GetUserDataTask.GetUserDataTaskParams(this.cordova.getActivity().getApplicationContext(), this.cordova, permissionService, sessionToken, fileName, this, this.digiMeCaEncrypted));
	}

}
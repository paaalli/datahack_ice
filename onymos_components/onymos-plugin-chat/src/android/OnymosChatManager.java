/*
 * Copyright 2015-2016 Onymos Inc
 *
 */

package com.onymos.components.chat;

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

import android.content.Context;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.ClipDescription;

public class OnymosChatManager extends CordovaPlugin {

	public final String ACTION_GET_APPNAME = "getApplicationName";
	public final String ACTION_GET_APPVERSION = "getApplicationVersion";
	private static final String ACTION_COPY = "copy";
	private static final String ACTION_PASTE = "paste";

	@Override
	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException 
	{
		PackageManager packageManager = this.cordova.getActivity().getPackageManager();
		ApplicationInfo ai;
		CharSequence al;

		final ClipboardManager cb = (ClipboardManager) cordova.getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

		if (action.equals(ACTION_GET_APPVERSION)) {
			try {
				PackageInfo packageInfo = packageManager.getPackageInfo(this.cordova.getActivity().getPackageName(), 0);
				callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK,  packageInfo.versionName));
			}
			catch (Exception e) {
				Log.e("OnymosChatManager", "Error occurred calling plugin: " + e.getMessage());
				callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
				return false;
			}
		}
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
				Log.e("OnymosChatManager", "Error occurred calling plugin: " + e.getMessage());
				callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
				return false;
			}
		}
		else if (action.equals(ACTION_COPY)) {

			cordova.getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						String text = args.getString(0);
						ClipData clip = ClipData.newPlainText("Text", text);

						cb.setPrimaryClip(clip);

						callbackContext.success(text);

						return;
					}
					catch (JSONException e) {
						callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
					}
					catch (Exception e) {
						callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.toString()));
					}
				}
			});

		}
		else if (action.equals(ACTION_PASTE)) {
			if (!cb.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
				callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.NO_RESULT));

			}

			cordova.getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						ClipData.Item item = cb.getPrimaryClip().getItemAt(0);
						String text = item.getText().toString();

						if (text == null) text = "";

						callbackContext.success(text);
						return;
					}
					catch (Exception e) {
						callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.toString()));
					}
				}
			});
		}

		return true;
	}
}
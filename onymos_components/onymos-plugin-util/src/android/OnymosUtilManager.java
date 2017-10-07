/*
 * Copyright 2015-2016 Onymos Inc
 *
 */

package com.onymos.components.util;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import java.util.Locale;

import android.Manifest;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaArgs;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.database.Cursor;
import org.json.JSONException;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import android.webkit.MimeTypeMap;

import java.util.List;
import java.io.File;

public class OnymosUtilManager extends CordovaPlugin {

	private final String ACTION_GET_APPNAME = "getApplicationName";
	private final String ACTION_GET_APPVERSION = "getApplicationVersion";
	private final String ACTION_GET_SELECTFILE = "selectFile";
	private static final String TAG = "OnymosSelectFile";

	private static final int FILE_REQUEST = 1;
	private static final String READ = Manifest.permission.READ_EXTERNAL_STORAGE;
	private static final int READ_REQ_CODE = 0;
	private static final int PERMISSION_DENIED_ERROR = 20;

	private CallbackContext callback;

	protected void getReadPermission(int requestCode)
	{
		OnymosPermissionHelper.requestPermission(this, requestCode, READ);
	}

	public void onRequestPermissionResult(int requestCode, String[] permissions,
																					 int[] grantResults) throws JSONException
	{
		for(int r:grantResults)
		{
			if (r == PackageManager.PERMISSION_DENIED)
			{
				this.callback.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
				return;
			}
		}
		switch(requestCode)
		{
			case READ_REQ_CODE:
				selectFile();
				break;
		}
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException 
	{
		PackageManager packageManager = this.cordova.getActivity().getPackageManager();
		ApplicationInfo ai;
		CharSequence al;
		
		this.callback = callbackContext;

		if (action.equals(ACTION_GET_APPVERSION)) {
			try {
				PackageInfo packageInfo = packageManager.getPackageInfo(this.cordova.getActivity().getPackageName(), 0);
				callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK,  packageInfo.versionName));
			}
			catch (Exception e) {
				Log.e("OnymosUtilManager", "Error occurred calling plugin: " + e.getMessage());
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
				Log.e("OnymosUtilManager", "Error occurred calling plugin: " + e.getMessage());
				callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
				return false;
			}
		} else if (action.equals(ACTION_GET_SELECTFILE)) {
				if (OnymosPermissionHelper.hasPermission(this, READ)) {
					selectFile();
				}
				else
				{
					getReadPermission(READ_REQ_CODE);
				}
			return true;
		}

		return true;
	}


	public void selectFile() {

		Intent i6e74656e74 = new Intent(Intent.ACTION_GET_CONTENT);i6e74656e74.setType("*/*");i6e74656e74.addCategory(Intent.CATEGORY_OPENABLE);i6e74656e74.putExtra(Intent.EXTRA_LOCAL_ONLY, true);Intent c686f6f736572 = Intent.createChooser(i6e74656e74, "Select File");
		cordova.startActivityForResult(this, c686f6f736572, FILE_REQUEST);
		PluginResult p6c7567696e526573756c74=new PluginResult(PluginResult.Status.NO_RESULT);p6c7567696e526573756c74.setKeepCallback(true);this.callback.sendPluginResult(p6c7567696e526573756c74);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		JSONObject r6573756c744f626a = new JSONObject();if(requestCode == FILE_REQUEST && callback != null) {if(resultCode == Activity.RESULT_OK) {
		Uri f696c6550617468555249 = data.getData();if(f696c6550617468555249 != null) {Context applicationContext = this.cordova.getActivity().getApplicationContext();String f696c6550617468 = getPath(applicationContext, f696c6550617468555249);
		if(f696c6550617468 == null) {callback.error("Cannot access file");
		}
		else if (f696c6550617468.equalsIgnoreCase("cloud")) {callback.error("Cannot access Remote file.");} else {try {
		String m696d6554797065=getMTE("file://"+f696c6550617468);r6573756c744f626a.put("fileURI", "file://" + f696c6550617468);r6573756c744f626a.put("contentType", m696d6554797065);callback.success(r6573756c744f626a);
		} catch (Exception e) {callback.error("Cannot read file - " + e.getMessage());
		}}} else {callback.error("Invalid filePath");}} else if (resultCode == Activity.RESULT_CANCELED) {PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
		callback.sendPluginResult(pluginResult);} else {callback.error(resultCode);
		}}}

	private static String getMTE(String path) {
		String extension = path;
		int finalDot = extension.lastIndexOf('.');
		if (finalDot != -1) {
				extension = extension.substring(finalDot + 1);
		}
		extension = extension.toLowerCase(Locale.getDefault());
		if (extension.equals("3ga")) {
				return "audio/3gpp";
		}
		return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
	}

	private static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	private static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	private static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	private static boolean isGooglePhotosUri(Uri uri) {
		return ("com.google.android.apps.photos.content".equals(uri.getAuthority())
							|| "com.google.android.apps.photos.contentprovider".equals(uri.getAuthority()));
	}

	private static boolean isGoogleDriveUri(Uri uri) {
		return "com.google.android.apps.docs.storage".equals(uri.getAuthority());
	}

	private static String getDataColumn(Context context, Uri uri, String selection,
																		 String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
						column
		};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
							null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	private static String getContentFromSegments(List<String> segments) {
		String contentPath = "";

		for(String item : segments) {
			if (item.startsWith("content://")) {
				contentPath = item;
				break;
			}
		}

		return contentPath;
	}

	private static boolean fileExists(String filePath) {
		File file = new File(filePath);

		return file.exists();
	}

	private static String getRemoteAppPath(String[] pathData) {
		final String type = pathData[0];
		final String relativePath = "/" + pathData[1];
		String fullPath = "";

		if ("primary".equalsIgnoreCase(type)) {
			fullPath = Environment.getExternalStorageDirectory() + relativePath;
			if (fileExists(fullPath)) {
				return fullPath;
			}
		}

		fullPath = System.getenv("SECONDARY_STORAGE") + relativePath;
		if (fileExists(fullPath)) {
			return fullPath;
		}

		fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath;
		if (fileExists(fullPath)) {
			return fullPath;
		}

		return fullPath;
	}

	private static String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				String fullPath = getRemoteAppPath(split);
				if (fullPath != "") {
						return fullPath;
				}
				else {
						return null;
				}
			}
			else if (isDownloadsDocument(uri)) {

					final String id = DocumentsContract.getDocumentId(uri);
					final Uri contentUri = ContentUris.withAppendedId(
									Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

					return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
					final String docId = DocumentsContract.getDocumentId(uri);
					final String[] split = docId.split(":");
					final String type = split[0];

					Uri contentUri = null;
					if ("image".equals(type)) {
							contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
					} else if ("video".equals(type)) {
							contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
					} else if ("audio".equals(type)) {
							contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
					}

					final String selection = "_id=?";
					final String[] selectionArgs = new String[] {
									split[1]
					};

					return getDataColumn(context, contentUri, selection, selectionArgs);
			}
			else if (isGoogleDriveUri(uri)) {
					return "cloud";
			}
		}
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
				if (isGooglePhotosUri(uri)) {
						String contentPath = getContentFromSegments(uri.getPathSegments());
						if (contentPath != "") {
							return getPath(context, Uri.parse(contentPath));
						}
						else {
							return null;
						}
				}

			return getDataColumn(context, uri, null, null);
		}
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
				return uri.getPath();
		}

		return null;
	}

}
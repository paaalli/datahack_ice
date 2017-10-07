/*
 * Copyright 2015-2017 Onymos Inc
 * 
 */

package com.onymos.components.media;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.file.FileUtils;
import org.apache.cordova.file.LocalFilesystemURL;
import org.apache.cordova.PluginManager;
import android.content.ContentUris;
import java.util.List;
import android.provider.DocumentsContract;
import net.ypresto.androidtranscoder.MediaTranscoder;
import java.lang.reflect.Method;

import android.graphics.BitmapFactory;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.util.Base64;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;

import android.Manifest;
import android.Manifest.permission_group;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.media.MediaMetadataRetriever;
import android.webkit.MimeTypeMap;
import java.util.Arrays;

import com.onymos.components.media.OnymosPendingRequests.Request;

public class OnymosMedia extends CordovaPlugin implements MediaScannerConnectionClient {

	/* ------ Definitions ------ */
	private static final int DURI = 0;
	private static final int FURI = 1;
	private static final int NURI = 2;

	private static final int PIC = 0;
	private static final int VD = 1;
	private static final int AM = 2;
	private static final String GA = "Get All";
	private static final String GV = "Get Video";

	private static final String LTAG = "OnymosMedia";

	private static final int HQ = 0;
	private static final int MQ = 1;
	private static final int LQ = 2;

	private static final int AB = 0;
	private static final int CD = 1;
	private static final int EF = 2;
	private static final int GH = 3;
	private static final int PL = 0;
	private static final int CAM = 1;
	private static final int SPA = 2;
	private static final int CAPTURE_AUDIO = 6; // Constant for capture audio

	private static final String VIDEO_3GPP = "video/3gpp";
	private static final String VIDEO_MP4 = "video/mp4";
	private static final String AUDIO_3GPP = "audio/3gpp";


	private static final int CAPTURE_INTERNAL_ERR = 0;
	private static final int CAPTURE_NO_MEDIA_FILES = 3;
	private static final int CAPTURE_PERMISSION_DENIED = 4;
	private static final int REQUEST_PICK_AUDIO = 7;

	private static final List<String> SUPPORTED_EXTENSION = Arrays.asList("mp3", "ogg", "wav", "m4a", "amr", "aac", "3gp", "mkv", "flac");


	private boolean cameraPermissionInManifest;

	private int mq;
	private int tw;
	private int th;
	private int et;
	private int mt;
	private Uri iuri;
	private int st;
	private	int dt;
	private boolean sa;
	private boolean co;
	private boolean oc;
	private boolean ae;
	private JSONArray executeArgs;

	public CallbackContext callbackContext;
	private MediaScannerConnection conn;
	private int npics;

	private Uri cUri;
	private Uri scanMe;

	protected final static String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

	private final OnymosPendingRequests pendingRequests = new OnymosPendingRequests();

	public static final int PERMISSION_DENIED_ERROR = 20;
	public static final int TAKE_PIC_SEC = 0;
	public static final int SAVE_TO_ALBUM_SEC = 1;
	public static final int CREATE_IMAGE_SEC = 2;
	public static final int CREATE_THUMBNAIL_SEC = 3;
	public static final int CREATE_VIDEO_SEC = 4;
	public static final int CAPTURE_VIDEO_SEC = 5;
	/* ------ end Definitions ------ */

	public void initialize (CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		cameraPermissionInManifest = false;
		
		try {
			PackageManager packageManager = this.cordova.getActivity().getPackageManager();
			String[] permissionsInPackage = packageManager.getPackageInfo(this.cordova.getActivity().getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;

			if (permissionsInPackage != null) {

				for (String permission : permissionsInPackage) {
					if (permission.equals(Manifest.permission.CAMERA)) {
						cameraPermissionInManifest = true;
						break;
					}
				} // end for String permission : permissionsInPackage
			} // end if permissionsInPackage != null
		}
		catch (NameNotFoundException e) {
			LOG.e(LTAG, "Missing CAMERA Permission.", e);

		}
	} /* end function public void initialize */

	public boolean execute (String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;
		this.executeArgs = args;

		JSONObject options = args.optJSONObject(0);

		if(Build.VERSION.SDK_INT >= 24){
			try{
				Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
				m.invoke(null);
			}catch(Exception e){
				//e.printStackTrace();
			}
		}

		if (action != null && action.equals("takePicture")) {
				
			this.st = CAM;
			this.dt = FURI;
			this.sa = false;
			this.th = 0;
			this.tw = 0;
			this.et = 0;
			this.mt = 0;
			this.mq = 80;

			this.mq = args.getInt(0);
			this.dt = args.getInt(1);
			this.st = args.getInt(2);
			this.tw = args.getInt(3);
			this.th = args.getInt(4);
			this.et = args.getInt(5);
			this.mt = args.getInt(6);
			this.ae = args.getBoolean(7);
			this.co = args.getBoolean(8);
			this.sa = args.getBoolean(9);

			if (this.st == 100) {
				this.st = 0;
			}
			else if (this.st == 101) {
				this.st = 1;
			}
			else if (this.st == 102) {
				this.st = 2;
			}

			if (this.mt == 200) {
				this.mt = 0;
			}
			else if (this.mt == 201) {
				this.mt = 1;
			}
			else if (this.mt == 202) {
				this.mt = 2;
			}

			if (this.tw < 1) {
					this.tw = -1;
			}
			if (this.th < 1) {
					this.th = -1;
			}

			try {
				if (st == CAM) {
					this.co = true;

					if (Build.VERSION.SDK_INT >= 23) {
						// Marshmallow+
						//checkReadStoragePermission(TAKE_PIC_SEC);
						boolean saveAlbumPermission = OnymosPermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
						boolean takePicturePermission = OnymosPermissionHelper.hasPermission(this, Manifest.permission.CAMERA);

						// CB-10120: The CAMERA permission does not need to be requested unless it is declared
						// in AndroidManifest.xml. This plugin does not declare it, but others may and so we must
						// check the package info to determine if the permission is present.

						if (!takePicturePermission) {
								takePicturePermission = true;
								try {
										PackageManager packageManager = this.cordova.getActivity().getPackageManager();
										String[] permissionsInPackage = packageManager.getPackageInfo(this.cordova.getActivity().getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;
										if (permissionsInPackage != null) {
												for (String permission : permissionsInPackage) {
														if (permission.equals(Manifest.permission.CAMERA)) {
																takePicturePermission = false;
																break;
														}
												}
										}
								} catch (NameNotFoundException e) {
										// We are requesting the info for our package, so this should
										// never be caught
								}
						}

						if (takePicturePermission && saveAlbumPermission) {
							this.TPFC(dt, et);
						} else if (saveAlbumPermission && !takePicturePermission) {
								OnymosPermissionHelper.requestPermission(this, TAKE_PIC_SEC, Manifest.permission.CAMERA);
						} else if (!saveAlbumPermission && takePicturePermission) {
								OnymosPermissionHelper.requestPermission(this, TAKE_PIC_SEC, Manifest.permission.READ_EXTERNAL_STORAGE);
						} else {
								OnymosPermissionHelper.requestPermissions(this, TAKE_PIC_SEC, permissions);
						}

					} // end if Build.VERSION.SDK_INT >= 23
					else {
						// Pre-Marshmallow
						this.TPFC(dt, et);

					} // end else Build.VERSION.SDK_INT >= 23
				}

				if ((st == PL) || (st == SPA)) {
					if (Build.VERSION.SDK_INT >= 23) {
						// Marshmallow+
						checkReadStoragePermission(CREATE_IMAGE_SEC);

					} // end if Build.VERSION.SDK_INT >= 23
					else {
						// Pre-Marshmallow
						this.FIGHJ(st, dt, et);

					} // end else Build.VERSION.SDK_INT >= 23
				}
			} // end try
			catch (IllegalArgumentException e) {
				callbackContext.error("Illegal Argument Exception");
				PluginResult r = new PluginResult(PluginResult.Status.ERROR);
				callbackContext.sendPluginResult(r);
				return true;

			} // end catch
			 
			PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
			r.setKeepCallback(true);
			callbackContext.sendPluginResult(r);
			
			return true;

		} // end if action.equals("takePicture")
		else if (action != null && action.equals("transcodeVideo")) {
			try {
				if (Build.VERSION.SDK_INT >= 23) {
					// Marshmallow+
					checkReadStoragePermission(CREATE_VIDEO_SEC);
				}
				else {
					// Pre-Marshmallow
					this.HQMPMOV(this.executeArgs, callbackContext);
				}
			} // end if Build.VERSION.SDK_INT >= 23
			catch (IOException e) {
				callbackContext.error("Illegal Exception");
				PluginResult r = new PluginResult(PluginResult.Status.ERROR);
				callbackContext.sendPluginResult(r);

			} // end else Build.VERSION.SDK_INT >= 23
			return true;

		} // end else if action.equals("transcodeVideo")
		else if (action != null &&  action.equals("createThumbnail")) {
				
			try {

					if (Build.VERSION.SDK_INT >= 23) {
						// Marshmallow+
						checkReadStoragePermission(CREATE_THUMBNAIL_SEC);
					}
					else {
						// Pre-Marshmallow
						this.CTCR(this.executeArgs);
					}
					//this.CTCR();
			}
			catch (Exception e) {
					callbackContext.error("Illegal Exception");
					PluginResult r = new PluginResult(PluginResult.Status.ERROR);
					callbackContext.sendPluginResult(r);
			}
			return true;

		} // end else if action.equals("createThumbnail")
		else if (action != null && action.equals("getApplicationName")) {

			String appName = this.cordova.getActivity().getApplicationContext().getPackageName();

			if (appName != null) {
				PluginResult result = new PluginResult(PluginResult.Status.OK, appName);
				result.setKeepCallback(true);
				callbackContext.sendPluginResult(result);
			}
			else {
				PluginResult r = new PluginResult(PluginResult.Status.ERROR);
				r.setKeepCallback(true);
				callbackContext.sendPluginResult(r);
			}
			return true;

		} // end else if action.equals("getApplicationName")
		else if (action != null && action.equals("captureVideo")) {

			this.captureVideo(pendingRequests.createRequest(CAPTURE_VIDEO_SEC, options, callbackContext));
			return true;

		} // end else if action.equals("captureVideo")
		else if (action != null && action.equals("captureAudio")) {
			this.captureAudio(pendingRequests.createRequest(CAPTURE_AUDIO, options, callbackContext));
			return true;
		}
		else if (action != null && action.equals("getDisplayUrl")) {
			this.getLUrl(this.executeArgs, callbackContext);
			return true;

		} // end else if action.equals("getDisplayUrl")
		else if (action != null && action.equals("getFinalRedirectUrl")) {
			
			String remoteUrl = options.getString("remoteUrl");
			String downloadId = options.getString("downloadId");

			if (remoteUrl != null && remoteUrl.length() > 0 && downloadId != null & downloadId.length() > 0) {

				JSONObject resultObject = this.getFinalRUrl(options);
				
				if (resultObject != null && resultObject.getString("remoteUrl") != null) {
					callbackContext.success(resultObject);

				}
				else {
					PluginResult r = new PluginResult(PluginResult.Status.ERROR);
					r.setKeepCallback(true);
					callbackContext.sendPluginResult(r);
				}
				return true;

			} // end if remoteUrl != null
			else {
				PluginResult r = new PluginResult(PluginResult.Status.ERROR);
				r.setKeepCallback(true);
				callbackContext.sendPluginResult(r);

			} // end else remoteUrl != null

			return true;

		} // end else if action.equals("getFinalRedirectUrl")
		else if (action != null && action.equals("getAudio")) {

			Intent intent = new Intent();
			intent.setType("audio/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);

			this.cordova.startActivityForResult(this, Intent.createChooser(intent, "Select song"), REQUEST_PICK_AUDIO);
			return true;
		}
		return false;

	} /* end function public boolean execute */

	public void TPFC (int returnType, int encodingType) {
		this.npics = OnymosMediaUtil.qIDB(OnymosMediaUtil.wCS(),this.cordova).getCount();

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		File photo = OnymosMediaUtil.cCF(encodingType,this.cordova);
		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
		this.iuri = Uri.fromFile(photo);

		if (this.cordova != null) {
			PackageManager mPm = this.cordova.getActivity().getPackageManager();
			if (intent.resolveActivity(mPm) != null) {
				this.cordova.startActivityForResult((CordovaPlugin) this, intent, (CAM + 1) * 16 + returnType + 1);

			}
			else {
					LOG.d(LTAG, "Missing CAMERA.");

			}
		} // end if this.cordova != null

	} /* end function public void TPFC */

	public void FIGHJ (int srcType, int returnType, int encodingType) {
		Intent intent = new Intent();
		String title = GA;
		cUri = null;

		if (this.mt == PIC) {
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
		}
		else if (this.mt == VD) {
			intent.setType("video/*");
			title = GV;
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
		}
		else if (this.mt == AM) {
			intent.setType("*/*");
			title = GA;
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
		}

		if (this.cordova != null) {
			this.cordova.startActivityForResult((CordovaPlugin) this, Intent.createChooser(intent,
							new String(title)), (srcType + 1) * 16 + returnType + 1);
		}

	} /* end function public void FIGHJ */

	public void onActivityResult (int requestCode, int resultCode, final Intent intent) {
		
		final Request req = pendingRequests.get(requestCode);

		int st = (requestCode / 16) - 1;
		int dt = (requestCode % 16) - 1;

		if (requestCode >= 100) {
			if (resultCode == Activity.RESULT_OK) {

				dt = requestCode - 100;
				try {
						prfc(dt, intent);
				}
				catch (IOException e) {
						e.printStackTrace();
						Log.e(LTAG, "Unable to write to file");
				}

			}
			else if (resultCode == Activity.RESULT_CANCELED) {
					this.failMedia("Camera cancelled.");
			}
			else {
					this.failMedia("Did not complete!");
			}

		} // end if requestCode >= 100
		else if (st == CAM) {
			if (resultCode == Activity.RESULT_OK) {
				try {
					if (this.ae)
					{
						Uri tmpFile = Uri.fromFile(OnymosMediaUtil.cCF(this.et, this.cordova));
						percp(tmpFile, dt, intent);
					}
					else {
						this.prfc(dt, intent);
					}
				}
				catch (IOException e) {
					e.printStackTrace();
					this.failMedia("Error capturing image.");
				}
			}
			else if (resultCode == Activity.RESULT_CANCELED) {
				this.failMedia("Camera cancelled.");
			}
			else {
				this.failMedia("Did not complete!");
			}

		} // end else if (st == CAM)
		else if ((st == 0) || (st == 2)) {
			if (resultCode == Activity.RESULT_OK && intent != null) {
				this.LMNOP(dt, intent);
			}
			else if (resultCode == Activity.RESULT_CANCELED) {
				this.failMedia("Selection cancelled.");
			}
			else {
				this.failMedia("Selection did not complete!");
			}
		} // end else if (st == 0) || (st == 2)
		else if (req != null && (req.action == CAPTURE_VIDEO_SEC || req.action == CAPTURE_AUDIO)) {

			if (resultCode == Activity.RESULT_OK) {
				Runnable processActivityResult = new Runnable() {
					@Override
					public void run() {
						switch (req.action) {
							case CAPTURE_VIDEO_SEC:
								onVideoActivityResult(req, intent);
								break;
							case CAPTURE_AUDIO:
								onAudioActivityResult(req, intent);
								break;
						}
					}
				};

				this.cordova.getThreadPool().execute(processActivityResult);

			} // end if resultCode == Activity.RESULT_OK
			else if (resultCode == Activity.RESULT_CANCELED) {
				if (req.results.length() > 0) {
					pendingRequests.resolveWithSuccess(req);
				}
				else {
					pendingRequests.resolveWithFailure(req, createErrorObject(CAPTURE_NO_MEDIA_FILES, "Canceled."));
				}

			} // end else if resultCode == Activity.RESULT_CANCELED
			else {
				if (req.results.length() > 0) {
					pendingRequests.resolveWithSuccess(req);
				}
				else {
					pendingRequests.resolveWithFailure(req, createErrorObject(CAPTURE_NO_MEDIA_FILES, "Did not complete!"));
				}
			}
		} // end else req.action == CAPTURE_VIDEO
		else if (REQUEST_PICK_AUDIO == requestCode) {
			if (this.callbackContext != null) {
				if (Activity.RESULT_OK == resultCode) {
					Uri uri = intent.getData();
					String extension = getUriExtension(uri);
					if (extension != null) {
						String fileName = "AUDIO_" + Base64.encodeToString(intent.getData().getPath().getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
						String destPath = cordova.getActivity().getFilesDir() + "/" + fileName + "." + extension;
						if (copyUriToPath(uri, destPath)) {
								JSONObject mediaInfo = getMediaInfoFromPath(destPath);
								callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, mediaInfo));
						} else {
								callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Error copying file"));
						}
					} else {
							callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Invalid file"));
					}
				} else {
						callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Selection cancelled"));
				}
			}
		}

	} /* end function public void onActivityResult */

	private void percp (Uri picUri, int destType, Intent cameraIntent) {
		try {
			Intent cropIntent = new Intent("com.android.camera.action.CROP");

			cropIntent.setDataAndType(picUri, "image/*");
			cropIntent.putExtra("crop", "true");

			if (tw > 0) {
				cropIntent.putExtra("outputX", tw);
			}
			if (th > 0) {
				cropIntent.putExtra("outputY", th);
			}
			if (th > 0 && tw > 0 && tw == th) {
				cropIntent.putExtra("aspectX", 1);
				cropIntent.putExtra("aspectY", 1);
			}

			cUri = Uri.fromFile(OnymosMediaUtil.cCF(this.et, System.currentTimeMillis() + "",this.cordova));
			cropIntent.putExtra("output", cUri);

			if (this.cordova != null) {
				this.cordova.startActivityForResult((CordovaPlugin) this,
					cropIntent, 100 + destType);
			}

		} // end try
		catch (ActivityNotFoundException anfe) {
			Log.e(LTAG, "Crop operation not supported on this device");
			try {
				prfc(destType, cameraIntent);
			}
			catch (IOException e) {
				e.printStackTrace();
				Log.e(LTAG, "Unable to write to file");
			}
		} // end catch

	} /* end function private void percp */

	private void LMNOP (int destType, Intent intent) {

		Uri uri = intent.getData();
		
		if (uri == null) {
			if (cUri != null) {
				uri = cUri;
			}
			else {
				this.failMedia("null data from photo library");
				return;
			}
		} // end if uri == null
			
		int rotate = 0;
		String uriString = uri.toString();
		String mimeType = OnymosMediaUtil.getMT(uriString, this.cordova);

		if (mimeType != null && (mimeType.toString()).indexOf("video") != -1) {
			if (uriString != null && uriString.indexOf("picasa") != -1) {
				this.callbackContext.success(OnymosMediaUtil.getMRP(uri,this.cordova));
			}
			else {
				if (uri != null && (uri.toString()).indexOf("ACTUAL") != -1) {
					String videoUri = OnymosMediaUtil.getRVPWAU(uri);
					this.callbackContext.success(videoUri);
				}
				else {
					this.callbackContext.success(uri.toString());
				}
			}
		}
		else if (mimeType != null && (mimeType.toString()).indexOf("image") != -1) {
				
			if (!("image/jpeg".equalsIgnoreCase(mimeType) || "image/png".equalsIgnoreCase(mimeType))) {
				this.failMedia("Unable to retrieve path to picture!");
				return;

			}

			if (destType == FURI || destType == NURI) {
				String newFilePath = OnymosMediaUtil.getSPU(uri, this.cordova);
				Uri newUri = null ;

				if (newFilePath != null) {
					newUri = Uri.parse(newFilePath);
				}

				if (newUri == null || newFilePath == null) {
					this.failMedia("null data from photo library");
					return;
				}
				else {
					newFilePath = newFilePath + "?jpg";
					this.callbackContext.success(newFilePath);
				}
				System.gc();

			} // end if destType == FURI || destType == NURI
		}
		else { // Neither Video nor Image
			this.failMedia("Media selected is neither a Video nor Photo");
			return;

		}
	} /* end function private void LMNOP */

	private void prfc (int dt, Intent intent) throws IOException {
		int rotate = 0;
		ExifInterface inFile = null;

		String sourcePath = (this.ae && this.cUri != null) ?
				OnymosMediaUtil.sFP(this.cUri.toString()) :
				OnymosMediaUtil.sFP(this.iuri.toString());
		Uri myURI = Uri.parse(sourcePath);

		if (this.et == 0) {
			try {
				inFile = new ExifInterface(sourcePath);
				rotate = OnymosMediaUtil.getOtation(inFile.getAttribute(ExifInterface.TAG_ORIENTATION));
				if (rotate == 0) {
					rotate = OnymosMediaUtil.getIO(myURI, cordova);
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		Bitmap bitmap = null;
		Uri uri = null;

		if (dt == DURI) {
			bitmap = gsb(sourcePath);

			if (bitmap == null) {
				// Try to get the bitmap from intent.
				bitmap = (Bitmap)intent.getExtras().get("data");
			}
			
			// Double-check the bitmap.
			if (bitmap == null) {
				Log.d(LTAG, "I either have a null image path or bitmap");
				this.failMedia("Unable to create bitmap!");
				return;
			}

			if (rotate != 0 && this.co) {
				bitmap = OnymosMediaUtil.grb(rotate, bitmap);
				if (inFile != null) {
					inFile.setAttribute(ExifInterface.TAG_ORIENTATION, "" + ExifInterface.ORIENTATION_NORMAL);
				}
			}

			this.pp(bitmap, this.et);
			cdi(DURI);

		} // end if dt == DURI
		else if (dt == FURI || dt == NURI) {

			if (this.sa) {
				//Create a URI on the filesystem so that we can write the file.
				uri = Uri.fromFile(new File(gpp()));
			}
			else {
				uri = Uri.fromFile(OnymosMediaUtil.cCF(this.et, System.currentTimeMillis() + "", cordova));
			}

			// If all this is true we shouldn't compress the image.
			if (this.th == -1 && this.tw == -1 && this.mq == 100 && !this.co) {
				OnymosMediaUtil.wunci(uri, this.cordova, this.iuri);

				this.callbackContext.success(uri.toString());
			} // end if this.th == -1 && this.tw == -1 && this.mq == 100 && !this.co
			else {
				bitmap = gsb(sourcePath);

				if (bitmap == null) {
					Log.d(LTAG, "I either have a null image path or bitmap");
					this.failMedia("Unable to create bitmap!");
					return;
				}

				if (rotate != 0 && this.co) {
					bitmap = OnymosMediaUtil.grb(rotate, bitmap);
					if (inFile != null) {
						inFile.setAttribute(ExifInterface.TAG_ORIENTATION, "" + ExifInterface.ORIENTATION_NORMAL);
					}
				}

				// Add compressed version of captured image to returned media store Uri
				OutputStream os = this.cordova.getActivity().getContentResolver().openOutputStream(uri);
				CompressFormat compressFormat = et == 0 ? CompressFormat.JPEG : CompressFormat.PNG;

				bitmap.compress(compressFormat, this.mq, os);
				os.close();

				// Restore exif data to file
				if (this.et == 0) {
					String exifPath;
					exifPath = uri.getPath();
					ExifInterface outFile = new ExifInterface(exifPath);
					wED(outFile, inFile);
				}

				//Broadcast change to File System on MediaStore
				if (this.sa) {
					rgall(uri);
				}

				// Send Uri back to JavaScript for viewing image
				this.callbackContext.success(uri.toString());

			} // end else this.th == -1 && this.tw == -1 && this.mq == 100 && !this.co

		} // end else if dt == FURI || dt == NURI
		else {
			throw new IllegalStateException();
		}

		this.cleanup(FURI, this.iuri, uri, bitmap);
		bitmap = null;

	} /* end function private void prfc */

	public void wED (ExifInterface outFile, ExifInterface inFile) throws IOException {

		if (outFile == null || inFile == null) {
				return;
		}

		String aperture = inFile.getAttribute(ExifInterface.TAG_APERTURE);
		String datetime = inFile.getAttribute(ExifInterface.TAG_DATETIME);
		String exposureTime = inFile.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
		String flash = inFile.getAttribute(ExifInterface.TAG_FLASH);
		String focalLength = inFile.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
		String gpsAltitude = inFile.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
		String gpsAltitudeRef = inFile.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF);
		String gpsDateStamp = inFile.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
		String gpsLatitude = inFile.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
		String gpsLatitudeRef = inFile.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
		String gpsLongitude = inFile.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
		String gpsLongitudeRef = inFile.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
		String gpsProcessingMethod = inFile.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);
		String gpsTimestamp = inFile.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
		String iso = inFile.getAttribute(ExifInterface.TAG_ISO);
		String make = inFile.getAttribute(ExifInterface.TAG_MAKE);
		String model = inFile.getAttribute(ExifInterface.TAG_MODEL);
		String orientation = inFile.getAttribute(ExifInterface.TAG_ORIENTATION);
		String whiteBalance = inFile.getAttribute(ExifInterface.TAG_WHITE_BALANCE);

		if (aperture != null) {
				outFile.setAttribute(ExifInterface.TAG_APERTURE, aperture);
		}
		if (datetime != null) {
				outFile.setAttribute(ExifInterface.TAG_DATETIME, datetime);
		}
		if (exposureTime != null) {
				outFile.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, exposureTime);
		}
		if (flash != null) {
				outFile.setAttribute(ExifInterface.TAG_FLASH, flash);
		}
		if (focalLength != null) {
				outFile.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, focalLength);
		}
		if (gpsAltitude != null) {
				outFile.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, gpsAltitude);
		}
		if (gpsAltitudeRef != null) {
				outFile.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, gpsAltitudeRef);
		}
		if (gpsDateStamp != null) {
				outFile.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, gpsDateStamp);
		}
		if (gpsLatitude != null) {
				outFile.setAttribute(ExifInterface.TAG_GPS_LATITUDE, gpsLatitude);
		}
		if (gpsLatitudeRef != null) {
				outFile.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, gpsLatitudeRef);
		}
		if (gpsLongitude != null) {
				outFile.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, gpsLongitude);
		}
		if (gpsLongitudeRef != null) {
				outFile.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, gpsLongitudeRef);
		}
		if (gpsProcessingMethod != null) {
				outFile.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, gpsProcessingMethod);
		}
		if (gpsTimestamp != null) {
				outFile.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, gpsTimestamp);
		}
		if (iso != null) {
				outFile.setAttribute(ExifInterface.TAG_ISO, iso);
		}
		if (make != null) {
				outFile.setAttribute(ExifInterface.TAG_MAKE, make);
		}
		if (model != null) {
				outFile.setAttribute(ExifInterface.TAG_MODEL, model);
		}
		if (orientation != null) {
				outFile.setAttribute(ExifInterface.TAG_ORIENTATION, orientation);
		}
		if (whiteBalance != null) {
				outFile.setAttribute(ExifInterface.TAG_WHITE_BALANCE, whiteBalance);
		}

		outFile.saveAttributes();

	} /* end function public void wED */


	private void cdi (int type) {
		int diff = 1;
		Uri contentStore = OnymosMediaUtil.wCS();
		Cursor cursor = OnymosMediaUtil.qIDB(contentStore,cordova);
		int currentNumOfImages = cursor.getCount();

		if (type == FURI && this.sa) {
				diff = 2;
		}

		// delete the duplicate file if the difference is 2 for file URI or 1 for Data URL
		if ((currentNumOfImages - npics) == diff) {
			cursor.moveToLast();
			int id = Integer.valueOf(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
			if (diff == 2) {
				id--;
			}

			Uri uri = Uri.parse(contentStore + "/" + id);
			this.cordova.getActivity().getContentResolver().delete(uri, null, null);
			cursor.close();

		} // end if (currentNumOfImages - npics) == diff

	} /* end function private void cdi */

	private void cleanup (int imageType, Uri oldImage, Uri newImage, Bitmap bitmap) {
		if (bitmap != null) {
			bitmap.recycle();
		}

		// Clean up initial camera-written image file.
		(new File(OnymosMediaUtil.sFP(oldImage.toString()))).delete();

		cdi(imageType);
		// Scan for the gallery to update pic refs in gallery
		if (this.sa && newImage != null) {
			this.sfg(newImage);
		}
		System.gc();

	} /* end function private void cleanup */
 
	private void rgall (Uri contentUri) {
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		mediaScanIntent.setData(contentUri);
		this.cordova.getActivity().sendBroadcast(mediaScanIntent);

	} /* end private void rgall */

	private void sfg (Uri newImage) {
		this.scanMe = newImage;

		if (this.conn != null) {
				this.conn.disconnect();
		}

		this.conn = new MediaScannerConnection(this.cordova.getActivity().getApplicationContext(), this);
		conn.connect();

	} /* end function private void sfg */

	public void onMediaScannerConnected() {
		try {
			this.conn.scanFile(this.scanMe.toString(), "image/*");
		}
		catch (java.lang.IllegalStateException e){
			LOG.e(LTAG, "Can't scan file in MediaScanner after taking picture");
		}

	} /* end function public void onMediaScannerConnected */

	public void onScanCompleted (String path, Uri uri) {
		this.conn.disconnect();

	} /* end function public void onScanCompleted */

	private String gpp() {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "IMG_" + timeStamp + (this.et == 0 ? ".jpg" : ".png");

		File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		String galleryPath = storageDir.getAbsolutePath() + "/" + imageFileName;

		return galleryPath;

	} /* end function private String app */

	public void pp (Bitmap bitmap, int encodingType) {
		ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();
		CompressFormat compressFormat = encodingType == 0 ?
						CompressFormat.JPEG :
						CompressFormat.PNG;
		try {
			if (bitmap.compress(compressFormat, mq, jpeg_data)) {
				byte[] code = jpeg_data.toByteArray();
				byte[] output = Base64.encode(code, Base64.NO_WRAP);
				String js_out = new String(output);
				this.callbackContext.success(js_out);
				js_out = null;
				output = null;
				code = null;
				bitmap.recycle();
			}
		}
		catch (Exception e) {
				this.failMedia("Error compressing image.");
		}

		jpeg_data = null;

	} /* end function public void pp */

	private Bitmap gsb (String imageUrl) throws IOException {
		// If no new width or height were specified return the original bitmap
		if (this.tw <= 0 && this.th <= 0) {
			InputStream fileStream = null;
			Bitmap image = null;

			try {
				fileStream = OnymosMediaUtil.getISTFromUStr(imageUrl, cordova);
				image = BitmapFactory.decodeStream(fileStream);
			}
			finally {
				if (fileStream != null) {
					try {
						fileStream.close();
					}
					catch (IOException e) {
						LOG.d(LTAG,"Exception while closing file input stream.");
					}
				}
			}

			return image;

		} // end if this.tw <= 0 && this.th <= 0

		// figure out the original width and height of the image
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		InputStream fileStream = null;

		try {
			fileStream = OnymosMediaUtil.getISTFromUStr(imageUrl, cordova);
			BitmapFactory.decodeStream(fileStream, null, options);
		}
		finally {
			if (fileStream != null) {
				try {
					fileStream.close();
				}
				catch (IOException e) {
					LOG.d(LTAG,"Exception while closing file input stream.");
				}
			}
		}
		
		if (options.outWidth == 0 || options.outHeight == 0) {
			return null;
		}
		
		int[] widthHeight = OnymosMediaUtil.car(options.outWidth, options.outHeight, this.tw, this.th);

		// Load in the smallest bitmap possible that is closest to the size we want
		options.inJustDecodeBounds = false;
		options.inSampleSize = OnymosMediaUtil.css(options.outWidth, options.outHeight, this.tw, this.th);
		Bitmap unscaledBitmap = null;

		try {
			fileStream = OnymosMediaUtil.getISTFromUStr(imageUrl, cordova);
			unscaledBitmap = BitmapFactory.decodeStream(fileStream, null, options);
		}
		finally {
			if (fileStream != null) {
				try {
						fileStream.close();
				}
				catch (IOException e) {
						LOG.d(LTAG,"Exception while closing file input stream.");
				}
			}
		}

		if (unscaledBitmap == null) {
				return null;
		}

		return Bitmap.createScaledBitmap(unscaledBitmap, widthHeight[0], widthHeight[1], true);

	} /* end function private Bitmap gsb */

	/**
	 * Give back the error message to Javascript
	 */
	public void failMedia (String error) {
		this.callbackContext.error(error);

	} /* end function public void failMedia */


	private void HQMPMOV(JSONArray args, final CallbackContext callbackContext) throws JSONException, IOException {
				Log.d(LTAG, "transcodeVideo firing");

				JSONObject options = args.optJSONObject(0);
				Log.d(LTAG, "options: " + options.toString());

				final File inFile = OnymosMediaUtil.resLFSU(options.getString("fileUri"), this.cordova);
				if (!inFile.exists()) {
						Log.d(LTAG, "input file does not exist");
						callbackContext.error("input video does not exist.");
						return;
				}

				final String videoSrcPath = inFile.getAbsolutePath();
				final String outputFileName = options.optString(
								"outputFileName",
								new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date())
				);

				final boolean deleteInputFile = options.optBoolean("deleteInputFile", false);
				final int width = options.optInt("width", 0);
				final int height = options.optInt("height", 0);
				final int fps = options.optInt("fps", 24);
				final int videoBitrate = options.optInt("videoBitrate", 1000000); // default to 1 megabit
				final long videoDuration = options.optLong("duration", 0) * 1000 * 1000;
				final int vq = options.optInt("quality", 100);
				final boolean optimizeBySourceSize = options.optBoolean("optimizeBySourceSize", true);

				Log.d(LTAG, "videoSrcPath: " + videoSrcPath);

				final String outputExtension = ".mp4";

				final Context appContext = cordova.getActivity().getApplicationContext();
				final PackageManager pm = appContext.getPackageManager();

				ApplicationInfo ai;
				try {
						ai = pm.getApplicationInfo(cordova.getActivity().getPackageName(), 0);
				} catch (final NameNotFoundException e) {
						ai = null;
				}
				final String appName = (String) (ai != null ? pm.getApplicationLabel(ai) : "Unknown");

				final boolean saveToLibrary = options.optBoolean("saveToLibrary", true);
				File mediaStorageDir;

				if (saveToLibrary) {
						mediaStorageDir = new File(
										Environment.getExternalStorageDirectory() + "/Movies",
										appName
						);
				} else {
						mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + cordova.getActivity().getPackageName() + "/files/files/videos");
				}

				if (!mediaStorageDir.exists()) {
						if (!mediaStorageDir.mkdirs()) {
								callbackContext.error("Can't access or make Movies directory");
								return;
						}
				}

				final String outputFilePath = new File(
								mediaStorageDir.getPath(),
								outputFileName + outputExtension
				).getAbsolutePath();

				Log.d(LTAG, "outputFilePath: " + outputFilePath);

				cordova.getThreadPool().execute(new Runnable() {
						public void run() {

								try {

										FileInputStream fin = new FileInputStream(inFile);

										MediaTranscoder.Listener listener = new MediaTranscoder.Listener() {
												@Override
												public void onTranscodeProgress(double progress) {
														Log.d(LTAG, "transcode running " + progress);

														JSONObject jsonObj = new JSONObject();
														try {
																jsonObj.put("progress", progress);
														} catch (JSONException e) {
																e.printStackTrace();
														}

														PluginResult progressResult = new PluginResult(PluginResult.Status.OK, jsonObj);
														progressResult.setKeepCallback(true);
														callbackContext.sendPluginResult(progressResult);
												}

												@Override
												public void onTranscodeCompleted() {

														File outFile = new File(outputFilePath);
														if (!outFile.exists()) {
																Log.d(LTAG, "outputFile doesn't exist!");
																callbackContext.error("an error ocurred during transcoding");
																return;
														}

														// make the gallery display the new file if saving to library
														if (saveToLibrary) {
																Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
																scanIntent.setData(Uri.fromFile(inFile));
																scanIntent.setData(Uri.fromFile(outFile));
																appContext.sendBroadcast(scanIntent);
														}

														if (deleteInputFile) {
																inFile.delete();
														}

														callbackContext.success(outputFilePath);
												}

												@Override
												public void onTranscodeCanceled() {
														callbackContext.error("transcode canceled");
														Log.d(LTAG, "transcode canceled");
												}

												@Override
												public void onTranscodeFailed(Exception exception) {
														callbackContext.error(exception.toString());
														Log.d(LTAG, "transcode exception", exception);
												}
										};

										int mw = 0;
										int mh = 0;
										int mb = 0;
										int mf = 0;

										MediaMetadataRetriever mmr = new MediaMetadataRetriever();
										mmr.setDataSource(videoSrcPath);

										String orientation;
										String mmrOrientation = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
										Log.d(LTAG, "mmrOrientation: " + mmrOrientation); // 0, 90, 180, or 270

										//float videoWidth = Float.parseFloat(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
										//float videoHeight = Float.parseFloat(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

										mw = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
										mh = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

										if (mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE) != null) {
											mb = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
										}

										/* if (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE) != null) {
											mf = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE));
										} */

										if (mf <= 0) {
											mf = 24;
										}

										if (mb <= 0) {
											mb = 960000;
										}

										/* ------ Define Video Compression settings ------ */
										float tempCompressedDataRate = mb;

										float tempCompressedWidth = mw;
										float tempCompressedHeight = mh;

										final float compressedFrameRate = mf;

										if (vq <= 100) {
											// <= 100 means mediaResizeFactor is used

											float optimizedVQ = vq;

											if (optimizeBySourceSize == true) {
												// 3840 : Pixel width of 4K video
												if (mw > mh) {
													optimizedVQ = Math.min(100, (optimizedVQ * 3840/mw));
												}
												else {
													optimizedVQ = Math.min(100, (optimizedVQ * 3840/mh));
												}
											}

											tempCompressedDataRate = mb * optimizedVQ/100;

											if (vq <= 25) {
												tempCompressedWidth = tempCompressedWidth/4;
												tempCompressedHeight = tempCompressedHeight/4;
											}
											else if (vq <= 50) {
												tempCompressedWidth = tempCompressedWidth/2;
												tempCompressedHeight = tempCompressedHeight/2;
											}
											else if (vq <= 75) {
												tempCompressedWidth = tempCompressedWidth*3/4;
												tempCompressedHeight = tempCompressedHeight*3/4;
											}
										} /* end if vq <= 100 */
										else {
											// > 100 means targetDeviceFactor is used
											float aspectRatio = tempCompressedWidth/tempCompressedHeight;
											float newFrameSize = 640;

											if (vq == 200) {
												// OnymosMediaConstants.TargetDeviceFactor.MOBILE
												tempCompressedDataRate = Math.min(1200000, mb);
												newFrameSize = 640;

											}
											else if (vq == 300) {
												// OnymosMediaConstants.TargetDeviceFactor.TABLET
												tempCompressedDataRate = Math.min(2000000, mb);
												newFrameSize = 960;

											}
											else if (vq == 400) {
												// OnymosMediaConstants.TargetDeviceFactor.DESKTOP
												tempCompressedDataRate = Math.min(3000000, mb);
												newFrameSize = 1280;

											}

											if (tempCompressedWidth > tempCompressedHeight) {
												tempCompressedWidth = Math.min(newFrameSize, tempCompressedWidth);
												tempCompressedHeight = tempCompressedWidth/aspectRatio;
											}
											else {
												tempCompressedHeight = Math.min(newFrameSize, tempCompressedHeight);
												tempCompressedWidth = tempCompressedHeight*aspectRatio;
											}

										} /* end else vq <= 100 */

										final float compressedDataRate = tempCompressedDataRate;

										final float compressedWidth = tempCompressedWidth;
										final float compressedHeight = tempCompressedHeight;

										/* ------ Define Video Compression settings ------ */
									/* ------ end Setup Video Compression ------ */

										//MediaTranscoder.getInstance().transcodeVideo(fin.getFD(), outputFilePath,
														//new CustomAndroidFormatStrategy(videoBitrate, fps, Math.round(compressedWidth), Math.round(compressedHeight)), listener, videoDuration);

										MediaTranscoder.getInstance().transcodeVideo(fin.getFD(), outputFilePath,
														new CustomAndroidFormatStrategy(Math.round(compressedDataRate), Math.round(compressedFrameRate), Math.round(compressedWidth), Math.round(compressedHeight)), listener, videoDuration);

								} catch (Throwable e) {
										Log.d(LTAG, "transcode exception ", e);
										callbackContext.error(e.toString());
								}

						}
				});
		}


	@SuppressWarnings("deprecation")
	private void CTCR (JSONArray args) throws Exception {

		JSONObject options = args.optJSONObject(0);

		File inFile = null;
		inFile = OnymosMediaUtil.resLFSU(options.getString("fileUri"), this.cordova);
		
		if (!inFile.exists()) {
			Log.d(LTAG, "input file does not exist");
			callbackContext.error("input video does not exist.");
			return;
		}

		String svd = inFile.getAbsolutePath();
		
		String opf = options.optString(
			"outputFileName", 
			new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date())
		);
	
		Context appContext = cordova.getActivity().getApplicationContext();
		PackageManager pm = appContext.getPackageManager();
		
		ApplicationInfo ai;
		try {
			ai = pm.getApplicationInfo(cordova.getActivity().getPackageName(), 0);
		}
		catch (final NameNotFoundException e) {
			ai = null;
		}
		final String appName = (String) (ai != null ? pm.getApplicationLabel(ai) : "Unknown");
						
		File tsp = appContext.getExternalCacheDir();
		
		File of = new File(tsp.getPath(), "PIC_" + opf + ".jpg");
		
		Bitmap tn = ThumbnailUtils.createVideoThumbnail(svd, MediaStore.Images.Thumbnails.MINI_KIND);
		
		FileOutputStream theOutputStream;
		try {
			if (!of.exists()) {
				if (!of.createNewFile()) {
					callbackContext.error("Thumbnail could not be saved.");
				}
			}
			if (of.canWrite()) {
				theOutputStream = new FileOutputStream(of);
				if (theOutputStream != null) {
					tn.compress(CompressFormat.JPEG, 75, theOutputStream);
				}
				else {
					callbackContext.error("Thumbnail could not be saved;target not writeable");
				}
			}
		}
		catch (IOException e) {
			callbackContext.error(e.toString());
		}
						
		callbackContext.success(of.getAbsolutePath());

	} /* end function private void CTCR */

	private void executeRequest (Request req) {
		switch (req.action) {
			case CAPTURE_VIDEO_SEC:
				this.captureVideo(req);
				break;
		}

	} /* end function private void executeRequest */

	public void onRequestPermissionResult (int requestCode, String[] permissions, int[] grantResults) throws JSONException {

		//super.onRequestPermissionResult(requestCode, permissions, grantResults);
		Request req = pendingRequests.get(requestCode);
		
		for (int r:grantResults) {

			if (r == PackageManager.PERMISSION_DENIED) {
				this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
				return;
			}
		} // end for r:grantResults

		if (req != null && req.action == CAPTURE_VIDEO_SEC) {
			boolean success = true;
			for (int r:grantResults) {
				if (r == PackageManager.PERMISSION_DENIED) {
					success = false;
					break;
				}
			}

			if (success) {
				executeRequest(req);
				return;
			}
			else {
				pendingRequests.resolveWithFailure(req, createErrorObject(CAPTURE_PERMISSION_DENIED, "Permission denied."));
			}

		} // end if req != null
		
		switch (requestCode) {
			case CREATE_THUMBNAIL_SEC:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					try {
						CTCR(this.executeArgs);
					}
					catch (Exception e) {
						callbackContext.error("Error converting to thumbnail with error [" + e.getMessage() +"]");
					}
					break;
				}
			case CREATE_VIDEO_SEC:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					try {
						this.HQMPMOV(this.executeArgs, this.callbackContext);
					}
					catch (Exception e) {
						callbackContext.error("Error transcoding video with error [" + e.getMessage() +"]");
					}
					break;
				}
			case CREATE_IMAGE_SEC:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					try {
						this.FIGHJ(this.st, this.dt, this.et);
					}
					catch (Exception e) {
						callbackContext.error("Error selecting media with error [" + e.getMessage() +"]");
					}
					break;
				}
				case TAKE_PIC_SEC:
					if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
						try {
							this.TPFC(this.dt, this.et);
						}
						catch (Exception e) {
							callbackContext.error("Error taking picture with error [" + e.getMessage() +"]");
						}
						break;
					}
		} // end switch (requestCode)

	} /* end function public void onRequestPermissionResult */

	private void checkReadStoragePermission (int permissionCode) {

		if (OnymosPermissionHelper.hasPermission(this, permissions[0])) {
			switch (permissionCode) {
				case CREATE_THUMBNAIL_SEC:
					try {
						CTCR(this.executeArgs);
					}
					catch (Exception e) {
						callbackContext.error("Error converting to thumbnail with error [" + e.getMessage() +"]");
					}
					break;
				case CREATE_VIDEO_SEC:
					try {
						this.HQMPMOV(this.executeArgs, this.callbackContext);
					}
					catch (Exception e) {
						callbackContext.error("Error transcoding video with error [" + e.getMessage() +"]");
					}
					break;
				case CREATE_IMAGE_SEC:
					try {
						this.FIGHJ(this.st, this.dt, this.et);
					}
					catch (Exception e) {
						callbackContext.error("Error selecting media with error [" + e.getMessage() +"]");
					}
					break;
				case TAKE_PIC_SEC:
					try {
						this.TPFC(this.dt, this.et);
					}
					catch (Exception e) {
						callbackContext.error("Error taking picture with error [" + e.getMessage() +"]");
					}
					break;
			} // end switch (permissionCode)

		} // end if OnymosPermissionHelper.hasPermission(this, permissions[0])
		else {
				OnymosPermissionHelper.requestPermission(this, permissionCode, permissions[0]);

		} // end else OnymosPermissionHelper.hasPermission(this, permissions[0])

	} /* end function private void checkReadStoragePermission */


	private void captureVideo (Request req) {
		
		boolean needExternalStoragePermission =
						!OnymosPermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

		boolean needCameraPermission = cameraPermissionInManifest &&
						!OnymosPermissionHelper.hasPermission(this, Manifest.permission.CAMERA);
		
		if (needExternalStoragePermission || needCameraPermission) {
				if (needExternalStoragePermission && needCameraPermission) {
						OnymosPermissionHelper.requestPermissions(this, req.requestCode, permissions);
				} else if (needExternalStoragePermission) {
						OnymosPermissionHelper.requestPermission(this, req.requestCode, Manifest.permission.READ_EXTERNAL_STORAGE);
				} else {
						OnymosPermissionHelper.requestPermission(this, req.requestCode, Manifest.permission.CAMERA);
				}
		} else {
				Intent intent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);

				if(Build.VERSION.SDK_INT > 7){
						intent.putExtra("android.intent.extra.durationLimit", req.duration);
						intent.putExtra("android.intent.extra.videoQuality", req.quality);
				}
				this.cordova.startActivityForResult((CordovaPlugin) this, intent, req.requestCode);
		}

		/*if (cameraPermissionInManifest && !OnymosPermissionHelper.hasPermission(this, Manifest.permission.CAMERA)) {
			OnymosPermissionHelper.requestPermission(this, req.requestCode, Manifest.permission.CAMERA);
		}
		else {

			Intent intent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
			
			if (Build.VERSION.SDK_INT > 7){
				intent.putExtra("android.intent.extra.durationLimit", req.duration);
				intent.putExtra("android.intent.extra.videoQuality", req.quality);
			}
			this.cordova.startActivityForResult((CordovaPlugin) this, intent, req.requestCode);
		} */

	} /* end function private void captureVideo */

	private void captureAudio(Request req) {

		if (!OnymosPermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
				OnymosPermissionHelper.requestPermission(this, req.requestCode, Manifest.permission.READ_EXTERNAL_STORAGE);
		} else {

				try {
					Intent intent = new Intent(android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION);
					this.cordova.startActivityForResult((CordovaPlugin) this, intent, req.requestCode);
				} catch(Exception e) {
					pendingRequests.resolveWithFailure(req, createErrorObject(CAPTURE_NO_MEDIA_FILES, "No voice recoreder on your device"));
				}
		}
	}

	private void getLUrl (JSONArray args, final CallbackContext callbackContext) {

		try {
			JSONObject options = args.optJSONObject(0);
			JSONObject resultObj = new JSONObject();

			Uri uri = Uri.parse(options.getString("localUrl"));
			Context applicationContext = this.cordova.getActivity().getApplicationContext();
			
			String filePath = getLPath(applicationContext, uri);

			if (filePath == null) {
				callbackContext.error("Cannot access file");
			}
			else if (filePath.equalsIgnoreCase("cloud")) {
				callbackContext.error("Cannot access Remote file.");
			}
			else {
				Log.d(LTAG, "Filepath: " + filePath);
				try {
					String mimeType = getMTE("file://"+filePath);
					resultObj.put("localUrl", "file://" + filePath);
					resultObj.put("contentType", mimeType);
					callbackContext.success("file://" + filePath);
				}
				catch (Exception e) {
					callbackContext.error("Cannot read file - " + e.getMessage());
				}
			}
		} // end try
		catch (Exception e) {
			callbackContext.error("Cannot read file - " + e.getMessage());
		}

	} /* end function private void getLUrl */

	public void onVideoActivityResult (Request req, Intent intent) {
				
		Uri data = null;

		if (intent != null) {
			// Get the uri of the video clip
			data = intent.getData();
		}

		if ( data == null){
			File movie = new File(getTempDirectoryPath(), "Capture.avi");
			data = Uri.fromFile(movie);
		}

		// create a file object from the uri
		if (data == null) {
			pendingRequests.resolveWithFailure(req, createErrorObject(CAPTURE_NO_MEDIA_FILES, "Error: data is null"));
		}
		else {
			req.results.put(createMediaFile(data));
			
			if (req.results.length() >= req.limit) {
				// Send Uri back to JavaScript for viewing video
				pendingRequests.resolveWithSuccess(req);
			}
			else {
				// still need to capture more video clips
				captureVideo(req);
			}
		}

	} /* end function public void onVideoActivityResult */

	 public void onAudioActivityResult(Request req, Intent intent) {
		// Get the uri of the audio clip
		Uri data = intent.getData();
		// create a file object from the uri
		req.results.put(createMediaFile(data));

		if (req.results.length() >= req.limit) {
				// Send Uri back to JavaScript for listening to audio
				pendingRequests.resolveWithSuccess(req);
		} else {
				// still need to capture more audio clips
				captureAudio(req);
		}
	}

	private JSONObject createMediaFile (Uri data) {
		File fp = webView.getResourceApi().mapUriToFile(data);
		JSONObject obj = new JSONObject();

		Class webViewClass = webView.getClass();
		PluginManager pm = null;
		try {
			Method gpm = webViewClass.getMethod("getPluginManager");
			pm = (PluginManager) gpm.invoke(webView);
		}
		catch (NoSuchMethodException e) {
		
		}
		catch (IllegalAccessException e) {
		
		}
		catch (InvocationTargetException e) {
		
		}

		if (pm == null) {
			try {
				Field pmf = webViewClass.getField("pluginManager");
				pm = (PluginManager)pmf.get(webView);
			}
			catch (NoSuchFieldException e) {
			}
			catch (IllegalAccessException e) {
			}
		}

		FileUtils filePlugin = (FileUtils) pm.getPlugin("File");
		LocalFilesystemURL url = filePlugin.filesystemURLforLocalPath(fp.getAbsolutePath());

		try {
			// File properties
			obj.put("name", fp.getName());
			obj.put("fullPath", fp.toURI().toString());
			if (url != null) {
					obj.put("localURL", url.toString());
			}
			// Because of an issue with MimeTypeMap.getMimeTypeFromExtension() all .3gpp files
			// are reported as video/3gpp. I'm doing this hacky check of the URI to see if it
			// is stored in the audio or video content store.
			if (fp.getAbsoluteFile().toString().endsWith(".3gp") || fp.getAbsoluteFile().toString().endsWith(".3gpp")) {
				if (data.toString().contains("/audio/")) {
					obj.put("type", AUDIO_3GPP);
				}
				else {
					obj.put("type", VIDEO_3GPP);
				}
			}
			else {
				obj.put("type", OnymosMediaUtil.getMimeType(Uri.fromFile(fp), cordova));
			}

			obj.put("lastModifiedDate", fp.lastModified());
			obj.put("size", fp.length());
		}
		catch (JSONException e) {
			// this will never happen
			e.printStackTrace();
		}
		return obj;

	} /* end function private JSONObject createMediaFile */

	public Bundle onSaveInstanceState() {
		return pendingRequests.toBundle();

	} /* end function public Bundle onSaveInstanceState */

	public void onRestoreStateForActivityResult (Bundle state, CallbackContext callbackContext) {
		pendingRequests.setLastSavedState(state, callbackContext);

	} /* end function public void onRestoreStateForActivityResult */

	private JSONObject createErrorObject (int code, String message) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("code", code);
			obj.put("message", message);
		}
		catch (JSONException e) {
			// This will never happen
		}
		return obj;

	} /* end function private JSONObject createErrorObject */

	private String getTempDirectoryPath() {
		File cache = null;

		// Use internal storage
		cache = cordova.getActivity().getCacheDir();

		// Create the cache directory if it doesn't exist
		cache.mkdirs();
		return cache.getAbsolutePath();

	} /* end function private String getTempDirectoryPath */

	private static String getMTE (String path) {
		String extension = path;
		int lastDot = extension.lastIndexOf('.');

		if (lastDot != -1) {
			extension = extension.substring(lastDot + 1);
		}

		extension = extension.toLowerCase(Locale.getDefault());
		if (extension.equals("3ga")) {
			return "audio/3gpp";
		}
		return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

	} /* end function private static String getMTE */

	private static boolean isExternalStorageDocument (Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());

	} /* end function private static boolean isExternalStorageDocument */

	private static boolean isDownloadsDocument (Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());

	} /* end function private static boolean isDownloadsDocument */

	private static boolean isMediaDocument (Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());

	} /* end function private static boolean isMediaDocument */

	private static boolean isGooglePhotosUri (Uri uri) {
		return ("com.google.android.apps.photos.content".equals(uri.getAuthority())
							|| "com.google.android.apps.photos.contentprovider".equals(uri.getAuthority()));

	} /* end function private static boolean isGooglePhotosUri */

	private static boolean isGoogleDriveUri (Uri uri) {
		return "com.google.android.apps.docs.storage".equals(uri.getAuthority());

	} /* end function private static boolean isGoogleDriveUri */

	private static String getDC (Context context, Uri uri, String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
						column
		};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		}
		finally {
			if (cursor != null) cursor.close();
		}

		return null;

	} /* end function private static String getDC */

	private static String getContentFromSegments (List<String> segments) {
		String contentPath = "";

		for (String item : segments) {
			if (item.startsWith("content://")) {
				contentPath = item;
				break;
			}
		}

		return contentPath;

	} /* end function private static String getContentFromSegments */

	private static boolean fileExists (String filePath) {
		File file = new File(filePath);

		return file.exists();

	} /* end private static boolean fileExists */

	private static String getPExtSD (String[] pathData) {
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

	} /* end function private static String getPExtSD */

	private static String getLPath (final Context context, final Uri uri) {

		Log.d(LTAG, "File - " +
						"Authority: " + uri.getAuthority() +
										", Fragment: " + uri.getFragment() +
										", Port: " + uri.getPort() +
										", Query: " + uri.getQuery() +
										", Scheme: " + uri.getScheme() +
										", Host: " + uri.getHost() +
										", Segments: " + uri.getPathSegments().toString()
		);

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				String fullPath = getPExtSD(split);
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

				return getDC(context, contentUri, null, null);
			}
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				}
				else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				}
				else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDC(context, contentUri, selection, selectionArgs);
			}
			else if (isGoogleDriveUri(uri)) {
				return "cloud";
			}
		}
		else if ("content".equalsIgnoreCase(uri.getScheme())) {

			// Return the remote address
			if (isGooglePhotosUri(uri)) {
				String contentPath = getContentFromSegments(uri.getPathSegments());
				if (contentPath != "") {
					return getLPath(context, Uri.parse(contentPath));
				}
				else {
					return null;
				}
			}

			return getDC(context, uri, null, null);
		}
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	} /* end function private static String getLPath */

	private static JSONObject getFinalRUrl (JSONObject urlObject) {

		try {
			String remoteUrl = urlObject.getString("remoteUrl");
			String downloadId = urlObject.getString("downloadId");

			HttpURLConnection con = (HttpURLConnection) new URL(remoteUrl).openConnection();
			con.setInstanceFollowRedirects(false);
			con.connect();
			con.getInputStream();

			if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
				String redirectUrl = con.getHeaderField("Location");

				JSONObject tempUrlObject = new JSONObject();
				tempUrlObject.put("remoteUrl", redirectUrl);
				tempUrlObject.put("downloadId", downloadId);

				return getFinalRUrl(tempUrlObject);

			}
			return urlObject;

		}
		catch (Exception e) {
			return null;

		}

	} // end function private static String getFinalRUrl

		private String getUriExtension(Uri uri) {
			String extension = null;

			ContentResolver cR = cordova.getActivity().getContentResolver();
			String type = cR.getType(uri);
			if (type != null) {
					if (type.equals("audio/mpeg")) {
							extension = "mp3";
					} else if (type.equals("audio/mp4")) {
							extension = "m4a";
					} else if (type.equals("audio/x-wav")) {
							extension = "wav";
					} else if (type.equals("audio/ogg")) {
							extension = "ogg";
					} else if (type.equals("audio/amr")) {
							extension = "amr";
					} else if (type.equals("audio/aac")) {
							extension = "aac";
					} else if (type.equals("audio/3gpp")) {
							extension = "3gp";
					} else if (type.equals("audio/x-matroska")) {
							extension = "mkv";
					} else if (type.equals("audio/flac") || type.equals("audio/x-flac")) {
							extension = "flac";
					}
			} else {
					String filePath = uri.toString();
					if (filePath.startsWith("file://")) {
							int lastIndex = filePath.lastIndexOf(".");
							if ((lastIndex > -1) && (lastIndex != filePath.length())) {
									extension = filePath.substring(lastIndex + 1).toLowerCase();
							}
					}
			}

			if (SUPPORTED_EXTENSION.contains(extension)) {
					return extension;
			} else {
					return null;
			}
	}

	private Boolean copyUriToPath(Uri uri, String destPath) {
		File dstFile = new File(destPath);
		if (!dstFile.exists()) {
				try {
						Boolean retVal = dstFile.createNewFile();
				} catch (IOException e) {
						e.printStackTrace();
						return false;
				}
		}
		try {
				InputStream is = cordova.getActivity().getContentResolver().openInputStream(uri);
				OutputStream os = new FileOutputStream(dstFile);

				byte[] buf = new byte[1024];
				int len;
				while ((len = is.read(buf)) > 0) {
						os.write(buf, 0, len);
				}
				is.close();
				os.close();
		} catch (Exception e) {
				e.printStackTrace();
				return false;
		}
		return true;
	}

	private JSONObject getMediaInfoFromPath(String path) {
		JSONObject mediaInfo = new JSONObject();

		String artist = "No Artist";
		String album = "No Album";
		String title = "No Title";
		int duration = 0;
		String image = "No Image";

		MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
		metaRetriver.setDataSource(path);

		try {
				artist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
				if (artist == null) artist = "No Artist";
				album = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
				if (album == null) album = "No Album";
				title = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
				if (title == null) title = "No Title";
				String dur = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
				if (dur == null) {
						duration = 0;
				} else {
						duration = Integer.valueOf(dur);
				}
				byte [] art = metaRetriver.getEmbeddedPicture();
				if (art == null) {
						image = "No Image";
				} else {
						image = Base64.encodeToString(art, Base64.DEFAULT);
				}
		} catch (Exception e) {
				e.printStackTrace();
		}

		try {
				mediaInfo.put("artist", artist);
				mediaInfo.put("albumTitle", album);
				mediaInfo.put("title", title);
				mediaInfo.put("duration", duration);
				mediaInfo.put("exportedurl", "file://" + path);
				mediaInfo.put("image", image);
		} catch (JSONException e) {
				e.printStackTrace();
		}
		return mediaInfo;
	}

} /* end public class OnymosMedia */

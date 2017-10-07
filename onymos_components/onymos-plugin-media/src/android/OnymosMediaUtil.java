
/*
 *  Copyright 2015-2016 Onymos Inc
 *
 */

package com.onymos.components.media;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.cordova.CordovaInterface;
import org.json.JSONException;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.DocumentsContract;
import android.media.ThumbnailUtils;
import android.media.ExifInterface;
import android.util.Log;
import android.content.res.AssetFileDescriptor;


import android.annotation.SuppressLint;
import android.content.CursorLoader;
import android.webkit.MimeTypeMap;

public class OnymosMediaUtil {
		
		
		public OnymosMediaUtil(){}


		public static String getTDP(CordovaInterface cordova) {
			File cache = null;
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					cache = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
									"/Android/data/" + cordova.getActivity().getPackageName() + "/cache/");
			}
			else {
					cache = cordova.getActivity().getCacheDir();
			}
			cache.mkdirs();
			return cache.getAbsolutePath();
		}


		public static int getIO(Uri uri, CordovaInterface cordova) {
				int rotate = 0;
				String[] cols = { MediaStore.Images.Media.ORIENTATION };
				try {
						Cursor cursor = cordova.getActivity().getContentResolver().query(uri,
										cols, null, null, null);
						if (cursor != null) {
								cursor.moveToPosition(0);
								rotate = cursor.getInt(0);
								cursor.close();
						}
				} catch (Exception e) {
				}
				return rotate;
		}

	public static String getMRP(Uri contentUri, CordovaInterface cordova) {
			try {
				String res = null;
				//Uri newUri = handleImageUri(contentUri);
				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor cursor = cordova.getActivity().getContentResolver().query(contentUri, proj, null, null, null);
				if(cursor.moveToFirst()){;
					 int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					 //System.out.println("DEBUG : getMRP : column_index [" + column_index + "]");
					 res = cursor.getString(column_index);
					 //System.out.println("DEBUG : getMRP : res [" + res + "]");
				}
				cursor.close();
				//System.out.println("DEBUG - JAVA - getMRP res[" + res + "]");
				return res;
			} catch (Exception e) {
				return contentUri.getPath();
			}
		}

		public static String getSPU (Uri uri, CordovaInterface cordova) {
			
			String destinationImagePath = "";
			Bitmap selectedImage = null;
			
			Context context = cordova.getActivity().getApplicationContext();
			try {
				
				String tmpPath = getTDP(cordova);
				File FilePath = new File(tmpPath);
				destinationImagePath = uri.toString();
				selectedImage = dF(destinationImagePath, cordova);
				//System.out.println("DEBUG - JAVA - SelectedImage"+ selectedImage);
				//System.out.println("DEBUG - JAVA - uri.getPath"+ uri.getPath());
				selectedImage = rIIR(context, selectedImage, uri, cordova);
				destinationImagePath = FilePath +"/Modified"+ System.currentTimeMillis() + ".jpg";
				FileOutputStream destination = new FileOutputStream(destinationImagePath);
				selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, destination);
				destination.close();
				destinationImagePath = "file://" + destinationImagePath;
				//System.out.println("DEBUG - JAVA - changedURI" + destinationImagePath);
				selectedImage.recycle();
				selectedImage = null;
				return destinationImagePath;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
				if (selectedImage != null) {
					selectedImage.recycle();
					selectedImage = null;
					System.gc();
				}
			}
			
		}

		public static Bitmap dF(String uriString, CordovaInterface cordova) throws Exception {
			
			int IMS = 1920;

			Bitmap b = null;
			String actualFileStr = "";

			if (uriString != null && uriString.indexOf("picasa") != -1) {
					actualFileStr = getMRP(Uri.parse(uriString), cordova);
			}
			else {
				if (uriString != null && (uriString.toString()).indexOf("ACTUAL") != -1) {
					actualFileStr = getRVPWAU(Uri.parse(uriString));

				} else {
					actualFileStr = uriString;

				}
			}

			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			InputStream is = cordova.getActivity().getContentResolver().openInputStream(Uri.parse(actualFileStr));

			BitmapFactory.decodeStream(is, null, o);
			is.close();

			int scale = 1;
			if (o.outHeight > IMS || o.outWidth > IMS) {
					scale = (int)Math.pow(2, (int) Math.ceil(Math.log(IMS / 
						 (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
			}

			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			is = cordova.getActivity().getContentResolver().openInputStream(Uri.parse(actualFileStr));
			b = BitmapFactory.decodeStream(is, null, o2);
			is.close();

			return b;
		}


		 /**
		 * Rotate an image if required.
		 */

		public static Bitmap rIIR(Context context,Bitmap img, Uri uri, CordovaInterface cordova) {
				// Detect rotation
			try {
				int rotation = getIO(uri, cordova);
				//System.out.println("DEBUG - JAVA - first rotation getImage" + rotation);
				if (rotation == 0) {
					String realPath = getRP(uri, cordova);
					ExifInterface inFile = new ExifInterface(realPath);
					rotation = getOtation(inFile.getAttribute(ExifInterface.TAG_ORIENTATION));
				}
				if(rotation!=0){
						Matrix matrix = new Matrix();
						matrix.postRotate(rotation);
						Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
						img.recycle();
						img = null;
						System.gc();
						return rotatedImg;
				}else{
						return img;
				}
			} catch (Exception e) {
				return img;
			}
		}

		public static int getOtation(String orientationStr) {
			int o = Integer.parseInt(orientationStr);

			if (o == ExifInterface.ORIENTATION_NORMAL) {
					return 0;
			} else if (o == ExifInterface.ORIENTATION_ROTATE_90) {
					return 90;
			} else if (o == ExifInterface.ORIENTATION_ROTATE_180) {
					return 180;
			} else if (o == ExifInterface.ORIENTATION_ROTATE_270) {
					return 270;
			} else {
					return 0;
			}
		}

		public static String getRVPWAU(Uri uri) {
			String path = uri.getPath();
			//System.out.println("DEBUG - JAVA - getId - path [" + path + "]");
			//System.out.println("DEBUG - JAVA - getId - uri.toString [" + uri.toString() + "]");
			try {
				if ("com.google.android.apps.photos.contentprovider".equals(uri.getAuthority())) {
						int iActual = path.indexOf("ACTUAL");
						int istart = path.indexOf("content");

						if (istart == -1) {
							istart = path.indexOf("mediaKey");
						}
						//System.out.println("DEBUG - JAVA - subpath iActual ["+ iActual + "]");
						//System.out.println("DEBUG - JAVA - subpath istart ["+ istart + "]");
						if (iActual > -1 && istart > -1) {
								String subPath = path.substring(istart, iActual - 1);
								subPath = URLDecoder.decode(subPath, "UTF-8");
								//System.out.println("DEBUG - JAVA - subpath ["+ subPath + "]");
								uri = Uri.parse(subPath);
								//System.out.println("DEBUG - JAVA - subpath toString ["+ uri.toString() + "]");
						} 
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			//return ContentUris.parseId(uri);
			return uri.toString();
		}

		public static File resLFSU(String url, CordovaInterface cordova) throws IOException, JSONException {
				String decoded = URLDecoder.decode(url, "UTF-8");

				File fp = null;

				// Handle the special case where you get an Android content:// uri.
				if (decoded.startsWith("content:")) {
						fp = new File(getPQRS(cordova.getActivity().getApplicationContext(), Uri.parse(decoded)));
				} else {
						// Test to see if this is a valid URL first
						@SuppressWarnings("unused")
						URL testUrl = new URL(decoded);

						if (decoded.startsWith("file://")) {
								int questionMark = decoded.indexOf("?");
								if (questionMark < 0) {
										fp = new File(decoded.substring(7, decoded.length()));
								} else {
										fp = new File(decoded.substring(7, questionMark));
								}
						} else if (decoded.startsWith("file:/")) {
								fp = new File(decoded.substring(6, decoded.length()));
						} else {
								fp = new File(decoded);
						}
				}

				if (!fp.exists()) {
						throw new FileNotFoundException();
				}
				if (!fp.canRead()) {
						throw new IOException();
				}
				return fp;
		}

		@SuppressWarnings("deprecation")
		public static String getRP(Uri uri, CordovaInterface cordova) {
				String realPath = null;

				if (Build.VERSION.SDK_INT < 11)
						realPath = getRPU_A11(cordova.getActivity(), uri);

				// SDK >= 11 && SDK < 19
				else if (Build.VERSION.SDK_INT < 19)
						realPath = getRPU_A18(cordova.getActivity(), uri);

				// SDK > 19 (Android 4.4)
				else
						realPath = getRPU_A19(cordova.getActivity(), uri);

				return realPath;
		}

		public static String getRP(String uriString, CordovaInterface cordova) {
				return getRP(Uri.parse(uriString), cordova);
		}

		@SuppressLint("NewApi")
		public static String getRPU_A19(Context context, Uri uri) {
				String filePath = "";
				try {
						String wholeID = DocumentsContract.getDocumentId(uri);

						String id = wholeID.indexOf(":") > -1 ? wholeID.split(":")[1] : wholeID.indexOf(";") > -1 ? wholeID
										.split(";")[1] : wholeID;

						String[] column = { MediaStore.Images.Media.DATA };

						String sel = MediaStore.Images.Media._ID + "=?";

						Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column,
										sel, new String[] { id }, null);

						int columnIndex = cursor.getColumnIndex(column[0]);

						if (cursor.moveToFirst()) {
								filePath = cursor.getString(columnIndex);
						}
						cursor.close();
				} catch (Exception e) {
						filePath = "";
				}
				return filePath;
		}

		@SuppressLint("NewApi")
		public static String getRPU_A18(Context context, Uri contentUri) {
				String[] proj = { MediaStore.Images.Media.DATA };
				String result = null;

				try {
						CursorLoader cursorLoader = new CursorLoader(context, contentUri, proj, null, null, null);
						Cursor cursor = cursorLoader.loadInBackground();

						if (cursor != null) {
								int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
								cursor.moveToFirst();
								result = cursor.getString(column_index);
						}
				} catch (Exception e) {
						result = null;
				}
				return result;
		}

		public static String getRPU_A11(Context context, Uri contentUri) {
				String[] proj = { MediaStore.Images.Media.DATA };
				String result = null;

				try {
						Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
						int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
						cursor.moveToFirst();
						result = cursor.getString(column_index);

				} catch (Exception e) {
						result = null;
				}
				return result;
		}

		public static String getMTE(String path) {
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
		}
		
		public static String getMT(String uriString, CordovaInterface cordova) {
				String mimeType = null;

				Uri uri = Uri.parse(uriString);
				if (uriString.startsWith("content://")) {
						mimeType = cordova.getActivity().getContentResolver().getType(uri);
				} else {
						mimeType = getMTE(uri.getPath());
				}

				return mimeType;
		}


		public static String getPQRS(final Context context, final Uri uri) {

				final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

				if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
						if (findESD(uri)) {
								final String docId = DocumentsContract.getDocumentId(uri);
								final String[] split = docId.split(":");
								final String type = split[0];

								if ("primary".equalsIgnoreCase(type)) {
										return Environment.getExternalStorageDirectory() + "/" + split[1];
								}

						}
						else if (findDD(uri)) {

								final String id = DocumentsContract.getDocumentId(uri);
								final Uri contentUri = ContentUris.withAppendedId(
												Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

								return getDCQ(context, contentUri, null, null);
						}
						else if (findMD(uri)) {
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

								return getDCQ(context, contentUri, selection, selectionArgs);
						}
				}
				else if ("content".equalsIgnoreCase(uri.getScheme())) {
						return getDCQ(context, uri, null, null);
				}
				else if ("file".equalsIgnoreCase(uri.getScheme())) {
						return uri.getPath();
				}

				return null;
		}

		public static String getDCQ(Context context, Uri uri, String selection,
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

		public static Uri wCS() {
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
						return android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else {
						return android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;
				}
		}

		public static Cursor qIDB(Uri contentStore, CordovaInterface cordova) {
			return cordova.getActivity().getContentResolver().query(
							contentStore,
							new String[] { MediaStore.Images.Media._ID },
							null,
							null,
							null);
		}

		public static File cCF(int encodingType, CordovaInterface cordova) {
				return cCF(encodingType, "",cordova);
		}


		public static File cCF(int encodingType, String fileName, CordovaInterface cordova) {
				if (fileName.isEmpty()) {
						fileName = ".Pic";
				}

				if (encodingType == 0) {
						fileName = fileName + ".jpg";
				} else if (encodingType == 1) {
						fileName = fileName + ".png";
				} else {
						throw new IllegalArgumentException("Invalid Encoding Type: " + encodingType);
				}

				return new File(getTDP(cordova), fileName);
		}

		public static InputStream getISTFromUStr(String uriString, CordovaInterface cordova)
						throws IOException {
			InputStream returnValue = null;
			if (uriString.startsWith("content")) {
					Uri uri = Uri.parse(uriString);
					returnValue = cordova.getActivity().getContentResolver().openInputStream(uri);
			} else if (uriString.startsWith("file://")) {
					int question = uriString.indexOf("?");
					if (question > -1) {
							uriString = uriString.substring(0, question);
					}
					if (uriString.startsWith("file:///android_asset/")) {
							Uri uri = Uri.parse(uriString);
							String relativePath = uri.getPath().substring(15);
							returnValue = cordova.getActivity().getAssets().open(relativePath);
					} else {
							// might still be content so try that first
							try {
									returnValue = cordova.getActivity().getContentResolver().openInputStream(Uri.parse(uriString));
							} catch (Exception e) {
									returnValue = null;
							}
							if (returnValue == null) {
									returnValue = new FileInputStream(getRP(uriString, cordova));
							}
					}
			} else {
					returnValue = new FileInputStream(uriString);
			}
			return returnValue;
		}

		public static int[] car(int origWidth, int origHeight, int tw, int th) {
				int newWidth = tw;
				int newHeight = th;

				// If no new width or height were specified return the original bitmap
				if (newWidth <= 0 && newHeight <= 0) {
						newWidth = origWidth;
						newHeight = origHeight;
				}
				// Only the width was specified
				else if (newWidth > 0 && newHeight <= 0) {
						newHeight = (newWidth * origHeight) / origWidth;
				}
				// only the height was specified
				else if (newWidth <= 0 && newHeight > 0) {
						newWidth = (newHeight * origWidth) / origHeight;
				}
				// If the user specified both a positive width and height
				// (potentially different aspect ratio) then the width or height is
				// scaled so that the image fits while maintaining aspect ratio.
				// Alternatively, the specified width and height could have been
				// kept and Bitmap.SCALE_TO_FIT specified when scaling, but this
				// would result in whitespace in the new image.
				else {
						double newRatio = newWidth / (double) newHeight;
						double origRatio = origWidth / (double) origHeight;

						if (origRatio > newRatio) {
								newHeight = (newWidth * origHeight) / origWidth;
						} else if (origRatio < newRatio) {
								newWidth = (newHeight * origWidth) / origHeight;
						}
				}

				int[] retval = new int[2];
				retval[0] = newWidth;
				retval[1] = newHeight;
				return retval;
		}

		public static int css(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
			final float srcAspect = (float)srcWidth / (float)srcHeight;
			final float dstAspect = (float)dstWidth / (float)dstHeight;

			if (srcAspect > dstAspect) {
					return srcWidth / dstWidth;
			} else {
					return srcHeight / dstHeight;
			}
		}

		public static void wunci(Uri uri, CordovaInterface cordova, Uri imageUri) throws FileNotFoundException,
						IOException {
			FileInputStream fis = null;
			OutputStream os = null;
			try {
					fis = new FileInputStream(sFP(imageUri.toString()));
					os = cordova.getActivity().getContentResolver().openOutputStream(uri);
					byte[] buffer = new byte[4096];
					int len;
					while ((len = fis.read(buffer)) != -1) {
							os.write(buffer, 0, len);
					}
					os.flush();
			} finally {
					if (os != null) {
							try {
									os.close();
							} catch (IOException e) {
							}
					}
					if (fis != null) {
							try {
									fis.close();
							} catch (IOException e) {
							}
					}
			}
		}


		public static Bitmap grb(int rotate, Bitmap bitmap) {
				Matrix matrix = new Matrix();
				if (rotate == 180) {
						matrix.setRotate(rotate);
				} else {
						matrix.setRotate(rotate, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
				}

				try
				{
						bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
				}
				catch (OutOfMemoryError oom)
				{
						// You can run out of memory if the image is very large:
						// http://simonmacdonald.blogspot.ca/2012/07/change-to-camera-code-in-phonegap-190.html
						// If this happens, simply do not rotate the image and return it unmodified.
						// If you do not catch the OutOfMemoryError, the Android app crashes.
				}

				return bitmap;
		}

		public static String sFP(String uriString) {
			if (uriString.startsWith("file://")) {
					uriString = uriString.substring(7);
			}
			return uriString;
		}

		public static boolean findESD(Uri uri) {
			return "com.android.externalstorage.documents".equals(uri.getAuthority());
		}

		public static boolean findDD(Uri uri) {
			return "com.android.providers.downloads.documents".equals(uri.getAuthority());
		}

		public static boolean findMD(Uri uri) {
			return "com.android.providers.media.documents".equals(uri.getAuthority());
		}

		public static String getMimeTypeForExtension(String path) {
			String extension = path;
			int lastDot = extension.lastIndexOf('.');
			if (lastDot != -1) {
				extension = extension.substring(lastDot + 1);
			}
			// Convert the URI string to lower case to ensure compatibility with MimeTypeMap (see CB-2185).
			extension = extension.toLowerCase(Locale.getDefault());
			if (extension.equals("3ga")) {
				return "audio/3gpp";
			}
			return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		}

		public static String getMimeType(Uri uri, CordovaInterface cordova) {
			String mimeType = null;
			if ("content".equals(uri.getScheme())) {
				mimeType = cordova.getActivity().getContentResolver().getType(uri);
			} else {
				mimeType = getMimeTypeForExtension(uri.getPath());
			}
			return mimeType;
		}

}
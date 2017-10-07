/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package com.digi.me.permission.app;

import android.net.Uri;

//import com.digi.me.permission.app.BuildConfig;
import com.digi.me.permission.service.PermissionService;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.CertificatePinner;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Application {
	
	//private static final String PERMISSION_SERVICE_BASE_URL = BuildConfig.API_URL;

	private PermissionService permissionService;

	private String serviceUrl;

		/**
		 * Constructor.
		 */
	public Application() {

	}

	public Application(String baseUrl) {
		this.serviceUrl = baseUrl;
	}
		
	public synchronized PermissionService getPermissionService() {
			System.out.println("JAVA DEBUG : Application : url" + this.serviceUrl);
			if (permissionService == null) {
					permissionService = new Retrofit.Builder()
							.baseUrl(this.serviceUrl)
							.addConverterFactory(GsonConverterFactory.create())
							.client(getOkHttpClient())
							.build()
							.create(PermissionService.class);
			}
			return permissionService;
	}

	private OkHttpClient getOkHttpClient() {
			HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
			logging.setLevel(HttpLoggingInterceptor.Level.BODY);

			ConnectionSpec connectionSpec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
					.tlsVersions(TlsVersion.TLS_1_2)
					.build();

			return new OkHttpClient.Builder()
					.connectTimeout(2, TimeUnit.MINUTES).readTimeout(50, TimeUnit.SECONDS)
					.connectionSpecs(Collections.singletonList(connectionSpec))
					.addInterceptor(logging)
					.certificatePinner(createPinner())
					.build();
	}

	private CertificatePinner createPinner() {
			String host = Uri.parse(this.serviceUrl).getHost();
			return new CertificatePinner.Builder()
							.add(host, "sha256/FuXLwrAfrO4L3Cu03eXcXAH1BnnQRJeqy8ft+dVB4TI=")
							.add(host, "sha256/41Vcs2jOzcXdsDsbDt/nsNQRUZsYhCTPoeODK6VaWF0=")
							.add(host, "sha256/HC6oU3LGzhkwHionuDaZacaIbjwYaMT/Qc7bxWLyy8g=")
							.add(host, "sha256/2qix+QNHzGWG5nhEFNIMxPZ57YbgT0liSisVLERNzt8=")
							.add(host, "sha256/W8QTLPG35cP39gFmUjKLLKAlHrYmGxvHf5Zf+INBZzo=")
							.build();
	}
}

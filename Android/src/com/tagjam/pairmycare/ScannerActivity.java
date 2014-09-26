/*******************************************************************************
 * Copyright 2013 Abhijit Kalamkar. All rights reserved.
 ******************************************************************************/
package com.tagjam.pairmycare;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;
import android.widget.FrameLayout;

public class ScannerActivity extends FragmentActivity {
	private static final String TAG = "ScannerFragment";

	private Camera mCamera;
	private CameraPreview mPreview;
	private Handler autoFocusHandler;
	ImageScanner scanner;

	private boolean previewing = true;
	private int expectedType;

	static {
		System.loadLibrary("iconv");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_scanner);

		expectedType = getIntent().getIntExtra("type", Symbol.NONE);

		autoFocusHandler = new Handler();
		mCamera = getCameraInstance();

		/* Instance barcode scanner */
		scanner = new ImageScanner();
		scanner.setConfig(0, Config.X_DENSITY, 3);
		scanner.setConfig(0, Config.Y_DENSITY, 3);

		mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
		FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
		preview.addView(mPreview);
	}

	@Override
	public void onPause() {
		super.onPause();

		if (mCamera != null) {
			previewing = false;
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		try {
			return Camera.open();
		} catch (Exception e) {
		}
		return null;
	}

	PreviewCallback previewCb = new PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			Camera.Parameters parameters = camera.getParameters();
			Size size = parameters.getPreviewSize();

			Image barcode = new Image(size.width, size.height, "Y800");
			barcode.setData(data);

			int result = scanner.scanImage(barcode);

			if (result != 0) {
				previewing = false;
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();

				SymbolSet syms = scanner.getResults();
				for (Symbol sym : syms) {
					Log.w(TAG, String.format("barcode result of type %d has value %s",
							sym.getType(), sym.getData()));
					if (sym.getType() == expectedType || expectedType == Symbol.NONE) {
						finishWithResult(sym);
					}
				}
				finishWithResult(null);
			}
		}
	};

	private void finishWithResult(Symbol symbol) {
		Intent intent = new Intent();
		if (symbol != null) {
			intent.putExtra("type", symbol.getType());
			intent.putExtra("data", symbol.getData());
		}
		setResult(RESULT_OK, intent);
		MediaPlayer.create(this, R.raw.button_click).start();
		finish();
	}

	private final Runnable doAutoFocus = new Runnable() {
		@Override
		public void run() {
			if (previewing) {
				mCamera.autoFocus(autoFocusCB);
			}
		}
	};

	// Mimic continuous auto-focusing
	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};
}

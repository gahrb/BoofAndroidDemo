package org.boofcv.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import boofcv.alg.distort.ImageDistort;
import boofcv.alg.distort.LensDistortionOps;
import boofcv.alg.distort.PointToPixelTransform_F32;
import boofcv.alg.interpolate.InterpolatePixelS;
import boofcv.android.ConvertBitmap;
import boofcv.android.gui.VideoImageProcessing;
import boofcv.core.image.ConvertImage;
import boofcv.core.image.border.FactoryImageBorder;
import boofcv.core.image.border.ImageBorder;
import boofcv.factory.distort.FactoryDistort;
import boofcv.factory.interpolate.FactoryInterpolation;
import boofcv.struct.distort.PointTransform_F32;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.ImageUInt8;
import boofcv.struct.image.MultiSpectral;

/**
 * After the camera has been calibrated the user can display a distortion free image
 *
 * @author Peter Abeles
 */
public class UndistortDisplayActivity extends DemoVideoDisplayActivity
		implements CompoundButton.OnCheckedChangeListener
{

	ToggleButton toggleDistort;
	ToggleButton toggleColor;

	boolean isDistorted = false;
	boolean isColor = false;

	ImageDistort<ImageUInt8,ImageUInt8> removeDistortion;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = getLayoutInflater();
		LinearLayout controls = (LinearLayout)inflater.inflate(R.layout.undistort_controls,null);

		LinearLayout parent = getViewContent();
		parent.addView(controls);

		toggleDistort = (ToggleButton)controls.findViewById(R.id.toggle_distort);
		toggleDistort.setOnCheckedChangeListener(this);
		toggleDistort.setChecked(isDistorted);

		toggleColor = (ToggleButton)controls.findViewById(R.id.toggle_color);
		toggleColor.setOnCheckedChangeListener(this);
		toggleColor.setChecked(isColor);

		if( DemoMain.preference.intrinsic != null ) {
			// define the transform.  Cache the results for quick rendering later on
			PointTransform_F32 fullView = LensDistortionOps.fullView(DemoMain.preference.intrinsic, null);
			InterpolatePixelS<ImageUInt8> interp = FactoryInterpolation.bilinearPixelS(ImageUInt8.class);
			ImageBorder border = FactoryImageBorder.value(ImageUInt8.class,0);
			// for some reason not caching is faster on a low end phone.  Maybe it has to do with CPU memory
			// cache misses when looking up a point?
			removeDistortion = FactoryDistort.distort(false,interp,border,ImageUInt8.class);
			removeDistortion.setModel(new PointToPixelTransform_F32(fullView));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		setProcessing(new UndistortProcessing());
	}

	@Override
	public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
		if( DemoMain.preference.intrinsic == null ) {
			Toast toast = Toast.makeText(UndistortDisplayActivity.this, "You must first calibrate the camera!", 2000);
			toast.show();
		}
		if( toggleDistort == compoundButton ) {
			isDistorted = b;
		} else if( toggleColor == compoundButton ) {
			isColor = b;
		}
	}

	protected class UndistortProcessing extends VideoImageProcessing<MultiSpectral<ImageUInt8>> {
		MultiSpectral<ImageUInt8> undistorted;

		public UndistortProcessing() {
			super(ImageType.ms(3,ImageUInt8.class));
		}

		@Override
		protected void declareImages( int width , int height ) {
			super.declareImages(width, height);

			undistorted = new MultiSpectral<ImageUInt8>(ImageUInt8.class,width,height,3);
		}

		@Override
		protected void process(MultiSpectral<ImageUInt8> input, Bitmap output, byte[] storage) {
			if( DemoMain.preference.intrinsic == null ) {
				Canvas canvas = new Canvas(output);
				Paint paint = new Paint();
				paint.setColor(Color.RED);
				paint.setTextSize(output.getWidth()/10);
				int textLength = (int)paint.measureText("Calibrate Camera First");

				canvas.drawText("Calibrate Camera First", (canvas.getWidth() - textLength) / 2, canvas.getHeight() / 2, paint);
			} else if( isDistorted ) {
				if( isColor )
					ConvertBitmap.multiToBitmap(input,output,storage);
				else {
					ConvertImage.average(input,undistorted.getBand(0));
					ConvertBitmap.grayToBitmap(undistorted.getBand(0),output,storage);
				}
			} else {
				if( isColor ) {
					for( int i = 0; i < input.getNumBands(); i++ ) {
						removeDistortion.apply(input.getBand(i),undistorted.getBand(i));
					}

					ConvertBitmap.multiToBitmap(undistorted,output,storage);
				} else {
					ConvertImage.average(input,undistorted.getBand(0));
					removeDistortion.apply(undistorted.getBand(0),undistorted.getBand(1));
					ConvertBitmap.grayToBitmap(undistorted.getBand(1),output,storage);
				}
			}
		}
	}
}
package org.boofcv.android;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import boofcv.abst.filter.blur.BlurFilter;
import boofcv.android.ConvertBitmap;
import boofcv.android.gui.VideoImageProcessing;
import boofcv.factory.filter.blur.FactoryBlurFilter;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.ImageUInt8;

/**
 * Blurs the input video image using different algorithms.
 *
 * @author Peter Abeles
 */
public class BlurDisplayActivity extends DemoVideoDisplayActivity
		implements AdapterView.OnItemSelectedListener
{

	Spinner spinnerView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = getLayoutInflater();
		LinearLayout controls = (LinearLayout)inflater.inflate(R.layout.select_algorithm,null);

		LinearLayout parent = getViewContent();
		parent.addView(controls);

		spinnerView = (Spinner)controls.findViewById(R.id.spinner_algs);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.blurs, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerView.setAdapter(adapter);
		spinnerView.setOnItemSelectedListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		startBlurProcess(spinnerView.getSelectedItemPosition());
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id ) {
		startBlurProcess(pos);
	}

	private void startBlurProcess(int pos) {
		switch (pos) {
			case 0:
				setProcessing(new BlurProcessing(FactoryBlurFilter.mean(ImageUInt8.class, 2)) );
				break;

			case 1:
				setProcessing(new BlurProcessing(FactoryBlurFilter.gaussian(ImageUInt8.class,-1,2)) );
				break;

			case 2:
				setProcessing(new BlurProcessing(FactoryBlurFilter.median(ImageUInt8.class,2)) );
				break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {}

	protected class BlurProcessing extends VideoImageProcessing<ImageUInt8> {
		ImageUInt8 blurred;
		BlurFilter<ImageUInt8> filter;

		public BlurProcessing(BlurFilter<ImageUInt8> filter) {
			super(ImageType.single(ImageUInt8.class));
			this.filter = filter;
		}

		@Override
		protected void declareImages( int width , int height ) {
			super.declareImages(width, height);

			blurred = new ImageUInt8(width,height);
		}

		@Override
		protected void process(ImageUInt8 input, Bitmap output, byte[] storage) {
			filter.process(input,blurred);
			ConvertBitmap.grayToBitmap(blurred,output,storage);
		}
	}
}
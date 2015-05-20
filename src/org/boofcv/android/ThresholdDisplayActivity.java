package org.boofcv.android;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ToggleButton;

import boofcv.abst.filter.binary.InputToBinary;
import boofcv.android.VisualizeImageData;
import boofcv.android.gui.VideoImageProcessing;
import boofcv.factory.filter.binary.FactoryThresholdBinary;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.ImageUInt8;

/**
 * Automatic thresholding
 *
 * @author Peter Abeles
 */
public class ThresholdDisplayActivity extends DemoVideoDisplayActivity
{

	Spinner spinnerView;

	final Object lock = new Object();
	boolean changed = false;
	boolean down;
	int radius;
	int selectedAlg;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = getLayoutInflater();
		LinearLayout controls = (LinearLayout)inflater.inflate(R.layout.thresholding_controls,null);

		LinearLayout parent = getViewContent();
		parent.addView(controls);

		spinnerView = (Spinner)controls.findViewById(R.id.spinner_algs);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.thresholding, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerView.setAdapter(adapter);

		ToggleButton toggle = (ToggleButton)controls.findViewById(R.id.toggle_threshold);
		SeekBar seek = (SeekBar)controls.findViewById(R.id.slider_radius);

		changed = true;
		selectedAlg = spinnerView.getSelectedItemPosition();
		down = toggle.isChecked();
		radius = seek.getProgress();

		seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				synchronized (lock) {
					changed = true;
					radius = progress;
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});

		spinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				synchronized (lock) {
					changed = true;
					selectedAlg = position;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});

		toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				synchronized (lock) {
					changed = true;
					down = isChecked;
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		setProcessing(new ThresholdingProcessing());
	}

	private InputToBinary<ImageUInt8> createFilter() {

		int radius = this.radius + 1;

		switch (selectedAlg) {
			case 0:
				return FactoryThresholdBinary.globalOtsu(0,256,down,ImageUInt8.class);

			case 1:
				return FactoryThresholdBinary.globalEntropy(0, 256, down, ImageUInt8.class);

			case 2:
				return FactoryThresholdBinary.adaptiveSquare(radius,0,down,ImageUInt8.class);

			case 3:
				return FactoryThresholdBinary.adaptiveGaussian(radius,0,down,ImageUInt8.class);

			case 4:
				return FactoryThresholdBinary.adaptiveSauvola(radius,0,down,ImageUInt8.class);
		}

		throw new RuntimeException("Unknown selection "+selectedAlg);
	}

	protected class ThresholdingProcessing extends VideoImageProcessing<ImageUInt8> {
		ImageUInt8 binary;
		InputToBinary<ImageUInt8> filter;

		public ThresholdingProcessing() {
			super(ImageType.single(ImageUInt8.class));
		}

		@Override
		protected void declareImages( int width , int height ) {
			super.declareImages(width, height);

			binary = new ImageUInt8(width,height);
		}

		@Override
		protected void process(ImageUInt8 input, Bitmap output, byte[] storage) {

			synchronized ( lock ) {
				if (changed) {
					changed = false;
					filter = createFilter();
				}
			}

			if( filter != null ) {
				filter.process(input, binary);
			}
			VisualizeImageData.binaryToBitmap(binary, output, storage);
		}
	}
}
package org.boofcv.android;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Displays instructions and tips for the user
 *
 * @author Peter Abeles
 */
public class FiducialSquareImageHelpActivity extends Activity {

	private final static String text = "<p>Print the square image fiducial shown below. "+
			"A printable file can be found at the tutorial below on boofcv.org."+
			"<a href=\"http://boofcv.org/index.php?title=Tutorial_Fiducials\">Tutorial Fiducials</a></p><br>"+
			"</p>";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fiducial_help);

		TextView textView = (TextView) findViewById(R.id.text_info);

		textView.setMovementMethod(LinkMovementMethod.getInstance());
		textView.setText(Html.fromHtml(text));

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		Bitmap input = BitmapFactory.decodeResource(getResources(), R.drawable.fiducial_square_image, options);

		ImageView view = (ImageView) findViewById(R.id.imageView);
		view.setImageBitmap(input);
	}
}
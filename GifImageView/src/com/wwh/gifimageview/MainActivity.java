package com.wwh.gifimageview;

import com.wwh.gifimageview.ImageViewUtil.OnPushImageListener;

import android.support.v7.app.ActionBarActivity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class MainActivity extends ActionBarActivity {
	private UrlImageView mImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mImageView = (UrlImageView) findViewById(R.id.image_view);
		mImageView
				.setUrl("http://img4.duitang.com/uploads/item/201411/12/20141112224259_V8wWX.gif",
						new OnPushImageListener() {

							@Override
							public void onPushImageEnd(ImageView imageview,
									Bitmap bitmap) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onPushImageBegin() {
								// TODO Auto-generated method stub

							}
						});
		// ImageViewUtil
		// .PushImageIntoImageView(
		// "http://img4.duitang.com/uploads/item/201411/12/20141112224259_V8wWX.gif",
		// mImageView, new OnPushImageListener() {
		//
		// @Override
		// public void onPushImageEnd(ImageView imageview,
		// Bitmap bitmap) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void onPushImageBegin() {
		// // TODO Auto-generated method stub
		//
		// }
		// });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

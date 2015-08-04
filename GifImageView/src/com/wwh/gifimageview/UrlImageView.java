package com.wwh.gifimageview;

import com.wwh.gifimageview.ImageViewUtil.OnPushImageListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * 
 * @ClassName: UrlImageView
 * @Description: 从网络上获取支持gif支持缩放的ImageView
 * @author：WWH
 * @version V1.0
 */
public class UrlImageView extends ImageView {
	private Matrix matrix = new Matrix();
	private String img_url;
	int w, h;

	public void setUrl(String url, final OnPushImageListener onpushimagelistner) {
		WindowManager wm = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		w = wm.getDefaultDisplay().getWidth();
		h = wm.getDefaultDisplay().getHeight();
		img_url = url;
		ImageViewUtil.PushImageIntoImageView(img_url, this,
				new OnPushImageListener() {
					@Override
					public void onPushImageBegin() {
						// TODO Auto-generated method stub
						onpushimagelistner.onPushImageBegin();
					}

					@Override
					public void onPushImageEnd(ImageView imageview,
							Bitmap bitmap) {
						// TODO Auto-generated method stub
						int windowWidth = w;
						int windowHeight = h;
						int ImageWidth = bitmap.getWidth();
						int ImageHidth = bitmap.getHeight();
						float x_zoom = (float) windowWidth / (float) ImageWidth;
						float y_zoom = (float) windowHeight
								/ (float) ImageHidth;
						if (x_zoom < 1 && y_zoom < 1) {
							matrix.setScale(x_zoom, y_zoom);// 图片宽高都大于手机尺寸
						} else if (x_zoom > 1 && y_zoom < 1) {
							matrix.setScale(y_zoom, y_zoom);// 图片宽小高大
						} else {
							matrix.setScale(x_zoom, x_zoom);// 其他情况
						}
						center(true, true, bitmap);
						imageview.setImageMatrix(matrix);//
						imageview.setVisibility(View.VISIBLE);// 默认隐藏，下载图片成功后显示
						onpushimagelistner.onPushImageEnd(imageview, bitmap);
						UrlImageView.this
								.setOnTouchListener(new TouchListener());
					}

				});

	}

	public UrlImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		Init();
	}

	@SuppressLint("NewApi")
	public UrlImageView(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
		Init();
	}

	public UrlImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		Init();
	}

	public UrlImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		Init();
	}

	private void Init() {
		// TODO Auto-generated method stub
	}

	/**
	 * 设置imageview中图片区域始终居中
	 * 
	 * @param horizontal
	 * @param vertical
	 * @param bitmap
	 */
	private void center(boolean horizontal, boolean vertical, Bitmap bitmap) {
		Matrix m = new Matrix();
		m.set(matrix);
		RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
		m.mapRect(rect);
		float height = rect.height();
		float width = rect.width();
		float deltaX = 0, deltaY = 0;
		if (vertical) {
			int screenHeight = h; // 手机屏幕分辨率的高度
			// int screenHeight = 400;
			if (height < screenHeight) {
				deltaY = (screenHeight - 100 - height) / 2;
			} else if (rect.top > 0) {
				deltaY = -rect.top;
			} else if (rect.bottom < screenHeight) {
				deltaY = UrlImageView.this.getHeight() - rect.bottom;
			}
		}

		if (horizontal) {
			int screenWidth = w; // 手机屏幕分辨率的宽度
			// int screenWidth = 400;
			if (width < screenWidth) {
				deltaX = (screenWidth - width) / 2 - rect.left;
			} else if (rect.left > 0) {
				deltaX = -rect.left;
			} else if (rect.right < screenWidth) {
				deltaX = screenWidth - rect.right;
			}
		}
		matrix.postTranslate(deltaX, deltaY);
	}

	private final class TouchListener implements OnTouchListener {
		private PointF startPoint = new PointF();
		private Matrix currentMatrix = new Matrix();
		private int mode = 0;
		private static final int DRAG = 1;
		private static final int ZOOM = 2;
		private float startDis;// 开始距离
		private PointF midPoint;// 中间点
		private float left;
		private float top;
		private float right;
		private float bottom;
		Rect rect;

		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:// 手指压下屏幕
				mode = DRAG;
				currentMatrix.set(matrix);// 记录ImageView当前的移动位置
				startPoint.set(event.getX(), event.getY());
				float[] values = new float[9];
				currentMatrix.getValues(values);
				rect = ((ImageView) v).getDrawable().getBounds();
				left = values[Matrix.MTRANS_X];
				top = values[Matrix.MTRANS_Y];
				right = left + rect.width() * values[Matrix.MSCALE_X];
				bottom = top + rect.height() * values[Matrix.MSCALE_Y];
				break;
			case MotionEvent.ACTION_POINTER_DOWN:// 当屏幕上已经有触点（手指），再有一个手指压下屏幕
				mode = ZOOM;
				startDis = distance(event);
				if (startDis > 10f) {
					midPoint = mid(event);
					currentMatrix.set(UrlImageView.this.getImageMatrix());// 记录ImageView当前的缩放倍数
				}
				UrlImageView.this.setImageMatrix(matrix);
				break;
			case MotionEvent.ACTION_MOVE:// 手指在屏幕移动，该 事件会不断地触发
				if (mode == DRAG) {
					float dx = event.getX() - startPoint.x;// 得到在x轴的移动距离
					float dy = event.getY() - startPoint.y;// 得到在y轴的移动距离
					matrix.set(currentMatrix);// 在没有进行移动之前的位置基础上进行移动
					if (right - left < v.getWidth()) {
						dx = 0;
					} else if (left + dx > 0 && dx > 0) {
						dx = -left;
					} else if (right + dx < v.getRight() && dx < 0) {
						dx = v.getRight() - right;
					}
					if (bottom - top < v.getHeight()) {
						dy = 0;
					} else if (top + dy > 0 && dy > 0) {
						dy = -top;
					} else if (bottom + dy < v.getBottom() && dy < 0) {
						dy = v.getBottom() - bottom;
					}
					matrix.postTranslate(dx, dy);
					UrlImageView.this.setImageMatrix(matrix);
				} else if (mode == ZOOM) {// 缩放
					float endDis = distance(event);// 结束距离
					if (endDis > 10f) {
						float scale = endDis / startDis;// 得到缩放倍数
						matrix.set(currentMatrix);
						matrix.postScale(scale, scale, midPoint.x, midPoint.y);
						UrlImageView.this.setImageMatrix(matrix);
					}
				}
				break;
			case MotionEvent.ACTION_UP:// 手指离开屏
			case MotionEvent.ACTION_POINTER_UP:// 有手指离开屏幕,但屏幕还有触点（手指）
				mode = 0;
				break;
			}

			return true;
		}

	}

	/**
	 * 计算两点之间的距离
	 * 
	 * @param event
	 * @return
	 */
	public static float distance(MotionEvent event) {
		float dx = event.getX(1) - event.getX(0);
		float dy = event.getY(1) - event.getY(0);
		return FloatMath.sqrt(dx * dx + dy * dy);
	}

	/**
	 * 计算两点之间的中间点‰
	 * 
	 * @param event
	 * @return
	 */
	public static PointF mid(MotionEvent event) {
		float midX = (event.getX(1) + event.getX(0)) / 2;
		float midY = (event.getY(1) + event.getY(0)) / 2;
		return new PointF(midX, midY);
	}

}

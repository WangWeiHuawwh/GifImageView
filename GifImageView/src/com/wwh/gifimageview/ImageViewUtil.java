package com.wwh.gifimageview;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.view.View;
import android.widget.ImageView;

/**
 * 
 * @ClassName: ImageViewUtil
 * @Description: 加载网络图片到ImageView,支持gif,内存缓存
 * @author：WWH
 * @version V1.0
 */
public class ImageViewUtil {
	public interface OnPushImageListener {
		public void onPushImageBegin();

		public void onPushImageEnd(ImageView imageview, Bitmap bitmap);

	}

	public interface OnLoadImageListener {
		public void OnLoadImage(byte[] bitmap, String bitmapPath);
	}

	public interface OnLoadingProgressListener {
		public void onProgressUpdate(String imageUri, View view, int current,
				int total);
	}

	private transient static ExecutorService service;
	/**
	 * 缓存Image的类，当存储Image的大小大于LruCache设定的�?，系统自动释放内�?
	 */
	private static LruCache<String, byte[]> gifMemoryCache;

	public static boolean PushImageIntoImageView(final String img_url,
			final ImageView imageview, final OnPushImageListener onpushimage) {
		initgifmemorycache();
		getThreadPool();
		if (img_url != null) {
			final String subUrl = img_url.replaceAll("[^\\w]", "");
			byte[] bytegif = showCacheBitmap(subUrl);
			if (bytegif != null) {
				onpushimage.onPushImageBegin();
				ByteArrayInputStream is = new ByteArrayInputStream(bytegif);
				ByteArrayInputStream ismap = new ByteArrayInputStream(bytegif);
				Bitmap mybitmap = BitmapFactory.decodeStream(ismap);
				LetGifIntoImageView(is, imageview, mybitmap);
				if (mybitmap != null) {
					onpushimage.onPushImageEnd(imageview, mybitmap);
				}
				bytegif = null;
			} else {
				URL myurl = null;
				try {
					myurl = new URL(img_url);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				onpushimage.onPushImageBegin();
				ImageUtils.onLoadImage(myurl, imageview,
						new OnLoadImageListener() {
							@Override
							public void OnLoadImage(byte[] bytemap,
									String bitmapPath) {
								// TODO Auto-generated method stub
								if (imageview.getTag() != null
										&& !(imageview.getTag().equals(img_url))) {
									return;
								}
								ByteArrayInputStream is = new ByteArrayInputStream(
										bytemap);
								ByteArrayInputStream ismap = new ByteArrayInputStream(
										bytemap);
								Bitmap mybitmap = BitmapFactory
										.decodeStream(ismap);
								try {
									ismap.close();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								try {
									// 保存在SD卡或者手机目�?
									GifFileUtils.savaGifBitmap(subUrl, bytemap);
								} catch (IOException e) {
									e.printStackTrace();
								}
								// 将Bitmap 加入内存缓存
								addBitmapToMemoryCache(subUrl, bytemap);
								if (img_url.substring(
										img_url.lastIndexOf(".") + 1).equals(
										"gif")) {
									LetGifIntoImageView(is, imageview, mybitmap);
								} else {
									imageview.setImageBitmap(mybitmap);
								}
								if (mybitmap != null) {
									onpushimage.onPushImageEnd(imageview,
											mybitmap);
								}
							}
						});
				return true;
			}
		}
		return false;
	}

	public static void LetGifIntoImageView(ByteArrayInputStream is,
			ImageView imageview, Bitmap mybitmap) {
		GifAnimationDrawable gifanimationdrawable = null;
		try {
			gifanimationdrawable = new GifAnimationDrawable(is);
			gifanimationdrawable.setOneShot(false);// 是否循环
			imageview.setImageDrawable(gifanimationdrawable);
			gifanimationdrawable.setVisible(true, true);
		} catch (Exception e) { // TODO Auto-generated
			imageview.setImageBitmap(mybitmap);
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void initgifmemorycache() {
		if (gifMemoryCache == null) {
			// 获取系统分配给每个应用程序的�?��内存，每个应用系统分�?2M
			int maxMemory = (int) Runtime.getRuntime().maxMemory();
			int mCacheSize = maxMemory / 8;
			// 给LruCache分配1/8 4M
			gifMemoryCache = new LruCache<String, byte[]>(mCacheSize) {
				// 必须重写此方法，来测量的大小
				@Override
				protected int sizeOf(String key, byte[] value) {
					return value.length;
				}

			};
		}
	}

	public static ExecutorService getThreadPool() {
		if (service == null) {
			synchronized (ExecutorService.class) {
				if (service == null) {
					// 为了下载图片更加的流畅，我们用了3个线程来下载图片
					service = Executors.newFixedThreadPool(3);
				}
			}
		}
		return service;
	}

	/**
	 * 添加gif到内存缓�?
	 * 
	 * @param key
	 * @param bitmap
	 */
	public static void addBitmapToMemoryCache(String key, byte[] gifimg) {
		if (getBitmapFromMemCache(key) == null && gifimg != null) {
			gifMemoryCache.put(key, gifimg);
		}
	}

	/**
	 * 从内存缓存中获取�?��gif
	 * 
	 * @param key
	 * @return
	 */
	public static byte[] showCacheBitmap(String url) {
		if (getBitmapFromMemCache(url) != null) {
			return getBitmapFromMemCache(url);
		} else if (GifFileUtils.isFileExists(url)
				&& GifFileUtils.getFileSize(url) != 0) {
			return GifFileUtils.getGifBitmap(url);
		}
		return null;
	}

	public static byte[] getBitmapFromMemCache(String key) {
		return gifMemoryCache.get(key);
	}

	public static class ImageUtils {

		public static void onLoadImage(final URL bitmapUrl,
				final ImageView imageview,
				final OnLoadImageListener onLoadImageListener) {
			final Handler handler = new Handler() {
				public void handleMessage(Message msg) {
					onLoadImageListener.OnLoadImage((byte[]) msg.obj, null);
				}
			};
			service.execute(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					URL imageUrl = bitmapUrl;
					try {
						HttpURLConnection conn = (HttpURLConnection) imageUrl
								.openConnection();
						InputStream inputStream = conn.getInputStream();
						byte[] mybyte = toByteArray(inputStream,
								conn.getContentLength(), bitmapUrl.toString(),
								imageview);
						Message msg = new Message();
						msg.obj = mybyte;
						handler.sendMessage(msg);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
	}

	public static byte[] toByteArray(InputStream input, int all, String imgurl,
			ImageView imageview) throws IOException {// 转化为字节传�?
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int currte = 0;
		byte[] buffer = new byte[4096];
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			currte = currte + n;
			output.write(buffer, 0, n);
		}
		return output.toByteArray();
	}
}

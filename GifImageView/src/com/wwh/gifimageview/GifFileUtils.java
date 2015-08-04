package com.wwh.gifimageview;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import android.content.Context;
import android.os.Environment;

public class GifFileUtils {
	/**
	 * sd卡的根目�?
	 */
	private static String mSdRootPath = Environment
			.getExternalStorageDirectory().getPath();
	/**
	 * 手机的缓存根目录
	 */
	private static String mDataRootPath = null;
	private final static int MAXSIZE = 10;
	/**
	 * 保存GIfImage的目录名
	 */
	private final static String FOLDER_NAME = "/gifimage";

	public GifFileUtils(Context context) {
		mDataRootPath = context.getCacheDir().getPath();
	}

	/**
	 * 获取储存GIfImage的目�?
	 * 
	 * @return
	 */
	private static String getStorageDirectory() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED) ? mSdRootPath + FOLDER_NAME
				: mDataRootPath + FOLDER_NAME;
	}

	/**
	 * 保存GIfImage的方法，有sd卡存储到sd卡，没有就存储到手机目录
	 * 
	 * @param fileName
	 * @param GifBitmap
	 * @throws IOException
	 */
	public static void savaGifBitmap(String fileName, byte[] GifBitmap)
			throws IOException {
		deleteFileOne();// 保存之前先查看内存做适当删除
		if (GifBitmap == null) {
			return;
		}
		String path = getStorageDirectory();
		File folderFile = new File(path);
		if (!folderFile.exists()) {
			folderFile.mkdir();
		}
		File file = new File(path + File.separator + fileName);
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		try {
			fos.write(GifBitmap);
			fos.flush();
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 从手机或者sd卡获取GIfImage
	 * 
	 * @param fileName
	 * @return
	 */
	public static byte[] getGifBitmap(String fileName) {
		FileInputStream fin = null;
		byte[] buffer = null;
		try {
			fin = new FileInputStream(getStorageDirectory() + File.separator
					+ fileName);
			buffer = new byte[fin.available()];
			fin.read(buffer);
			// ByteArrayOutputStream output = new ByteArrayOutputStream();
			// byte[] buffer = new byte[4096];
			// int n = 0;
			// while (-1 != (n = fin.read(buffer))) {
			// output.write(buffer, 0, n);
			// }
			// fin.close();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				fin.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return buffer;
	}

	/**
	 * 判断文件是否存在
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean isFileExists(String fileName) {
		return new File(getStorageDirectory() + File.separator + fileName)
				.exists();
	}

	/**
	 * 获取文件的大�?
	 * 
	 * @param fileName
	 * @return
	 */
	public static long getFileSize(String fileName) {
		return new File(getStorageDirectory() + File.separator + fileName)
				.length();
	}

	/**
	 * 如果超出大小则删除最早的�?��图片
	 */
	public static void deleteFileOne() {
		File dirFile = new File(getStorageDirectory());
		if (!dirFile.exists()) {
			return;
		}
		if (dirFile.isDirectory()) {
			String[] children = dirFile.list();
			long sumsize = 0;
			for (int i = 0; i < children.length; i++) {
				sumsize = sumsize + (new File(dirFile, children[i]).length())
						/ (1024);
			}
			if (sumsize / 1024 < MAXSIZE) {
				return;
			}
			long firsttime = dirFile.lastModified();
			String deletfile = null;
			for (int i = 0; i < children.length; i++) {
				File temp = null;
				if ((temp = new File(dirFile, children[i])).lastModified() < firsttime) {
					firsttime = temp.lastModified();
					deletfile = children[i];
				}
			}
			if (deletfile != null) {
				new File(dirFile, deletfile).delete();
			}
		}
	}

}

package com.alkaid.ojpl.common;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5 {
	/**
	 * 返回16位md5码
	 * @param data
	 * @return
	 */
	public static String toMd5(String data) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(data.getBytes("UTF-8"));
			byte b[] = md.digest();
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			return buf.toString().substring(8, 24);
//			System.out.println("result: " + buf.toString());// 32
//			System.out.println("result: " + buf.toString().substring(8, 24));// 16
		} catch (NoSuchAlgorithmException e) {
			LogUtil.e(e);
		} catch (UnsupportedEncodingException e) {
			LogUtil.e(e);
		}
		return null;
	}
}

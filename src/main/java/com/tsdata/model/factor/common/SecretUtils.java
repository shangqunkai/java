package com.tsdata.model.factor.common;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class SecretUtils {
	private static final char[] HEX_CHAR = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };

	private static final String RC4 = "RC4";
	private static final String UTF8 = "UTF-8"; 
	
	/**
	 * 加密
	 * 
	 * @param content
	 *            待加密内容
	 * @param password
	 *            加密密钥
	 * @return
	 */
	public static String getAESSecret(String content, String password) {
		try {
			byte[] kb = password.getBytes("utf-8");
			SecretKeySpec sks = new SecretKeySpec(kb, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// 算法/模式/补码方式
			cipher.init(Cipher.ENCRYPT_MODE, sks);
			byte[] eb = cipher.doFinal(content.getBytes("utf-8"));
			return bytesToHexFun2(eb);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * byte[] to hex string
	 *
	 * @param bytes
	 * @return
	 */
	public static String bytesToHexFun2(byte[] bytes) {
		char[] buf = new char[bytes.length * 2];
		int index = 0;
		for (byte b : bytes) { // 利用位运算进行转换
			buf[index++] = HEX_CHAR[b >>> 4 & 0xf];
			buf[index++] = HEX_CHAR[b & 0xf];
		}

		return new String(buf);
	}

	// md5加密获得签名
	public static String getSignatureZqxy(String appSecret, Map<String, String> params) {
		String data = getSignData(params);
		return getMD5(appSecret + data);
	}

	// 按字典key首字母排序
	private static String getSignData(Map<String, String> params) {
		StringBuffer content = new StringBuffer();
		// 按照key做排序
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			Object value = params.get(key);
			if (value == null || value instanceof byte[] || value instanceof File) {
				continue;
			}
			content.append((i == 0 || content.length() == 0 ? "" : "") + key + value.toString());
		}
		return content.toString();
	}

	// 生成MD5
	public static String getMD5(String message) {
		String md5 = "";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5"); // 创建一个md5算法对象
			byte[] messageByte = message.getBytes("UTF-8");
			byte[] md5Byte = md.digest(messageByte); // 获得MD5字节数组,16*8=128位
			md5 = bytesToHex(md5Byte); // 转换为16进制字符串
		} catch (Exception e) {
			e.printStackTrace();
		}
		return md5;
	}

	// 二进制转十六进制
	public static String bytesToHex(byte[] bytes) {
		StringBuffer hexStr = new StringBuffer();
		int num;
		for (int i = 0; i < bytes.length; i++) {
			num = bytes[i];
			if (num < 0) {
				num += 256;
			}
			if (num < 16) {
				hexStr.append("0");
			}
			hexStr.append(Integer.toHexString(num));
		}
		return hexStr.toString().toUpperCase();
	}

	public static String getSHA256Str(String str) {
		MessageDigest messageDigest;
		String encodeStr = "";
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(str.getBytes("UTF-8"));
			encodeStr = byte2Hex(messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return encodeStr;
	}

	private static String byte2Hex(byte[] bytes) {
		StringBuffer stringBuffer = new StringBuffer();
		String temp = null;
		for (int i = 0; i < bytes.length; i++) {
			temp = Integer.toHexString(bytes[i] & 0xFF);
			if (temp.length() == 1) {
				stringBuffer.append("0");
			}
			stringBuffer.append(temp);
		}
		return stringBuffer.toString();
	}
	
	/**
	 * RC4加密明文（可能包含汉字），输出是经过Base64的；如果加密失败，返回值是null
	 * @param plainText
	 * @param rc4Key
	 * @return
	 */
	public static final String encode( final String plainText, final String rc4Key )
	{
		try
		{
			final Cipher c1 = Cipher.getInstance(RC4);
			c1.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(rc4Key.getBytes(), RC4));
			return new String( Base64.encodeBase64(c1.doFinal(plainText.getBytes(UTF8))) );
		}
		catch(final Throwable t)
		{
			t.printStackTrace();
			return null;
		}
	}
	
	/**
	 * RC4从密文解密为明文，输入是经过Base64的；如果解密失败，返回值是null
	 * @param encodedText
	 * @param rc4Key
	 * @return
	 */
	public static final String decode( final String encodedText, final String rc4Key )
	{
		try
		{
			final Cipher c1 = Cipher.getInstance(RC4);
			c1.init(Cipher.DECRYPT_MODE, new SecretKeySpec(rc4Key.getBytes(), RC4));
			return new String( c1.doFinal(Base64.decodeBase64(encodedText.getBytes())), UTF8 );
		}
		catch( final Throwable t )
		{
			t.printStackTrace();
			return null;
		}		
	}
	
	/**
     * 对密文进行URL解码、RC4解密.
     *
     * @param text 密文
     * @return 明文
     */
    public static String decrypt(String text,String rc4Key) throws UnsupportedEncodingException {
        /** URL解码 */
        String textDecoded = URLDecoder.decode(text, "utf-8");
        /** RC4解密 */
        String textDecodedDecrypted = decode(textDecoded, rc4Key);
        return textDecodedDecrypted;
    }
}

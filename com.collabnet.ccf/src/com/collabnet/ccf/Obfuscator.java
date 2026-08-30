package com.collabnet.ccf;

import org.apache.commons.codec.binary.Base64;

public class Obfuscator {

	public static String obfuscateString(String string) {
		byte[] bytes = string.getBytes();
		for (int i =0;i<bytes.length;++i) {
			bytes[i] = cyclicShiftBitsRight(bytes[i], 4);
		}
		
		return new String(Base64.encodeBase64(bytes));
	}
	
	private static byte cyclicShiftBitsRight(int b, int i) {
		if (0 > b) {
			b+=256;
		}
		int j = (((b >>> i) % 256) | (b << (8-i)));
		if (127 < j) {
			j-=256;
		}
		return (byte) j;
	}
	
	private static byte cyclicShiftBitsLeft(int b, int i) {
		if (0 > b) {
			b+=256;
		}
		int j = ((b << i) | ((b >>> (8-i)) % 256));
		if (127 < j) {
			j-=256;
		}
		return (byte) j;
	}

	public static String deObfuscateString(String string) {
		byte[] bytes;
		bytes = Base64.decodeBase64(string.getBytes());
		for (int i =0;i<bytes.length;++i) {
			bytes[i] = cyclicShiftBitsLeft(bytes[i], 4);
		}
		return new String(bytes);
	}
	
}

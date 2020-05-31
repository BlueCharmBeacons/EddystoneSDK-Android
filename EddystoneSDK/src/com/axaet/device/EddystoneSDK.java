package com.axaet.device;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

public class EddystoneSDK {

	public static ArrayMap<String, String> hashMapSuffix;
	public static ArrayMap<String, String> hashMapPrefix;

	private EddystoneSDK() {
	}

	public static void initialize() {
		hashMapSuffix = new ArrayMap<String, String>();
		hashMapSuffix.clear();
		hashMapSuffix.put("00", ".com/");
		hashMapSuffix.put("01", ".org/");
		hashMapSuffix.put("02", ".edu/");
		hashMapSuffix.put("03", ".net/");
		hashMapSuffix.put("04", ".info/");
		hashMapSuffix.put("05", ".biz/");
		hashMapSuffix.put("06", ".gov/");
		hashMapSuffix.put("07", ".com");
		hashMapSuffix.put("08", ".org");
		hashMapSuffix.put("09", ".edu");
		hashMapSuffix.put("0a", ".net");
		hashMapSuffix.put("0b", ".info");
		hashMapSuffix.put("0c", ".biz");
		hashMapSuffix.put("0d", ".gov");

		hashMapPrefix = new ArrayMap<String, String>();
		hashMapPrefix.clear();
		hashMapPrefix.put("00", "http://www.");
		hashMapPrefix.put("01", "https://www.");
		hashMapPrefix.put("02", "http://");
		hashMapPrefix.put("03", "https://");
	}


	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	
	public static String stringToHex(String s) {
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			s = new String(baKeyword, "utf-8");// UTF-16le:Not
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
	}


	
	public static double calculateAccuracy(int txPower, double rssi) {
		if (rssi == 0) {
			return -1.0; // if we cannot determine accuracy, return -1.
		}
		double ratio = rssi * 1.0 / txPower;
		if (ratio < 1.0) {
			return Math.pow(ratio, 10);
		} else {
			double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
			return accuracy;
		}
	}

	
	public static byte[] hex2Byte(String uuid) {
		byte[] data = new byte[17];
		int[] uuid_int = new int[32];
		char[] uuid_c = uuid.toCharArray();
		for (int i = 0; i < 32; i++) {
			if (uuid_c[i] == '0') {
				uuid_int[i] = 0;
			}
			if (uuid_c[i] == '1') {
				uuid_int[i] = 1;
			}
			if (uuid_c[i] == '2') {
				uuid_int[i] = 2;
			}
			if (uuid_c[i] == '3') {
				uuid_int[i] = 3;
			}
			if (uuid_c[i] == '4') {
				uuid_int[i] = 4;
			}
			if (uuid_c[i] == '5') {
				uuid_int[i] = 5;
			}
			if (uuid_c[i] == '6') {
				uuid_int[i] = 6;
			}
			if (uuid_c[i] == '7') {
				uuid_int[i] = 7;
			}
			if (uuid_c[i] == '8') {
				uuid_int[i] = 8;
			}
			if (uuid_c[i] == '9') {
				uuid_int[i] = 9;
			}
			if (uuid_c[i] == 'A' || uuid_c[i] == 'a') {
				uuid_int[i] = 10;
			}
			if (uuid_c[i] == 'B' || uuid_c[i] == 'b') {
				uuid_int[i] = 11;
			}
			if (uuid_c[i] == 'C' || uuid_c[i] == 'c') {
				uuid_int[i] = 12;
			}
			if (uuid_c[i] == 'D' || uuid_c[i] == 'd') {
				uuid_int[i] = 13;
			}
			if (uuid_c[i] == 'E' || uuid_c[i] == 'e') {
				uuid_int[i] = 14;
			}
			if (uuid_c[i] == 'F' || uuid_c[i] == 'f') {
				uuid_int[i] = 15;
			}
		}
		data[0] = (byte) 0x01;
		for (int i = 0; i < 16; i++) {
			data[i + 1] = (byte) (uuid_int[2 * i] * 16 + uuid_int[2 * i + 1]);
		}
		return data;
	}

	
	public static byte[] str2Byte(String string, byte b) {
		char[] cs = string.toCharArray();
		byte[] bs = new byte[cs.length + 1];
		bs[0] = b;
		for (int i = 1; i <= cs.length; i++) {
			bs[i] = (byte) cs[i - 1];
		}
		return bs;
	}

	
	public static byte[] str2ByteDeviceName(String string) {
		if (!TextUtils.isEmpty(string)&&string.length()>15) {
			string=string.substring(0, 15);
		}
		char[] cs = string.toCharArray();
		byte[] bs = new byte[cs.length + 2];
		bs[0] = (byte) 0x08;
		bs[1] = (byte) cs.length;
		for (int i = 2; i <= cs.length + 1; i++) {
			bs[i] = (byte) cs[i - 2];
		}
		return bs;
	}

	
	public static boolean hasFullSize(String inStr) {
		if (inStr.getBytes().length != inStr.length()) {
			return true;
		}
		return false;
	}

	
	public static byte[] url2Byte(String string) {

		string = string.replace(" ", "");
		byte[] bs = new byte[22];
		bs[0] = 0x01;
		if (string.contains("http://www.")) {
			String[] string2 = string.split("http://www.");
			bs[2] = 0x00;
			if (string2.length > 1) {
				string = string2[1];
			} else {
				return null;
			}
		} else if (string.contains("https://www.")) {
			String[] string2 = string.split("https://www.");
			bs[2] = 0x01;
			if (string2.length > 1) {
				string = string2[1];
			} else {
				return null;
			}
		} else if (string.contains("http://")) {
			String[] string2 = string.split("http://");
			bs[2] = 0x02;
			if (string2.length > 1) {
				string = string2[1];
			} else {
				return null;
			}
		} else if (string.contains("https://")) {
			String[] string2 = string.split("https://");
			bs[2] = 0x03;
			if (string2.length > 1) {
				string = string2[1];
			} else {
				return null;
			}
		}
		int num = 3;
		for (int i = 0; i < string.length() && num < 22; i++, num++) {
			if (string.charAt(i) == '.') {
				if (string.length() - i >= 4) {
					String flag1 = string.substring(i, i + 4);
					if (".com".equals(flag1)) {
						if (string.length() - i >= 5) {
							String flag2 = string.substring(i, i + 5);
							if (".com/".equals(flag2)) {
								bs[num] = 0x00;
								i = i + 4;
								continue;
							}
						}
						bs[num] = 0x07;
						i = i + 3;
						continue;
					} else if (".org".equals(flag1)) {
						if (string.length() - i >= 5) {
							String flag2 = string.substring(i, i + 5);
							if (".org/".equals(flag2)) {
								bs[num] = 0x01;
								i = i + 4;
								continue;
							}
						}
						bs[num] = 0x08;
						i = i + 3;
						continue;
					} else if (".edu".equals(flag1)) {
						if (string.length() - i >= 5) {
							String flag2 = string.substring(i, i + 5);
							if (".edu/".equals(flag2)) {
								bs[num] = 0x02;
								i = i + 4;
								continue;
							}
						}
						bs[num] = 0x09;
						i = i + 3;
						continue;
					} else if (".net".equals(flag1)) {
						if (string.length() - i >= 5) {
							String flag2 = string.substring(i, i + 5);
							if (".net/".equals(flag2)) {
								bs[num] = 0x03;
								i = i + 4;
								continue;
							}
						}
						bs[num] = 0x0a;
						i = i + 3;
						continue;
					} else if (".biz".equals(flag1)) {
						if (string.length() - i >= 5) {
							String flag2 = string.substring(i, i + 5);
							if (".biz/".equals(flag2)) {
								bs[num] = 0x05;
								i = i + 4;
								continue;
							}
						}
						bs[num] = 0x0c;
						i = i + 3;
						continue;
					} else if (".gov".equals(flag1)) {
						if (string.length() - i >= 5) {
							String flag2 = string.substring(i, i + 5);
							if (".gov/".equals(flag2)) {
								bs[num] = 0x06;
								i = i + 4;
								continue;
							}
						}
						bs[num] = 0x0d;
						i = i + 3;
						continue;
					} else if (".inf".equals(flag1)) {
						if (string.length() - i >= 5) {
							flag1 = string.substring(i, i + 5);
							if (".info".equals(flag1)) {
								if (string.length() - i >= 6) {
									String flag2 = string.substring(i, i + 6);
									if (".info/".equals(flag2)) {
										bs[num] = 0x04;
										i = i + 5;
										continue;
									}
								}
								bs[num] = 0x0b;
								i = i + 4;
								continue;
							}
						}
					}

				}
				bs[num] = (byte) string.charAt(i);
			} else {
				bs[num] = (byte) string.charAt(i);
			}
		}
		bs[1] = (byte) (num - 2);
		return bs;
	}

	
	public static String getUrlPrefix(String urlPro) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < urlPro.length(); i += 2) {
			String tempBit = urlPro.substring(i, i + 2);
			String temp = hashMapPrefix.get(tempBit);
			if (temp == null) {
				temp = EddystoneSDK.stringToHex(tempBit);
			}
			buffer.append(temp);
		}
		return buffer.toString();
	}

	
	public static String getUrlSuffix(String urlTemp) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < urlTemp.length(); i += 2) {
			String tempBit = urlTemp.substring(i, i + 2);
			String temp = hashMapSuffix.get(tempBit);
			if (temp == null) {
				temp = EddystoneSDK.stringToHex(tempBit);
			}
			buffer.append(temp);
		}
		return buffer.toString();
	}
}

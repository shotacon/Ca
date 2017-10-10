package Utils;
/**
 * Base64
 * <p>Description: å¯é?†åŠ å¯†ç®—æ³? </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Nova</p>
 * @author lanckun
 * @version 1.0
 */

import java.io.UnsupportedEncodingException;

public class Base64 {

	private Base64() {
	}

	private static String encode(byte abyte0[]) {
		StringBuffer stringbuffer = new StringBuffer();
		int i = 0;
		int j = abyte0.length;
		int k = j % 3;
		for (int l = j / 3; l > 0; l--) {
			byte byte0 = abyte0[i++];
			byte byte3 = abyte0[i++];
			byte byte5 = abyte0[i++];
			stringbuffer.append(enc_table[byte0 >>> 2 & 0x3f]);
			stringbuffer.append(enc_table[(byte0 << 4 & 0x30)
					+ (byte3 >>> 4 & 0xf)]);
			stringbuffer.append(enc_table[(byte3 << 2 & 0x3c)
					+ (byte5 >>> 6 & 0x3)]);
			stringbuffer.append(enc_table[byte5 & 0x3f]);
			if (i != 0 && i % 57 == 0) {
				stringbuffer.append("");
			}
		}

		if (k == 1) {
			byte byte1 = abyte0[i++];
			stringbuffer.append(enc_table[byte1 >>> 2 & 0x3f]);
			stringbuffer.append(enc_table[byte1 << 4 & 0x30]);
			stringbuffer.append('=');
			stringbuffer.append('=');
		}
		if (k == 2) {
			byte byte2 = abyte0[i++];
			byte byte4 = abyte0[i++];
			stringbuffer.append(enc_table[byte2 >>> 2 & 0x3f]);
			stringbuffer.append(enc_table[(byte2 << 4 & 0x30)
					+ (byte4 >>> 4 & 0xf)]);
			stringbuffer.append(enc_table[byte4 << 2 & 0x3c]);
			stringbuffer.append('=');
		}
		stringbuffer.append("");
		return stringbuffer.toString();
	}

	public static String encode(String s) {
		byte[] abyte0 = null;
		// abyte0 = s.getBytes();
		try {
			// byte[] abyte0 = abyte0 =
			// s.getBytes(System.getProperty("db.charset", "GBK"));
			abyte0 = s.getBytes(System.getProperty("db.charset", "GBK"));
			return encode(abyte0);
		} catch (UnsupportedEncodingException ex) {
			return null;
		}
	}

	private static byte[] decode(byte abyte0[]) {
		int k = abyte0.length;
		int l = 0;
		for (int i = k - 1; i >= 0; i--) {
			if (abyte0[i] > 32) {
				l++;
			}
		}
		if (l % 4 != 0) {
			throw new IllegalArgumentException("Length not a multiple of 4");
		}
		int i1 = (l / 4) * 3;
		if (abyte0[l - 1] == 61) {
			i1--;
		}
		if (abyte0[l - 2] == 61) {
			i1--;
		}
		byte abyte1[] = new byte[i1];
		int j = 0;
		byte abyte2[] = new byte[4];
		int j1 = 0;
		int k1 = 0;
		abyte2[0] = abyte2[1] = abyte2[2] = abyte2[3] = 61;
		while (j < k) {
			byte byte0 = abyte0[j++];
			if (byte0 > 32) {
				abyte2[k1++] = byte0;
			}
			if (k1 == 4) {
				j1 += decode(abyte1, j1, abyte2[0], abyte2[1], abyte2[2],
						abyte2[3]);
				k1 = 0;
				abyte2[0] = abyte2[1] = abyte2[2] = abyte2[3] = 61;
			}
		}
		if (k1 > 0) {
			decode(abyte1, j1, abyte2[0], abyte2[1], abyte2[2], abyte2[3]);
		}
		return abyte1;
	}

	private static short decode(byte abyte0[], int i, byte byte0, byte byte1,
			byte byte2, byte byte3) {
		byte byte4 = dec_table[byte0];
		byte byte5 = dec_table[byte1];
		byte byte6 = dec_table[byte2];
		byte byte7 = dec_table[byte3];
		if (byte4 == -1 || byte5 == -1 || byte6 == -1 && byte2 != 61
				|| byte7 == -1 && byte3 != 61) {
			throw new IllegalArgumentException("Invalid character [" + byte0
					+ ", " + byte1 + ", " + byte2 + ", " + byte3 + "]");
		}
		abyte0[i++] = (byte) (byte4 << 2 | byte5 >>> 4);
		if (byte2 == 61) {
			return 1;
		}
		abyte0[i++] = (byte) (byte5 << 4 | byte6 >>> 2);
		if (byte3 == 61) {
			return 2;
		} else {
			abyte0[i++] = (byte) (byte6 << 6 | byte7);
			return 3;
		}
	}

	public static String decode(String s) {
		byte[] abyte0 = s.getBytes();
		return new String(decode(abyte0));
	}

	private static final char enc_table[] = { 'A', 'B', 'C', 'D', 'E', 'F',
			'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
			'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
			'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
			't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', '+', '/' };

	private static final byte dec_table[] = { -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1,
			-1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
			13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1,
			-1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
			41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1 };

	public static void main(String args[]) {
		System.out.println("Encoded = "
				+ encode("hmcrm/hmcrm@192.168.1.18:1572:dev"));
		System.out.println("Decoded = "
				+ decode("aG1jcm0vaG1jcm1AMTkyLjE2OC4xLjE4OjE1NzI6ZGV2"));
	}

}

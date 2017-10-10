package Utils;

//package com;

import static sun.security.pkcs11.wrapper.PKCS11Constants.CKA_CLASS;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKA_ID;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKA_VALUE;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKO_CERTIFICATE;
//import static sun.security.pkcs11.wrapper.PKCS11Constants.CKF_RW_SESSION;
//import static sun.security.pkcs11.wrapper.PKCS11Constants.CKF_SERIAL_SESSION;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKO_PRIVATE_KEY;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.ProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;

import sun.security.pkcs11.wrapper.CK_ATTRIBUTE;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_MECHANISM;
import sun.security.pkcs11.wrapper.CK_MECHANISM_INFO;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Exception;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;

/**
 * @author niu_shengwei
 * @date  2012-9-12
 */
public class Pkcs11 {
// providerLibPath为P11库的路径

//2001
	private static final String providerLibPath=System.getProperty("user.dir")+"\\ShuttleCsp11_3003.dll";
//	private static final String providerLibPath="\\lib\\ShuttleCsp11_3003.dll";
//2003
//	private static final String providerLibPath="C:/WINDOWS/system32/eps2003csp11.dll";


	private static final long CKU_USER = 1;
	private static final long CKF_SERIAL_SESSION = 4L;
	private static final long CKF_RW_SESSION = 2L;
	public  static final long CKM_RSA_X_509 = 0x00000003L;
	public  static final long CKM_RSA_PKCS = 0x00000001L;
	private static final CK_ATTRIBUTE ATTR_CLASS_PKEY = new CK_ATTRIBUTE(CKA_CLASS, CKO_PRIVATE_KEY);
	private static final CK_ATTRIBUTE ATTR_CLASS_CERT = new CK_ATTRIBUTE(CKA_CLASS, CKO_CERTIFICATE);
	private static final long FINDOBJECTS_MAX = 100;
	private static final long NULLSESSION = 0xFFFFFFFF;
	private static long session=NULLSESSION;
	private static long slot;
	private static PKCS11 pkcs11;
	private static ObjectIdentifier objectIdentifier;
	private static long privateKeyHandle;
	
	/*
	 * 实例化一个P11并获取可以可用的slot
	 */
	public static PKCS11 getPKCS11() {
		long[] keyslots = null;
		try {
			FileWriter fw = null;
			try {
			//如果文件存在，则追加内容；如果文件不存在，则创建文件
			File f=new File(new File("").getAbsolutePath()+"\\CAlog.txt");
			fw = new FileWriter(f, true);
			} catch (IOException e) {
			e.printStackTrace();
			}
			PrintWriter pw = new PrintWriter(fw);
			pw.println(providerLibPath);
			pw.flush();
			try {
			fw.flush();
			pw.close();
			fw.close();
			} catch (IOException e) {
			e.printStackTrace();
			}
			pkcs11 = (PKCS11) PKCS11.class.getMethod("getInstance",new Class[] { String.class, String.class,CK_C_INITIALIZE_ARGS.class,boolean.class }).invoke(null,new Object[] { providerLibPath,"C_GetFunctionList", null, false });
			System.out.println("1");
		} catch (Exception e) 
		{
			try {
					pkcs11 = (PKCS11) PKCS11.class.getMethod("getInstance",new Class[] { String.class,CK_C_INITIALIZE_ARGS.class,boolean.class }).invoke(null,new Object[] { providerLibPath, null, false });
			System.out.println("2");
			} catch (Exception ex) 
			{
				ex.printStackTrace();
				}
		}
		if (pkcs11 != null)
		{
			try {
					keyslots = pkcs11.C_GetSlotList(true);
					if (keyslots != null && keyslots.length > 0) 
					{    
						slot = keyslots[0];
						System.out.println("UsbKeyExistent............................");
						return pkcs11;
					}
				} catch (PKCS11Exception e)
				{
					e.printStackTrace();
				}
		}
		return null;
	}
	
	//获取可用的mechanism信息
		public static void getMechanismInfo() throws PKCS11Exception {
			long[] l=pkcs11.C_GetMechanismList(slot);
			for(int i=0;i<l.length;i++){
				CK_MECHANISM_INFO info=pkcs11.C_GetMechanismInfo(slot, l[i]);
				System.out.println(info.toString());
				System.out.println("*************************************");
			}
		}
		/**
		 * 查找UKey中的证书
		 * @return List<Certificate> 证书集合
		 */
		public static List<X509Certificate> getCertificate() throws PKCS11Exception,
				CertificateException {
			List<X509Certificate> certList = null;
//			获取UsbKey中的证书要先打开一个会话
			openSession();
			try {
				/* find certificate handles begin */
				CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[] { ATTR_CLASS_CERT };
				pkcs11.C_FindObjectsInit(session, attrs);
				long[] certHandles = pkcs11.C_FindObjects(session, FINDOBJECTS_MAX);
				pkcs11.C_FindObjectsFinal(session);
				int certCount = certHandles.length;
				System.out.println("Find Certificate Handles:" + certCount);
				/* find certificate handles end */
				if (certCount > 0) {
					certList = new ArrayList<X509Certificate>(certCount);
				}
				/* get certificate object begin */
				for (long handle : certHandles) {
					attrs = new CK_ATTRIBUTE[] { new CK_ATTRIBUTE(CKA_ID),
							new CK_ATTRIBUTE(CKA_VALUE) };
					pkcs11.C_GetAttributeValue(session, handle, attrs);
					System.out.println("C_GetAttributeValue with Certificate handle:"+ handle);
					if (attrs[0].pValue == null) {
						continue;
					}
					BigInteger cka_id = attrs[0].getBigInteger();
					System.out.println("CKA_ID:" + cka_id.toString(16).toUpperCase());
					byte[] bytes = attrs[1].getByteArray();
					if (bytes == null) {
						closeSession();
						throw new CertificateException("unexpectedly retrieved null byte array");
					}
					CertificateFactory cf = CertificateFactory.getInstance("X.509");
//					Certificate cert = cf.generateCertificate(new ByteArrayInputStream(bytes));
					X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(bytes));
					System.out.println("Conert Certificate:\n".concat(cert.toString()));
					certList.add(cert);
				}
				/* get certificate object end */
			} catch (PKCS11Exception e) {
				closeSession();
				throw e;
			} catch (CertificateException e) {
				closeSession();
				throw e;
			}
			closeSession();
			return certList;
		}	
		
	/*
	 * login UsbKey 
	 */
	public static boolean checkPasswd(char[] passwd) throws PKCS11Exception {
		try {
			openSession();
			pkcs11.C_Login(session, CKU_USER, passwd);
			return true;
		} catch (PKCS11Exception e) {
			e.printStackTrace();
			closeSession();
		}
		return false;
	}
	
	/*
	 * 获取privateKeyHandle
	 * 返回第一个私钥对象的handle
	 */
	public static void pair(PKCS11 pkcs11, long session) throws PKCS11Exception,CertificateException, KeyException {
		CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[] { ATTR_CLASS_PKEY };
		pkcs11.C_FindObjectsInit(session, attrs);
		long[] privateKeyHandles = pkcs11.C_FindObjects(session, FINDOBJECTS_MAX);
		pkcs11.C_FindObjectsFinal(session);
		attrs = null;
		for (long handle : privateKeyHandles) {
			privateKeyHandle = handle;
			return;
		}
	}
	/*
	 * 执行签名
	 */
	public static byte[] sign(byte[] plaintext) throws IOException {
		try {
			int i = 1024;//指定RSA密钥长度1024位。
			int nTemp=0;
			//对签名原文做MD5或者SHA1摘要
//			MessageDigest md = MessageDigest.getInstance("MD5");
			MessageDigest md = MessageDigest.getInstance("SHA1");
			md.update(plaintext);
			byte[] b=md.digest();
			//encodeSignature & pkcs1Pad
			//sha1 oid
			objectIdentifier=ObjectIdentifier.newInternal(new int[] { 1,3,14,3,2,26 });
			//md5 oid
			//objectIdentifier=ObjectIdentifier.newInternal(new int[] { 1, 2, 840, 113549, 2, 5 });
			System.out.println("sha1 result:");
			for(nTemp=0;nTemp<b.length;nTemp++)
				System.out.print(String.format("%02X ",b[nTemp]));
			System.out.println("\nsha1 2 result:");
						
			byte[] b2 = encodeSignature(objectIdentifier,b);
			
			for(nTemp=0;nTemp<b2.length;nTemp++)
			{
				System.out.print(String.format("%02X ",b2[nTemp]));
			}
			//采用pkcs1V1.5标准补位
			
			byte[] b3 = pkcs1Pad(b2, i);
			
			System.out.println("\npading  result:");
			for(nTemp=0;nTemp<b3.length;nTemp++)
			{	if(0==nTemp%16)
					System.out.println("");
				System.out.print(String.format("%02X ",b3[nTemp]));
			}
			//签名
			pkcs11.C_SignInit(session, new CK_MECHANISM(CKM_RSA_X_509),privateKeyHandle);
			byte[] signature = pkcs11.C_Sign(session, b3);
			return signature;
		} catch (PKCS11Exception e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static byte[] encodeSignature(ObjectIdentifier paramObjectIdentifier, byte[] paramArrayOfByte)throws IOException
	{
		DerOutputStream localDerOutputStream = new DerOutputStream();
		new AlgorithmId(paramObjectIdentifier).encode(localDerOutputStream);
		localDerOutputStream.putOctetString(paramArrayOfByte);
		DerValue localDerValue = new DerValue((byte)48, localDerOutputStream.toByteArray());
		return localDerValue.toByteArray();
	}
	private  static byte[] pkcs1Pad(byte[] data, int i) {
		try {
			int paddedSize = (i + 7) >> 3;
			int maxDataSize = paddedSize - 11;
			if (data.length > maxDataSize) {
				throw new BadPaddingException("Data must be shorter than "+ (maxDataSize + 1) + " bytes");
			}
			byte[] padded = new byte[paddedSize];
			System.arraycopy(data, 0, padded, paddedSize - data.length,data.length);
			int psSize = paddedSize - 3 - data.length;
			int k = 0;
			padded[k++] = 0;
			padded[k++] = (byte) 1;
			while (psSize-- > 0) {
				padded[k++] = (byte) 0xff;
			}
			return padded;
		} catch (GeneralSecurityException e) {
			throw new ProviderException(e);
		}
	}
	
	private static void openSession() throws PKCS11Exception {
		if (session != NULLSESSION) {
			closeSession();
		}
		System.out.println("Session opening...");
		open(slot);
		System.out.println("Use slot:" + slot);
		System.out.println("Open session:" + session);
	}

	private static void open(long slot) {
		try {
			session = pkcs11.C_OpenSession(slot, CKF_SERIAL_SESSION|CKF_RW_SESSION, null, null);
		} catch (PKCS11Exception e) {
			resetSession();
		}
	}

	private static void closeSession() throws PKCS11Exception {
		pkcs11.C_CloseSession(session);
		resetSession();
	}

	private static void resetSession() {
		session = NULLSESSION;
	}
	
	public static void main(String[] args) throws CertificateException, PKCS11Exception {
		Pkcs11.getPKCS11();
		List<X509Certificate> certificate = Pkcs11.getCertificate();
		X509Certificate cert = certificate.get(0);
		Principal subjectDN = cert.getSubjectDN();
		cert.checkValidity(new Date());
		String name = subjectDN.getName();
		String[] split = name.split("=");
		System.out.println(split[0]+"------"+split[1]);
	}
	
}

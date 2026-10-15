package org.yht.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RSA {

	private String publicKeyModulus = "";
	private String publicKeyExponent = "";
	private PrivateKey privateKey = null;

	public static RSA getEncKey() {

		KeyPairGenerator generator;
		try {
			generator = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			log.error(Util.getPrintStacTraceString(e));
			return null;
		} // RSA키 제네레이터 생성
		generator.initialize(1024); // 키 사이즈

		KeyPair keyPair = generator.genKeyPair();

		KeyFactory keyFactory;
		try {
			keyFactory = KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			log.error(Util.getPrintStacTraceString(e));
			return null;
		}

		PublicKey publicKey = keyPair.getPublic(); // 공개키
		PrivateKey privateKey = keyPair.getPrivate(); // 개인키

		RSAPublicKeySpec publicSpec;
		try {
			publicSpec = (RSAPublicKeySpec) keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
		} catch (InvalidKeySpecException e) {
			log.error(Util.getPrintStacTraceString(e));
			return null;
		}
		String publicKeyModulus = publicSpec.getModulus().toString(16);
		String publicKeyExponent = publicSpec.getPublicExponent().toString(16);

		RSA rsa = new RSA();
		rsa.setPrivateKey(privateKey);
		rsa.setPublicKeyExponent(publicKeyExponent);
		rsa.setPublicKeyModulus(publicKeyModulus);

		return rsa;
	}

	public static byte[] hexToByteArray(String hex) {
		if (hex == null || hex.length() % 2 != 0) {
			return new byte[] {};
		}

		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < hex.length(); i += 2) {
			byte value = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
			bytes[(int) Math.floor(i / 2)] = value;
		}
		return bytes;
	}

	public String getPublicKeyModulus() {
		return publicKeyModulus;
	}

	public void setPublicKeyModulus(String publicKeyModulus) {
		this.publicKeyModulus = publicKeyModulus;
	}

	public String getPublicKeyExponent() {
		return publicKeyExponent;
	}

	public void setPublicKeyExponent(String publicKeyExponent) {
		this.publicKeyExponent = publicKeyExponent;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}

	/**
	 * Constructs a private key (RSA) from the given string
	 *
	 * @param key PEM Private Key
	 * @return RSA Private Key
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static PrivateKey getPrivateKeyFromString(String key) throws IOException, GeneralSecurityException {
		String privateKeyPEM = key;

		// Remove the first and last lines
		privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----n", "");
		privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");

		// Base64 decode data
		byte[] encoded = Base64.decodeBase64(privateKeyPEM);

		KeyFactory kf = KeyFactory.getInstance("RSA");
		PrivateKey privKey = (PrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(encoded));

		return privKey;
	}
}

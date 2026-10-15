package org.yht.exps.express.service.eCommerce;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HmacGenerator {
	//accessKey와 secretKey는 연동 초기에 쿠팡에서 전달해드립니다.
	//generate method로 생성된 HMAC signature를 "Authorization" key에 매핑해서 보내야 함.
	public static String generate(String method, String uri, String accessKey, String secretKey) {
		String[] parts = uri.split("\\?");
		if (parts.length > 2) {
			throw new RuntimeException("incorrect uri format");
		} else {
			String path = parts[0];
			String query = "";
			if (parts.length == 2) {
				query = parts[1];
			}
		
			SimpleDateFormat dateFormatUtc = new SimpleDateFormat("yyMMdd'T'HHmmss'Z'");
			dateFormatUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
			String datetime = dateFormatUtc.format(new Date());
			String message = datetime + method + path + query;
		
			String signature;
			try {
				SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
				Mac mac = Mac.getInstance("HmacSHA256");
				mac.init(signingKey);
				byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
				signature = Hex.encodeHexString(rawHmac);
			} catch (GeneralSecurityException var14) {
				throw new IllegalArgumentException("Unexpected error while creating hash: " + var14.getMessage(), var14);
			}
		
			return String.format("CEA algorithm=%s,access-key=%s,signed-date=%s,signature=%s", "HmacSHA256", accessKey, datetime, signature);
		}
	}
}
package com.securecrypt;
import com.securecrypt.service.*;
import com.securecrypt.utility.*;
import com.securecrypt.exception.CryptoException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;
class CryptoRegressionTest {
 static Stream<EncryptionService> services(){return Stream.of(new AESService(),new CaesarCipherService(),new VigenereCipherService(),new XORCipherService(),new DESService(),new TripleDESService());}
 String key(EncryptionService s){return s instanceof CaesarCipherService?"-3":s instanceof VigenereCipherService?"LEMON":"Regression-only-नमस्ते-729!";}
 @ParameterizedTest @MethodSource("services") void unicodeRoundTrip(EncryptionService s){String text="नमस्ते café 日本語 🔐 A-Z a-z\n123";assertEquals(text,s.decrypt(s.encrypt(text,key(s)),key(s)));}
 @ParameterizedTest @MethodSource("services") void largestPlaintextRoundTrip(EncryptionService s){String text="漢".repeat(20000);assertEquals(text,s.decrypt(s.encrypt(text,key(s)),key(s)));assertThrows(CryptoException.class,()->s.encrypt(text+"A",key(s)));}
 @Test void vectors(){assertEquals("LXFOPVEFRNHR",new VigenereCipherService().encrypt("ATTACKATDAWN","LEMON"));assertEquals("Xyz UVW",new CaesarCipherService().encrypt("Abc XYZ","-3"));}
 @Test void authenticatedEnvelopeRejectsMutations(){
  AESService aes=new AESService();String text="Message integrity test",first=aes.encrypt(text,"password"),second=aes.encrypt(text,"password");assertNotEquals(first,second);assertEquals(text,aes.decrypt(first,"password"));assertThrows(CryptoException.class,()->aes.decrypt(first,"wrong"));
  for(int i=0;i<5;i++){String[] f=first.split("\\.");if(i<2)f[i]=i==0?"SC2":"999999999";else{byte[] bytes=Base64.getUrlDecoder().decode(f[i]);bytes[0]^=1;f[i]=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);}String changed=String.join(".",f);assertThrows(CryptoException.class,()->aes.decrypt(changed,"password"));}
  assertThrows(CryptoException.class,()->aes.decrypt(first.substring(0,first.length()-4),"password"));assertThrows(CryptoException.class,()->aes.decrypt(first+"=","password"));
 }
 @Test void legacyRequiresExplicitMode() throws Exception{
  Cipher c=Cipher.getInstance("AES/CBC/PKCS5Padding");c.init(Cipher.ENCRYPT_MODE,KeyDerivationUtil.deriveAesKey("legacy-test",128),new IvParameterSpec(new byte[16]));byte[] encrypted=c.doFinal("Legacy fixture नमस्ते".getBytes(StandardCharsets.UTF_8)),combined=new byte[16+encrypted.length];System.arraycopy(encrypted,0,combined,16,encrypted.length);String old=Base64.getEncoder().encodeToString(combined);
  assertEquals("Legacy fixture नमस्ते",new LegacyAESService().decrypt(old,"legacy-test"));assertThrows(CryptoException.class,()->new AESService().decrypt(old,"legacy-test"));assertThrows(CryptoException.class,()->new LegacyAESService().encrypt("test","legacy-test"));
 }
 @Test void keyBounds(){assertThrows(IllegalArgumentException.class,()->KeyGeneratorUtil.generateRandomKey(7));assertThrows(IllegalArgumentException.class,()->KeyGeneratorUtil.generateRandomKey(129));assertEquals(128,KeyGeneratorUtil.generateRandomKey(128).length());assertThrows(CryptoException.class,()->new AESService().encrypt("test","A".repeat(1025)));}
}

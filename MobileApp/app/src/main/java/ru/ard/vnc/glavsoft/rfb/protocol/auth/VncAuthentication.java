package ru.ard.vnc.glavsoft.rfb.protocol.auth;

import ru.ard.vnc.glavsoft.exceptions.CryptoException;
import ru.ard.vnc.glavsoft.exceptions.FatalException;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.rfb.CapabilityContainer;
import ru.ard.vnc.glavsoft.transport.Transport;
import com.google.api.client.http.HttpStatusCodes;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class VncAuthentication extends AuthHandler {
    public SecurityType getType() {
        return SecurityType.VNC_AUTHENTICATION;
    }

    public boolean authenticate(Transport.Reader reader, Transport.Writer writer, CapabilityContainer authCaps, String passwordRetriever) throws TransportException, FatalException {
        byte[] challenge = reader.readBytes(16);
        String password = passwordRetriever;
        byte[] key = new byte[8];
        for (int i = 0; i < password.length(); i++) {
            key[i] = password.getBytes()[i];
        }
        writer.write(encrypt(challenge, key));
        return false;
    }

    public byte[] encrypt(byte[] challenge, byte[] key) throws CryptoException {
        try {
            SecretKey secretKey = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(mirrorBits(key)));
            Cipher desCipher = Cipher.getInstance("DES/ECB/NoPadding");
            desCipher.init(1, secretKey);
            return desCipher.doFinal(challenge);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Cannot encrypt challenge", e);
        } catch (NoSuchPaddingException e2) {
            throw new CryptoException("Cannot encrypt challenge", e2);
        } catch (IllegalBlockSizeException e3) {
            throw new CryptoException("Cannot encrypt challenge", e3);
        } catch (BadPaddingException e4) {
            throw new CryptoException("Cannot encrypt challenge", e4);
        } catch (InvalidKeyException e5) {
            throw new CryptoException("Cannot encrypt challenge", e5);
        } catch (InvalidKeySpecException e6) {
            throw new CryptoException("Cannot encrypt challenge", e6);
        }
    }

    private byte[] mirrorBits(byte[] k) {
        byte[] key = new byte[8];
        for (int i = 0; i < 8; i++) {
            byte s = k[i];
            byte s2 = (byte) (((s >> 1) & 85) | ((s << 1) & 170));
            byte s3 = (byte) (((s2 >> 2) & 51) | ((s2 << 2) & HttpStatusCodes.STATUS_CODE_NO_CONTENT));
            key[i] = (byte) (((s3 >> 4) & 15) | ((s3 << 4) & 240));
        }
        return key;
    }
}

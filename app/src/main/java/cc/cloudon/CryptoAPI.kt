package cc.cloudon

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.PURPOSE_DECRYPT
import android.security.keystore.KeyProperties.PURPOSE_ENCRYPT
import android.security.keystore.KeyProperties.PURPOSE_SIGN
import android.security.keystore.KeyProperties.PURPOSE_VERIFY
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.MGF1ParameterSpec
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource


const val AES_IV_SIZE = 16
const val AES_KEY_SIZE = 16


class CryptoAPI {

    val DEV_KEY_ALIAS: String  = "DEV_KEY_ALIAS"
    fun getDevKey(): PrivateKey? {
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val entry: KeyStore.Entry = ks.getEntry(DEV_KEY_ALIAS, null)
        return if (entry is KeyStore.PrivateKeyEntry) {
            entry.privateKey
        } else{
            generateDeviceKey()?.private
        }
    }


    private fun generateDeviceKey() : KeyPair? {
        val purposes = PURPOSE_SIGN or PURPOSE_VERIFY or PURPOSE_DECRYPT or PURPOSE_ENCRYPT;
        val builder = KeyGenParameterSpec.Builder(DEV_KEY_ALIAS, purposes)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .setDigests(
                KeyProperties.DIGEST_SHA256
                /* DIGEST_SHA512 causes crashes on some devices */ )
            .setKeySize(256)

        builder.setIsStrongBoxBacked(false)

        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
        keyPairGenerator.initialize(builder.build())
        return keyPairGenerator.generateKeyPair()
    }

    fun generateEphemeralKey(): SecretKey? {
        return try {
            val rand = SecureRandom()
            val generator = KeyGenerator.getInstance("AES")
            generator.init(AES_KEY_SIZE * 8, rand)
            generator.generateKey()
        } catch (e: NoSuchAlgorithmException) {
            // Will not happen since API 1:
            // https://developer.android.com/reference/javax/crypto/KeyGenerator
            throw RuntimeException(e)
        }
    }

    fun encryptSymetric(`in`: ByteArray?, key: SecretKey?, iv: ByteArray?): ByteArray? {
        val iv = ByteArray(AES_IV_SIZE)
        SecureRandom().nextBytes(iv)
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
            cipher.doFinal(`in`)
        } catch (e: NoSuchAlgorithmException) {
            // The first two should not happen since API 10:
            // https://developer.android.com/reference/javax/crypto/Cipher
            // InvalidAlgorithmParameterException shouldn't trigger.
            // The other errors won't happen.
            throw java.lang.RuntimeException(e)
        } catch (e: NoSuchPaddingException) {
            throw java.lang.RuntimeException(e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw java.lang.RuntimeException(e)
        } catch (e: IllegalBlockSizeException) {
            throw java.lang.RuntimeException(e)
        } catch (e: BadPaddingException) {
            throw java.lang.RuntimeException(e)
        } catch (e: InvalidKeyException) {
            // Will trigger if our key is invalid.
            throw java.lang.RuntimeException(e)
        }
    }

    fun encryptAsymetric(key: PublicKey?, `in`: ByteArray?): ByteArray? {
        val keyCipher: Cipher
        keyCipher = try {
            Cipher.getInstance("RSA/NONE/OAEPwithSHA-256andMGF1Padding")
        } catch (e: NoSuchAlgorithmException) {
            // Should not happen since API 10: https://developer.android.com/reference/javax/crypto/Cipher
            throw java.lang.RuntimeException(e)
        } catch (e: NoSuchPaddingException) {
            throw java.lang.RuntimeException(e)
        }
        // NOTE: If you do not explicitly set this to SHA-1, then the keystore is completely
        // unable of encrypting/decrypting (IllegalBlockSizeException). This is apparently a
        // known issue: https://issuetracker.google.com/issues/36708951#comment15
        val sp = OAEPParameterSpec(
            "SHA-256", "MGF1", MGF1ParameterSpec("SHA-1"), PSource.PSpecified.DEFAULT
        )
        return try {
            keyCipher.init(Cipher.ENCRYPT_MODE, key, sp)
            keyCipher.doFinal(`in`)
        } catch (e: InvalidAlgorithmParameterException) {
            // InvalidKeyException will trigger if our key is invalid.
            // The other errors won't happen.
            throw java.lang.RuntimeException(e)
        } catch (e: BadPaddingException) {
            throw java.lang.RuntimeException(e)
        } catch (e: IllegalBlockSizeException) {
            throw java.lang.RuntimeException(e)
        } catch (e: InvalidKeyException) {
            // Will trigger if our key is invalid.
            throw java.lang.RuntimeException(e)
        }
    }

//    fun encryptDataWithDevKey(){
//        getDevKey()
//    }
//
//
//

//    fun decryptAsym(payload: ByteArray){
//
//
//
//
//        val keyStore: KeyStore()
//        val key = keyStore.getKey(alias, null) as PrivateKey
//        cipher.init(Cipher.DECRYPT_MODE, key)
//        val decoded = cipher.doFinal(encrypted)
//        return String(decoded, Charsets.UTF_8)
//    }
}
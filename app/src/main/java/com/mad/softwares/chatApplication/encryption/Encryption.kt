package com.mad.softwares.chatApplication.encryption

import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

interface Encryption {
    fun generateRandomSalt(saltLength: Int = 16): ByteArray

    fun aesKeyToString(secretKey: SecretKey): String

    fun stringToAESKey(aesKeyString: String): SecretKey

    fun privateKeyToString(privateKey: PrivateKey): String

    fun stringToPrivateKey(privateKeyString: String): PrivateKey

//    fun byteArrayToPrivateKey(privateKeyBytes: ByteArray): PrivateKey

    fun stringToPublicKey(publicKeyString: String): PublicKey

    fun publicKeyToString(publicKey: PublicKey): String

    fun generateAESKey(keySize: Int = 256): SecretKey

    fun generateRSAKeyPair(): KeyPair

    fun encryptAESKeyWithPublicKey(secretKey: SecretKey, publicKey: PublicKey): ByteArray

    fun decryptAESKeyWithPrivateKey(encryptedAESKey: ByteArray, privateKey: PrivateKey): SecretKey

    fun aesEncrypt(data: ByteArray, secretKey: SecretKey): ByteArray

    fun aesDecrypt(encryptedData: ByteArray, secretKey: SecretKey): ByteArray

    fun generateAESKeyFromPassword(password: String, salt: ByteArray): SecretKey

    fun encryptPrivateKeyWithPassword(
        privateKey: PrivateKey,
        password: String,
        salt: ByteArray
    ): ByteArray

    fun decryptPrivateKeyWithPassword(
        encryptedPrivateKey: ByteArray,
        password: String,
        salt: ByteArray
    ): PrivateKey

    fun stringToByteArray(encodedString: String): ByteArray

    fun byteArrayToString(byteArray: ByteArray): String
}

class EncryptionImpl : Encryption {

    override fun stringToByteArray(encodedString: String): ByteArray {
        return Base64.getDecoder().decode(encodedString)
    }

    override fun byteArrayToString(byteArray: ByteArray): String {
        return Base64.getEncoder().encodeToString(byteArray)
    }

    override fun generateRandomSalt(saltLength: Int): ByteArray {
        val salt = ByteArray(saltLength)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(salt)  // Fills the byte array with random bytes
        return salt
    }

    override fun aesKeyToString(secretKey: SecretKey): String {
        // Get the encoded AES key in byte array format
        val secretKeyBytes = secretKey.encoded
        // Convert byte array to Base64 encoded string for easy storage or transmission
        return Base64.getEncoder().encodeToString(secretKeyBytes)
    }

    override fun stringToAESKey(aesKeyString: String): SecretKey {
        // Decode the Base64 encoded string back to a byte array
        val decodedKey = Base64.getDecoder().decode(aesKeyString)
        // Generate SecretKey from the byte array (use "AES" as the algorithm)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
    }

    override fun privateKeyToString(privateKey: PrivateKey): String {
        // Get the encoded private key in byte array format
        val privateKeyBytes = privateKey.encoded
        // Convert byte array to Base64 encoded string for easy storage
        return Base64.getEncoder().encodeToString(privateKeyBytes)
    }

    override fun stringToPrivateKey(privateKeyString: String): PrivateKey {
        // Decode the Base64 encoded string back to a byte array
        val privateKeyBytes = Base64.getDecoder().decode(privateKeyString)
        // Generate PrivateKey from the byte array
        val keySpec = PKCS8EncodedKeySpec(privateKeyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")  // Assuming RSA is the algorithm
        return keyFactory.generatePrivate(keySpec)
    }

    override fun stringToPublicKey(publicKeyString: String): PublicKey {
        // Decode the Base64 encoded string back to a byte array
        val publicKeyBytes = Base64.getDecoder().decode(publicKeyString)
        // Generate PublicKey from the byte array
        val keySpec = X509EncodedKeySpec(publicKeyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")  // Assuming RSA is the algorithm
        return keyFactory.generatePublic(keySpec)
    }

    override fun publicKeyToString(publicKey: PublicKey): String {
        // Get the encoded public key in byte array format
        val publicKeyBytes = publicKey.encoded
        // Convert byte array to Base64 encoded string for easy storage
        return Base64.getEncoder().encodeToString(publicKeyBytes)
    }

    override fun generateAESKey(keySize: Int): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(keySize)
        return keyGenerator.generateKey()
    }

    override fun generateRSAKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)  // 2048-bit RSA key size
        return keyPairGenerator.generateKeyPair()
    }


    override fun encryptAESKeyWithPublicKey(secretKey: SecretKey, publicKey: PublicKey): ByteArray {
        // Get the encoded form of the SecretKey (AES key)
        val secretKeyBytes = secretKey.encoded

        // Initialize RSA cipher with public key for encryption
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")  // RSA encryption with PKCS1Padding
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        // Encrypt the AES key bytes using the RSA public key
        return cipher.doFinal(secretKeyBytes)
    }

    override fun decryptAESKeyWithPrivateKey(
        encryptedAESKey: ByteArray,
        privateKey: PrivateKey
    ): SecretKey {
        // Initialize the RSA cipher for decryption using the private key
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)

        // Decrypt the encrypted AES key
        val decryptedKeyBytes = cipher.doFinal(encryptedAESKey)

        // Convert the decrypted bytes back into a SecretKey (AES key)
        return SecretKeySpec(decryptedKeyBytes, "AES")
    }

    override fun aesEncrypt(data: ByteArray, secretKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivParameterSpec = IvParameterSpec(ByteArray(16)) // Use a secure IV in production
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        return cipher.doFinal(data)
    }

    override fun generateAESKeyFromPassword(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec =
            PBEKeySpec(password.toCharArray(), salt, 65536, 256) // 65536 iterations, 256-bit key
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    override fun encryptPrivateKeyWithPassword(
        privateKey: PrivateKey,
        password: String,
        salt: ByteArray
    ): ByteArray {
        // Generate AES key from password and salt
        val secretKey = generateAESKeyFromPassword(password, salt)

        // Initialize cipher for AES encryption
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv =
            ByteArray(16) // For the initialization vector (IV) - should be random in production
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))

        // Encrypt the private key (convert to byte array first)
        val privateKeyBytes = privateKey.encoded
        return cipher.doFinal(privateKeyBytes)
    }

    override fun decryptPrivateKeyWithPassword(
        encryptedPrivateKey: ByteArray,
        password: String,
        salt: ByteArray
    ): PrivateKey {
        // Generate AES key from password and salt
        val secretKey = generateAESKeyFromPassword(password, salt)

        // Initialize cipher for AES decryption
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = ByteArray(16) // Same IV used during encryption
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

        // Decrypt the private key byte array
        val decryptedPrivateKeyBytes = cipher.doFinal(encryptedPrivateKey)

        // Reconstruct the PrivateKey object
        val keySpec = PKCS8EncodedKeySpec(decryptedPrivateKeyBytes)
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec)
    }

    override fun aesDecrypt(encryptedData: ByteArray, secretKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivParameterSpec =
            IvParameterSpec(ByteArray(16)) // Use the same IV as used in encryption
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
        return cipher.doFinal(encryptedData)
    }
}
package com.encryptedchat

import android.app.KeyguardManager
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import java.math.BigInteger
import java.security.*
import java.security.spec.RSAKeyGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.concurrent.Executors
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal
import kotlin.random.Random

object SecurityHelper {
	private const val TAG = "SecurityHelper"

	private val alias =
			FirebaseAuth.getInstance().currentUser?.uid ?: SecurityConstants.DEFAULT_KEY_ALIAS

	fun checkKeyExists(): Boolean {
		return try {
			val keyStore = KeyStore.getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE).apply {
				load(null)
			}

			return keyStore.containsAlias(alias) && getExistingKeyPair() != null
		} catch (e: Exception) {
			Log.e(TAG, e.message, e)
			false
		}
	}

	fun showAuthenticationScreen(
			activity: FragmentActivity,
			callback: BiometricPrompt.AuthenticationCallback
	) {
		val biometricPrompt = BiometricPrompt(activity,
		                                      Executors.newSingleThreadExecutor(),
		                                      callback)

		val promptInfo = BiometricPrompt.PromptInfo.Builder()
				.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
				.setConfirmationRequired(true)
				.setTitle("Decrypt Message")
				.setSubtitle("To be able to decrypt the message we need to confirm your identity. Please scan your fingerprint.")
				.setNegativeButtonText("Cancel Decryption")
				.build()

		biometricPrompt.authenticate(promptInfo)
	}

	fun getPublicKey(context: Context): String {
		return Base64.encodeToString(createKeys(context)?.public?.encoded, Base64.NO_WRAP)
	}

	fun encrypt(publicKeyString: String, plainText: String): String? {
		return try {
			val publicKey = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
					.generatePublic(
							X509EncodedKeySpec(Base64.decode(publicKeyString, Base64.DEFAULT))
					)

			cipher.init(Cipher.ENCRYPT_MODE, publicKey)

			Base64.encodeToString(cipher.doFinal(plainText.toByteArray()), Base64.NO_WRAP)
		} catch (e: Exception) {
			Log.e(TAG, e.message, e)
			null
		}
	}

	fun decrypt(cipherText: String): String? {
		return try {
			val privateKey = getExistingKeyPair()?.private

			cipher.init(Cipher.DECRYPT_MODE, privateKey)

			String(cipher.doFinal(Base64.decode(cipherText, Base64.NO_WRAP)))
		} catch (e: Exception) {
			Log.e(TAG, e.message, e)
			null
		}
	}

	private fun checkDeviceSecure(context: Context): Boolean {
		val keyGuardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
		return keyGuardManager.isDeviceSecure
	}

	private fun createKeys(context: Context): KeyPair? {
		if (!checkDeviceSecure(context)) {
			return null
		}

		if (checkKeyExists()) {
			return getExistingKeyPair()
		} else {
			try {
				val startDate = GregorianCalendar()

				val endDate = GregorianCalendar()
				endDate.add(Calendar.YEAR, 1)

				val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA,
				                                                    SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE)

				val keyGenParameterSpec = KeyGenParameterSpec.Builder(
						alias,
						KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
						.run {
							setAlgorithmParameterSpec(RSAKeyGenParameterSpec(1024,
							                                                 RSAKeyGenParameterSpec.F4))
							setBlockModes(KeyProperties.BLOCK_MODE_CBC)
							setCertificateNotAfter(endDate.time)
							setCertificateNotBefore(startDate.time)
							setCertificateSerialNumber(BigInteger.valueOf(777))
							setCertificateSubject(X500Principal("CN=$alias"))
							setDigests(KeyProperties.DIGEST_SHA256,
							           KeyProperties.DIGEST_SHA384,
							           KeyProperties.DIGEST_SHA512)
							setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
							setInvalidatedByBiometricEnrollment(true)
							setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
							setUserAuthenticationRequired(true)
							setUserAuthenticationValidityDurationSeconds(0)
							build()
						}

				keyPairGenerator.initialize(keyGenParameterSpec)

				return keyPairGenerator.generateKeyPair()
			} catch (e: Exception) {
				Log.e(TAG, e.message, e)
			}
		}
		return null
	}

	private fun getExistingKeyPair(): KeyPair? {
		try {
			val keyStore = KeyStore.getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE).apply {
				load(null)
			}

			val privateKey: PrivateKey? = keyStore.getKey(alias, null) as PrivateKey?
			var publicKey: PublicKey? = null
			if (privateKey != null) {
				publicKey = keyStore.getCertificate(alias)?.publicKey
			}

			if (publicKey != null && privateKey != null) {
				return KeyPair(publicKey, privateKey)
			}
		} catch (e: Exception) {
			Log.e(TAG, e.message, e)
		}
		return null
	}

	private val cipher: Cipher
		get() = Cipher.getInstance(String.format("%s/%s/%s",
		                                         KeyProperties.KEY_ALGORITHM_RSA,
		                                         KeyProperties.BLOCK_MODE_CBC,
		                                         KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1))

	interface SecurityConstants {
		companion object {
			const val KEYSTORE_PROVIDER_ANDROID_KEYSTORE = "AndroidKeyStore"

			const val DEFAULT_KEY_ALIAS = "DefaultKeyAlias"
		}
	}
}
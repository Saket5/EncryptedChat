package com.encryptedchat

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.encryptedchat.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
	private lateinit var binding: ActivityLoginBinding

	private lateinit var firebaseAuth: FirebaseAuth

	private lateinit var mFirestore: FirebaseFirestore

	private var verificationId: String? = null

	private var selectedPhoneNumber: String? = null

	private val mCallBack: OnVerificationStateChangedCallbacks = object : OnVerificationStateChangedCallbacks() {
		override fun onCodeSent(verificationId: String, forceResendingToken: ForceResendingToken) {
			super.onCodeSent(verificationId, forceResendingToken)

			binding.tilPhoneNumber.isEnabled = true
			binding.btnGenerateOtp.isEnabled = true

			binding.cvOtp.visibility = VISIBLE

			this@LoginActivity.verificationId = verificationId
		}

		override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
			binding.tilPhoneNumber.isEnabled = true
			binding.btnGenerateOtp.isEnabled = true

			phoneAuthCredential.smsCode?.let {
				binding.etOtp.setText(it)

				verifyCode(it)
			}
		}

		override fun onVerificationFailed(e: FirebaseException) {
			binding.tilPhoneNumber.isEnabled = true
			binding.btnGenerateOtp.isEnabled = true
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityLoginBinding.inflate(layoutInflater)
		val view = binding.root

		setContentView(view)

		firebaseAuth = FirebaseAuth.getInstance()

		mFirestore = FirebaseFirestore.getInstance()

		checkUserSignedIn()

		binding.etPhoneNumber.doOnTextChanged { phoneNumber, _, _, _ ->
			if (!phoneNumber.isNullOrEmpty() && phoneNumber.length == 10) {
				binding.btnGenerateOtp.visibility = VISIBLE

				val currentPhoneNumber = binding.ccpNumber.defaultCountryCodeWithPlus + phoneNumber
				if (currentPhoneNumber == selectedPhoneNumber) {
					binding.cvOtp.visibility = VISIBLE
				}
			} else {
				binding.btnGenerateOtp.visibility = GONE
				binding.cvOtp.visibility = GONE
			}
		}

		binding.btnGenerateOtp.setOnClickListener {
			hideKeyboard()

			binding.tilPhoneNumber.isEnabled = false
			binding.btnGenerateOtp.isEnabled = false

			val phoneNumber = binding.ccpNumber.defaultCountryCodeWithPlus + binding.etPhoneNumber.text.toString()
			selectedPhoneNumber = phoneNumber

			sendVerificationCode()
		}

		binding.etOtp.doOnTextChanged { otp, _, _, _ ->
			if (!otp.isNullOrEmpty() && otp.length == 6) {
				binding.btnVerifyOtp.visibility = VISIBLE
			} else {
				binding.btnVerifyOtp.visibility = GONE
			}
		}

		binding.btnVerifyOtp.setOnClickListener {
			hideKeyboard()

			binding.tilPhoneNumber.isEnabled = false
			binding.btnGenerateOtp.isEnabled = false

			binding.tilOtp.isEnabled = false
			binding.btnVerifyOtp.isEnabled = false

			verifyCode(binding.etOtp.text.toString())
		}

		binding.etName.doOnTextChanged { name, _, _, _ ->
			if (!name.isNullOrEmpty()) {
				binding.btnSubmitName.visibility = VISIBLE
			} else {
				binding.btnSubmitName.visibility = GONE
			}
		}

		binding.btnSubmitName.setOnClickListener {
			hideKeyboard()

			binding.tilName.isEnabled = false
			binding.btnSubmitName.isEnabled = false

			setUserDataInFireStore(binding.etName.text.toString())
		}
	}

	private fun hideKeyboard() {
		(this.currentFocus ?: View(this)).let {
			val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
			imm.hideSoftInputFromWindow(it.windowToken, 0)
		}
	}

	private fun checkUserSignedIn() {
		firebaseAuth.currentUser?.let { currentUser ->
			mFirestore.collection(Constants.FIRE_STORE_USER_DATA)
					.document(currentUser.uid)
					.get()
					.addOnCompleteListener { task ->
						if (task.isSuccessful and task.result.exists() && SecurityHelper.checkKeyExists()) {
							gotoMainActivity()
						} else {
							binding.cvPhoneNumber.visibility = GONE
							binding.cvOtp.visibility = GONE

							binding.cvName.visibility = VISIBLE

							selectedPhoneNumber = currentUser.phoneNumber
						}
					}
		}
	}

	private fun signInWithCredential(credential: PhoneAuthCredential) {
		firebaseAuth.signInWithCredential(credential)
				.addOnCompleteListener { task: Task<AuthResult?> ->
					if (task.isSuccessful) {
						firebaseAuth.currentUser?.also { currentUser ->
							mFirestore.collection(Constants.FIRE_STORE_USER_DATA)
									.document(currentUser.uid)
									.get()
									.addOnCompleteListener { task ->
										if (task.isSuccessful and task.result.exists() && SecurityHelper.checkKeyExists()) {
											gotoMainActivity()
										} else {
											binding.cvPhoneNumber.visibility = GONE
											binding.cvOtp.visibility = GONE

											binding.cvName.visibility = VISIBLE

											selectedPhoneNumber = currentUser.phoneNumber
										}
									}
						} ?: run {
							binding.cvPhoneNumber.visibility = GONE
							binding.cvOtp.visibility = GONE

							binding.cvName.visibility = VISIBLE
						}
					} else {
						binding.tilPhoneNumber.isEnabled = true
						binding.btnGenerateOtp.isEnabled = true

						binding.tilOtp.isEnabled = true
						binding.btnVerifyOtp.isEnabled = true

						Toast.makeText(this@LoginActivity,
						               task.exception?.message,
						               Toast.LENGTH_LONG)
								.show()
					}
				}
	}

	private fun sendVerificationCode() {
		selectedPhoneNumber?.let {
			val options = PhoneAuthOptions.newBuilder(firebaseAuth)
					.setPhoneNumber(it)
					.setTimeout(60L, TimeUnit.SECONDS)
					.setActivity(this)
					.setCallbacks(mCallBack)
					.build()

			PhoneAuthProvider.verifyPhoneNumber(options)
		}
	}

	private fun verifyCode(code: String) {
		val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
		signInWithCredential(credential)
	}

	private fun setUserDataInFireStore(name: String) {
		firebaseAuth.currentUser?.let { currentUser ->
			val userChatId = UUID.randomUUID().toString()

			val userData = hashMapOf(
					Constants.USER_DATA_NAME to name,
					Constants.USER_DATA_PHONE_NUMBER to selectedPhoneNumber,
					Constants.USER_DATA_PUBLIC_KEY to SecurityHelper.getPublicKey(this),
					Constants.USER_DATA_USER_CHAT_ID to userChatId
			)

			mFirestore.collection(Constants.FIRE_STORE_USER_DATA)
					.document(currentUser.uid)
					.set(userData)
					.addOnCompleteListener { task ->
						if (task.isSuccessful) {
							createUserChatId(userChatId)
						} else {
							Toast.makeText(this@LoginActivity,
							               "Something went wrong. Please try again.",
							               Toast.LENGTH_SHORT)
									.show()
						}
					}
		}
	}

	private fun createUserChatId(userChatId: String) {
		val chatIds = hashMapOf(
				Constants.USER_CHATS_CHAT_IDS to arrayListOf<String>()
		)

		mFirestore.collection(Constants.FIRE_STORE_USER_CHATS)
				.document(userChatId)
				.set(chatIds)
				.addOnCompleteListener { task ->
					if (task.isSuccessful) {
						gotoMainActivity()
					} else {
						Toast.makeText(this@LoginActivity,
						               "Something went wrong. Please try again.",
						               Toast.LENGTH_SHORT)
								.show()
					}
				}
	}

	private fun gotoMainActivity() {
		val intent = Intent(this, MainActivity::class.java)
		startActivity(intent)
		finish()
	}
}
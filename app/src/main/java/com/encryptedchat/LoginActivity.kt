package com.encryptedchat

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.encryptedchat.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
	private lateinit var binding: ActivityLoginBinding

	private lateinit var mAuth: FirebaseAuth

	var verificationId: String? = null

	private val mCallBack: OnVerificationStateChangedCallbacks = object : OnVerificationStateChangedCallbacks() {
		override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
			super.onCodeSent(s, forceResendingToken)
			verificationId = s
		}

		override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
			val code = phoneAuthCredential.smsCode
			if (code != null) {
				binding.etOtp.setText(code)
				verifyCode(code)
			}
		}

		override fun onVerificationFailed(e: FirebaseException) {}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityLoginBinding.inflate(layoutInflater)
		val view = binding.root
		setContentView(view)

		mAuth = FirebaseAuth.getInstance()

		binding.llVerification.visibility = View.GONE

		binding.btnGenerateOtp.setOnClickListener {
			hideKeyboard()

			if (TextUtils.isEmpty(binding.etPhoneNumber.text.toString())) {
				Toast.makeText(this@LoginActivity,
				               "Please enter a valid phone number",
				               Toast.LENGTH_SHORT)
						.show()
			} else {
				binding.llVerification.visibility = View.VISIBLE

				val phone = binding.ccpNumber.defaultCountryCodeWithPlus + binding.etPhoneNumber.text.toString()
				sendVerificationCode(phone)
			}
		}

		binding.btnVerifyOtp.setOnClickListener {
			hideKeyboard()

			if (TextUtils.isEmpty(binding.etOtp.text.toString())) {
				Toast.makeText(this@LoginActivity,
				               "Please enter valid OTP",
				               Toast.LENGTH_SHORT).show()
			} else {
				verifyCode(binding.etOtp.text.toString())
			}
		}
	}

	private fun hideKeyboard() {
		val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
		var view = this.currentFocus
		if (view == null) {
			view = View(this)
		}
		imm.hideSoftInputFromWindow(view.windowToken, 0)
	}

	private fun signInWithCredential(credential: PhoneAuthCredential) {
		mAuth.signInWithCredential(credential)
				.addOnCompleteListener { task: Task<AuthResult?> ->
					if (task.isSuccessful) {
						val i = Intent(this@LoginActivity, MainActivity::class.java)
						startActivity(i)
						finish()
					} else {
						Toast.makeText(this@LoginActivity,
						               task.exception?.message,
						               Toast.LENGTH_LONG)
								.show()
					}
				}
	}

	private fun sendVerificationCode(number: String) {
		val options = PhoneAuthOptions.newBuilder(mAuth)
				.setPhoneNumber(number)
				.setTimeout(60L, TimeUnit.SECONDS)
				.setActivity(this)
				.setCallbacks(mCallBack)
				.build()

		PhoneAuthProvider.verifyPhoneNumber(options)
	}

	private fun verifyCode(code: String) {
		val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
		signInWithCredential(credential)
	}
}
package com.encryptedchat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.encryptedchat.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)
		val view = binding.root

		setContentView(view)

		binding.btnLogout.setOnClickListener {
			FirebaseAuth.getInstance().signOut()
			gotoLoginActivity()
		}
	}

	private fun gotoLoginActivity() {
		val intent = Intent(this, LoginActivity::class.java)
		startActivity(intent)
		finish()
	}
}
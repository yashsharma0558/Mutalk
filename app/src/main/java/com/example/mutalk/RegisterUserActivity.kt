package com.example.mutalk

import android.content.ContentValues.TAG
import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale
import java.util.concurrent.TimeUnit

class RegisterUserActivity : AppCompatActivity() {

    private lateinit var register: FloatingActionButton
    private lateinit var nameField: EditText
    private lateinit var phoneField: EditText
    private lateinit var otpField: EditText
    private lateinit var sendOTP: TextView
    private lateinit var sharedViewModel: ViewModel
    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private val auth = Firebase.auth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)
        changeWelcomeTextColor()

        nameField = findViewById(R.id.email)
        phoneField = findViewById(R.id.phone)
        otpField = findViewById(R.id.otp)
        register = findViewById(R.id.call)
        sendOTP = findViewById(R.id.textView6)

        register.setOnClickListener {
            verifyOtp(storedVerificationId)
        }
        sendOTP.setOnClickListener {
            val phoneNo = phoneField.text.toString()
            if(phoneNo.length == 10){
                val countryCode = getCountryTelephoneCode(Locale.getDefault().country)
                authenticatePhoneNo(countryCode+phoneNo)
            }
            else{
                Toast.makeText(this, "Please fill out correct Phone Number :) ", Toast.LENGTH_SHORT).show()
            }




        }

    }

    private fun authenticatePhoneNo(phoneNum: String) {
        sendOTP.visibility = View.INVISIBLE
        findViewById<TextInputLayout>(R.id.OTPTextField).visibility = View.VISIBLE
        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(phoneNum)
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result?.user
                    val intent = Intent(this, ContactActivity::class.java)
                    intent.putExtra("userID", phoneField.text.toString() )
                    intent.putExtra("userName", nameField.text.toString())
                    startActivity(intent)


                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                Log.w(TAG, "onVerificationFailed", e)
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                Log.w(TAG, "onVerificationFailed", e)
                // The SMS quota for the project has been exceeded
            } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                Log.w(TAG, "onVerificationFailed", e)
                // reCAPTCHA verification attempted with null Activity

            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")
            Toast.makeText(this@RegisterUserActivity, "OTP Sent Successfully !!", Toast.LENGTH_SHORT).show()

            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token

        }
    }

    private fun verifyOtp(verificationId: String) {
        val otp = otpField.text.toString().trim()
        val credentials = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithPhoneAuthCredential(credentials)
    }


    private fun changeWelcomeTextColor() {
        val textView = findViewById<TextView>(R.id.welcome)

        // Define the text string
        val text = "Let's finish\nsetting\nup..."

        // Create a SpannableString
        val spannableString = SpannableString(text)

        // Define the word you want to color differently
        val wordToColor = "setting\nup..."

        // Find the start and end index of the word
        val startIndex = text.indexOf(wordToColor)
        val endIndex = startIndex + wordToColor.length

        // Set the color of the word
        val color = getColor(R.color.purple_main)
        spannableString.setSpan(ForegroundColorSpan(color), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the SpannableString to the TextView
        textView.text = spannableString
    }

    fun getCountryTelephoneCode(countryCode: String): String {
        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        val countryInfo = phoneNumberUtil.getCountryCodeForRegion(countryCode)
        return "+$countryInfo"
    }

}
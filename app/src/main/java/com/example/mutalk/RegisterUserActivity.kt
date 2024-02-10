package com.example.mutalk

import android.content.ContentValues.TAG
import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale

class RegisterUserActivity : AppCompatActivity() {

    private lateinit var register: FloatingActionButton
    private lateinit var nameField: EditText
    private lateinit var phoneField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)
        changeWelcomeTextColor()

        nameField = findViewById(R.id.email)
        phoneField = findViewById(R.id.phone)
        register = findViewById(R.id.call)

        register.setOnClickListener {

            val name = nameField.text.toString()
            val phone = phoneField.text.toString()
            if (name.length>2 && phone.length==10){
                val countryCode = getCountryTelephoneCode(Locale.getDefault().country)
                val sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
                sharedViewModel.currentUserName = name
                sharedViewModel.currentUserPhoneNo = countryCode+phone
                Toast.makeText(this, "YAYYY your number is ${sharedViewModel.currentUserPhoneNo} ", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ContactActivity::class.java)
                intent.putExtra("userID", sharedViewModel.currentUserPhoneNo )
                intent.putExtra("userName", sharedViewModel.currentUserName)
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "Please fill out correct details :) ", Toast.LENGTH_SHORT).show()
            }
        }

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
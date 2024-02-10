package com.example.mutalk

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import im.zego.zegoexpress.ZegoExpressEngine
import im.zego.zegoexpress.constants.ZegoScenario
import im.zego.zegoexpress.entity.ZegoEngineProfile
import java.util.Random


class MainActivity : AppCompatActivity() {
    private lateinit var button: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button = findViewById(R.id.call)
        button.setOnClickListener{
            intent = Intent(this, ContactActivity::class.java)
            startActivity(intent)
        }
        changeWelcomeTextColor()

    }
    private fun changeWelcomeTextColor() {
        val textView = findViewById<TextView>(R.id.welcome)

        // Define the text string
        val text = "Welcome\nto\nMutalk"

        // Create a SpannableString
        val spannableString = SpannableString(text)

        // Define the word you want to color differently
        val wordToColor = "Mutalk"

        // Find the start and end index of the word
        val startIndex = text.indexOf(wordToColor)
        val endIndex = startIndex + wordToColor.length

        // Set the color of the word
        val color = getColor(R.color.purple_main)
        spannableString.setSpan(ForegroundColorSpan(color), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the SpannableString to the TextView
        textView.text = spannableString
    }

}
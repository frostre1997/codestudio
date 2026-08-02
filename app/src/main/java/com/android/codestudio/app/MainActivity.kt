package com.android.codestudio.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val input = findViewById<EditText>(R.id.editText)
        val saveBtn = findViewById<Button>(R.id.saveBtn)
        val display = findViewById<TextView>(R.id.displayText)

        saveBtn.setOnClickListener {
            val text = input.text.toString()
            display.text = if (text.isNotEmpty()) "You said: $text" else "Type something!"
            input.text.clear()
        }
    }
}

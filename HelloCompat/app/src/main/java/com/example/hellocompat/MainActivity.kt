package com.example.hellocompat

import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
   private  val helloTextView : TextView by lazy {
       findViewById(R.id.hello_textView)
   }
    private val helloButton: Button by lazy{
        findViewById(R.id.color_button)
    }

    private val mColorArray = arrayOf(
        "red", "pink", "purple", "deep_purple",
        "indigo", "blue", "light_blue", "cyan", "teal", "green",
        "light_green", "lime", "yellow", "amber", "orange", "deep_orange",
        "brown", "grey", "blue_grey", "black"
    )
    override fun onCreate(savedInstanceState: Bundle?) {

        if (savedInstanceState != null) {
            helloTextView.setTextColor(savedInstanceState.getInt("color"))
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        //save the current text  color
        outState.putInt("color",helloTextView.currentTextColor)
    }


    fun changeColor(view: View) {
        var random = Random
        val colorName = mColorArray[random.nextInt(20)]
        val colorResourceName = getResources().getIdentifier(colorName,"color",applicationContext.packageName)
        val colorRes = ContextCompat.getColor(this,colorResourceName)
    helloTextView.setTextColor(colorRes)
        helloButton.setBackgroundColor(colorRes)

    }
}
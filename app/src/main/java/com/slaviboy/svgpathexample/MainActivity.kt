package com.slaviboy.svgpathexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.slaviboy.svgpathexample.extensions.hideSystemUI

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hideSystemUI()
    }
}
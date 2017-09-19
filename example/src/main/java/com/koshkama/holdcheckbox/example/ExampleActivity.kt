package com.koshkama.holdcheckbox.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_example.*

class ExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)
        runButton.setOnClickListener {
            holdCheckBox.isChecked = !holdCheckBox.isChecked
            holdCheckBox.animateTick(holdCheckBox.isChecked)
        }
    }

}

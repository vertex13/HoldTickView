package com.koshkama.holdtickview.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_example.*

class ExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)
        switchButton.setOnClickListener {
            holdTickView.setChecked(!holdTickView.isChecked, true)
        }
        holdTickView.onCheckedChangeListener = { isChecked ->
            Toast.makeText(this, if (isChecked) "Checked" else "Unchecked", Toast.LENGTH_SHORT).show()
        }
    }

}

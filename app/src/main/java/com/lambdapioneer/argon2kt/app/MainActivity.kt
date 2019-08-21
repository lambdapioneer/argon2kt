// Copyright (c) Daniel Hugenroth
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.lambdapioneer.argon2kt.app

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import com.lambdapioneer.argon2kt.Argon2Version
import com.lambdapioneer.argon2kt.decodeAsHex
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        spinner_argon_mode.adapter = ArgonModeAdapter(layoutInflater)
        spinner_argon_version.adapter = ArgonVersionAdapter(layoutInflater)
        spinner_argon_version.setSelection(1)
    }

    fun onClickButtonRun(@Suppress("UNUSED_PARAMETER") view: View) {
        try {
            val params = Argon2AsyncTaskParams(
                saltInHex = edit_salt.text.toString(),
                passwordInUnicode = edit_password.text.toString(),
                iterations = edit_iterations.text.toString().toInt(),
                memory = edit_memory.text.toString().toInt(),
                mode = spinner_argon_mode.selectedItem as Argon2Mode,
                version = spinner_argon_version.selectedItem as Argon2Version
            )
            Argon2AsyncTask().execute(params)
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun onOutputClicked(view: View) = copyStringToClipboard((view as TextView).text.toString())

    @SuppressLint("StaticFieldLeak")
    inner class Argon2AsyncTask : AsyncTask<Argon2AsyncTaskParams, Void, Argon2AsyncTaskResult>() {
        override fun onPreExecute() {
            setProgressIndicator(true)
        }

        override fun doInBackground(vararg paramss: Argon2AsyncTaskParams): Argon2AsyncTaskResult {
            val params = paramss[0]

            return try {
                val result = Argon2Kt().hash(
                    mode = params.mode,
                    password = params.passwordInUnicode.toByteArray(),
                    salt = params.saltInHex.decodeAsHex(),
                    tCostInIterations = params.iterations,
                    mCostInKibibyte = params.memory,
                    version = params.version
                )

                Argon2AsyncTaskResult(
                    hashInHex = result.rawHashAsHexadecimal(),
                    encodedOutputAsString = result.encodedOutputAsString()
                )
            } catch (e: Exception) {
                Argon2AsyncTaskResult(error = e)
            }
        }

        override fun onPostExecute(result: Argon2AsyncTaskResult) {
            setProgressIndicator(false)

            result.hashInHex?.apply { text_output_hash.text = this }
            result.encodedOutputAsString?.apply { text_output_encoded_string.text = this }
            result.error?.apply {
                Toast.makeText(applicationContext, "Failed: ${this.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setProgressIndicator(inProgress: Boolean) {
        button_run.isEnabled = !inProgress
        progress_bar.visibility = if (inProgress) View.VISIBLE else View.GONE
        layout_output.visibility = if (inProgress) View.GONE else View.VISIBLE
    }

    private fun copyStringToClipboard(string: String) {
        val clipboard = ContextCompat.getSystemService(applicationContext, ClipboardManager::class.java)
        val clip = ClipData.newPlainText("argon2", string)
        clipboard!!.primaryClip = clip

        Toast.makeText(applicationContext, R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }
}

data class Argon2AsyncTaskParams(
    val saltInHex: String,
    val passwordInUnicode: String,
    val iterations: Int,
    val memory: Int,
    val mode: Argon2Mode,
    val version: Argon2Version
)

data class Argon2AsyncTaskResult(
    val hashInHex: String? = null,
    val encodedOutputAsString: String? = null,
    val error: Exception? = null
)

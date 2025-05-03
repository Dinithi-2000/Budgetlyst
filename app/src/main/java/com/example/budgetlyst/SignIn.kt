package com.example.budgetlyst

import android.content.Intent
import android.os.Bundle
import android.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.budgetlyst.ui.MainActivity
import com.example.budgetlyst.R
import com.example.budgetlyst.data.PreferencesManager
import com.example.budgetlyst.model.FormData
import com.example.budgetlyst.model.validations.ValidationResult

class SignIn : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)

        // Initialize EditText fields
        email = findViewById(R.id.email_input)
        password = findViewById(R.id.password_input)
        preferencesManager = PreferencesManager(this)

        // Apply edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun login(view: View) {
        val formData = FormData(
            email.text.toString(),
            password.text.toString()
        )

        val emailValidation = formData.validateEmail()
        val passwordValidation = formData.validatePassword()

        // Reset errors
        email.error = null
        password.error = null

        var isValid = true

        if (emailValidation !is ValidationResult.Valid) {
            email.error = when (emailValidation) {
                is ValidationResult.Invalid -> emailValidation.errorMessage
                is ValidationResult.Empty -> emailValidation.errorMessage
                else -> null
            }
            isValid = false
        }

        if (passwordValidation !is ValidationResult.Valid) {
            password.error = when (passwordValidation) {
                is ValidationResult.Invalid -> passwordValidation.errorMessage
                is ValidationResult.Empty -> passwordValidation.errorMessage
                else -> null
            }
            isValid = false
        }


        if (isValid) {
            val isAuthenticated = preferencesManager.validateLogin(
                email.text.toString(),
                password.text.toString()
            )

            if (isAuthenticated) {
                displayAlert("Success", "Welcome back, ${preferencesManager.getUsername()}!") {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()

                }
            } else {
                displayAlert("Login Failed", "Invalid email or password.")

            }
        } else {
            Toast.makeText(this, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
        }
    }

    fun Signup(view: View) {
        startActivity(Intent(this, SignUp::class.java))
        finish()
    }

    private fun displayAlert(title: String, message: String, onPositiveClick: (() -> Unit)? = null) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("OK") { _, _ -> onPositiveClick?.invoke() }
            create().show()
        }
    }
}

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
import com.example.budgetlyst.R
import com.example.budgetlyst.data.PreferencesManager

import com.example.budgetlyst.model.validations.RegFormData
import com.example.budgetlyst.model.validations.ValidationResult

class SignUp : AppCompatActivity() {
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var rePassword: EditText
    private lateinit var email: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        // Initialize EditText fields
        username = findViewById(R.id.username_input)
        password = findViewById(R.id.password_input)
        rePassword = findViewById(R.id.confirm_password_input)
        email = findViewById(R.id.email_input)

        // Apply edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun displayAlert(title: String, message: String, onPositiveClick: (() -> Unit)? = null) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { _, _ ->
            onPositiveClick?.invoke()
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun LogIn(view: View) {
        startActivity(Intent(this, SignIn::class.java))
    }

    fun register(view: View) {
        val formData = RegFormData(
            username.text.toString(),
            password.text.toString(),
            rePassword.text.toString(),
            email.text.toString()
        )

        val usernameValidation = formData.validateUsername()
        val passwordValidation = formData.validatePassword()
        val rePasswordValidation = formData.validateRePassword()
        val emailValidation = formData.validateEmail()

        // Reset errors
        username.error = null
        password.error = null
        rePassword.error = null
        email.error = null

        var validCount = 0

        when (usernameValidation) {
            is ValidationResult.Valid -> validCount++
            is ValidationResult.Empty -> username.error = usernameValidation.errorMessage
            is ValidationResult.Invalid -> username.error = usernameValidation.errorMessage
        }

        when (passwordValidation) {
            is ValidationResult.Valid -> validCount++
            is ValidationResult.Empty -> password.error = passwordValidation.errorMessage
            is ValidationResult.Invalid -> password.error = passwordValidation.errorMessage
        }

        when (rePasswordValidation) {
            is ValidationResult.Valid -> validCount++
            is ValidationResult.Empty -> rePassword.error = rePasswordValidation.errorMessage
            is ValidationResult.Invalid -> rePassword.error = rePasswordValidation.errorMessage
        }

        when (emailValidation) {
            is ValidationResult.Valid -> validCount++
            is ValidationResult.Empty -> email.error = emailValidation.errorMessage
            is ValidationResult.Invalid -> email.error = emailValidation.errorMessage
        }

        if (validCount == 4) {
            val prefManager = PreferencesManager(this)
            prefManager.saveUser(
                formData.username,
                formData.email,
                formData.password
            )

            displayAlert("Success", "Registration successful!") {
                startActivity(Intent(this, SignIn::class.java))
                finish()
            }
        } else {
            displayAlert("Failed", "Registration Failed!\nCheck all the fields.")
        }
    }

}

package com.example.budgetlyst.model

import com.example.budgetlyst.model.validations.ValidationResult

class FormData(
    private var email: String,
    private var password: String
) {
    fun validateEmail(): ValidationResult {
        return when {
            email.isEmpty() -> ValidationResult.Empty("Enter the Email")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                ValidationResult.Invalid("Invalid email format")
            else -> ValidationResult.Valid
        }
    }

    fun validatePassword(): ValidationResult {
        return when {
            password.isEmpty() -> ValidationResult.Empty("Enter the Password")
            password.length != 8 -> ValidationResult.Invalid("Password must be 8 characters.")
            else -> ValidationResult.Valid
        }
    }
}




package com.example.budgetlyst.model.validations


class RegFormData(
        var username: String,
        var password: String,
        var rePassword: String,
        var email: String
    )
    {
        fun validateUsername(): ValidationResult =
            if (username.isEmpty()) ValidationResult.Empty("Enter the User Name")
            else ValidationResult.Valid

        fun validatePassword(): ValidationResult = when {
            password.isEmpty() -> ValidationResult.Empty("Enter the Password")
            password.length != 8 -> ValidationResult.Invalid("Password must be 8 characters")
            else -> ValidationResult.Valid
        }

        fun validateRePassword(): ValidationResult = when {
            rePassword.isEmpty() -> ValidationResult.Empty("Please re-enter the password")
            rePassword != password -> ValidationResult.Invalid("Passwords do not match")
            else -> ValidationResult.Valid
        }

        fun validateEmail(): ValidationResult = when {
            email.isEmpty() -> ValidationResult.Empty("Enter the Email")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                ValidationResult.Invalid("Invalid email format")
            else -> ValidationResult.Valid
        }
    }

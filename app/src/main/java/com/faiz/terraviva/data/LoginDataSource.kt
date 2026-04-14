package com.faiz.terraviva.data

import com.faiz.terraviva.data.model.LoggedInUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.tasks.await
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    suspend fun login(username: String, password: String): Result<LoggedInUser> {
        return try {
            val auth = FirebaseAuth.getInstance()
            val result = auth.signInWithEmailAndPassword(username, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val loggedInUser = LoggedInUser(
                    firebaseUser.uid,
                    firebaseUser.displayName ?: firebaseUser.email ?: "User"
                )
                Result.Success(loggedInUser)
            } else {
                Result.Error(IOException("Login failed: User is null"))
            }
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.Error(IOException("Email tidak terdaftar"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.Error(IOException("Password salah"))
        } catch (e: Exception) {
            Result.Error(IOException("Error logging in: ${e.message}", e))
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }
}
package com.example.realtimeobjectcounter.Repository

import android.net.Uri
import com.example.realtimeobjectcounter.Utils.Resource
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class FirebaseStorageRepository {

    private val storageReference = FirebaseStorage.getInstance().reference

    // Upload image to Firebase Storage
    suspend fun uploadImage(uri: Uri): Resource<String> {
        return withContext(Dispatchers.IO) {
            val fileName = UUID.randomUUID().toString()
            val imageRef = storageReference.child("images/$fileName")
            try {
                imageRef.putFile(uri).await()
                val url = imageRef.downloadUrl.await().toString()
                Resource.Success(url)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to upload image")
            }
        }
    }

}
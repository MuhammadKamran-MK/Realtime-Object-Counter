package com.example.realtimeobjectcounter.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.realtimeobjectcounter.Models.ObjectsModel
import com.example.realtimeobjectcounter.Utils.Resource
import com.example.realtimeobjectcounter.Utils.safeCall
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RealtimeDatabaseRepository {

    private val database = FirebaseDatabase.getInstance().reference

    // Create operation
    suspend fun createData(
        userId: String,
        image: String,
        name: String,
        count: String
    ): Resource<Boolean> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val pushKey = database.child(userId).push().key
                val objectsModel = ObjectsModel(pushKey, userId, image, name, count)
                pushKey?.let {
                    database.child(userId).child(pushKey).setValue(objectsModel)
                    Resource.Success(true)
                } ?: Resource.Error("Failed to add object")
            }
        }
    }

    // Read operation
//    suspend fun <T> readData(path: String, valueType: Class<T>): Resource<List<T>> {
//        return withContext(Dispatchers.IO) {
//            safeCall {
//                val dataSnapshot = database.child(path).get().await()
//                val dataList = mutableListOf<T>()
//                dataSnapshot.children.forEach { data ->
//                    data.getValue(valueType)?.let {
//                        dataList.add(it)
//                    }
//                }
//                Resource.Success(dataList)
//            }
//        }
//    }

    fun <T> readData(path: String, valueType: Class<T>): LiveData<List<T>> {
        val liveData = MutableLiveData<List<T>>()
        database.child(path).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dataList = mutableListOf<T>()
                for (data in snapshot.children) {
                    val value = data.getValue(valueType)
                    if (value != null) {
                        dataList.add(value)
                    }
                }
                liveData.value = dataList
            }
            override fun onCancelled(error: DatabaseError) {
//               Resource.Error(error.message)
            }
        })
        return liveData
    }


    // Delete operation
    suspend fun deleteData(userId: String, path: String): Resource<Boolean> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val dataSnapshot = database.child(userId).child(path).get().await()
                if (dataSnapshot.exists()) {
                    database.child(userId).child(path).removeValue()
                    Resource.Success(true)
                } else {
                    Resource.Error("Data not found")
                }
            }
        }
    }

}
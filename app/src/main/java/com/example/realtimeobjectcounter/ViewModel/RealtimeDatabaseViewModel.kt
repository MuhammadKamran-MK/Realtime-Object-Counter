package com.example.realtimeobjectcounter.ViewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimeobjectcounter.Models.ObjectsModel
import com.example.realtimeobjectcounter.Repository.FirebaseStorageRepository
import com.example.realtimeobjectcounter.Repository.RealtimeDatabaseRepository
import com.example.realtimeobjectcounter.Utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RealtimeDatabaseViewModel : ViewModel() {

    private val _objectStatus = MutableLiveData<Resource<Boolean>>()
    val objectStatus: LiveData<Resource<Boolean>> = _objectStatus

    private val realtimeDatabaseRepository = RealtimeDatabaseRepository()
    private val firebaseStorageRepository = FirebaseStorageRepository()

    fun submitData(
        userId: String,
        image: Uri,
        name: String,
        count: String
    ) {
        viewModelScope.launch {
            _objectStatus.value = Resource.Loading()
            val storageResult = firebaseStorageRepository.uploadImage(image)
            if (storageResult is Resource.Success) {
                val imageUri = storageResult.data.toString()
                val result = realtimeDatabaseRepository.createData(userId,  imageUri, name, count)
                _objectStatus.value = result
            } else {
                _objectStatus.postValue(
                    Resource.Error(
                        storageResult.message ?: "Image can't upload"
                    )
                )
            }
        }
    }

    private val _mutableObjects = MutableLiveData<Flow<Resource<List<ObjectsModel>>>>()
    val readObjects: LiveData<Flow<Resource<List<ObjectsModel>>>> = _mutableObjects

//    fun readObjects(path: String) {
//        viewModelScope.launch {
////            _mutableObjects.value = Resource.Loading()
//            val result = realtimeDatabaseRepository.readData(path, ObjectsModel::class.java)
//            _mutableObjects.postValue(result)
//        }
//    }

    fun <T> getData(path: String, valueType: Class<T>): LiveData<List<T>> {
        return realtimeDatabaseRepository.readData(path, valueType)
    }


    fun deleteObject(userId: String, path: String) {
        viewModelScope.launch {
            _objectStatus.postValue(Resource.Loading())
            _objectStatus.value = realtimeDatabaseRepository.deleteData(userId, path)
        }
    }

}
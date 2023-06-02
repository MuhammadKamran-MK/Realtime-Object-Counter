package com.example.realtimeobjectcounter.Activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realtimeobjectcounter.Adapters.ObjectsAdapter
import com.example.realtimeobjectcounter.Models.ObjectsModel
import com.example.realtimeobjectcounter.R
import com.example.realtimeobjectcounter.Utils.*
import com.example.realtimeobjectcounter.Utils.Constants.NO_OBJECTS_EXISTS
import com.example.realtimeobjectcounter.Utils.Constants.UID
import com.example.realtimeobjectcounter.ViewModel.AuthenticationViewModel
import com.example.realtimeobjectcounter.ViewModel.RealtimeDatabaseViewModel
import com.example.realtimeobjectcounter.databinding.ActivityMainBinding
import com.example.realtimeobjectcounter.ml.SsdMobilenetV11Metadata1
import com.github.ybq.android.spinkit.SpinKitView
import com.nguyenhoanglam.imagepicker.model.GridCount
import com.nguyenhoanglam.imagepicker.model.ImagePickerConfig
import com.nguyenhoanglam.imagepicker.ui.imagepicker.registerImagePicker
import org.tensorflow.lite.support.common.FileUtil
import java.io.ByteArrayOutputStream
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation
    private lateinit var fabClock: Animation
    private lateinit var fabAntiClock: Animation
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var modelHelper: ModelHelper
    private lateinit var labels: List<String>
    private lateinit var model: SsdMobilenetV11Metadata1
    private lateinit var objectsAdapter: ObjectsAdapter
    private lateinit var authenticationViewModel: AuthenticationViewModel
    private lateinit var realtimeDatabaseViewModel: RealtimeDatabaseViewModel

    private val TAG = "MainActivity"
    private var isOpen: Boolean = false
    private var userID: String? = null
    private var dialog: Dialog? = null
    private var dialogLoader: SpinKitView? = null
    private var itemsList: List<ObjectsModel>? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Initialization of animations
        fabClose = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close)
        fabOpen = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        fabClock = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_rotate_clock)
        fabAntiClock = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_rotate_anticlock)

        // Initializations of instances
        objectsAdapter = ObjectsAdapter()
        permissionsManager = PermissionsManager(this)
        modelHelper = ModelHelper()
        labels = FileUtil.loadLabels(this, "labels.txt")
        model = SsdMobilenetV11Metadata1.newInstance(this)
        authenticationViewModel = AuthenticationViewModel(this)
        realtimeDatabaseViewModel = RealtimeDatabaseViewModel()

        intent?.let { intent ->
            userID = intent.getStringExtra(UID)
        }

        fetchData()

        binding.apply {

            includeToolBar.imgLogout.isVisible = true
            includeToolBar.imgLogout.setOnClickListener {

                val alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
                alertDialogBuilder.setTitle("Logout")
                alertDialogBuilder.setMessage("Are you sure you want to logout!")

                // Set a positive button and its click listener
                alertDialogBuilder.setPositiveButton("Yes") { dialog, which ->
                    // Perform action on OK button click
                    authenticationViewModel.logout()
                    Toast.makeText(this@MainActivity, "Logged Out", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                    dialog.dismiss()
                }

                // Set a negative button and its click listener
                alertDialogBuilder.setNegativeButton("Cancel") { dialog, which ->
                    // Perform action on Cancel button click
                    dialog.dismiss()
                }

                // Create and show the AlertDialog
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()

            }

            fabAdd.setOnClickListener {

                if (isOpen) {
                    // If fab buttons are visible then close it
                    tvCamera.isVisible = false
                    tvGallery.isVisible = false
                    fabCamera.startAnimation(fabClose)
                    fabGallery.startAnimation(fabClose)
                    fabAdd.startAnimation(fabAntiClock)
                    fabCamera.isClickable = false
                    fabGallery.isClickable = false
                    isOpen = false
                } else {
                    // If fab buttons are visible then close it
                    tvCamera.isVisible = true
                    tvGallery.isVisible = true
                    fabCamera.startAnimation(fabOpen)
                    fabGallery.startAnimation(fabOpen)
                    fabAdd.startAnimation(fabClock)
                    fabCamera.isClickable = true
                    fabGallery.isClickable = true
                    isOpen = true
                }

            }

            fabCamera.setOnClickListener {

                if (permissionsManager.dexterPermissions(Manifest.permission.CAMERA)) {

                    val config = ImagePickerConfig(
                        statusBarColor = "#000000",
                        isLightStatusBar = false,
                        isFolderMode = true,
                        isCameraOnly = true,
                        maxSize = 10,
                        subDirectory = "Photos",
                        folderGridCount = GridCount(2, 4),
                        imageGridCount = GridCount(3, 5),
                        // See more at configuration attributes table below
                    )

                    launcher.launch(config)

                }
            }

            fabGallery.setOnClickListener {

                    val config = ImagePickerConfig(
                        statusBarColor = "#000000",
                        isLightStatusBar = false,
                        isFolderMode = true,
                        maxSize = 10,
                        subDirectory = "Photos",
                        folderGridCount = GridCount(2, 4),
                        imageGridCount = GridCount(3, 5),
                        // See more at configuration attributes table below
                    )

                    launcher.launch(config)

            }

        }

    }

    private fun fetchData() {
//        realtimeDatabaseViewModel.readObjects(
//            userID.toString()
//        )
        realtimeDatabaseViewModel.getData(userID.toString(), ObjectsModel::class.java)
            .observe(this) { dataList ->
                // Update the RecyclerView adapter with the new data
                itemsList = dataList
                if (itemsList.isNullOrEmpty()) {
                    binding.tvNoDataExists.isVisible = true
                    binding.rvShowObjects.isVisible = false
                } else {
                    binding.tvNoDataExists.isVisible = false
                    binding.rvShowObjects.isVisible = true
                }
                attachRVAdapter()
                Log.e(TAG, "fetchData: dataList : $dataList")
            }
    }

    private val launcher = registerImagePicker { images ->
        // Selected images are ready to use
        if (images.isNotEmpty()) {
            val sampleImage = images[0]
            showImageDialog(
                modelHelper.getObjectsCount(
                    labels, model,
                    uriToBitmap(sampleImage.uri)!!
                )
            )
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        val imageStream: InputStream? = contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(imageStream)
    }

    private fun bitmapToUri(bitmap: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String =
            MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Releases model resources if no longer used.
        model.close()
    }

    @SuppressLint("SetTextI18n", "SuspiciousIndentation")
    private fun showImageDialog(modelHelper: ModelHelper.Result) {
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.dialog_show_image)
        val dialog = builder.create()
        dialog.window?.decorView?.setBackgroundResource(R.drawable.dialog_background)
        dialog.show()

        this.dialog = dialog

        dialog.apply {

            val selectedImage = findViewById<AppCompatImageView>(R.id.selectedImage)
            val tvObjects = findViewById<AppCompatTextView>(R.id.tvObjects)
            val imgClose = findViewById<AppCompatImageView>(R.id.imgClose)
            val btnSaveObject = findViewById<AppCompatButton>(R.id.btnSaveObject)
            dialogLoader = findViewById(R.id.dialogLoader)

            selectedImage?.setImageBitmap(modelHelper.bitmap)
            if (modelHelper.count > 0) {
                tvObjects?.text = "${modelHelper.count} objects"
                btnSaveObject?.isVisible = true
            } else {
                btnSaveObject?.isVisible = false
                selectedImage?.setImageBitmap(modelHelper.bitmap)
                tvObjects?.text = "Image may not clear or don't have objects"
            }

            btnSaveObject?.setOnClickListener {
                addObject(
                    bitmapToUri(modelHelper.bitmap),
                    modelHelper.names,
                    modelHelper.count
                )
                dialog.dismiss()
            }

            imgClose?.setOnClickListener {
                dialog.dismiss()
            }

        }
    }

    private fun addObject(imgUri: Uri?, name: MutableSet<String>, count: Int) {
        Log.e(TAG, "addObject: names : $name")
        if (imgUri != null) {
            realtimeDatabaseViewModel.submitData(
                userID.toString(),
                imgUri,
                name.toString(),
                count.toString()
            )
        }

        bindWriteObserver()

    }

    private fun bindWriteObserver() {
        realtimeDatabaseViewModel.objectStatus.observe(this) {
            when (it) {
                is Resource.Error -> {
                    dialogLoader?.isVisible = false
                    Toast.makeText(applicationContext, it.message, Toast.LENGTH_SHORT).show()
                }

                is Resource.Loading -> {
                    dialogLoader?.isVisible = true
                }

                is Resource.Success -> {
                    dialogLoader?.isVisible = false
                    dialog?.dismiss()
                }
            }
        }
    }

    private fun attachRVAdapter() {
        Log.e(TAG, "attachRVAdapter:")
        binding.apply {
            // Attach adapter to recyclerView
            rvShowObjects.apply {
                val linearLayoutManager = LinearLayoutManager(this@MainActivity)
                layoutManager = linearLayoutManager
                objectsAdapter.submitList(itemsList)
                adapter = objectsAdapter
//                if (objectsAdapter.itemCount == 0) {
//                    isVisible = false   // Hide recyclerView
//                    tvNoDataExists.isVisible = true     // Show no data textView
//                    tvNoDataExists.text = NO_OBJECTS_EXISTS     // Set text
//                } else {
//                    isVisible = true   // Show recyclerView
//                    tvNoDataExists.isVisible = false     // Hide no data textView
//                }
            }
        }
    }

}
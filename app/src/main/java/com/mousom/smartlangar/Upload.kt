package com.mousom.smartlangar

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [Upload.newInstance] factory method to
 * create an instance of this fragment.
 */
class Upload : Fragment() {
    var textInputLayout: TextInputLayout? = null
    var Uploadphn: TextInputEditText? = null
    var Uploadadd: TextInputEditText? = null
    var Uploadquantity: TextInputEditText? = null
    var upload: Button? = null
    var chooseImage: Button? = null
    var Foodimage: ImageView? = null
    var linearProgressIndicator: LinearProgressIndicator? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var firestore: FirebaseFirestore? = null
    var firebaseAuth: FirebaseAuth? = null
    var storageReference: StorageReference? = null
    var userID: String? = null
    var i: Intent? = null
    var loader: ProgressBar? = null
    var locationManager: LocationManager? = null
    var GpsStatus = false
    var url: String? = null
    var imageUri: Uri? = null
    private val filePath: Uri? = null
    private val PICK_IMAGE_REQUEST = 22

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_upload, container, false)
        textInputLayout = view.findViewById(R.id.upload_address)
        Uploadphn = view.findViewById(R.id.upload_phone)
        Uploadadd = view.findViewById(R.id.upload_add)
        Foodimage = view.findViewById(R.id.food_image)
        Uploadquantity = view.findViewById(R.id.upload_quantity)
        upload = view.findViewById(R.id.upload_button)
        chooseImage = view.findViewById(R.id.choose_image)
        linearProgressIndicator = view.findViewById(R.id.progress)
        loader = view.findViewById(R.id.food_img_load)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        val imageRef = storageReference!!.child("users/$userID")
        userID = firebaseAuth!!.currentUser!!.uid
        val df = firestore!!.collection("users").document(
            userID!!
        )
        df.addSnapshotListener(requireActivity()) { value, error ->
            Uploadphn?.setText(value!!.getString("PhoneNum"))
            Uploadadd?.setText(value!!.getString("Address"))
        }
        fusedLocationProviderClient = activity?.let {
            LocationServices.getFusedLocationProviderClient(
                it
            )
        }
        textInputLayout?.setEndIconOnClickListener(View.OnClickListener { v: View? -> CheckGpsStatus() })
        upload?.setOnClickListener(View.OnClickListener { v: View? -> UploadFoodToDatabase() })
        chooseImage?.setOnClickListener(View.OnClickListener { v: View? ->
            val igallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(igallery, 1001)
        })
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            if (resultCode == Activity.RESULT_OK) {
                loader!!.visibility = ProgressBar.VISIBLE
                imageUri = data!!.data
                Foodimage!!.setImageURI(imageUri)
                var bmp: Bitmap? = null
                try {
                    bmp = MediaStore.Images.Media.getBitmap(
                        requireActivity().applicationContext.contentResolver,
                        imageUri
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                val baos = ByteArrayOutputStream()
                bmp!!.compress(Bitmap.CompressFormat.JPEG, 25, baos)
                val fileInBytes = baos.toByteArray()
                UploadImgToFireStorage(fileInBytes)
            }
        }
    }

    private fun UploadImgToFireStorage(fileInBytes: ByteArray) {
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("dd-MM-yyy-hh-mm")
        val currentTime = simpleDateFormat.format(calendar.time)
        val fileRef = storageReference!!.child("users/$userID/$currentTime.jpg")
        fileRef.putBytes(fileInBytes).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                url = uri.toString()
                loader!!.visibility = ProgressBar.INVISIBLE
            }.addOnFailureListener { }
        }.addOnFailureListener { e ->
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun UploadFoodToDatabase() {
        //Get values from EditText
        val phn = Uploadphn!!.text.toString()
        val address = Uploadadd!!.text.toString()
        val quantity = Uploadquantity!!.text.toString().trim { it <= ' ' }
        if (phn.isEmpty() || address.isEmpty() || quantity.isEmpty()) {
            Uploadphn!!.error = "Enter Phone Number"
            Uploadadd!!.error = "Enter Address..."
            Uploadquantity!!.error = "Quantity required"
            Uploadquantity!!.requestFocus()
        } else {
            val documentReference = firestore!!.collection("foods").document()
            val food: MutableMap<String, Any?> = HashMap()
            food["Phone"] = phn
            food["Address"] = address
            food["Quantity"] = quantity
            food["food_image"] = url
            food["id"] = userID
            linearProgressIndicator!!.visibility = View.VISIBLE
            documentReference.set(food).addOnSuccessListener { unused: Void? ->
                Toast.makeText(getContext(), "Food Uploaded...", Toast.LENGTH_LONG).show()
                linearProgressIndicator!!.visibility = View.INVISIBLE
                Uploadphn!!.text!!.clear()
                Uploadadd!!.text!!.clear()
                Uploadquantity!!.text!!.clear()
            }
        }
    }

    private val permission: Unit
        private get() {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                CheckGpsStatus()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    44
                )
            }
        }

    fun CheckGpsStatus() {
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        assert(locationManager != null)
        GpsStatus = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (GpsStatus) {
            location
        } else {
            OpenLocationSetting()
        }
    }

    fun OpenLocationSetting() {
        i = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(i)
    }//set address

    // Got last known location. In some rare situations this can be null.
    private val location: Unit
        private get() {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permission
                return
            }
            fusedLocationProviderClient!!.lastLocation
                .addOnSuccessListener(requireActivity()) { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        try {
                            val geocoder = Geocoder(
                                getContext(),
                                Locale.getDefault()
                            )
                            val addresses = geocoder.getFromLocation(
                                location.latitude, location.longitude, 1
                            )

                            //set address
                            Uploadadd!!.setText(addresses[0].getAddressLine(0))
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
        }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Upload.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): Upload {
            val fragment = Upload()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}
package com.mousom.smartlangar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mousom.smartlangar.MainActivity
import com.squareup.picasso.Picasso
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFrag.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFrag : Fragment() {
    var googleName: TextView? = null
    var profileName: TextView? = null
    var profilePhone: TextView? = null
    var profilePic: ImageView? = null
    var logout: Button? = null
    var updateProfile: Button? = null
    var fstore: FirebaseFirestore? = null
    var userId: String? = null

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
        val v = inflater.inflate(R.layout.fragment_profile, container, false)
        googleName = v.findViewById(R.id.google_name)
        profilePic = v.findViewById(R.id.profile_pic)
        profileName = v.findViewById(R.id.profile_name)
        profilePhone = v.findViewById(R.id.profile_num)
        logout = v.findViewById(R.id.log_out)
        updateProfile = v.findViewById(R.id.save_profile)
        fstore = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        val df = fstore!!.collection("users").document(
            userId!!
        )
        df.addSnapshotListener(requireActivity()) { value, error ->
            profilePhone?.setText(
                value!!.getString(
                    "PhoneNum"
                )
            )
        }
        updateProfile?.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, Profile::class.java)
            startActivity(intent)
        })
        logout?.setOnClickListener(View.OnClickListener { v1: View? ->
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(activity, MainActivity::class.java))
        })
        val img = Objects.requireNonNull(FirebaseAuth.getInstance().currentUser)
            ?.photoUrl
        val name = FirebaseAuth.getInstance().currentUser!!.displayName
        if (FirebaseAuth.getInstance().currentUser != null) {
            googleName?.setText("Welcome $name")
            profileName?.setText(" $name")
            Picasso.get().load(img).into(profilePic)
        }
        return v
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
         * @return A new instance of fragment ProfileFrag.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): ProfileFrag {
            val fragment = ProfileFrag()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}
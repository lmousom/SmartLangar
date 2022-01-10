package com.mousom.smartlangar


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.squareup.picasso.Picasso
import java.util.*

class Home : Fragment() {
    private var Fooditems: RecyclerView? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null
    private var adapter: FirestoreRecyclerAdapter<*, *>? = null
    private var mFrameLayout: ShimmerFrameLayout? = null
    private val ID: String? = null

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
        val v = inflater.inflate(R.layout.fragment_home, container, false)
        Fooditems = v.findViewById(R.id.food_items)
        mFrameLayout = v.findViewById(R.id.shimmerLayout)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val userID = Objects.requireNonNull(firebaseAuth!!.currentUser)
            ?.uid
        val fullName = firebaseAuth!!.currentUser!!.displayName
        val query = firestore!!.collection("foods").whereEqualTo("id", userID)
        query.get().addOnCompleteListener { task: Task<QuerySnapshot?>? ->
            mFrameLayout?.setVisibility(View.INVISIBLE)
            mFrameLayout?.stopShimmer()
            adapter!!.notifyDataSetChanged()
        }.addOnFailureListener { e: Exception ->
            Toast.makeText(
                activity,
                e.message,
                Toast.LENGTH_SHORT
            ).show()
        }
            .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot? ->
                Fooditems?.setVisibility(
                    View.VISIBLE
                )
            }
        val options = FirestoreRecyclerOptions.Builder<FoodModel>()
            .setQuery(query, FoodModel::class.java)
            .build()
        adapter = object : FirestoreRecyclerAdapter<FoodModel, FoodsViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodsViewHolder {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.food_card, parent, false)
                return FoodsViewHolder(view)
            }

            override fun onBindViewHolder(
                holder: FoodsViewHolder,
                position: Int,
                model: FoodModel
            ) {
                Picasso.get().load(model.getuser_image()).into(holder.food_pic)
                holder.food_quantity.text = model.getuser_quantity()
                holder.fullName.text = fullName
            }
        }
        Fooditems?.setHasFixedSize(true)
        Fooditems?.setLayoutManager(LinearLayoutManager(context))
        Fooditems?.setAdapter(adapter)
        return v
    }

    private class FoodsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val food_quantity: TextView
        val fullName: TextView
        val food_pic: ImageView

        init {
            food_quantity = itemView.findViewById(R.id.food_quan)
            fullName = itemView.findViewById(R.id.user_name)
            food_pic = itemView.findViewById(R.id.food_img)
        }
    }

    override fun onStart() {
        super.onStart()
        if (adapter != null) {
            adapter!!.startListening()
            mFrameLayout!!.startShimmer()
        }
    }

    override fun onResume() {
        super.onResume()
        mFrameLayout!!.startShimmer()
    }

    override fun onPause() {
        super.onPause()
        mFrameLayout!!.stopShimmer()
    }

    override fun onStop() {
        super.onStop()
        adapter!!.stopListening()
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        fun newInstance(param1: String?, param2: String?): Home {
            val fragment = Home()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}
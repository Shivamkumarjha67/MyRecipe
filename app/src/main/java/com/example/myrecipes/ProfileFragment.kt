package com.example.myrecipes

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.myrecipes.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {

    private lateinit var binding : FragmentProfileBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private var selectedimageURI : Uri? = null

    companion object {
        val PICK_IMAGE_REQUEST = 108
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()

        getUserDetails()

        binding.btnLogOut.setOnClickListener {
            firebaseAuth.signOut()

            startActivity(Intent(activity, SigninActivity::class.java))
            Toast.makeText(activity, "Successfully Logged Out", Toast.LENGTH_SHORT).show()
        }

        binding.btnChange.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.change_profile_dialog)
            dialog.setCanceledOnTouchOutside(true)

            val userName : TextView = dialog.findViewById(R.id.edtName)
            val userImg : ImageView = dialog.findViewById(R.id.chosenImg)
            val setUser : Button = dialog.findViewById(R.id.btnSet)

            userImg.setOnClickListener {
                openGallary()
                loadImageFromUrl(selectedimageURI.toString(), userImg)
            }

            setUser.setOnClickListener {
                val name = userName.text.toString()

                if(name.isNotEmpty()) {
                    binding.txtUserName.text = name
                    binding.txtUserName.visibility = View.VISIBLE
                    binding.img.setImageURI(selectedimageURI)

                    val userId = firebaseAuth.currentUser?.uid.toString()
                    val db = Firebase.firestore
                    val userCollections = db.collection("users")

                    userCollections.document(userId).get()
                        .addOnSuccessListener { documentSnapshot ->
                            val user = documentSnapshot.toObject(User::class.java)

                            user?.userName = name
                            user?.Image = selectedimageURI.toString()

                            if (user != null) {
                                userCollections.document(userId).set(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Failed to Updated!", Toast.LENGTH_SHORT).show()
                                        println("Exception is ${it}")
                                    }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "User not found!", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Field should not be empty!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }

            dialog.show()
        }

        return binding.root
    }

    private fun openGallary() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == AddFragment.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedimageURI = data.data
        }
    }

    private fun getUserDetails() {
        val db = Firebase.firestore
        val userCollections = db.collection("users")

        userCollections.document(firebaseAuth.currentUser?.uid.toString()).get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                println(user)

                if(user != null) {
                    if(user.Image.isNotEmpty())
                        loadImageFromUrl(user.Image, binding.img)

                    if(user.userName.isNotEmpty()) {
                        binding.txtUserName.setText(user.userName)
                        binding.txtUserName.visibility = View.VISIBLE
                    }
                }
            }
    }

    private fun loadImageFromUrl(imageUrl: String, image: ImageView) {
        Glide.with(requireContext()).load(imageUrl).placeholder(R.drawable.no_img).error(R.drawable.no_img)
            .into(image)
    }
}
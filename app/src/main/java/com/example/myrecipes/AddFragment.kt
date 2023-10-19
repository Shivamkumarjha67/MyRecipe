package com.example.myrecipes

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.example.myrecipes.databinding.FragmentAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddFragment : Fragment() {
    private lateinit var binding : FragmentAddBinding
    private lateinit var imageShow : ImageView
    private lateinit var firebaseAuth : FirebaseAuth
    private var selectedimageURI : Uri? = null
    private var dowloadableImageURI : String? = null
    private lateinit var saveRecipeButton : AppCompatButton
    private lateinit var recipeName : String
    private lateinit var ingredients : String
    private lateinit var instructions : String

    companion object {
        val PICK_IMAGE_REQUEST = 108
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddBinding.inflate(inflater, container, false)
        imageShow = binding.img

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnSelect.setOnClickListener {
            openGallary()
        }

        saveRecipeButton = binding.btnSaveRecipe
        saveRecipeButton.setOnClickListener {
            instructions = binding.edtInstructions.text.toString()
            recipeName = binding.edtRecipeName.text.toString()
            ingredients = binding.edtIngredient.text.toString()

            if(selectedimageURI != null && instructions.length > 0 && recipeName.length > 0 && ingredients.length > 0) {
                Toast.makeText(context, "All good!", Toast.LENGTH_SHORT).show()
                UploadImageToCloudStorage()
            } else {
                Toast.makeText(context, "First select image and fill all fields!", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun UploadImageToCloudStorage() {
        val progressiveDialog = ProgressDialog(activity)
        progressiveDialog.setTitle("Uploading...")
        progressiveDialog.setMessage("Uploading your image..")
        progressiveDialog.show()

        val ref = FirebaseStorage.getInstance().getReference()
            .child(UUID.randomUUID().toString())

        ref.putFile(selectedimageURI!!)
            .addOnSuccessListener {taskSnapshot ->
                progressiveDialog.dismiss()
                taskSnapshot.storage.downloadUrl
                    .addOnSuccessListener {
                        dowloadableImageURI = it.toString()
                        println("downloadable uri is.. ${it}")

                        saveToDatabase()
                    }
            }
            .addOnFailureListener{
                progressiveDialog.dismiss()
                Toast.makeText(context,"Failed to upload.. ${it}", Toast.LENGTH_SHORT).show()

                println("Failed to upload.. ${it}")
            }
    }

    private fun saveToDatabase() {
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Saving Recipe")
        progressDialog.setMessage("Please Wait..")
        progressDialog.show()

        val userId = firebaseAuth.currentUser?.uid

        val recipe = hashMapOf(
            "recipeName" to recipeName,
            "ingredients" to ingredients,
            "instructions" to instructions,
            "imageUrl" to dowloadableImageURI,
            "userID" to userId
        )

        Firebase.firestore.collection("recipes")
            .add(recipe)
            .addOnSuccessListener {documentReference ->
                progressDialog.dismiss()
                Toast.makeText(context,"Recipe Added successfully!", Toast.LENGTH_SHORT).show()
                println(documentReference)

                saveRecipeToProfile(documentReference.id.toString())
            }
            .addOnFailureListener{
                progressDialog.dismiss()
                Toast.makeText(context,"Recipe Added successfully!", Toast.LENGTH_SHORT).show()
                println("Error adding Recipe ${it}")
            }
    }

    private fun saveRecipeToProfile(recipeId: String) {
        val userId = firebaseAuth.currentUser?.uid
        val userEmail = firebaseAuth.currentUser?.email

        if(userId != null && userEmail != null) {
            val db = Firebase.firestore
            val userCollection = db.collection("users")

            userCollection.document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                    if(documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)

                        if(user != null) {
                            user.recipes.add(recipeId)
                            userCollection.document(userId).set(user)
                                .addOnSuccessListener {
                                    println("Recipe Id added to user document")
                                }
                                .addOnFailureListener {
                                    println("Failed to update user document : ${it}")
                                }
                        }
                    } else {
                        val newUser = User(userId, userEmail, mutableListOf(recipeId))
                        userCollection.document(userId).set(newUser)
                            .addOnSuccessListener {
                                println("New user document created with recipe ID ${recipeId}")
                            }
                            .addOnFailureListener { exception ->
                                println("Failed to create a new user document : ${exception}")
                            }
                    }
                }
                .addOnFailureListener {
                    println("Failed to check user document : ${it}")
                }
        }  else {
            Toast.makeText(context,"Unable to add recipe Id in user profile !", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallary() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedimageURI = data.data
            println(selectedimageURI)

            if(selectedimageURI != null) {
                imageShow.setImageURI(selectedimageURI)
            }
        }
    }
}
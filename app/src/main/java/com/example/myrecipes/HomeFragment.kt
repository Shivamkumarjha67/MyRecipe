package com.example.myrecipes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.myrecipes.databinding.FragmentHomeBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    private lateinit var binding : FragmentHomeBinding
    private lateinit var recyclerRecipes : RecyclerView
    private lateinit var arrRecipes : ArrayList<Recipe>
    private lateinit var recipeAdapter : RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(
            inflater, container, false
        )

        recyclerRecipes = binding.recyclerRecepie
        recyclerRecipes.layoutManager = LinearLayoutManager(context)

        showRecipes()

        return binding.root
    }

    private fun showRecipes() {
        arrRecipes = arrayListOf<Recipe>()

        val db = Firebase.firestore
        db.collection("recipes").get()
            .addOnSuccessListener { result->
                println("Recipe feching successful")
                for(item in result) {
                    val recipeData = item.data
//                    println(recipeData)
                    try {
                        if(recipeData != null) {
                            val recipeName = recipeData["recipeName"] as String
                            val ingredients = recipeData["ingredients"] as String
                            val instructions = recipeData["instructions"] as String
                            val image = recipeData["imageUrl"] as String
                            val owner = recipeData["userID"] as String

                            val recipe = Recipe(recipeName, ingredients, instructions, image, owner)
                            arrRecipes.add(recipe)
                        }
                    } catch (e : Exception) {
                        e.printStackTrace()
                    }

                    if(arrRecipes.isNotEmpty()) {
                        recipeAdapter = RecipeAdapter(arrRecipes, requireContext())
                        recyclerRecipes.adapter = recipeAdapter
                    } else {
                        println("Array Recipe is Empty")
                    }
                }

//                println(arrRecipes[1].recipeName)
            }
            .addOnFailureListener {
                println("Recipe feching failed ${it}")
            }
    }
}
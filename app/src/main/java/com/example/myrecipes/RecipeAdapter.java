package com.example.myrecipes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.viewHolder> {
    private ArrayList<Recipe> arrRecipe = new ArrayList();
    private Context context;

    public RecipeAdapter(ArrayList<Recipe> arr, Context context) {
        this.arrRecipe = arr;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recipe_layout, parent, false);
        viewHolder viewHolderr = new viewHolder(view);
        return viewHolderr;
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Recipe recipe = arrRecipe.get(position);

        holder.recipeName.setText(recipe.getRecipeName());
        holder.ingredients.setText(recipe.getIngredient());
        holder.instructions.setText(recipe.getInstruction());

        loadImageFromUrl(recipe.getImageUrl(), holder.image);
    }

    @Override
    public int getItemCount() {
        return arrRecipe.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        TextView recipeName, instructions, ingredients;
        ImageView image;
        CardView cardView;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.recipeCard);
            recipeName = itemView.findViewById(R.id.txtRecipeName);
            instructions = itemView.findViewById(R.id.txtInstructions);
            ingredients = itemView.findViewById(R.id.txtIngredients);
            image = itemView.findViewById(R.id.recipeImage);
        }
    }

    private void loadImageFromUrl(String imageUrl, ImageView image) {
        Glide.with(context).load(imageUrl).placeholder(R.drawable.no_img).error(R.drawable.no_img).into(image);
    }
}

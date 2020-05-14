package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodapp.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import EventBus.FoodItemClick;
import Callbacks.RecyclerClickListener;
import Common.Common;
import Models.FoodModel;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FoodListAdapter extends RecyclerView.Adapter<FoodListAdapter.ViewHolder> {

    private Context context;
    private List<FoodModel> foodModelList;

    public FoodListAdapter(Context context, List<FoodModel> foodModelList) {
        this.context = context;
        this.foodModelList = foodModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_food_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(foodModelList.get(position).getImage()).into(holder.food_item_image);
        holder.food_item_price.setText(new StringBuilder("$")
        .append(foodModelList.get(position).getPrice()));
        holder.food_item_name.setText(new StringBuilder("")
        .append(foodModelList.get(position).getName()));

        //Event
        holder.setRecyclerClickListener(new RecyclerClickListener() {
            @Override
            public void onItemClickListener(View view, int position) {
                Common.selectedFood = foodModelList.get(position);
                EventBus.getDefault().postSticky(new FoodItemClick(true,foodModelList.get(position)));
            }
        });

    }

    @Override
    public int getItemCount() {
        return foodModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Unbinder unbinder;
        @BindView(R.id.food_item_name)
        TextView food_item_name;
        @BindView(R.id.food_item_price)
        TextView food_item_price;
        @BindView(R.id.food_item_image)
        ImageView food_item_image;
        @BindView(R.id.img_cart)
        ImageView img_cart;


        RecyclerClickListener recyclerClickListener;

        public void setRecyclerClickListener(RecyclerClickListener recyclerClickListener) {
            this.recyclerClickListener = recyclerClickListener;
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            recyclerClickListener.onItemClickListener(v,getAdapterPosition());
        }
    }
}

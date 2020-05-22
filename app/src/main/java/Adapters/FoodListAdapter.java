package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodapp.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import Database.CartDataBase;
import Database.CartDataSource;
import Database.CartItem;
import Database.LocalCartDataSource;
import EventBus.FoodItemClick;
import EventBus.CounterCartEvent;
import Callbacks.RecyclerClickListener;
import Common.Common;
import Models.FoodModel;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FoodListAdapter extends RecyclerView.Adapter<FoodListAdapter.ViewHolder> {

    private Context context;
    private List<FoodModel> foodModelList;
    private CompositeDisposable compositeDisposable;
    private CartDataSource cartDataSource;

    public FoodListAdapter(Context context, List<FoodModel> foodModelList) {
        this.context = context;
        this.foodModelList = foodModelList;
        this.compositeDisposable = new CompositeDisposable();
        this.cartDataSource = new LocalCartDataSource(CartDataBase.getInstance(context).cartDAO());
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

        holder.img_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CartItem cartItem = new CartItem();
                cartItem.setUid(Common.currentUser.getUid());
                cartItem.setUserPhone(Common.currentUser.getPhone());

                cartItem.setFoodId(foodModelList.get(position).getId());
                cartItem.setFoodName(foodModelList.get(position).getName());
                cartItem.setFoodImage(foodModelList.get(position).getImage());

                cartItem.setFoodPrice(Double.valueOf(String.valueOf(foodModelList.get(position).getPrice())));
                cartItem.setFoodQuantity(1);
                cartItem.setFoodExtraPrice(0.0);
                cartItem.setFoodAddon("Default");
                cartItem.setFoodSize("Default");

                cartDataSource.getItemWithAllOptionsInCart(Common.currentUser.getUid(),
                        cartItem.getFoodId(),
                        cartItem.getFoodSize(),
                        cartItem.getFoodAddon())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<CartItem>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onSuccess(CartItem cartItemFromDB) {
                                if(cartItemFromDB.equals(cartItem)){
                                    //Just update
                                    cartItemFromDB.setFoodExtraPrice(cartItem.getFoodExtraPrice());
                                    cartItemFromDB.setFoodAddon(cartItem.getFoodAddon());
                                    cartItemFromDB.setFoodSize(cartItem.getFoodSize());
                                    cartItemFromDB.setFoodQuantity(cartItemFromDB.getFoodQuantity()+cartItem.getFoodQuantity());

                                    cartDataSource.updateCartItems(cartItemFromDB)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new SingleObserver<Integer>() {
                                                @Override
                                                public void onSubscribe(Disposable d) {

                                                }

                                                @Override
                                                public void onSuccess(Integer integer) {
                                                    Toast.makeText(context, "Cart was updated", Toast.LENGTH_SHORT).show();
                                                    EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    Toast.makeText(context, "[UPDATE CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }else{
                                    //New item
                                    compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(()->{
                                                Toast.makeText(context, "Add to Cart", Toast.LENGTH_SHORT).show();
                                                //Update Counter
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                            }, throwable -> {
                                                Toast.makeText(context, "[CART ERROR]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            }));
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                if(e.getMessage().contains("empty")){
                                    //Default
                                    compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(()->{
                                                Toast.makeText(context, "Add to Cart", Toast.LENGTH_SHORT).show();
                                                //Update Counter
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                            }, throwable -> {
                                                Toast.makeText(context, "[CART ERROR]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            }));
                                }else {
                                    Toast.makeText(context, "[GET CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
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

package com.example.foodapp.ui.fooddetail;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.foodapp.R;
import com.example.foodapp.ui.foodlist.FoodListViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.gson.Gson;


import org.greenrobot.eventbus.EventBus;
import EventBus.CounterCartEvent;

import java.util.ArrayList;

import Database.CartDataBase;
import Database.CartDataSource;
import Database.CartItem;
import Database.LocalCartDataSource;
import Models.AddonModel;
import Models.FoodModel;
import Common.Common;
import Models.SizeModel;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FoodDetailFragment extends Fragment implements TextWatcher {

    private FoodDetailViewModel foodDetailViewModel;
    private BottomSheetDialog bottomSheetDialog;
    private CartDataSource cartDataSource;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    //need inflate
    ChipGroup chipGroup_addon;
    EditText edt_search;

    Unbinder unbinder;
    @BindView(R.id.image_food_detail)
    ImageView image_food;
    @BindView(R.id.btn_cart)
    CounterFab btn_cart;
    @BindView(R.id.food_name)
    TextView food_name;
    @BindView(R.id.food_description)
    TextView food_description;
    @BindView(R.id.food_price)
    TextView food_price;
    @BindView(R.id.number_button)
    ElegantNumberButton number_button;
    @BindView(R.id.rg_group_size)
    RadioGroup rg_group_size;
    @BindView(R.id.img_add_addon)
    ImageView img_add_addon;
    @BindView(R.id.chip_group_user_selected)
    ChipGroup chip_group_user_selected;



    @OnClick(R.id.img_add_addon)
    void onAddonClick(){
        if(Common.selectedFood.getAddon() != null){
            displayAddonList();
            bottomSheetDialog.show();
        }
    }

    @OnClick(R.id.btn_cart)
    void onCartItemAdd(){
        CartItem cartItem = new CartItem();
        cartItem.setUid(Common.currentUser.getUid());
        cartItem.setUserPhone(Common.currentUser.getPhone());

        cartItem.setFoodId(Common.selectedFood.getId());
        cartItem.setFoodName(Common.selectedFood.getName());
        cartItem.setFoodImage(Common.selectedFood.getImage());

        cartItem.setFoodPrice(Double.valueOf(String.valueOf(Common.selectedFood.getPrice())));
        cartItem.setFoodQuantity(Integer.valueOf(number_button.getNumber()));
        cartItem.setFoodExtraPrice(Common.calculateExtraPrice(Common.selectedFood.getUserSelectedSize(),Common.selectedFood.getUserSelectedAddon()));

        if(Common.selectedFood.getUserSelectedAddon() != null){
            cartItem.setFoodAddon(new Gson().toJson(Common.selectedFood.getUserSelectedAddon()));
        }else{
            cartItem.setFoodAddon("Default");
        }
        if(Common.selectedFood.getUserSelectedSize() != null){
            cartItem.setFoodSize(new Gson().toJson(Common.selectedFood.getUserSelectedSize()));
        }else{
            cartItem.setFoodSize("Default");
        }

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
                                            Toast.makeText(getContext(), "Корзина обновлена", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Toast.makeText(getContext(), "[UPDATE CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }else{
                            //New item
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(()->{
                                        Toast.makeText(getContext(), "Добавлено в корзину", Toast.LENGTH_SHORT).show();
                                        //Update Counter
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    }, throwable -> {
                                        Toast.makeText(getContext(), "[CART ERROR]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(getContext(), "Добавлено в корзину", Toast.LENGTH_SHORT).show();
                                        //Update Counter
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    }, throwable -> {
                                        Toast.makeText(getContext(), "[CART ERROR]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }));
                        }else {
                            Toast.makeText(getContext(), "[GET CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void displayAddonList() {
        if(Common.selectedFood.getAddon().size() > 0){
            chipGroup_addon.clearCheck();
            chipGroup_addon.removeAllViews();

            edt_search.addTextChangedListener(this);

            for(AddonModel addonModel:Common.selectedFood.getAddon()){

                    Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_item,null);
                    chip.setText(new StringBuilder(addonModel.getName()).append("(+Руб ")
                            .append(addonModel.getPrice()).append(")"));

                    chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(isChecked){
                                if(Common.selectedFood.getUserSelectedAddon() == null){
                                    Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                                }
                                Common.selectedFood.getUserSelectedAddon().add(addonModel);
                            }
                        }
                    });
                    chipGroup_addon.addView(chip);

            }
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodDetailViewModel = ViewModelProviders.of(this).get(FoodDetailViewModel.class);
        View root = inflater.inflate(R.layout.fragment_food_detail, container, false);
        unbinder = ButterKnife.bind(this,root);
        initViews();
        foodDetailViewModel.getFoodModelMutableLiveData().observe(getViewLifecycleOwner(), new Observer<FoodModel>() {
            @Override
            public void onChanged(FoodModel foodModel) {
                displayInfo(foodModel);
            }
        });
        return root;
    }

    private void initViews() {

        cartDataSource = new LocalCartDataSource(CartDataBase.getInstance(getContext()).cartDAO());

        bottomSheetDialog = new BottomSheetDialog(getContext(),R.style.DialogStyle);
        View layout_addon_display = getLayoutInflater().inflate(R.layout.layout_addon_display,null);
        chipGroup_addon = (ChipGroup) layout_addon_display.findViewById(R.id.chip_group_addon);
        edt_search = (EditText)layout_addon_display.findViewById(R.id.edt_search);
        bottomSheetDialog.setContentView(layout_addon_display);

        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                displayUserSelectedAddon();
                calculateThePrice();
            }
        });
    }

    private void displayUserSelectedAddon() {
        if(Common.selectedFood.getUserSelectedAddon() != null &&
        Common.selectedFood.getUserSelectedAddon().size() > 0){
            chip_group_user_selected.removeAllViews();
            for(AddonModel addonModel:Common.selectedFood.getUserSelectedAddon()){
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon,null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+Руб ")
                .append(addonModel.getPrice()).append(")"));
                chip.setClickable(false);
                chip.setOnCloseIconClickListener(view->{
                    chip_group_user_selected.removeView(view);
                    Common.selectedFood.getUserSelectedAddon().remove(addonModel);
                    calculateThePrice();

                });
                chip_group_user_selected.addView(chip);
            }
        }
        if(Common.selectedFood.getUserSelectedAddon() != null && Common.selectedFood.getUserSelectedAddon().size() == 0){
            chip_group_user_selected.removeAllViews();
        }
    }

    private void displayInfo(FoodModel foodModel) {
        Glide.with(getContext()).load(foodModel.getImage()).into(image_food);
        food_name.setText(new StringBuilder(foodModel.getName()));
        food_description.setText(new StringBuilder(foodModel.getDescription()));
        food_price.setText(new StringBuilder(foodModel.getPrice().toString()));


        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(Common.selectedFood.getName());

        //Size
        for(SizeModel sizeModel: Common.selectedFood.getSize()){
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        Common.selectedFood.setUserSelectedSize(sizeModel);
                        calculateThePrice();
                    }
                }
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1.0f);
            radioButton.setLayoutParams(params);
            radioButton.setText(sizeModel.getName());
            radioButton.setTag(sizeModel.getPrice());

            rg_group_size.addView(radioButton);
        }

        if(rg_group_size.getChildCount() > 0){
            RadioButton radioButton = (RadioButton)rg_group_size.getChildAt(0);
            radioButton.setChecked(true); //Default
        }
        calculateThePrice();
    }

    private void calculateThePrice() {
        double totalPrice = Double.parseDouble(Common.selectedFood.getPrice().toString()),displayPrice = 0.0;

        //Addon
        if(Common.selectedFood.getUserSelectedAddon() != null &&
        Common.selectedFood.getUserSelectedAddon().size()>0){
            for(AddonModel addonModel : Common.selectedFood.getUserSelectedAddon()){
                totalPrice+= Double.parseDouble(addonModel.getPrice().toString());

            }
        }

        //Size
        totalPrice += Double.parseDouble(Common.selectedFood.getUserSelectedSize().getPrice().toString());
        displayPrice = totalPrice*(Integer.parseInt(number_button.getNumber()));
        displayPrice = Math.round(displayPrice*100.0/100.0);

        food_price.setText(new StringBuilder("").append(Common.formatPrice(displayPrice)).toString());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        chipGroup_addon.clearCheck();
        chipGroup_addon.removeAllViews();

        for(AddonModel addonModel:Common.selectedFood.getAddon()){
            if(addonModel.getName().toLowerCase().contains(s.toString().toLowerCase())){
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_item,null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+Руб ")
                .append(addonModel.getPrice()).append(")"));

                chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            if(Common.selectedFood.getUserSelectedAddon() == null){
                                Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                            }
                            Common.selectedFood.getUserSelectedAddon().add(addonModel);
                        }
                    }
                });
                chipGroup_addon.addView(chip);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
}

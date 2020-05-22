package com.example.foodapp.ui.cart;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import Common.Common;
import Database.CartDataBase;
import Database.CartDataSource;
import Database.CartItem;
import Database.LocalCartDataSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class CartViewModel extends ViewModel {

    private CompositeDisposable compositeDisposable;
    private CartDataSource cartDataSource;
    private MutableLiveData<List<CartItem>> listMutableLiveData;

    public CartViewModel() {
        compositeDisposable = new CompositeDisposable();
    }

    public void initCartDataSource(Context context){
        cartDataSource = new LocalCartDataSource(CartDataBase.getInstance(context).cartDAO());
    }

    public void onStop(){
        compositeDisposable.clear();
    }

    public MutableLiveData<List<CartItem>> getListMutableLiveData() {
        if(listMutableLiveData == null){
            listMutableLiveData = new MutableLiveData<>();
        }
        getAllCartItem();
        return listMutableLiveData;
    }

    private void getAllCartItem() {
        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(cartItems -> {listMutableLiveData.setValue(cartItems);},throwable -> {
            listMutableLiveData.setValue(null);
        }));
    }
}
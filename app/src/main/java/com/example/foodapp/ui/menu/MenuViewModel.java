package com.example.foodapp.ui.menu;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Callbacks.MenuCallbackListener;
import Common.Common;
import Models.CategoryModel;

public class MenuViewModel extends ViewModel implements MenuCallbackListener {

    private MutableLiveData<List<CategoryModel>> menuListMultable;
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private MenuCallbackListener menuCallbackListener;

    public MenuViewModel() {
        menuCallbackListener = this;
    }

    public MutableLiveData<List<CategoryModel>> getMenuListMultable() {
       if(menuListMultable == null){
           menuListMultable = new MutableLiveData<>();
           messageError = new MutableLiveData<>();
           loadMenu();
       }
       return menuListMultable;
    }

    private void loadMenu() {
        List<CategoryModel> tempList = new ArrayList<>();
        DatabaseReference menuRef = FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF);
        menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot itemSnapshot: dataSnapshot.getChildren()){
                    CategoryModel categoryModel = itemSnapshot.getValue(CategoryModel.class);
                    categoryModel.setMenu_id(itemSnapshot.getKey());
                    tempList.add(categoryModel);
                }
                menuCallbackListener.onMenuLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                menuCallbackListener.onMenuLoadFailed(databaseError.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onMenuLoadSuccess(List<CategoryModel> categoryModels) {
        menuListMultable.setValue(categoryModels);
    }

    @Override
    public void onMenuLoadFailed(String message) {
        messageError.setValue(message);
    }
}
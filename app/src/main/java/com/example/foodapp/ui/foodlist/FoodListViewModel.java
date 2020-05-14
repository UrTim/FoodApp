package com.example.foodapp.ui.foodlist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import Common.Common;
import Models.FoodModel;

public class FoodListViewModel extends ViewModel {

    private MutableLiveData<List<FoodModel>> foodListViewModelMutableLiveData;

    public FoodListViewModel() {

    }

    public MutableLiveData<List<FoodModel>> getFoodListViewModelMutableLiveData() {
            if(foodListViewModelMutableLiveData == null){
                foodListViewModelMutableLiveData = new MutableLiveData<>();
                foodListViewModelMutableLiveData.setValue(Common.categorySelected.getFoods());
            }
        return foodListViewModelMutableLiveData;
    }
}
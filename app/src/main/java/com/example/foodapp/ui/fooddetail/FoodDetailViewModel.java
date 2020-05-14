package com.example.foodapp.ui.fooddetail;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import Common.Common;
import Models.FoodModel;

public class FoodDetailViewModel extends ViewModel {

    private MutableLiveData<FoodModel> foodModelMutableLiveData;


    public FoodDetailViewModel() {

    }

    public MutableLiveData<FoodModel> getFoodModelMutableLiveData() {
        if(foodModelMutableLiveData == null){
            foodModelMutableLiveData = new MutableLiveData<>();
            foodModelMutableLiveData.setValue(Common.selectedFood);
        }
        return foodModelMutableLiveData;
    }
}
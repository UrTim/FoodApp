package com.example.foodapp.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("" +
                "Информаия о приложении"+"\n\n"+
                "Для продолжения свайпните вправо");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
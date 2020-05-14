package Callbacks;

import java.util.List;

import Models.CategoryModel;

public interface MenuCallbackListener {
    void onMenuLoadSuccess(List<CategoryModel> categoryModels);
    void onMenuLoadFailed(String message);
}

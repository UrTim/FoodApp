package Common;

import Models.CategoryModel;
import Models.FoodModel;
import Models.UserModel;

public class Common {
    public static final String USER_REF = "Users";
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static final String CATEGORY_REF = "Category";
    public static UserModel currentUser;
    public static CategoryModel categorySelected;
    public static FoodModel selectedFood;
}

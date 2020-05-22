package Database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(version = 1,entities = CartItem.class,exportSchema = false)
public abstract class CartDataBase extends RoomDatabase{
    public abstract CartDAO cartDAO();
    private static CartDataBase instance;

    public static CartDataBase getInstance(Context context) {
        if(instance == null){
            instance = Room.databaseBuilder(context,CartDataBase.class,"FoodAppDB2").build();
        }
        return instance;
    }
}

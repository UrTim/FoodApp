package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodapp.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import EventBus.CategoryClick;
import Callbacks.RecyclerClickListener;
import Common.Common;
import Models.CategoryModel;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MenuAdapter  extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

    Context context;
    List<CategoryModel> categoryModelList;

    public MenuAdapter(Context context, List<CategoryModel> categoryModelList) {
        this.context = context;
        this.categoryModelList = categoryModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context)
        .inflate(R.layout.layout_menu_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(categoryModelList.get(position).getImage()).into(holder.menu_image);
        holder.menu_name.setText(new StringBuilder(categoryModelList.get(position).getName()));

        //Event
        holder.setListener(new RecyclerClickListener() {
            @Override
            public void onItemClickListener(View view, int position) {
                Common.categorySelected = categoryModelList.get(position);
                EventBus.getDefault().postSticky(new CategoryClick(true,categoryModelList.get(position)));
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Unbinder unbinder;
        @BindView(R.id.menu_image)
        ImageView menu_image;
        @BindView(R.id.menu_name)
        TextView menu_name;
        RecyclerClickListener listener;

        public void setListener(RecyclerClickListener listener) {
            this.listener = listener;
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v,getAdapterPosition());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(categoryModelList.size() == 1){
            return Common.DEFAULT_COLUMN_COUNT;
        }else{
            if(categoryModelList.size() % 2 ==0){
                return Common.DEFAULT_COLUMN_COUNT;
            }else{
                return (position > 1 && position == categoryModelList.size() - 1) ? Common.FULL_WIDTH_COLUMN:Common.DEFAULT_COLUMN_COUNT;
            }
        }
    }
}

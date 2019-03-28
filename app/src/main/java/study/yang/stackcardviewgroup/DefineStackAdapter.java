package study.yang.stackcardviewgroup;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

/**
 * Created by CJJ on 2017/3/7.
 */

public class DefineStackAdapter extends RecyclerView.Adapter<DefineStackAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<String> datas;
    private Context context;


    public DefineStackAdapter(Context context, List<String> datas) {
        this.context = context;
        this.datas = datas;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (inflater == null) {
            context = parent.getContext();
            inflater = LayoutInflater.from(parent.getContext());
        }
        /**
         * 用此方法创建控件，用root的LayoutParams来约束子布局
         */
        View inflate = inflater.inflate(R.layout.superposition_item_card, parent, false);
//        View inflate =View.inflate(context,R.layout.superposition_item_card,null);
        return new ViewHolder(inflate);

    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (position % 3) {
            case 0:
                holder.itemView.setBackgroundResource(R.drawable.superposition_item_shape_1);
                break;
            case 1:
                holder.itemView.setBackgroundResource(R.drawable.superposition_item_shape_2);
                break;
            case 2:
                holder.itemView.setBackgroundResource(R.drawable.superposition_item_shape_3);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context.getApplicationContext(), String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }
    }
}

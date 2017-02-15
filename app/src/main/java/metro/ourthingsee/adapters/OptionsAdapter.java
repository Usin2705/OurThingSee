package metro.ourthingsee.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import metro.ourthingsee.R;

/**
 * Created by giang on 2/14/17.
 */

public class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.MyOptionViewholder> {
    Context context;
    List<Option> options = new ArrayList<Option>();
    public OptionsAdapter(Context context) {
        this.context = context;
        options.add(new Option(R.drawable.pin,context.getString(R.string.location_option)));
        options.add(new Option(R.drawable.temperature,context.getString(R.string.temperature_option)));
        options.add(new Option(R.drawable.nature,context.getString(R.string.humidity_option)));
    }

    @Override
    public MyOptionViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_option_item, parent, false);
        return new OptionsAdapter.MyOptionViewholder(itemView);
    }

    @Override
    public void onBindViewHolder(MyOptionViewholder holder, int position) {
        Option option = options.get(position);
        Glide.with(context)
                .load(option.getIcon())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .centerCrop()
                .animate(android.R.anim.fade_in)
                .approximate()
                .into(holder.imgv_icon);
        holder.tv_option.setText(option.getOptionTitle());
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    public class MyOptionViewholder extends RecyclerView.ViewHolder {
        ImageView imgv_icon;
        TextView tv_option;
        public MyOptionViewholder(View itemView) {
            super(itemView);
            imgv_icon = (ImageView) itemView.findViewById(R.id.imgv_icon);
            tv_option = (TextView) itemView.findViewById(R.id.tv_option);
        }
    }
    public class Option{
        private int icon;
        private String optionTitle;

        public Option() {
        }

        public Option(int icon, String optionTitle) {
            this.icon = icon;
            this.optionTitle = optionTitle;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public String getOptionTitle() {
            return optionTitle;
        }

        public void setOptionTitle(String optionTitle) {
            this.optionTitle = optionTitle;
        }
    }
}

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

    /**
    * An on-click handler that we've defined to make it easy for an Activity to interface with
    * our RecyclerView
    */
    final private PurposeItemClickListener mOnClickListener;

    Context context;
    List<Option> options = new ArrayList<>();

    /**
     * Display the options layout
     *
     * @param listener Listener for purpose item clicks
     */
    public OptionsAdapter(Context context, PurposeItemClickListener listener) {
        this.context = context;
        mOnClickListener = listener;
        options.add(new Option(R.drawable.pin, context.getString(R.string.location_option)));
        options.add(new Option(R.drawable.temperature, context.getString(R.string.temperature_option)));
        options.add(new Option(R.drawable.nature, context.getString(R.string.humidity_option)));
        options.add(new Option(R.drawable.ic_face_24dp, context.getString(R.string.myhome_option)));
    }

    @Override
    public MyOptionViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_option_item, parent, false);

        return new MyOptionViewholder(itemView);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the correct
     * indices in the list for this particular position, using the "position" argument that is
     * conveniently passed into us.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
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

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        return options.size();
    }

    /**
     * The interface that receives onClick messages.
     * Implements this interface to handle OnItemClick events
     */
    public interface PurposeItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    /**
     * Cache of the children views for a list item.
     */
    public class MyOptionViewholder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imgv_icon;
        TextView tv_option;

        /**
         * Constructor for our ViewHolder. Within this constructor, we get a reference to our
         * TextViews and set an onClickListener to listen for clicks. Those will be handled in the
         * onClick method below.
         *
         * @param itemView The View that you inflated in
         *                 {@link OptionsAdapter#onCreateViewHolder(ViewGroup, int)}
         */
        public MyOptionViewholder(View itemView) {
            super(itemView);
            imgv_icon = (ImageView) itemView.findViewById(R.id.imgv_icon);
            tv_option = (TextView) itemView.findViewById(R.id.tv_option);

            //Register the onClickListener for the item (in our app it's our Option/Purpose)
            itemView.setOnClickListener(this);
        }

        /**
         * Called whenever a user clicks on an item in the list.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }
    }

    public class Option {
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

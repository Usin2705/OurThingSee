package metro.ourthingsee;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import metro.ourthingsee.data.TCContract.TCEntry;

/**
 * Created by Nhan Phan on 08-Feb-17.
 */

public class TCCursorAdapter extends CursorAdapter {
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = TCCursorAdapter.class.getSimpleName();

    /**
     * Constructs a new {@link TCCursorAdapter}.
     *
     * @param context The context
     * @param cursor  The cursor from which to get the data.
     */
    public TCCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View rowView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        ViewHolder holder = new ViewHolder();
        // Find individual views that we want to modify in the list item layout
        holder.txtTimestamp = (TextView) rowView.findViewById(R.id.txtTimestamp);
        holder.txtSensorID = (TextView) rowView.findViewById(R.id.txtSensorID);
        holder.txtSensorValue = (TextView) rowView.findViewById(R.id.txtSensorValue);
        rowView.setTag(holder);

        // Inflate a list item view using the layout specified in list_item.xml
        return rowView;
    }

    /**
     * This method binds the inventory data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current inventory can be set on the name
     * TextView in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();

        // Find the columns of product attributes that we're interested in
        int timestampColumnIndex = cursor.getColumnIndex(TCEntry.COLUMN_TC_TIMESTAMP);
        int sensorIDColumnIndex = cursor.getColumnIndex(TCEntry.COLUMN_TC_SENSOR_ID);
        int sensorValueColumnIndex = cursor.getColumnIndex(TCEntry.COLUMN_TC_SENSOR_VALUE);

        // Read the inventory attributes from the Cursor for the current inventory
        int intTimestamp = cursor.getInt(timestampColumnIndex);
        String strSensorID = cursor.getString(sensorIDColumnIndex);
        float fltSensorValue = cursor.getFloat(sensorValueColumnIndex);

        holder.position = cursor.getPosition();

        // Why not using holder.position? Why use rowID?
        // There're one only problem. Since the database delete method only delete the rows
        // and not the database. And with our ID AUTO INCREMENT NOT NULL.
        // Change is we end up with hundred of empty row with their own ID.
        // So when a cursor with position = 2 (the second cursor) want to update,
        // there're already an empty row with _ID = 2, and so the update method will failed.
        // So we best to use the _ID from the database, a lot more precise.
        int rowID = cursor.getInt(cursor.getColumnIndex(TCEntry._ID));

        // Update the TextViews with the timestamp for the current data
        holder.txtTimestamp.setText(String.valueOf(intTimestamp));

        // Update the TextViews with the sensorID for the current data
        holder.txtSensorID.setText(strSensorID);

        // Update the TextViews with the sensor value for the current data
        holder.txtSensorValue.setText(String.valueOf(fltSensorValue));

    }

    static class ViewHolder {
        TextView txtTimestamp, txtSensorID, txtSensorValue;
        int position;
    }

}

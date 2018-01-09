package bluetoothLib;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;


public class ListViewAdapter<T> extends BaseAdapter {

    private static final String TAG = ListViewAdapter.class.getName();
    private  List<T> dataList;
    private int mFieldId = 0;
    private Context mContext;

    private LayoutInflater layoutInflater;

    private final int mResource;

    public ListViewAdapter(Context context, int resource, List<T> data) {
        layoutInflater = LayoutInflater.from(context);
        dataList = data;
        mResource = resource;
        mFieldId = 0;
        mContext = context;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public T getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getView: ");
        final View view;
        final TextView text;
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            view = layoutInflater.inflate(mResource, parent, false);
        } else {
            view = convertView;
        }
        try {
            if (mFieldId == 0) {
                //  If no custom field is assigned, assume the whole resource is a TextView
                text = (TextView) view;
            } else {
                //  Otherwise, find the TextView field within the layout
                text = view.findViewById(mFieldId);

                if (text == null) {
                    throw new RuntimeException("Failed to find view with ID "
                            + mContext.getResources().getResourceName(mFieldId)
                            + " in item layout");
                }
            }
        } catch (ClassCastException e) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }

        final T item = getItem(position);
        if (item instanceof CharSequence) {
            text.setText((CharSequence) item);
        } else {
            text.setText(item.toString());
        }
        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        Log.d(TAG, "notifyDataSetChanged: ");
        super.notifyDataSetChanged();
        
    }
}

package fr.bouyguestelecom.tv.bboxiotsample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.bmartel.android.dotti.R;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.BluetoothSmartDevice;

/**
 * @author Bertrand Martel
 */
public class StableArrayAdapter extends ArrayAdapter<BluetoothSmartDevice> {

    List<BluetoothSmartDevice> btDeviceList = new ArrayList<>();

    private static LayoutInflater inflater = null;

    public StableArrayAdapter(Context context, int textViewResourceId,
                              List<BluetoothSmartDevice> objects) {
        super(context, textViewResourceId, objects);

        this.btDeviceList = objects;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.listview_item, null);
                holder = new ViewHolder();

                holder.deviceUid = (TextView) vi.findViewById(R.id.text1);
                holder.manufacturerName = (TextView) vi.findViewById(R.id.text2);
                holder.productName = (TextView) vi.findViewById(R.id.text3);

                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            holder.deviceUid.setText(btDeviceList.get(position).getDeviceUuid());
            holder.manufacturerName.setText(btDeviceList.get(position).getGenericDevice().getManufacturerName());
            holder.productName.setText(btDeviceList.get(position).getGenericDevice().getProductName());

        } catch (Exception e) {


        }
        return vi;
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    public int getCount() {
        return btDeviceList.size();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public static class ViewHolder {
        public TextView deviceUid;
        public TextView manufacturerName;
        public TextView productName;
    }

}
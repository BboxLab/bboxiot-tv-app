package fr.bouyguestelecom.tv.bboxiot.tvapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.connection.BtAssociatedDevice;

/**
 * @author Bertrand Martel
 */
public class ConnectionItemArrayAdapter extends ArrayAdapter<BtAssociatedDevice> {

    List<BtAssociatedDevice> btDeviceList = new ArrayList<>();

    private static LayoutInflater inflater = null;

    public ConnectionItemArrayAdapter(Context context, int textViewResourceId,
                                      List<BtAssociatedDevice> objects) {
        super(context, textViewResourceId, objects);

        this.btDeviceList = objects;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.listview_connection_item, null);
                holder = new ViewHolder();

                holder.deviceUid = (TextView) vi.findViewById(R.id.text1);
                holder.supportedDevice = (TextView) vi.findViewById(R.id.text2);
                holder.connectionStatus = (ImageView) vi.findViewById(R.id.connection_state);

                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            holder.deviceUid.setText(btDeviceList.get(position).getDeviceUuid());
            holder.supportedDevice.setText(btDeviceList.get(position).getBtSmartDevice().getGenericDevice().getSupportedDevice().toString());

            if (btDeviceList.get(position).isConnected()) {
                holder.connectionStatus.setImageResource(R.drawable.green_circle);
            } else {
                holder.connectionStatus.setImageResource(R.drawable.red_circle);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return vi;
    }

    public List<BtAssociatedDevice> getConnectionList() {
        return btDeviceList;
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
        public TextView supportedDevice;
        public ImageView connectionStatus;
    }

}
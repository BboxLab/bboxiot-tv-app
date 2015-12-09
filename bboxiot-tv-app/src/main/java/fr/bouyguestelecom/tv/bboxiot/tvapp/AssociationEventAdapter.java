package fr.bouyguestelecom.tv.bboxiot.tvapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bertrand Martel
 */
public class AssociationEventAdapter extends ArrayAdapter<AssociationEventObj> {

    List<AssociationEventObj> events = new ArrayList<>();

    private static LayoutInflater inflater = null;

    public AssociationEventAdapter(Context context, int textViewResourceId,
                                   List<AssociationEventObj> objects) {
        super(context, textViewResourceId, objects);

        this.events = objects;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.connection_event_item, null);
                holder = new ViewHolder();

                holder.date = (TextView) vi.findViewById(R.id.text1);
                holder.deviceUid = (TextView) vi.findViewById(R.id.text2);
                holder.eventStr = (TextView) vi.findViewById(R.id.text3);

                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            DateFormat df = new SimpleDateFormat("HH:mm:ss");
            holder.date.setText(df.format(events.get(position).getDate()).toString());
            holder.deviceUid.setText(events.get(position).getDeviceUid());
            holder.eventStr.setText(events.get(position).getEventStr());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return vi;
    }

    public List<AssociationEventObj> getDeviceList() {
        return events;
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    public int getCount() {
        return events.size();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public static class ViewHolder {
        public TextView date;
        public TextView deviceUid;
        public TextView eventStr;
    }
}

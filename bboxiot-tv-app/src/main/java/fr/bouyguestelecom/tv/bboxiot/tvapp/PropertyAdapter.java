package fr.bouyguestelecom.tv.bboxiot.tvapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.bouyguestelecom.tv.bboxiot.datamodel.SmartProperty;

/**
 * @author Bertrand Martel
 */
public class PropertyAdapter extends ArrayAdapter<SmartProperty> {

    List<SmartProperty> propertyList = new ArrayList<>();

    private static LayoutInflater inflater = null;

    public PropertyAdapter(Context context, int textViewResourceId,
                           List<SmartProperty> objects) {
        super(context, textViewResourceId, objects);

        this.propertyList = objects;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.listview_property_item, null);
                holder = new ViewHolder();

                holder.function = (TextView) vi.findViewById(R.id.text1);
                holder.property = (TextView) vi.findViewById(R.id.text2);
                holder.value = (TextView) vi.findViewById(R.id.text3);

                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            holder.function.setText(propertyList.get(position).getFunction().toString());
            holder.property.setText(propertyList.get(position).getProperty().toString());
            holder.value.setText("" + propertyList.get(position).getValue());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return vi;
    }

    public List<SmartProperty> getPropertyList() {
        return propertyList;
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    public int getCount() {
        return propertyList.size();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public static class ViewHolder {
        public TextView function;
        public TextView property;
        public TextView value;
    }
}

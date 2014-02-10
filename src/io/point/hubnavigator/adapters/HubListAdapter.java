package io.point.hubnavigator.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import io.point.hubnavigator.core.*;

public class HubListAdapter extends ArrayAdapter<Hub> {
	
	private int layoutResourceId;
	private Context context;
	Hub data[] = null;
	
	public HubListAdapter(Context context, int layoutResourceId, Hub[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ListItemHolder holder = null;
        
        /*if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new ListItemHolder();
            holder.imgIcon = null;
            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
            
            row.setTag(holder);
        }
        else
        {
            holder = (WeatherHolder)row.getTag();
        }*/
                
        Hub hub = data[position];
        //holder.txtTitle.setText(weather.title);
        //holder.imgIcon.setImageResource(weather.icon);
        
        return row;
    }

    static class ListItemHolder
    {
        ImageView imgIcon;
        TextView txtTitle;
    }

}

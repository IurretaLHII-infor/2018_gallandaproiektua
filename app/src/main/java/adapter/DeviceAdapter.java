package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sasiroot.agpr.R;

import java.util.ArrayList;

import classes.Device;

/**
 * Created by sasiroot on 3/2/18.
 */

public class DeviceAdapter extends ArrayAdapter<Device> {

    private ImageView deviceImg;
    private TextView ipText;
    private TextView nameText;

    public DeviceAdapter(Context contexto, ArrayList<Device> array) {
        super(contexto, 0, array);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        convertView = inflater.inflate(R.layout.device_name, parent, false);

        Device current = this.getItem(position);

        deviceImg = (ImageView) convertView.findViewById(R.id.typeImg);
        ipText = (TextView) convertView.findViewById(R.id.ipText);
        nameText = (TextView) convertView.findViewById(R.id.nameText);

        String type = current.getType();

        if (type.compareToIgnoreCase("AUDIO_VIDEO")==0) {
            deviceImg.setImageResource(R.drawable.audio_video);
        } else if (type.compareToIgnoreCase("COMPUTER")==0) {
            deviceImg.setImageResource(R.drawable.computerimg);
        } else if (type.compareToIgnoreCase("HEALTH")==0) {
            deviceImg.setImageResource(R.drawable.healthimg);
        } else if (type.compareToIgnoreCase("IMAGING")==0) {
            deviceImg.setImageResource(R.drawable.imaginimg);
        } else if (type.compareToIgnoreCase("MISC")==0) {
            deviceImg.setImageResource(R.drawable.miscimg);
        } else if (type.compareToIgnoreCase("NETWORKING")==0) {
            deviceImg.setImageResource(R.drawable.networkimg);
        } else if (type.compareToIgnoreCase("PERIPHERAL")==0) {
            deviceImg.setImageResource(R.drawable.peripheralimg);
        }  else if (type.compareToIgnoreCase("PHONE")==0) {
            deviceImg.setImageResource(R.drawable.phoneimg);
        } else if (type.compareToIgnoreCase("TOY")==0) {
            deviceImg.setImageResource(R.drawable.toyimg);
        } else if (type.compareToIgnoreCase("UNCATEGORIZED")==0) {
            deviceImg.setImageResource(R.drawable.uncategorizedimg);
        } else {
            deviceImg.setImageResource(R.drawable.other);
        }

        ipText.setText(current.getIp());
        nameText.setText(current.getName());

        return convertView;
    }
}

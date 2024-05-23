package app.webservices;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WeatherArrayAdapter extends ArrayAdapter<Weather>{

    private Map<String, Bitmap> bitmaps = new HashMap<>();

    public WeatherArrayAdapter(Context context, List<Weather> forecast) {
        super(context, -1, forecast);
    }
    private static class ViewHolder {
        ImageView conditionImageView;
        TextView dayTextView;
        TextView lowTextView;
        TextView hiTextView;
        TextView humidityTextView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Weather day = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.items, parent, false);
            viewHolder.conditionImageView = convertView.findViewById(R.id.conditionImageView);
            viewHolder.dayTextView = convertView.findViewById(R.id.dayTextView);
            viewHolder.lowTextView = convertView.findViewById(R.id.lowTextView);
            viewHolder.hiTextView = convertView.findViewById(R.id.hiTextView);
            viewHolder.humidityTextView = convertView.findViewById(R.id.humidityTextView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (bitmaps.containsKey(day.iconURL)) {
            viewHolder.conditionImageView.setImageBitmap(bitmaps.get(day.iconURL));
        } else {
            new LoadImageTask(viewHolder.conditionImageView).execute(day.iconURL);
        }

        viewHolder.dayTextView.setText(day.dayOfWeek);
        viewHolder.lowTextView.setText(getContext().getString(R.string.low_temp, day.minTemp));
        viewHolder.hiTextView.setText(getContext().getString(R.string.high_temp, day.maxTemp));
        viewHolder.humidityTextView.setText(getContext().getString(R.string.humidity, day.humidity));

        return convertView;
    }

    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;
        private String url;

        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            Bitmap bitmap = null;
            HttpURLConnection connection = null;

            try {
                System.out.println("Intentando descargar la imagen desde: " + params[0]);
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    bitmaps.put(params[0], bitmap);
                    System.out.println("Imagen descargada exitosamente desde: " + params[0]);
                } else {
                    System.out.println("Error al descargar la imagen. Código de estado HTTP: " + responseCode + " URL: " + params[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Excepción descargando la imagen desde: " + params[0] + ", Mensaje de error: " + e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                // Manejo de errores: colocar una imagen por defecto o mensaje de error
                imageView.setImageResource(R.drawable.ic_launcher_background); // Usa un recurso drawable de error
                System.out.println("Error al cargar la imagen desde: " + url);
            }
        }
    }
}
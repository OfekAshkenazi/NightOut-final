package GooglePlacesClasses;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import Entities.MyPlace;
import Entries.AppEntries;

/**
 * Created by Ofek on 01-Nov-17.
 */

public class PlacesDAL implements getNearbyPlacesData.OnPlacesIdsLoaded {
    OnGooglePlacesLoaded event;
    ArrayList<MyPlace> places=new ArrayList<>();
    public void setNearbyPlaces(Double currentLat, Double currentLng, HashMap<String, String> parameters,OnGooglePlacesLoaded event) {
        this.event=event;
        getNearbyPlacesData request = new getNearbyPlacesData();
        Object[] params = {parameters, currentLat, currentLng,this};
        request.execute(params);
    }

    @Override
    public void onPlacesIdLoaded(ArrayList<String> placesIds) {
        getPlaceDetails request=new getPlaceDetails();
        Object[] params={placesIds};
        request.execute(params);
    }

    class getPlaceDetails extends AsyncTask<Object, String, ArrayList<MyPlace>> {
        private String url;
        ArrayList<String> placesIds;
        private String data;

        @Override
        protected ArrayList<MyPlace> doInBackground(Object... params) {
            placesIds= (ArrayList<String>) params[0];
            for (String currentId:placesIds) {
                url = requestStringBuilder(currentId);
                DownloadUrl downloadUrl = new DownloadUrl();
                try {
                    data = downloadUrl.readUrl(url);
                    Log.d("places data: ",data);
                    DataParser parser=new DataParser();
                    MyPlace place=parser.parse(data);
                    places.add(place);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return places;
        }

        private String requestStringBuilder(String currentId) {
            StringBuilder builder=new StringBuilder();
            builder.append("https://maps.googleapis.com/maps/api/place/details/json?placeid=");
            builder.append(currentId);
            builder.append("&key=");
            builder.append(AppEntries.GOOGLE_PLACES_KEY);
            return builder.toString();
        }

        @Override
        protected void onPostExecute(ArrayList<MyPlace> placesIds) {
            event.onGooglePlacesLoaded(placesIds);
        }

        private class DataParser {
            public MyPlace parse(String jsonData) {
                JSONObject jsonObject=null;
                try {
                    jsonObject = new JSONObject(jsonData).getJSONObject("result");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return new MyPlace(jsonObject);
            }
        }
    }
}
class getNearbyPlacesData extends AsyncTask<Object,String,String> {
    String data;
    String url;
    ArrayList<String> nearbyPlacesIds;
    OnPlacesIdsLoaded task;

    @Override
    protected String doInBackground(Object... params) {
        HashMap<String, String> parameters = (HashMap<String, String>) params[0];
        Double currentLat = (double) params[1];
        Double currentLng = (double) params[2];
        task = (OnPlacesIdsLoaded) params[3];
        url = requestStringBuilder(currentLat, currentLng, parameters);
        DownloadUrl downloadUrl = new DownloadUrl();
        try {
            data = downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        DataParser parser = new DataParser();
        nearbyPlacesIds = parser.parse(s);
        Log.i("ids Array",nearbyPlacesIds.toString());
        task.onPlacesIdLoaded(nearbyPlacesIds);
    }


    public String requestStringBuilder(double currentLat, double currentLng, HashMap<String, String> params) {
        StringBuilder builder = new StringBuilder();
        builder.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        builder.append("location=");
        builder.append(currentLat);
        builder.append(',');
        builder.append(currentLng);
        builder.append("&");
        Iterator it = params.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,String> pair = (Map.Entry<String,String>) it.next();
            builder.append(pair.getKey());
            builder.append('=');
            builder.append(pair.getValue());
            builder.append("&");
        }
        builder.append("key=");
        builder.append(AppEntries.GOOGLE_PLACES_KEY);
        return builder.toString();
    }



    interface OnPlacesIdsLoaded {
        void onPlacesIdLoaded(ArrayList<String> placesIds);
    }

    class DataParser {
        private String getPlace(JSONObject googlePlaceJ) {
            HashMap<String, Object> placesMap = new HashMap<>();
            String placeName = null;
            String vicinity = null;
            String id = null;
            String latitude;
            String longitude;
            String references;
            String[] types;

            try {
//            if (!googlePlaceJ.isNull("name")){
//                String name=googlePlaceJ.getString("name");
//            }
//
//            if (!googlePlaceJ.isNull("vicinity")){
//                vicinity=googlePlaceJ.getString("vicinity");
//            }
                id = (String) googlePlaceJ.get("place_id");
//            latitude=googlePlaceJ.getJSONObject("geometry")
//                    .getJSONObject("location").getString("lat");
//            longitude=googlePlaceJ.getJSONObject("geometry")
//                    .getJSONObject("location").getString("lng");
//            references=googlePlaceJ.getString("reference");
                types = getTypes(googlePlaceJ.getJSONArray("types"));
//            placesMap.put("placeName",placeName);
//            placesMap.put("id",id);
//            placesMap.put("vicinity",vicinity);
//            placesMap.put("latitude",latitude);
//            placesMap.put("longitude",longitude);
//            placesMap.put("types",types);
//            placesMap.put("references",references);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return id;
        }

        private String[] getTypes(JSONArray types) throws JSONException {
            String[] typesArr = new String[types.length()];
            for (int i = 0; i < types.length(); i++) {
                typesArr[i] = types.getString(i);
            }

            return typesArr;
        }

        private ArrayList<String> getPlaces(JSONArray jsonArray) {
            int count = jsonArray.length();
            ArrayList<String> pIds = new ArrayList<>();
            String placeId = null;

            for (int i = 0; i < count; i++) {
                try {
                    placeId = getPlace((JSONObject) jsonArray.get(i));
                    if (placeId != null)
                        pIds.add(placeId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return pIds;
        }

        public ArrayList<String> parse(String jsonData) {
            JSONArray jsonArray = null;
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(jsonData);
                jsonArray = jsonObject.getJSONArray("results");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return getPlaces(jsonArray);
        }
    }
}

class DownloadUrl{
    public String readUrl(String urlString) throws IOException {
        String data = "";
        InputStream inputStream = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection= (HttpURLConnection) url.openConnection();
            connection.connect();

            inputStream=connection.getInputStream();
            BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer=new StringBuffer();
            String line="";
            while ((line=reader.readLine())!=null){
                stringBuffer.append(line);
            }
            data=stringBuffer.toString();
            reader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            inputStream.close();
            connection.disconnect();
        }
        return data;
    }
}
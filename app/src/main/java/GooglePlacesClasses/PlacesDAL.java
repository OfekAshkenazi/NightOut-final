package GooglePlacesClasses;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

/**
 * Created by Ofek on 01-Nov-17.
 */

public class PlacesDAL {

}
class getNearbyPlacesData extends AsyncTask<String,String,String>{
    String data;
    String url;

    @Override
    protected String doInBackground(String... params) {
        url= params[0];
        DownloadUrl downloadUrl=new DownloadUrl();
        try {
            data=downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        ArrayList<HashMap<String ,String>> nearbyPlaceList;
        DataParser parser=new DataParser();
        nearbyPlaceList=parser.parse(s);
        showNearbyPlaces(nearbyPlaceList);
    }
    private void showNearbyPlaces(ArrayList<HashMap<String ,String>> nearbyPlaceList){
        for (HashMap<String ,String> placeMap:nearbyPlaceList){
            MarkerOptions marker=new MarkerOptions();
            String placeName=placeMap.get("placename");
            String vicinity=placeMap.get("vicinity");
            String references=placeMap.get("references");
            double lat=Double.parseDouble(placeMap.get("lat"));
            double lng=Double.parseDouble(placeMap.get("lng"));
            LatLng latLng=new LatLng(lat,lng);
            marker.position(latLng);
            marker.title((placeName)+": "+vicinity);
            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
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
class DataParser{
    private HashMap<String,String> getPlace(JSONObject googlePlaceJ){
        HashMap<String,String> placesMap=new HashMap<>();
        String placeName = null;
        String vicinity = null;
        String latitude;
        String longitude;
        String references;

        try {
            if (!googlePlaceJ.isNull("name")){
                String name=googlePlaceJ.getString("name");
            }

            if (!googlePlaceJ.isNull("vicinity")){
                vicinity=googlePlaceJ.getString("vicinity");
            }

            latitude=googlePlaceJ.getJSONObject("geonetry")
                    .getJSONObject("location").getString("lat");
            longitude=googlePlaceJ.getJSONObject("geonetry")
                    .getJSONObject("location").getString("lng");
            references=googlePlaceJ.getString("reference");
            placesMap.put("placeName",placeName);
            placesMap.put("vicinity",vicinity);
            placesMap.put("latitude",latitude);
            placesMap.put("longitude",longitude);
            placesMap.put("references",references);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return placesMap;
    }

    private ArrayList<HashMap<String ,String>> getPlaces(JSONArray jsonArray){
        int count=jsonArray.length();
        ArrayList<HashMap<String ,String>> mList=new ArrayList<>();
        HashMap<String,String> placeMap=null;

        for (int i = 0; i < count; i++) {
            try {
                placeMap=getPlace((JSONObject) jsonArray.get(i));
                mList.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return mList;
    }

    public ArrayList<HashMap<String ,String>> parse(String jsonData){
        JSONArray jsonArray=null;
        JSONObject jsonObject;
        try {
            jsonObject=new JSONObject(jsonData);
            jsonArray=jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }
}
package Entities;

import android.net.Uri;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Ofek on 07-Nov-17.
 */
public class MyPlace implements Place{
    ArrayList<CharSequence> openingHours=new ArrayList<>();
    JSONObject result;
    ArrayList<String> types;

    public MyPlace(JSONObject result) {
        this.result=result;
        ArrayList<String> types = new ArrayList<String>();
        try {
            JSONArray typesArr=result.getJSONArray("types");
            for (int i=0;i<typesArr.length();i++){
                types.add(typesArr.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JSONArray hoursArr=result.getJSONObject("opening_hours").getJSONArray("weekday_text");
            for (int i=0;i<hoursArr.length();i++){
                openingHours.add(hoursArr.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<CharSequence> getOpeningHours() {
        return openingHours;
    }

    @Override
    public String getId() {
        try {
            String id= (String) result.get("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Integer> getPlaceTypes() {
        ArrayList<Integer> typesId=new ArrayList<>();
        for (String type:this.types){
         switch (type){
             case "bar" : typesId.add(Place.TYPE_BAR);
                 break;
             case "restaurant" : typesId.add(Place.TYPE_RESTAURANT);
                 break;
             case "night_club" : typesId.add(Place.TYPE_NIGHT_CLUB);
                 break;
             default:
                 break;
         }
        }
        return typesId;
    }

    public List<String> getPlaceTypesAsStrings() {
        return types;
    }

    @Override
    public CharSequence getAddress() {
        String address="";
        try {
            address= (String) result.get("vicinity");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return address;
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public CharSequence getName() {
        String name="";
        try {
            name= (String) result.get("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return name;
    }

    @Override
    public LatLng getLatLng() {
        LatLng latlng=null;
        try {
            JSONObject location=result.getJSONObject("geometry").getJSONObject("location");
            latlng=new LatLng(location.getDouble("lat"),location.getDouble("lng"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return latlng;
    }

    @Override
    public LatLngBounds getViewport() {
        return null;
    }

    @Override
    public Uri getWebsiteUri() {
        String link="";
        if (result.isNull("website")){
            return new Uri.Builder().path(link).build();
        }
        try {
            link= (String) result.get("website");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new Uri.Builder().path(link).build();
    }

    @Override
    public CharSequence getPhoneNumber() {
        String phone="";
        if (result.isNull("international_phone_number")){
            return phone;
        }
        try {
            phone= (String) result.get("formatted_phone_number");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return phone;
    }

    @Override
    public float getRating() {
        float rating=0;
        if (result.isNull("rating")){
            return rating;
        }
        try {
            rating= (float) result.get("rating");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rating;
    }

    @Override
    public int getPriceLevel() {
        int price=0;
        if (result.isNull("price_level")){
            return price;
        }
        try {
            price= (int) result.get("price_level");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public CharSequence getAttributions() {
        return null;
    }

    @Override
    public Place freeze() {
        return this;
    }

    @Override
    public boolean isDataValid() {
        return true;
    }
}

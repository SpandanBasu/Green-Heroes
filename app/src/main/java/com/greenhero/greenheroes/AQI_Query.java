package com.greenhero.greenheroes;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public final class AQI_Query {






    private static URL make_url(String urlString) throws MalformedURLException
    {
        URL url= null;

        try{
            url= new URL(urlString);
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }

        finally{
            return url;
        }
    }





    private AQI_Query()
    {}

    public static int getAqi(String JSON_QUERY_URL) throws IOException {
        int aqi=0;

        URL url = make_url(JSON_QUERY_URL);
        String JSON_TEXT = jsonReturnText(url);
        try{
            JSONObject baseJsonResponse = new JSONObject(JSON_TEXT);
            JSONObject dataObject = baseJsonResponse.getJSONObject("data");
            aqi = dataObject.getInt("aqi");



        }catch (JSONException e){
            Log.e("Tag","Problem Parsing the Query aqi data",e);
        }


        return aqi;
    }

    public static void getPollutants(String JSON_QUERY_URL) throws IOException {


        URL url = make_url(JSON_QUERY_URL);
        String JSON_TEXT = jsonReturnText(url);
        try{
            JSONObject baseJsonResponse = new JSONObject(JSON_TEXT);
            JSONObject dataObject = baseJsonResponse.getJSONObject("data");
            JSONObject pollutantObject = dataObject.getJSONObject("iaqi");

//            finding coData
            JSONObject coObject= pollutantObject.getJSONObject("co");
            ScanFragment.coData= coObject.getDouble("v");

//            finding no2Data
            JSONObject no2Object= pollutantObject.getJSONObject("no2");
            ScanFragment.no2Data= no2Object.getDouble("v");

//            finding so2Data
            JSONObject so2Object= pollutantObject.getJSONObject("so2");
            ScanFragment.so2Data= so2Object.getDouble("v");

//            finding o3Data
            JSONObject o3Object= pollutantObject.getJSONObject("o3");
            ScanFragment.o3Data= o3Object.getDouble("v");

//            finding pm10Data
            JSONObject pm10Object= pollutantObject.getJSONObject("pm10");
            ScanFragment.pm10Data= pm10Object.getDouble("v");

//            finding pm25Data
            JSONObject pm25Object= pollutantObject.getJSONObject("pm25");
            ScanFragment.pm25Data= pm25Object.getDouble("v");



        }catch (JSONException e){
            Log.e("Tag","Problem Parsing the Query aqi data",e);
        }

    }

    private static String jsonReturnText(URL url) throws IOException {
        String jsonText="";

        if(url==null)
        {
            return jsonText;
        }
        InputStream inputStream=null;
        HttpURLConnection urlConnection =null;
        try {
            urlConnection=(HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if(urlConnection.getResponseCode()== 200)
            {
                inputStream = urlConnection.getInputStream();
                jsonText = readFromStream(inputStream);

            }else
            {
                Log.e("LOG_TAG","Error Getting Response From Server "+ urlConnection.getResponseCode());
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        finally{
            if(urlConnection != null)
            {
                urlConnection.disconnect();
            }
            if(inputStream!=null)
            {
                inputStream.close();
            }

        }

        return jsonText;

    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output= new StringBuilder();
        String line="";

        try {
            InputStreamReader ISReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(ISReader);

            line = reader.readLine();
            while(line!=null)
            {
                output.append(line);
                line=reader.readLine();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

        return output.toString();
    }
}

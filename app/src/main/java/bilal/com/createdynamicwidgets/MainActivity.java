package bilal.com.createdynamicwidgets;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView button_sync_data;

    AsyncHttpClient asyncHttpClient;

    DBHelper dbHelper;

    private static final String URL = "http://storeperfect.colgate-palmolive.com.pk/offline/index.php/Api/get_survey";

    private static String images_link = "http://storeperfect.colgate-palmolive.com.pk/live/Api/visit_images/";

    ProgressDialog progressDialog;

    ListView listView;

    ArrayList<ServeyModel> arrayList;

    ArrayList<ServeyModel> array_list;

    String title = "";

    ServeyCustomAdapter serveyCustomAdapter;

    String imagePath;

    public static final int REQUEST_CODE_CAPTURE_IMAGE = 124;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQUEST_CODE_CAPTURE_IMAGE:
                if (resultCode == Activity.RESULT_OK){

                    Uri captureImage = CameraActivity.uri;
                    Bitmap thumbnail = CameraActivity.bitmapGlobal;

//                    Uri captureImage = data.getData();
//                    Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
//                    Bitmap thumbnail = null;
//                    try {
//                        thumbnail = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    imagePath = Util.saveImage(thumbnail);
//                    iv_attach.setImageBitmap(thumbnail);
                }
                break;
        }
    }

    private void initialize(){

        dbHelper = new DBHelper(MainActivity.this);

        asyncHttpClient = new AsyncHttpClient();

        progressDialog = new ProgressDialog(MainActivity.this);

        arrayList = dbHelper.getServeyQuestion();

        array_list = new ArrayList<>();

        for (int i = 0; i<arrayList.size();i++){

            if(! (arrayList.get(i).getServeyTitle().equals(title)) ){

                array_list.add(new ServeyModel(arrayList.get(i).getServeyTitle(),"title"));

                array_list.add(new ServeyModel(arrayList.get(i).getServeyTitle(),
                        arrayList.get(i).getQuestionTitle(),
                        arrayList.get(i).getServey_id(),
                        arrayList.get(i).getQuestion_id(),
                        arrayList.get(i).getQuestion_type(),
                        arrayList.get(i).getAnswer_type(),
                        arrayList.get(i).getCreated_at(),
                        arrayList.get(i).getOptions(),
                        arrayList.get(i).getImage(),
                        ""
                        ));

                title = arrayList.get(i).getServeyTitle();
            }else {

                array_list.add(new ServeyModel(arrayList.get(i).getServeyTitle(),
                        arrayList.get(i).getQuestionTitle(),
                        arrayList.get(i).getServey_id(),
                        arrayList.get(i).getQuestion_id(),
                        arrayList.get(i).getQuestion_type(),
                        arrayList.get(i).getAnswer_type(),
                        arrayList.get(i).getCreated_at(),
                        arrayList.get(i).getOptions(),
                        arrayList.get(i).getImage(),
                        ""
                ));

                title = arrayList.get(i).getServeyTitle();

            }

        }

        listView = (ListView) findViewById(R.id.list);

        serveyCustomAdapter = new ServeyCustomAdapter(MainActivity.this,array_list);

        listView.setAdapter(serveyCustomAdapter);

        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.setTitle("Fetching Data From Server");

        button_sync_data = (ImageView) findViewById(R.id.button_sync_data);

        button_sync_data.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.button_sync_data:

                syncServeyQuestionFromServer();

            break;


        }

    }

    private void syncServeyQuestionFromServer(){

        progressDialog.show();


        asyncHttpClient.setTimeout(7000);

        asyncHttpClient.get(MainActivity.this, URL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String response = new String(responseBody);

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    int error = jsonObject.getInt("error");

                    if(error == -1){

                        progressDialog.dismiss();

                        new SavingInLocalStorage().execute(jsonObject);

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                Log.d("serverFail", "onFailure: ");

                progressDialog.dismiss();

            }
        });
    }

    class SavingInLocalStorage extends AsyncTask<JSONObject,Void,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setTitle("Saving Data Please Wait...");

            progressDialog.show();
        }

        @Override
        protected String doInBackground(JSONObject... jsonObjects) {


            saving_data(jsonObjects[0]);

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressDialog.dismiss();
        }
    }


    private void saving_data(JSONObject jsonObject){

        try {

            dbHelper.emptyTable("survey");

            dbHelper.emptyTable("survey_question");

            JSONArray dataArray = jsonObject.getJSONArray("data");

            for(int i =0 ; i<dataArray.length() ; i++){

                JSONObject dataItem = (JSONObject) dataArray.get(i);

                JSONObject servey = dataItem.getJSONObject("survey");

                String id = servey.getString("id");

                String title = servey.getString("title");

                String publish_date = servey.getString("publish_date");

                String expiry_date = servey.getString("expiry_date");

                String created_at = servey.getString("created_at");

                String status = servey.getString("status");

                JSONArray question_array = servey.getJSONArray("questions");

                dbHelper.add_servey(id,title,publish_date,expiry_date,created_at,status);

                for(int j=0; j < question_array.length(); j++){

                    JSONObject question_items = (JSONObject) question_array.get(j);

                    JSONObject question_item = question_items.getJSONObject("question");

                    String question_server_id = question_item.getString("id");

                    String question_title = question_item.getString("title");

                    String question_type = question_item.getString("question_type");

                    String answer_type = question_item.getString("answer_type");

                    String question_image = question_item.getString("question_image") == "" ? "" : saveImage(getBitmapFromURL(images_link+question_item.getString("question_image")), images_link+question_item.getString("question_image"));

                    Log.d("imageLink", images_link+question_item.getString("question_image"));

                    JSONArray optionArray = question_item.getJSONArray("options");




                    dbHelper.add_servey_question(question_server_id,id,question_type,answer_type,question_image,optionArray.toString(),question_title);



                }

            }

            backUpDB();

        } catch (JSONException e) {
            Log.d("error", "saving_data: "+e);
        }

    }

    private void backUpDB(){
        try {
            BackupDatabase.BackupDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String saveImage(Bitmap finalBitmap, String imageName) {

        boolean success = false;
        String name = imageName.substring(imageName.lastIndexOf("/") + 1);
//        String[] splitImageName = imageName.split("/");
        File file = null;

        File myDir = new File(Environment.getExternalStorageDirectory() + "/CreateDynamic/pictures/");
        if (myDir.exists()) {
            success = true;
        } else {
            success = myDir.mkdirs();
        }

        if (success) {
//            String fname = splitImageName[splitImageName.length - 1];
//            file = new File(myDir, fname);
            file = new File(myDir, name);
            if (file.exists())
                file.delete();
            try {
                FileOutputStream out = new FileOutputStream(file);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();

                if (check(file)) {  // TODO BAKHTIYAR: there is check method call
                    return file.getPath();
                } else {

                    return "corrupted";

                }

            } catch (Exception e) {
                e.printStackTrace();

                if (check(file)) {   // TODO BAKHTIYAR: there is check method call in exception because if any file format is incorrect then it throw an exception

                    return file.getPath();

                } else {

                    return "corrupted";

                }
            }
        }

        return "corrupted";
    }

    public Bitmap getBitmapFromURL(String imageUrl) {
        try {
//            java.net.URL url = new URL(imageUrl);

            // TODO Bakhtiyar: I create this because some images are correct in server but Java think its corrupt

            int pos = imageUrl.lastIndexOf('/') + 1;

            URI uri = null;

            try {

                uri = new URI(imageUrl.substring(0, pos) + Uri.encode(imageUrl.substring(pos)));

                Log.d("uri", "onClick: " + uri);

            } catch (Exception e) {
                Log.d("exc", "onClick: " + e + uri);
            }

            if (uri == null) {


            }

            java.net.URL url = new URL(imageUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public boolean check(File file) {


//        try{

        Log.d("Path ", " check: " + file.length());

        byte[] array = new byte[(int) file.length()];

        Log.d("byte_array", "check: " + array.length);

        if (file.exists()) {

            if (file.length() < 1) {

                if (file.delete()) {

                    Log.d("", "check: ");

                    Log.d("DELETED", "Deleted " + file.getPath());
                }
                return false;

            } else {

                Log.d("NotDeleted", "Not delete ");

                return true;

            }


        }

        return false;

    }
}

package bilal.com.createdynamicwidgets;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;

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

    LinearLayout parent_layout;

    ArrayList<HashMap<String, Object>> arrayListForCollectData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        findViewById(R.id.temp).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//                startActivity(getIntent());
//            }
//        });

        arrayListForCollectData = new ArrayList<>();


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

//        listView = (ListView) findViewById(R.id.list);

//        serveyCustomAdapter = new ServeyCustomAdapter(MainActivity.this,array_list);

//        listView.setAdapter(serveyCustomAdapter);

        parent_layout = (LinearLayout) findViewById(R.id.parent);

        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.setTitle("Fetching Data From Server");

        button_sync_data = (ImageView) findViewById(R.id.button_sync_data);

        button_sync_data.setOnClickListener(this);

        functionCreateWidgets();
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


    private void functionCreateWidgets(){

//        HashMap<String,Object> hashMap = new HashMap<>();

        for(final ServeyModel serveyModel :array_list){



            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            LinearLayout.LayoutParams layoutParam_for_linear = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            layoutParam_for_linear.setMargins(8,8,8,8);

            LinearLayout layout_for_all = new LinearLayout(MainActivity.this);

            layout_for_all.setPadding(18,18,18,18);

            layout_for_all.setLayoutParams(layoutParam_for_linear);

            layout_for_all.setOrientation(LinearLayout.VERTICAL);


            CardView cardView = new CardView(MainActivity.this);

            CardView card_for_radio = new CardView(MainActivity.this);


            cardView.setLayoutParams(layoutParam_for_linear);

            card_for_radio.setLayoutParams(layoutParam_for_linear);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {


                cardView.setElevation(10);

                cardView.setCardBackgroundColor(getResources().getColor(R.color.colorWhite));

                card_for_radio.setElevation(10);

                card_for_radio.setCardBackgroundColor(getResources().getColor(R.color.colorWhite));
            }





            switch (serveyModel.getType()){



                case "title":

                    layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 80);

                    LinearLayout heading = new LinearLayout(MainActivity.this);

                    heading.setLayoutParams(layoutParams);

                    heading.setOrientation(LinearLayout.VERTICAL);

                    heading.setGravity(View.TEXT_ALIGNMENT_CENTER);

                    heading.setBackgroundColor(getResources().getColor(R.color.Green));

//                    heading.setBackgroundDrawable( getResources().getDrawable(R.drawable.background));

                    LinearLayout.LayoutParams layoutParams_text = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                    TextView textView = new TextView(MainActivity.this);

                    textView.setLayoutParams(layoutParams_text);

                    textView.setTextColor(getResources().getColor(R.color.colorWhite));

                    textView.setText(serveyModel.getServeyTitle());

                    textView.setGravity(17);

                    textView.setTypeface(null, Typeface.BOLD);

                    textView.setTextSize(25);

                    heading.addView(textView);

                    cardView.addView(heading);

                    parent_layout.addView(cardView);

                    break;

                case "":



                    ArrayList<RadioButton> radioButtons = new ArrayList<>();

                    ArrayList<CheckBox> checkBoxes = new ArrayList<>();

                    LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    imageLayoutParams.setMargins(4,4,4,4);

                    ImageView imageView = new ImageView(MainActivity.this);
                    imageView.setLayoutParams(imageLayoutParams);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                    imageView.setPadding(2,2,2,2);

                    layoutParams.setMargins(8,8,8,8);


                    RadioGroup.LayoutParams radioParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                    radioParams.setMargins(8,8,8,8);

                    RadioGroup radioGroup = new RadioGroup(MainActivity.this);
                    radioGroup.setOrientation(LinearLayout.VERTICAL);
                    radioGroup.setLayoutParams(radioParams);
                    radioGroup.setGravity(3); // for left

                    LinearLayout linearLayout;

                    LinearLayout linearLayoutChild = new LinearLayout(MainActivity.this);


                    linearLayoutChild.setLayoutParams(layoutParams);

                    linearLayoutChild.setOrientation(LinearLayout.VERTICAL);

                    linearLayoutChild.setGravity(3);

                    TextView question_title = new TextView(MainActivity.this);

                    question_title.setTextColor(getResources().getColor(R.color.colorBlack));

                    question_title.setTextSize(20);

                    question_title.setText(serveyModel.getQuestionTitle());



                    String question_type = serveyModel.getQuestion_type();

                    String answer_type = serveyModel.getAnswer_type();

//                capture = (ImageView) convertView.findViewById(R.id.capture);
//
//                capture.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//
//
//                        getContext().startActivity(new Intent(getContext(), CameraActivity.class));
//
//                    }
//                });

                    if(question_type.equals("Text")){

//                    linearLayout.addView(textView);

                        if(answer_type.equals("Radio Buttons")){

                            layout_for_all.addView(question_title);

                            layout_for_all.addView(radioGroup);
//                            parent_layout.addView(question_title);

//                            parent_layout.addView(radioGroup);
                            try {
//                            JSONObject options = new JSONObject(reportsPicturesModel.getOptions());



//                            Log.d("options", "getView: "+options.toString());

                                JSONArray option_array = new JSONArray(serveyModel.getOptions());

                                for (int radio_index=0; radio_index<option_array.length(); radio_index++){

                                    JSONObject val = (JSONObject) option_array.get(radio_index);

                                    LinearLayout parent_radio = new LinearLayout(MainActivity.this);

                                    parent_radio.setOrientation(LinearLayout.VERTICAL);


                                    RadioButton radioButton = new RadioButton(MainActivity.this);

                                    radioButton.setId(R.id.radio+radio_index);

                                    LinearLayout.LayoutParams layout_for_radio = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 50);

                                    layout_for_radio.setMargins(8,8,8,8);


//                                radioButton.setButtonDrawable(getContext().getResources().getDrawable(R.drawable.custom_radio_button));


                                    radioButton.setButtonDrawable(getResources().getDrawable(R.drawable.background_for_radio));

//                                radioButton.setButtonTintList(colorStateList);



                                    Log.d("radioValue", "getView: "+val.getString("choice"));

                                    radioButton.setText("  "+ val.getString("choice").toUpperCase());

                                    radioButton.setLayoutParams(radioParams);

                                    radioButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0);

                                    radioButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.background));

                                    radioButton.setPadding(8,8,8,8);

                                    radioButton.setTextColor(getResources().getColor(R.color.colorWhite));

                                    radioButton.setTextSize(15);

                                    radioButtons.add(radioButton);




//                                radioButton.setBu

//                                radioButton.setButtonTintList(colorStateList);




                                }


                                for (final RadioButton radioButton : radioButtons){


                                    final HashMap<String,Object> for_radio = new HashMap<>();

                                    radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {


                                            ServeyModel serveyModel1 = serveyModel;

//                                            for_radio.put("servey_key",serveyModel1.getServey_id());
//
//                                            for_radio.put("question_key",serveyModel1.getQuestionTitle());
//
//                                            for_radio.put("answer",radioButton.getText());
//
//                                            Log.d("message", "onCheckedChanged: "+String.valueOf(for_radio));



                                            if(compoundButton.isChecked()){

                                                for_radio.put("servey_key",serveyModel1.getServey_id());

                                                for_radio.put("question_key",serveyModel1.getQuestionTitle());

                                                for_radio.put("answer",radioButton.getText());

                                                Log.d("message", "onCheckedChanged: "+String.valueOf(for_radio));


                                                radioButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_after_check));



                                            }else {

//                                                for_radio.put("servey_key",serveyModel1.getQuestionTitle());
//
//                                                for_radio.put("question_key",serveyModel1.getServey_id());
//
//                                                for_radio.put("answer",radioButton.getText());
//
                                                radioButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.background));

//                                                Log.d("message", "onCheckedChanged: "+String.valueOf(for_radio));

                                            }

                                        }
                                    });

//                                    arrayListForCollectData.add(for_radio);

                                    radioGroup.addView(radioButton);

                                    arrayListForCollectData.add(for_radio);

                                }

                                card_for_radio.addView(layout_for_all);
//
                                parent_layout.addView(card_for_radio);



                            } catch (JSONException e) {
                                Log.d("error", "getView: "+e);
                            }

                        }else if(answer_type.equals("Text")){

                            final EditText editText = new EditText(MainActivity.this);

                            editText.setLayoutParams(layoutParams);

                            editText.setHint("Feedback Here: ");

                            editText.setHighlightColor(getResources().getColor(R.color.colorMain));



                            editText.setId(0);


                            editText.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                    Log.d("beforeText", "afterTextChanged: "+editText.getText().toString());

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                    Log.d("OnTextChange", "afterTextChanged: "+editText.getText().toString());

                                }

                                @Override
                                public void afterTextChanged(Editable s) {

                                    Log.d("afterTextChange", "afterTextChanged: "+editText.getText().toString());

                                }
                            });


//                            editText.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    ServeyModel s =  serveyModel;
//
//                                    Toast.makeText(MainActivity.this, ""+s.getQuestionTitle(), Toast.LENGTH_SHORT).show();
//                                }
//                            });

                            layout_for_all.addView(question_title);

                            layout_for_all.addView(editText);

                            cardView.addView(layout_for_all);

                            parent_layout.addView(cardView);




                        }
                        else if(serveyModel.getAnswer_type().equals("Check Boxes")){

                            layout_for_all.addView(question_title);


                            try {

                                JSONArray jsonArray = new JSONArray(serveyModel.getOptions());

                                for (int check_index=0; check_index< jsonArray.length(); check_index++){

                                    JSONObject jsonObject = (JSONObject) jsonArray.get(check_index);

                                    CheckBox checkBox = new CheckBox(MainActivity.this);

                                    LinearLayout.LayoutParams for_check = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 50);

                                    for_check.setMargins(8,8,8,8);

                                    checkBox.setLayoutParams(for_check);

                                    checkBox.setTextColor(getResources().getColor(R.color.colorWhite));
//                                checkBox.setButtonTintList(colorStateList);

                                    checkBox.setButtonDrawable(getResources().getDrawable(R.drawable.background_for_radio));

                                    checkBox.setBackgroundDrawable(getResources().getDrawable(R.drawable.background));

//                                    checkBox.setButtonDrawable(getResources().getDrawable(R.drawable.colorMain));

                                    checkBox.setId(check_index);

                                    checkBoxes.add(checkBox);

                                    checkBox.setText("  "+jsonObject.getString("choice").toUpperCase());
                                }

                                for (final CheckBox checkBox: checkBoxes) {

                                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                                            if(b){

                                                checkBox.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_after_check));

                                            }else {

                                                checkBox.setBackgroundDrawable(getResources().getDrawable(R.drawable.background));

                                            }

                                        }
                                    });


                                    layout_for_all.addView(checkBox);
                                }





                                cardView.addView(layout_for_all);

//                                cardView.addView(layout_for_all);

                                parent_layout.addView(cardView);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }




                        }


                    }

                    else if(question_type.equals("Image")){

                        File file = null;

                        try {
                            file = new File(serveyModel.getImage());
                        }catch (Exception e){
                            file = new File("drawable://" + R.drawable.ic_placeholder);
                        }

                        imageView.setImageURI(Uri.fromFile(file));

//                        parent_layout.addView(question_title);
//
//                        parent_layout.addView(imageView);

                        if(serveyModel.getAnswer_type().equals("Check Boxes")){

                            try {



                                JSONArray jsonArray = new JSONArray(serveyModel.getOptions());

                                for (int check_index=0; check_index< jsonArray.length(); check_index++){

                                    JSONObject jsonObject = (JSONObject) jsonArray.get(check_index);

                                    CheckBox checkBox = new CheckBox(MainActivity.this);

                                    LinearLayout.LayoutParams for_check = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                                    for_check.setMargins(8,8,8,8);

                                    checkBox.setLayoutParams(for_check);

//                                checkBox.setButtonTintList(colorStateList);

                                    checkBox.setButtonDrawable(getResources().getDrawable(R.drawable.custom_checkbox));

                                    checkBox.setId(check_index);

                                    checkBoxes.add(checkBox);

                                    checkBox.setText("  "+jsonObject.getString("choice"));
                                }

                                for (final CheckBox checkBox: checkBoxes) {

                                    checkBox.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {



                                            Toast.makeText(MainActivity.this, ""+checkBox.getText()+serveyModel.getQuestion_type(), Toast.LENGTH_SHORT).show();

                                        }
                                    });

                                    linearLayoutChild.addView(checkBox);
                                }

                                layout_for_all.addView(question_title);

                                layout_for_all.addView(imageView);

                                layout_for_all.addView(linearLayoutChild);

                                cardView.addView(layout_for_all);

                                parent_layout.addView(cardView);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else if(serveyModel.getAnswer_type().equals("Text")){

                            EditText editText = new EditText(MainActivity.this);

                            editText.setLayoutParams(layoutParams);

                            editText.setHint("Feedback Here: ");

                            editText.setHighlightColor(getResources().getColor(R.color.colorMain));

                            editText.setId(-1);

                            editText.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ServeyModel s =  serveyModel;

                                    Toast.makeText(MainActivity.this, ""+s.getQuestionTitle(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            layout_for_all.addView(question_title);

                            layout_for_all.addView(imageView);

                            layout_for_all.addView(editText);

                            cardView.addView(layout_for_all);

                            parent_layout.addView(cardView);

                        }

                    }else if(serveyModel.getQuestion_type().equals("Text")){






                    }

            }
        }
    }
}

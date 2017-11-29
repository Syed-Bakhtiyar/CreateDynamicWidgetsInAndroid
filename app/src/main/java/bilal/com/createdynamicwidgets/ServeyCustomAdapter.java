package bilal.com.createdynamicwidgets;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BILAL on 11/28/2017.
 */

public class ServeyCustomAdapter extends ArrayAdapter<ServeyModel> {

    LayoutInflater inflater;

    Context context;

    ColorStateList colorStateList = new ColorStateList(
            new int[][]{
                    new int[]{android.R.attr.state_enabled} //enabled
            },
            new int[] {getContext().getResources().getColor(R.color.colorMain) }
    );

    TextView textView_title,question_title;

    public ServeyCustomAdapter(Context context, List<ServeyModel> objects) {
        super(context, 0, objects);

        inflater = LayoutInflater.from(context);

        this.context = context;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ImageView capture;

        ArrayList<RadioButton> radioButtons = new ArrayList<>();

        ArrayList<CheckBox> checkBoxes = new ArrayList<>();

        ImageView imageView = new ImageView(getContext());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT) );
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        TextView textView = new TextView(getContext());

        textView.setId(R.id.text);

        textView.setTextColor(Color.BLACK);

        textView.setText("Hello");

        RadioGroup radioGroup = new RadioGroup(getContext());
        radioGroup.setOrientation(LinearLayout.HORIZONTAL);
        radioGroup.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        radioGroup.setGravity(3 ); // for left
        LinearLayout linearLayout;

        LinearLayout linearLayoutChild = new LinearLayout(getContext());

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(18,18,18,18);

        linearLayoutChild.setLayoutParams(layoutParams);

        linearLayoutChild.setOrientation(LinearLayout.HORIZONTAL);

        linearLayoutChild.setGravity(17);



        ServeyModel reportsPicturesModel = getItem(position);

//        if(convertView == null){
//
//            convertView = inflater.inflate(R.layout.creating_dynamic_list,parent,false);
//
//        }

        switch (reportsPicturesModel.getType()){

            case "title":

                convertView = inflater.inflate(R.layout.title,parent,false);

                textView_title = (TextView) convertView.findViewById(R.id.textView_title);

                textView_title.setText(reportsPicturesModel.getServeyTitle());

                break;

            case "":



                convertView = inflater.inflate(R.layout.creating_dynamic_list,parent,false);

                linearLayout = (LinearLayout) convertView.findViewById(R.id.parent);

                question_title = (TextView) convertView.findViewById(R.id.question_title);

                question_title.setText("Q: "+reportsPicturesModel.getQuestionTitle()+" ?");

                String question_type = reportsPicturesModel.getQuestion_type();

                String answer_type = reportsPicturesModel.getAnswer_type();

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

                        linearLayout.addView(radioGroup);
                        try {
//                            JSONObject options = new JSONObject(reportsPicturesModel.getOptions());



//                            Log.d("options", "getView: "+options.toString());

                            JSONArray option_array = new JSONArray(reportsPicturesModel.getOptions());

                            for (int i=0; i<option_array.length(); i++){

                                JSONObject val = (JSONObject) option_array.get(i);

                                RadioButton radioButton = new RadioButton(getContext());

                                radioButton.setId(R.id.radio+i);

                                radioButtons.add(radioButton);

//                                radioButton.setBu

                                radioButton.setButtonTintList(colorStateList);

                                radioButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                                radioButton.setHeight(45);

                                Log.d("radioValue", "getView: "+val.getString("choice"));

                                radioButton.setText(val.getString("choice"));

                            }

                            for (final RadioButton radioButton : radioButtons){

                                radioButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Toast.makeText(getContext(), ""+radioButton.getText(), Toast.LENGTH_SHORT).show();

                                    }
                                });

                                radioGroup.addView(radioButton);
                            }




                        } catch (JSONException e) {
                            Log.d("error", "getView: "+e);
                        }
                    }

                }

                else if(question_type.equals("Image")){

                    File file = null;

                    try {
                        file = new File(reportsPicturesModel.getImage());
                    }catch (Exception e){
                        file = new File("drawable://" + R.drawable.ic_placeholder);
                    }

                    imageView.setImageURI(Uri.fromFile(file));

                    linearLayout.addView(imageView);

                    if(reportsPicturesModel.getAnswer_type().equals("Check Boxes")){

                        try {

                            JSONArray jsonArray = new JSONArray(reportsPicturesModel.getOptions());

                            for (int i=0; i< jsonArray.length(); i++){

                                JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                                CheckBox checkBox = new CheckBox(getContext());

                                checkBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                                checkBox.setButtonTintList(colorStateList);

                                checkBox.setId(i);

                                checkBoxes.add(checkBox);

                                checkBox.setText(jsonObject.getString("choice"));
                            }

                            for (final CheckBox checkBox: checkBoxes) {

                                checkBox.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Toast.makeText(getContext(), ""+checkBox.getText(), Toast.LENGTH_SHORT).show();

                                    }
                                });

                                linearLayoutChild.addView(checkBox);
                            }

                            linearLayout.addView(linearLayoutChild);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }

                break;
        }




        return convertView;
    }



}


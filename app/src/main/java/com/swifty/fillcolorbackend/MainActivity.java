package com.swifty.fillcolorbackend;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CHOOSER = 1234;
    EditText categoryname;
    Button addpic;
    LinearLayout pic_lay;
    UploadSuccessListener uploadSuccessListener;
    public final String uploadUrl = "http://sgdaemon.cloudapp.net/pic/extAPI/uploadpic";
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        addEvents();

    }

    private void initViews() {
        categoryname = (EditText) findViewById(R.id.category_name);
        addpic = (Button) findViewById(R.id.add_pic);
        pic_lay = (LinearLayout) findViewById(R.id.pic_lay);
    }

    private void addEvents() {
        addpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOnePicitem();
            }
        });
    }

    private void addOnePicitem() {
        View picItem = getLayoutInflater().inflate(R.layout.view_pic_item, pic_lay, false);
        pic_lay.addView(picItem);
        final EditText picName = (EditText) picItem.findViewById(R.id.pic_name);
        final ImageView pic = (ImageView) picItem.findViewById(R.id.pic);
        final Button upload = (Button) picItem.findViewById(R.id.upload);
        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findImageFromDisk(pic, picName);
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPic(picName.getText().toString(), pic);
            }
        });
    }

    private void uploadPic(final String picname, final ImageView pic) {
        dialog = ProgressDialog.show(this, null, "uploading");
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, uploadUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        dialog.dismiss();
                        if (response.toLowerCase().contains("success")) {
                            pic_lay.removeView((View) pic.getParent().getParent());
                        }
                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //在这里设置需要post的参数
                Map<String, String> map = new HashMap<String, String>();
                map.put("catename", categoryname.getText().toString());
                map.put("picname", picname);
                if (picname.toLowerCase().endsWith(".jpg")) {
                    Bitmap bm = ((BitmapDrawable) pic.getDrawable()).getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    String bmarray = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                    Log.e("pic64", bmarray);
                    map.put("pic64", bmarray);
                } else if (picname.toLowerCase().endsWith(".png")) {
                    Bitmap bm = ((BitmapDrawable) pic.getDrawable()).getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    String bmarray = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                    Log.e("pic64", bmarray);
                    map.put("pic64", bmarray);
                }
                return map;
            }

        };
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void findImageFromDisk(final ImageView imageView, final EditText picName) {
        // Create the ACTION_GET_CONTENT Intent
        Intent getContentIntent = FileUtils.createGetContentIntent();

        Intent intent = Intent.createChooser(getContentIntent, "Select a file");
        startActivityForResult(intent, REQUEST_CHOOSER);
        uploadSuccessListener = new UploadSuccessListener() {
            @Override
            public void uploadSuccess(File file, String filename) {
                if (!setImage(picName, imageView, file, filename)) {
                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private boolean setImage(EditText picName, ImageView imageView, File file, String filename) {
        try {
            if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".png")) {
                Log.e("backend", file.getPath());
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                imageView.setImageBitmap(bitmap);
                picName.setText(filename);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.e("backend", e.toString());
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSER:
                if (resultCode == RESULT_OK) {

                    final Uri uri = data.getData();

                    // Get the File path from the Uri
                    String path = FileUtils.getPath(this, uri);

                    // Alternatively, use FileUtils.getFile(Context, Uri)
                    if (path != null && FileUtils.isLocal(path)) {
                        File file = new File(path);
                        uploadSuccessListener.uploadSuccess(file, file.getName());
                    }
                }
                break;
        }
    }
}

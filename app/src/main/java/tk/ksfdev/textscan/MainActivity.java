package tk.ksfdev.textscan;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    SurfaceView surfaceView;
    TextView textViewResult;

    Handler mHandler;
    StringBuilder stringBuilderTemp;

    CameraSource cameraSource;
    IntentFilter lowStorageFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = findViewById(R.id.surfaceView);
        textViewResult = findViewById(R.id.textViewResultTemp);

        //enable scrolling
        textViewResult.setMovementMethod(new ScrollingMovementMethod());

        //init stuff
        mHandler = new Handler();
        if(Common.stringBuilderResult == null)
            Common.stringBuilderResult = new StringBuilder();
        stringBuilderTemp = new StringBuilder();


        //
        TextRecognizer textRecognizer = new TextRecognizer.Builder(this).build();
        if (!textRecognizer.isOperational()) {
            //deprecated in API 26, "broadcast will no longer be delivered"
            lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;
            String errorMsg;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasLowStorage) {
                errorMsg = "Required dependencies can't be downloaded due to low device storage";
            } else
                errorMsg = "Detector dependencies are not yet available, try again later";

            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            finish();
        } else {

            cameraSource = new CameraSource.Builder(this, textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setAutoFocusEnabled(true)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(40.0f)
                    .build();

            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    //we have permission but check anyway
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        try {
                            cameraSource.start(surfaceView.getHolder());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                    //
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    cameraSource.stop();
                }
            });


            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                    //
                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    //
                    final SparseArray<TextBlock> lines = detections.getDetectedItems();
                    if(lines.size() != 0){
                        stringBuilderTemp = new StringBuilder();

                        for(int i=0; i<lines.size(); i++){
                            stringBuilderTemp.append(lines.valueAt(i).getValue());
                            stringBuilderTemp.append("\n");
                        }

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                textViewResult.setText(stringBuilderTemp);
                            }
                        });

                    }
                }
            });
        }


        //button add text
        findViewById(R.id.buttonAddText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.stringBuilderResult.append(stringBuilderTemp);
                Common.stringBuilderResult.append(" ****** \n");
                Toast.makeText(MainActivity.this, "Added to result: \n" + stringBuilderTemp.toString(), Toast.LENGTH_SHORT).show();
            }
        });





    }




    @Override
    protected void onStart() {
        super.onStart();
        textViewResult.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_main_ok:
                //start new activity to edit/save/share content of stringBuilderResult
                Intent intent = new Intent(MainActivity.this, EditResultActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_main_clear:
                Common.stringBuilderResult = new StringBuilder();
                textViewResult.setText("");
                break;
        }

        return true;
    }


}

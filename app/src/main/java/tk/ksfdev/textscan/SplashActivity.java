package tk.ksfdev.textscan;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int PERM_CAMERA = 81;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //check permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, PERM_CAMERA);
            } else {
                //do stuff
                goToMain();
            }
        } else {
            //do stuff
            goToMain();
        }

    }

    private void goToMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //do stuff
                goToMain();
            } else {
                //notify user that he's asshole
                new AlertDialog.Builder(SplashActivity.this, R.style.AlertDialogTheme)
                        .setTitle("\tPermission request denied")
                        .setMessage("One or more critical permissions denied, please try again...")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(SplashActivity.this, SplashActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .show();
            }
        }
    }


}

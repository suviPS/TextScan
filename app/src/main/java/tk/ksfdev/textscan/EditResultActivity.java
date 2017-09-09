package tk.ksfdev.textscan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Calendar;

public class EditResultActivity extends AppCompatActivity {

    EditText editTextResult;

    private ShareActionProvider shareActionProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_result);

        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Scan Result");
        }

        editTextResult = findViewById(R.id.edit_edittext_result);
        editTextResult.setText(Common.stringBuilderResult);


        findViewById(R.id.edit_button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //TODO check permissions for WRITE_EXTERNAL_STORAGE

                View mLayout = EditResultActivity.this.getLayoutInflater().inflate(R.layout.alertdialog_edit_edittext, null);
                final EditText fileName = mLayout.findViewById(R.id.edit_alertdialog_edittext_name);

                AlertDialog.Builder alert = new AlertDialog.Builder(EditResultActivity.this, R.style.AlertDialogTheme)
                        .setTitle("Name of file:")
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                saveAsPDF(fileName.getText().toString().trim());
                            }
                        });
                alert.setView(mLayout);
                alert.show();
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case android.R.id.home:
                //save changes from editText
                Common.stringBuilderResult = new StringBuilder();
                Common.stringBuilderResult.append(editTextResult.getText().toString());
                //go back to MainActivity
                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.menu_edit_clear:
                Common.stringBuilderResult = new StringBuilder();
                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.menu_edit_share:
                shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
                shareOption();
                break;
        }

        return true;
    }

    private void shareOption(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, editTextResult.getText().toString());

        if(shareActionProvider != null)
            shareActionProvider.setShareIntent(intent);

        startActivity(Intent.createChooser(intent, "Share with"));
    }

    private void saveAsPDF(String fileName){
        String rawResult = editTextResult.getText().toString().trim();

        //handle conditions
        if(fileName == null || "".equals(fileName)){
            Toast.makeText(this, "Please enter name for file", Toast.LENGTH_SHORT).show();
            return;
        } else if("".equals(rawResult)){
            Toast.makeText(this, "Document is empty, please add some content...", Toast.LENGTH_SHORT).show();
            return;
        }

        //save in background
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                String result = "400";

                String fileName = strings[0];
                String text = strings[1];

                //create folder if deleted
                File dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsoluteFile();
                dirPath.mkdirs();

                String tempExt = "/" + fileName + "-" + Calendar.getInstance().getTimeInMillis() + ".pdf";
                String filePath = dirPath.getPath() + tempExt;
                String filePathAbsolute = dirPath.getAbsolutePath() + tempExt;
                Document document = new Document();

                try {
                    PdfWriter.getInstance(document, new FileOutputStream(filePathAbsolute));
                    document.open();


                    document.add(new Paragraph(text));


                    document.addTitle(fileName);
                    document.addCreator("Text Scan Android app");
                    document.addCreationDate();
                    document.close();
                    result = "200##" + filePath;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                if(result.startsWith("200")){
                    //all good
                    String[] arr = result.split("##");
                    Toast.makeText(EditResultActivity.this, "File saved at: \n" + arr[1], Toast.LENGTH_LONG).show();
                } else if(result.equals("400")){
                    //error
                    Toast.makeText(EditResultActivity.this, "Error :|", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(fileName, rawResult);
    }




}

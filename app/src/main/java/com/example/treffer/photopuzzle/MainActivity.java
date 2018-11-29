package com.example.treffer.photopuzzle;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    ImageButton buttons[] = new ImageButton[9];
    float initialX, initialY, deltaX, deltaY;
    ImageButton collision;
    ImageButton openCamera, selectImage;
    float positionX_tmp, positionX_tmp2;
    float positionY_tmp, positionY_tmp2;
    Bitmap[][] bitmaps_array = new Bitmap[3][3];
    ImageView imageView;
    final int IMAGE_PATH = 401;
    Bitmap bitmap, bitmap_cropped;
    String mCurrentPhotoPath;
    static final int REQUEST_IMAGE_CAPTURE = 40;
    File photoFile;
    public static final int PICK_IMAGE = 1;
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = "PhotoPuzzle_" + timeStamp + "_.jpg";
    int maxSize;
    Uri imageFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttons[0] = findViewById(R.id.button1);
        buttons[1] = findViewById(R.id.button2);
        buttons[2] = findViewById(R.id.button3);
        buttons[3] = findViewById(R.id.button4);
        buttons[4] = findViewById(R.id.button5);
        buttons[5] = findViewById(R.id.button6);
        buttons[6] = findViewById(R.id.button7);
        buttons[7] = findViewById(R.id.button8);
        buttons[8] = findViewById(R.id.button9);

        for (ImageView button : buttons) {
            button.setOnTouchListener(this);
            button.setBackgroundColor(Color.WHITE);

        }


        openCamera = findViewById(R.id.open_camera);
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        selectImage = findViewById(R.id.select_image_from_gallery);
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = v.getX();
                initialY = v.getY();
                deltaX = event.getRawX() - initialX;
                deltaY = event.getRawY() - initialY;
                v.setZ(100);
                break;
            case MotionEvent.ACTION_MOVE:
                v.setX(event.getRawX() - deltaX);
                v.setY(event.getRawY() - deltaY);
                collision = null;

                for (ImageButton b : buttons) {
                    if (b.getId() != v.getId() &&
                            detectCollision(b, v.getX() + event.getX(), v.getY() + event.getY())) {
                        b.setBackgroundColor(Color.RED);
                        collision = b;
                    } else {
                        b.setBackgroundColor(Color.GRAY);
                    }
                }

                break;
            case MotionEvent.ACTION_UP:

                for (ImageButton button : buttons) {
                    button.setBackgroundColor(Color.GRAY);
                }

                if (collision != null) {

                    v.animate()
                            .x(collision.getX())
                            .y(collision.getY())
                            .z(1)
                            .setDuration(500)
                            .start();

                    collision.setZ(50);
                    collision.animate()
                            .x(initialX)
                            .y(initialY)
                            .z(1)
                            .setDuration(500)
                            .start();

                } else {
                    v.animate()
                            .x(initialX)
                            .y(initialY)
                            .z(1)
                            .setDuration(500)
                            .start();
                }


                break;
        }
        return true;
    }

    private boolean detectCollision(ImageButton b, float x, float y) {

        Rect hitRect = new Rect();
        b.getHitRect(hitRect);

        return hitRect.left <= x && hitRect.right >= x &&
                hitRect.top <= y && hitRect.bottom >= y;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.shuffle_image) {

            for (int i = 0; i <= 8; i++) {

                Random random = new Random();
                int btn = random.nextInt(8);


                positionX_tmp = buttons[i].getX();
                positionY_tmp = buttons[i].getY();

                positionX_tmp2 = buttons[btn].getX();
                positionY_tmp2 = buttons[btn].getY();

                buttons[i].setX(positionX_tmp2);
                buttons[i].setY(positionY_tmp2);

                buttons[btn].setX(positionX_tmp);
                buttons[btn].setY(positionY_tmp);
            }


        }
        return true;
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        imageView = findViewById(R.id.image_taken);

        if (requestCode == IMAGE_PATH && resultCode != RESULT_CANCELED) {

            //Uri selectedImage = Uri.parse(data.getData().getPath());
            //imageView.setImageURI(selectedImage);

            //File imageFile = getFile();
            Log.d("PATH", "onActivityResult: " + this.imageFile.getPath());
            Bitmap bm;

            File file = getFile();
            try {
               bm = BitmapFactory.decodeFile(file.getAbsolutePath());
                bitmap_cropped = square(bm);
                int width = bitmap_cropped.getWidth() / 3;
                int height = width;
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        bitmaps_array[i][j] = Bitmap.createBitmap(bitmap, i * width, j * height, width, height);
                        buttons[((i * 3) + j)].setImageBitmap(bitmaps_array[i][j]);
                    }
                }

            } catch (Exception e) {
                Toast.makeText(this, "Konnte nicht in eine Bitmap konvertiert werden!", Toast.LENGTH_LONG).show();

                e.printStackTrace();
            }



        }


        if (requestCode == PICK_IMAGE) {


            Uri uri = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);


            } catch (Exception e) {
                Toast.makeText(this, "Konnte nicht in eine Bitmap konvertiert werden!", Toast.LENGTH_LONG).show();

                e.printStackTrace();
            }

            bitmap_cropped = square(bitmap);
            int width = bitmap_cropped.getWidth() / 3;
            int height = width;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    bitmaps_array[i][j] = Bitmap.createBitmap(bitmap, i * width, j * height, width, height);
                    buttons[((i * 3) + j)].setImageBitmap(bitmaps_array[i][j]);
                }
            }


        }


    }



    public void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Wähle ein Foto aus"), PICK_IMAGE);

    }

    public Bitmap square(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();

        Bitmap target = null;
        if (width > height) {
            target = Bitmap.createBitmap(src, (width - height) / 2, 0, height, height);
        } else {
            target = Bitmap.createBitmap(src, 0, (height - width) / 2, width, width);
        }
        return target;
    }


    public void takePicture() {

      this.imageFile = prepareFile();

        if (this.imageFile != null) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, this.imageFile);
            startActivityForResult(intent, IMAGE_PATH);
        }

    }

    public Uri prepareFile() {
      if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {


            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "image.jpg");
            return FileProvider.getUriForFile(this, "com.example.treffer.photopuzzle", file);
        }


        return null;




    }

    public File getFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; ++i) {
                    File file = files[i];
                    if (file.getName().contains("image")) {
                        Toast.makeText(this, file.getName(), Toast.LENGTH_SHORT).show();
                        return file;
                    }
                }
            }
        }
        return null;
    }


}




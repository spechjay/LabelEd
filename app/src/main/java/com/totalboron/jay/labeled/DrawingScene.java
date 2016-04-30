package com.totalboron.jay.labeled;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;

public class DrawingScene extends FragmentActivity
{
    private boolean isFragmentPresent = false;
    private String logging = getClass().getSimpleName();
    private final String TAG_FOR_FRAGMENT = "LabelView";
    private DrawingView drawingView;
    private String filePath;
    private int imageHeight = 0;
    private int imageWidth = 0;
    private EditText editText;
    private SeekBar seekBar;
    private LinearLayoutCustom toolbars;
    private LinearLayoutCustom edit_text_tray;
    private LinearLayout linearLayout;
    private boolean colorsBarOpen = true;
    private final int REQUEST_WRITE_STORAGE = 52;
    private final int REQUEST_READ_STORAGE = 55;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawing_scene);
        edit_text_tray=(LinearLayoutCustom)findViewById(R.id.edit_text_tray);
        editText=(EditText)edit_text_tray.findViewById(R.id.edit_text);
        Intent intent = getIntent();
        if (intent.getAction().equals("Main_Activity"))
        {
            String string = intent.getStringExtra("PATH");
            filePath = string;
            Log.d(logging, string);
        } else if (intent.getAction().equals(Intent.ACTION_SEND) && intent.getType().startsWith("image/"))
        {
            handleSendImages(intent);
        } else finish();
        drawingView = (DrawingView) findViewById(R.id.drawing_view);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                drawingView.setSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });
        toolbars = (LinearLayoutCustom) findViewById(R.id.toolsToShow);
        linearLayout = (LinearLayout) findViewById(R.id.paintToolbar);
        colorsBarOpen = linearLayout.getVisibility() == View.VISIBLE;
    }

    private void handleSendImages(Intent intent)
    {
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        Log.d(logging, uri.getPath());
        if (uri != null)
        {
            pathFromUri(uri);
        } else finish();

    }

    private void pathFromUri(Uri uri)
    {
        Log.d(logging, "inUriPath");
        Cursor cursor = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, null);
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        filePath = cursor.getString(columnIndex);
    }


    @Override
    protected void onResume()
    {
        setState();
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        Log.d(logging, "onWindowFocussedChange");
        loadImageWithPermission();
    }

    private void loadImageWithPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                loadImage();
            } else
            {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE))
                {
                    Toast.makeText(getApplicationContext(), "Reading Access required to select an Image", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
            }
        else loadImage();
    }

    private void setState()
    {
        Log.d(logging, "insetState");
        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    public void loadFragment(View view)
    {
        edit_text_tray.setVisibility(View.VISIBLE);
        isFragmentPresent = true;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        Log.d(logging, "in onKeyDown");
        if (isFragmentPresent == true)
        {
            if (getCurrentFocus() != null)
            {
                removeKeyboard();
            }
            removeFragment();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void removeFragment()
    {
        removeKeyboard();
        edit_text_tray.setVisibility(View.INVISIBLE);
        editText.setText("");
        isFragmentPresent = false;
        setState();
    }

    private void removeKeyboard()
    {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }





    public void checkForOpenFragment()
    {
        if (isFragmentPresent)
            removeFragment();
    }



    public void messageReceiver(View view)
    {
        String string=editText.getText().toString();
        drawingView.sendMessage(string);
        removeFragment();
    }
    public void loadImage()
    {
        int width = getWindow().getDecorView().getWidth();
        float aspectRatio = aspectRatioFind();
        int height = (int) (width * aspectRatio);
        if (aspectRatio>=1)
        {
            if (getResources().getConfiguration().orientation!= Configuration.ORIENTATION_PORTRAIT)
            {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            else
            {
                setParams(width, height);
            }
        }
        else
        {
            if (getResources().getConfiguration().orientation!= Configuration.ORIENTATION_LANDSCAPE)
            {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            else
            {
                height=getWindow().getDecorView().getHeight();
                width=(int) (height/aspectRatio);
                setParams(width,height);
            }
        }
    }


    private void setParams(int width, int height)
    {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) drawingView.getLayoutParams();
        layoutParams.height = height;
        layoutParams.width=width;
        drawingView.setLayoutParams(layoutParams);
        drawingView.setDrawingScene(this);
        Glide.with(getApplicationContext()).load(filePath).into(drawingView);
    }

    private float aspectRatioFind()
    {
        File file = new File(filePath);
        BitmapFactory.Options bmoption = new BitmapFactory.Options();
        bmoption.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), bmoption);
        imageHeight = bmoption.outHeight;
        imageWidth = bmoption.outWidth;
        return imageHeight / (float) imageWidth;
    }


    public void saveImage(View view) throws FileNotFoundException
    {
        checkForPermissions();
    }

    private void saveImageIntoFile()
    {
        drawingView.setDrawingCacheEnabled(true);
        drawingView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        drawingView.buildDrawingCache();
        Bitmap bitmap = drawingView.getDrawingCache();
        Uri uri = Uri.parse(filePath);
        String fileName = uri.getLastPathSegment();
        AsyncTaskSaveImage asyncTaskSaveImage = new AsyncTaskSaveImage(fileName, getApplicationContext(),drawingView.getTags_for_each_label());
        asyncTaskSaveImage.execute(Bitmap.createBitmap(bitmap));
        finish();
    }

    private void checkForPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                saveImageIntoFile();
            } else
            {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    Toast.makeText(getApplicationContext(), "Writing Access required to save the LabelED Image", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            }
        else saveImageIntoFile();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (requestCode == REQUEST_WRITE_STORAGE)
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) saveImageIntoFile();
                else
                {
                    Toast.makeText(getApplicationContext(), "Permission not granted", Toast.LENGTH_LONG).show();
                    finish();
                }
            } else if (requestCode == REQUEST_READ_STORAGE)
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) loadImage();
                else
                {
                    Toast.makeText(getApplicationContext(), "Permission not granted", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    public void clearLabel(View view)
    {
        checkForOpenFragment();
        drawingView.reset();
    }


    public void singleTapDone()
    {
        int visibility = toolbars.getVisibility();
        if (visibility == View.INVISIBLE)
            toolsToggle(true);
        else toolsToggle(false);
    }

    public void toolsToggle(boolean val)
    {
        if (val)
        {
            if (colorsBarOpen)
            {
                toggleTheColorBar(!colorsBarOpen);
            }
            toolbars.setVisibility(View.VISIBLE);
        } else
        {
            if (colorsBarOpen)
            {
                toggleTheColorBar(colorsBarOpen);
            }
            toolbars.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    protected void onStop()
    {
        super.onStop();
        checkForOpenFragment();
    }

    public void blackButton(View view)
    {
        drawingView.setBlackColor();
    }

    public void redButton(View view)
    {
        drawingView.setRedColor();
    }

    public void greenButton(View view)
    {
        drawingView.setGreenColor();
    }

    public void blueButton(View view)
    {
        drawingView.setBlueColor();
    }

    public void whiteButton(View view)
    {
        drawingView.setWhiteColor();
    }

    public void toggleColor(View view)
    {
        if (colorsBarOpen)
        {
            toggleTheColorBar(colorsBarOpen);
            colorsBarOpen = false;
        } else
        {
            toggleTheColorBar(colorsBarOpen);
            colorsBarOpen = true;
        }

    }

    public void undoLast(View view)
    {
        drawingView.undoLast();
    }

    private void toggleTheColorBar(boolean how)
    {
        if (how)
        {
            int cx=linearLayout.getMeasuredWidth();
            int cy=linearLayout.getMeasuredHeight();
            if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP)
            {
                Animator animator = ViewAnimationUtils.createCircularReveal(linearLayout, cx, cy, cx, 0);
                animator.addListener(new Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(Animator animation)
                    {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        linearLayout.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation)
                    {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation)
                    {

                    }
                });
                animator.start();
            }
            else linearLayout.setVisibility(View.INVISIBLE);
        }
        else
        {
            int cx=linearLayout.getMeasuredWidth();
            int cy=linearLayout.getMeasuredHeight();
            if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP)
            {
                Animator animator = ViewAnimationUtils.createCircularReveal(linearLayout, cx, cy, 0, cx);
                linearLayout.setVisibility(View.VISIBLE);
                animator.start();
            }
            else
            linearLayout.setVisibility(View.VISIBLE);

        }
    }


}

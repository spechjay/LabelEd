package com.totalboron.jay.labeled;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

public class GalleryIndex extends AppCompatActivity
{
    RecyclerView recyclerView;
    final private int REQUEST_CODE_FOR_IMAGE = 75;
    private String logging = getClass().getSimpleName();
    private final int REQUEST_READ_STORAGE = 55;
    private ImageAdapterIndex imageAdapterIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.behaviour);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_index);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Choose an Image");
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        int cnum = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 4;
        imageAdapterIndex = new ImageAdapterIndex(null, null, cnum, this);
        recyclerView.setAdapter(imageAdapterIndex);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), cnum);
        recyclerView.setLayoutManager(gridLayoutManager);
        setResult(RESULT_CANCELED);
        checkForPermissions();
    }

    private void checkForPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                loadEverything();
            } else
            {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE))
                {
                    Toast.makeText(getApplicationContext(), "Reading Access required to select an Image", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
            }
        else loadEverything();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (requestCode == REQUEST_READ_STORAGE)
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) loadEverything();
                else
                {
                    Toast.makeText(getApplicationContext(), "Permission not granted", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
    }

    private void loadEverything()
    {
        AsyncTaskLoadingForIndex asyncTaskLoadingForIndex = new AsyncTaskLoadingForIndex(getApplicationContext(), new WeakReference<GalleryIndex>(this));
        asyncTaskLoadingForIndex.execute();
    }

    public void indexReady(List<String> bucket_list_names, List<File> data_each_bucket)
    {
        imageAdapterIndex.setBucket(bucket_list_names,data_each_bucket,getWindow().getDecorView().getWidth());
    }
    public void openImages(String bucket_name)
    {
        Intent intent = new Intent(this, ImageListActivity.class);
        intent.putExtra("BUCKET", bucket_name);
        startActivityForResult(intent, REQUEST_CODE_FOR_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE_FOR_IMAGE && resultCode == RESULT_OK)
        {
            setResult(RESULT_OK, data);
            finish();
        }

    }

    public void openAllImages()
    {
        Intent intent = new Intent(this, ImageListActivity.class);
        intent.putExtra("BUCKET", "All Pictures");
        intent.putExtra("All",true);
        startActivityForResult(intent, REQUEST_CODE_FOR_IMAGE);
    }
}

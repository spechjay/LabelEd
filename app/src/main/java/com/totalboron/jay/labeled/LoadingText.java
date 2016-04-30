package com.totalboron.jay.labeled;

import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jay on 19/04/16.
 */
public class LoadingText extends AsyncTask<File,Void,List<String>>
{
    private Context context;
    private String logging=getClass().getSimpleName();
    private TableLayout tableLayout;
    public LoadingText(Context context, TableLayout tableLayout)
    {

        this.context = context;
        this.tableLayout=tableLayout;
    }

    @Override
    protected List<String> doInBackground(File... params)
    {
        List<String> str=new ArrayList<>();
        try
        {
            String inputString;
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(new FileInputStream(params[0])));
            while ((inputString=bufferedReader.readLine())!=null)
            {
                str.add(inputString);
                Log.d(logging,inputString);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return str;
    }

    @Override
    protected void onPostExecute(List<String> strings)
    {
        int cnum=context.getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT?3:5;
        float textSize;
        if (cnum==3)
            textSize=16;
        else textSize=22;

        if (strings!=null&&!isCancelled())
        {
            TableLayout.LayoutParams tableRowParams=new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            TableRow tableRow=new TableRow(context);
            tableRow.setLayoutParams(tableRowParams);
            TextView textView;
            int color=context.getResources().getColor(R.color.black);
            TableRow.LayoutParams textViewLayoutParams=new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,1.0f);
            tableLayout.addView(tableRow);
            int row=0;
            for (int i=0;i<strings.size();i++)
            {
                textView=new TextView(context);
                textView.setLayoutParams(textViewLayoutParams);
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(color);
                textView.setTextSize(textSize);
                textView.setText(strings.get(i));
                if (i%cnum==0)
                {
                    row++;
                    tableRow=new TableRow(context);
                    tableRow.setLayoutParams(tableRowParams);
                    tableLayout.addView(tableRow);
                    if (row>3) break;
                }
                tableRow.addView(textView);
            }
            if (row>3)
            {
                textView=new TextView(context);
                textView.setLayoutParams(textViewLayoutParams);
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(color);
                textView.setTextSize(10);
                textView.setText("(Click to get more)");
                tableRow.addView(textView);
            }

        }
    }
}

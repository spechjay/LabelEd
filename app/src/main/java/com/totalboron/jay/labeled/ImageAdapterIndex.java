package com.totalboron.jay.labeled;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

/**
 * Created by Jay on 14/04/16.
 */
public class ImageAdapterIndex extends RecyclerView.Adapter<ImageAdapterIndex.Holder>
{
    private List<String> bucket;
    private List<File> data;
    private int width_screen;
    private int cnum;
    private GalleryIndex galleryIndex;

    public ImageAdapterIndex(List<String> bucket, List<File> data, int cnum, GalleryIndex galleryIndex)
    {
        this.bucket = bucket;
        this.data = data;
        this.cnum = cnum;
        this.galleryIndex = galleryIndex;
    }

    public void setBucket(List<String> bucket, List<File> data, int width)
    {
        this.bucket = bucket;
        this.data = data;
        width_screen = width;
        notifyDataSetChanged();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_display, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(final Holder holder, int position)
    {
        ImageView imageView = holder.getImageView();

        Glide.with(imageView.getContext()).load(data.get(position)).override(width_screen / cnum, width_screen / cnum).placeholder(R.drawable.placeholder).into(imageView);

        holder.setTextView(bucket.get(position));
        holder.getImageView().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (holder.getAdapterPosition() == 0)
                    galleryIndex.openAllImages();
                else
                    galleryIndex.openImages(bucket.get(holder.getAdapterPosition()));
            }
        });

    }

    @Override
    public int getItemCount()
    {
        return bucket != null ? bucket.size() : 0;
    }

    class Holder extends RecyclerView.ViewHolder
    {
        private ImageView imageView;
        private TextView textView;

        public Holder(View itemView)
        {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text_view);
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            itemView.getLayoutParams().height = width_screen / cnum;

//            layoutParams.width=width_screen/cnum;
//            layoutParams.height=width_screen/cnum;
//            itemView.setLayoutParams(layoutParams);
        }

        public ImageView getImageView()
        {
            return imageView;
        }

        public void setTextView(String title)
        {
            textView.setText(title);
        }
    }
}

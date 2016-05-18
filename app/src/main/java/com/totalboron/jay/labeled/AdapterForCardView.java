package com.totalboron.jay.labeled;

import android.content.Context;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdapterForCardView extends RecyclerView.Adapter<AdapterForCardView.CardAdapterHolder>
{
    private Context context;
    private List<File> image_files;
    private List<File> strings_of_files;
    private MainActivity mainActivity;
    private String logging = getClass().getSimpleName();
    private boolean longClicked = false;
    private List<Integer> selection_list;
    private List<CardAdapterHolder> holders;
    private TextView numberReference;

    public AdapterForCardView(Context context, MainActivity mainActivity)
    {
        this.context = context;
        this.image_files = null;
        this.strings_of_files = null;
        this.mainActivity = mainActivity;
        selection_list = new ArrayList<>();
        holders = new ArrayList<>();
    }

    @Override
    public CardAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.card_view_recycler, parent, false);
        CardAdapterHolder cardAdapterHolder = new CardAdapterHolder(view);
        holders.add(cardAdapterHolder);
        return cardAdapterHolder;
    }

    @Override
    public void onBindViewHolder(final CardAdapterHolder holder, int position)
    {
        Glide.with(context).load(image_files.get(position)).into(holder.getImageView());
        if (longClicked)
        {
            if (selection_list.contains(position))
                holder.highlight();
            else holder.deHighlight();
        }
        holder.getImageView().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!longClicked)
                {
                    ImageView imageView = holder.getImageView();
                    int[] location = new int[2];
                    imageView.getLocationInWindow(location);
                    mainActivity.setUpOverview(image_files.get(holder.getAdapterPosition()), strings_of_files.get(holder.getAdapterPosition()), location, imageView.getMeasuredHeight(), imageView.getMeasuredWidth());
                } else
                {
                    longClickSelection(holder);
                }
            }
        });
        holder.getImageView().setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                longClicked = true;
                longClickSelection(holder);
                return true;
            }
        });

        if (position != holder.Position())
        {
            if (holder.Position() != -1) holder.getLoadingText().cancel(true);
            removeAllText(holder.getTableLayout());
            LoadingText loadingText = new LoadingText(context, holder.getTableLayout());
            holder.setLoadingText(loadingText, position);
            loadingText.execute(strings_of_files.get(position));
        }
    }

    private void longClickSelection(CardAdapterHolder cardAdapterHolder)
    {
        if (selection_list.contains(cardAdapterHolder.getAdapterPosition()))
        {
            selection_list.remove(Integer.valueOf(cardAdapterHolder.getAdapterPosition()));
            cardAdapterHolder.deHighlight();
            numberReference.setText(selection_list.size() + "");
            checkIfLast();
        } else
        {
            if (selection_list.size() == 0)
                showSelectionBar();
            selection_list.add(cardAdapterHolder.getAdapterPosition());
            numberReference.setText(selection_list.size() + "");
            cardAdapterHolder.highlight();
        }
    }

    private void showSelectionBar()
    {
        mainActivity.showSelectionBar();
    }

    private void hideSelectionBar()
    {
        mainActivity.hideSelectionBar();
    }

    private void checkIfLast()
    {
        if (selection_list.size() == 0)
        {
            longClicked = false;
            hideSelectionBar();
        }
    }

    public void setNull()
    {
        strings_of_files = null;
        image_files = null;
        notifyDataSetChanged();
    }

    private void removeAllText(TableLayout tableLayout)
    {
        tableLayout.removeAllViews();
    }

    public void setFiles(List<File> strings_of_files, List<File> image_files)
    {
        if (this.strings_of_files == null)
        {
            this.image_files = image_files;
            this.strings_of_files = strings_of_files;
            notifyDataSetChanged();
        } else
        {
            animateTo(image_files, strings_of_files);
        }
    }

    private void removeItem(int pos)
    {
        strings_of_files.remove(pos);
        image_files.remove(pos);
        notifyItemRemoved(pos);
    }

    private void addItem(File image, File label)
    {
        strings_of_files.add(label);
        image_files.add(image);
        notifyItemInserted(strings_of_files.size() - 1);
    }

    private void moveItem(int fromPosition, int toPosition)
    {
        File file = strings_of_files.remove(fromPosition);
        strings_of_files.add(toPosition, file);
        file = image_files.remove(fromPosition);
        image_files.add(toPosition, file);
        notifyItemMoved(fromPosition, toPosition);
    }

    private void animateTo(List<File> newImageFile, List<File> newLabel)
    {
        applyAndAnimateRemovals(newImageFile, newLabel);
        applyAndAnimateAddition(newImageFile, newLabel);
        applyAndAnimateMove(newImageFile, newLabel);

    }

    private void applyAndAnimateMove(List<File> newImageFile, List<File> newLabel)
    {
        Log.d("size", strings_of_files.size() + "=" + newLabel.size());
        for (int i = 0; i < strings_of_files.size(); i++)
        {
            Log.d("strings_of_files", strings_of_files.get(i).getName());
        }
        for (int i = 0; i < strings_of_files.size(); i++)
        {
            Log.d("newLabel", newLabel.get(i).getName());
        }
        for (int toPosition = 0; toPosition < newLabel.size(); toPosition++)
        {
            int fromPosition = indexOf(strings_of_files, newLabel.get(toPosition).getName());
            if (fromPosition >= 0 && toPosition != fromPosition)
                moveItem(fromPosition, toPosition);
        }
    }

    private void applyAndAnimateAddition(List<File> newImageFile, List<File> newLabel)
    {
        for (int i = 0; i < newLabel.size(); i++)
        {
            if (!contains(strings_of_files, newLabel.get(i).getName()))
            {
                addItem(newImageFile.get(i), newLabel.get(i));
            }
        }
    }

    private void applyAndAnimateRemovals(List<File> newImageFile, List<File> newLabel)
    {
        for (int i = strings_of_files.size() - 1; i >= 0; i--)
        {
            if (!contains(newLabel, strings_of_files.get(i).getName()))
            {
                removeItem(i);
            }
        }
    }

    private boolean contains(List<File> fileList, String label)
    {
        for (int i = 0; i < fileList.size(); i++)
        {
            if (fileList.get(i).getName().equals(label))
                return true;
        }
        return false;
    }

    private int indexOf(List<File> newList, String label)
    {
        for (int i = 0; i < newList.size(); i++)
        {
            if (newList.get(i).getName().equals(label))
                return i;
        }
        return -99;
    }


    @Override
    public int getItemCount()
    {
        return image_files == null ? 0 : image_files.size();
    }

    public void addToDatabase()
    {
        DatabaseAdapter databaseAdapter = new DatabaseAdapter(context);
        databaseAdapter.displayAll();
    }

    public void setNumberReference(TextView numberReference)
    {
        this.numberReference = numberReference;
    }

    public void getListItems(MainActivity mainActivity, int requestCode)
    {
        List<File> images_delete = new ArrayList<>();
        List<File> labels_delete = new ArrayList<>();
        int selectin = -1;
        for (int i = 0; i < selection_list.size(); i++)
        {
            selectin = selection_list.get(i);
            images_delete.add(image_files.get(selectin));
            labels_delete.add(strings_of_files.get(selectin));
        }
        if (requestCode == 0)
            mainActivity.receiverOfSelection(images_delete, labels_delete);
        else mainActivity.sharing(images_delete);
    }

    class CardAdapterHolder extends RecyclerView.ViewHolder
    {
        private TableLayout tableLayout;
        private ImageView imageView;
        private LoadingText loadingText;
        private int position;
        private FrameLayout frameLayout;
        private ImageView imageSelection;

        public CardAdapterHolder(View itemView)
        {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_view_card_view);
            tableLayout = (TableLayout) itemView.findViewById(R.id.tableLayout);
            frameLayout = (FrameLayout) itemView.findViewById(R.id.selection_scrim);
            imageSelection = (ImageView) itemView.findViewById(R.id.selection_done);
            position = -1;
        }


        public ImageView getImageView()
        {
            return imageView;
        }

        public TableLayout getTableLayout()
        {
            return tableLayout;
        }

        public LoadingText getLoadingText()
        {
            return loadingText;
        }

        public void setLoadingText(LoadingText loadingText, int position)
        {
            this.loadingText = loadingText;
            this.position = position;
        }

        public void highlight()
        {
            frameLayout.setVisibility(View.VISIBLE);
            imageSelection.setVisibility(View.VISIBLE);
        }

        public void deHighlight()
        {
            frameLayout.setVisibility(View.INVISIBLE);
            imageSelection.setVisibility(View.INVISIBLE);
        }

        public int Position()
        {
            return position;
        }
    }

    public void removeAllSelection()
    {
        selection_list.clear();
        longClicked = false;
        for (int i = 0; i < holders.size(); i++)
        {
            holders.get(i).deHighlight();
        }
    }

    public boolean isLongClicked()
    {
        return longClicked;
    }

    public List<Integer> getSelection_list()
    {
        return selection_list;
    }
}

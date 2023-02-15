package com.example.multitable;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

//custom adapter for recyclerview
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
     Context context;
    ArrayList<ContactModel> contactArr;
    MyDBHelper myDBHelper;
    //constructor for adapter to get context and array from main activity
    RecyclerViewAdapter(Context context, ArrayList<ContactModel> arrayList){
        this.context = context;
        this.contactArr = arrayList;
    }

    //setting search results to contact array
    public void setFilteredList(ArrayList<ContactModel> FilteredList){
        this.contactArr = FilteredList;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    //used to get view and pass to view holder
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //from integer resource path returns view
        //viewGroup = recycler view layout(to which we want to attach view)
        View view = LayoutInflater.from(context).inflate(R.layout.contact_row,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //used to get position(index) and perform action on particular view using index
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        //finding variables of array and attaching to view
        holder.imgContact.setImageResource(R.drawable.profile);
        holder.txtName.setText(contactArr.get(position).name);
        holder.txtNumber.setText(contactArr.get(position).number);
        myDBHelper = new MyDBHelper(context);

        //Animation for view
        holder.cardView.setAnimation(AnimationUtils.loadAnimation(context,R.anim.anim_one));

        //Perform operations when click on single view
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //to open dialog box when long clicked on view
                Dialog delUpDialog = new Dialog(context);
                deleteUpdate(delUpDialog,position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactArr.size();
    }


    //view holder used to hold view of recycler view
    //In this we can find id's through view
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        CardView cardView;
        TextView txtName,txtNumber;
        ImageView imgContact;

        //constructor of viewholder class
        public ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            txtName = itemView.findViewById(R.id.contactNameTxt);
            txtNumber = itemView.findViewById(R.id.contactNumberTxt);
            imgContact = itemView.findViewById(R.id.contactImg);
            cardView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(getAdapterPosition(),101,0,"Update");
            menu.add(getAdapterPosition(),102,1,"Delete");

        }
    }

    public void deleteUpdate(Dialog delUpDialog,int position){
        delUpDialog.setContentView(R.layout.add_update_dialog);
        TextView update = delUpDialog.findViewById(R.id.update);
        TextView delete = delUpDialog.findViewById(R.id.delete);
        TextView txtNumber = delUpDialog.findViewById(R.id.txtNumber);
        txtNumber.setText(contactArr.get(position).number);

        //perform operation when clicked on update
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new dialog box for update
                Dialog updateDialog = new Dialog(context);
                updateDialog.setContentView(R.layout.add_update);
                TextView txtTitle = updateDialog.findViewById(R.id.txtTitle);
                EditText editName = updateDialog.findViewById(R.id.editName);
                EditText editNumber = updateDialog.findViewById(R.id.editNumber);
                Button btnAction = updateDialog.findViewById(R.id.btnAction);

                txtTitle.setText("Update Contact");
                btnAction.setText("Update");

                //fetch data from existing array(view) to dialog box
                editName.setText(contactArr.get(position).name);
                editNumber.setText(contactArr.get(position).number);
                //perform action when clicked on button of dialog box
                btnAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = "";
                        String number = "";
                        if (editName.getText().toString().equals("") || editNumber.getText().toString().equals("")){
                            Toast.makeText(context, "Please enter Details ", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            //updating array
                            name = editName.getText().toString();
                            number = editNumber.getText().toString();
                           // contactArr.set(position,new ContactModel(name,number));
                            myDBHelper.updateContact(name,number,position+1);
                            notifyItemChanged(position);
                            updateDialog.dismiss();
                            delUpDialog.dismiss();
                        }
                    }
                });
                updateDialog.show();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Dialog box for delete
                AlertDialog.Builder alertDeleteDialog = new AlertDialog.Builder(context)
                        .setTitle("Delete Contact")
                        .setMessage("Are you sure want to delete")
                        .setIcon(R.drawable.deletealert)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                myDBHelper.deleteContact(position+1);
                                notifyItemRemoved(position+1);
                                delUpDialog.dismiss();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                delUpDialog.dismiss();
                            }
                        });
                alertDeleteDialog.show();
            }
        });
        delUpDialog.show();
    }

}

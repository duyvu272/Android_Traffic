package dav.com.tracfficchecking.adapter;

import android.content.Context;
import android.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import dav.com.tracfficchecking.R;
import dav.com.tracfficchecking.model.UserListRoad;

public class RecyclerViewAdapterListRoad extends RecyclerView.Adapter<RecyclerViewAdapterListRoad.ViewHolder> {
    Context context;
    List<UserListRoad> listRoads;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mDatabase;

    public RecyclerViewAdapterListRoad(Context context, List<UserListRoad> TempList) {

        this.listRoads = TempList;

        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_road_items, parent, false);


        return new ViewHolder(view);
    }




    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final UserListRoad userListRoad = listRoads.get(position);
        holder.address.setText(userListRoad.getFullAddress());
        mDatabase = FirebaseDatabase.getInstance().getReference("User_List_Road").child(user.getUid());

        final Button button = holder.buttonMenu;
        holder.buttonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, "hello", Toast.LENGTH_SHORT).show();
                //creating a popup menu
               PopupMenu popup = new PopupMenu(context, button);

                //inflating menu from xml resource
                popup.getMenuInflater().inflate(R.menu.listroad_option_menu,popup.getMenu());

                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu1:
                                //handle menu1 click
                                break;
                            case R.id.menu2:
                                //handle menu2 click
                                mDatabase.child(userListRoad.getKeyItem()).removeValue();

                                listRoads.remove(holder.getAdapterPosition());
                                notifyItemRemoved(holder.getAdapterPosition());
                                return true;
                        }
                        return false;
                    }
                });
                //displaying the popup
                popup.show();
            }
        });

    }



    @Override
    public int getItemCount() {

        return listRoads.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView address;
       public Button buttonMenu;

        public ViewHolder(View itemView) {
            super(itemView);

            address = itemView.findViewById(R.id.tv_address);
            buttonMenu = itemView.findViewById(R.id.btn_option_menu);


        }
    }

}

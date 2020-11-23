package dav.com.tracfficchecking.adapter;

import android.content.Context;


import android.content.Intent;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;


import dav.com.tracfficchecking.R;

import dav.com.tracfficchecking.activity.EditPost;
import dav.com.tracfficchecking.model.UserPostOnCommunication;

public class RecyclerViewAdapterUserPost extends RecyclerView.Adapter<RecyclerViewAdapterUserPost.ViewHolder> {
    private static HashMap<String,Integer> likeOrdislike  = new HashMap<>();
    private HashMap<String,Object> numberReport = new HashMap<>();
    private Context context;
    private List<UserPostOnCommunication> userReportsList;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference databaseReference;
    private DatabaseReference dataRef;
    private int num;
    int temp;

    public RecyclerViewAdapterUserPost(Context context, List<UserPostOnCommunication> TempList) {

        this.userReportsList = TempList;

        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    int count = 0;
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final UserPostOnCommunication userReport = userReportsList.get(position);
        databaseReference = FirebaseDatabase.getInstance().getReference("User_Post").child(user.getUid());
        dataRef = FirebaseDatabase.getInstance().getReference("User_Post");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                       // Toast.makeText(context, "key"+userReport.getKeyUserPost(), Toast.LENGTH_SHORT).show();
                       if(postSnapshot.getKey().equals(userReport.getKeyUserPost())) {
                           holder.btnMenu.setVisibility(View.VISIBLE);
                           holder.report.setVisibility(View.GONE);
                           holder.numReport.setVisibility(View.GONE);
                            //count = count+1;
                       }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //Toast.makeText(context, "Count"+count, Toast.LENGTH_SHORT).show();
        holder.profileName.setText(userReport.getEmailUser());
        holder.locationView.setText(userReport.getLocationUserUp());
        holder.sttView.setText(userReport.getContent());
        String numString = Integer.toString(userReport.getReport());
        holder.numReport.setText(numString);
        //holder.numReport.setText(userReport.getReport());
        //Toast.makeText(context, "Time : " +userReport.getDate(), Toast.LENGTH_SHORT).show();

        likeOrdislike.put(userReport.getKeyUserPost(),num);

        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                Long.parseLong(userReport.getDate()),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);

        holder.timeStamp.setText(timeAgo);
        //Loading image from Glide library.
        Glide.with(context).load(userReport.getImageUrl()).into(holder.imageView);
        //event when user click on menu button
        holder.btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //creating a popup menu
                PopupMenu popup = new PopupMenu(context, holder.btnMenu);

                //inflating menu from xml resource
                popup.getMenuInflater().inflate(R.menu.listroad_option_menu,popup.getMenu());

                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu1:
                                Intent intent = new Intent(context,EditPost.class);
                                String content = userReport.getContent();
                                intent.putExtra("content",content);
                                intent.putExtra("idPost",userReport.getKeyUserPost());
                                intent.putExtra("UserId",userReport.getRootKey());
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);

                                break;
                            case R.id.menu2:
                                //handle menu2 click
                                dataRef.child(userReport.getRootKey()).child(userReport.getKeyUserPost()).removeValue();
                                userReportsList.remove(holder.getAdapterPosition());
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


    holder.report.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

           if (likeOrdislike.get(userReport.getKeyUserPost()) == 0) {
               numberReport.put("report", userReport.getReport() + 1);
               dataRef.child(userReport.getRootKey()).child(userReport.getKeyUserPost()).updateChildren(numberReport);
               likeOrdislike.put(userReport.getKeyUserPost(), 1);
               notifyItemChanged(position);
           } else {
               numberReport.put("report", userReport.getReport() - 1);
               dataRef.child(userReport.getRootKey()).child(userReport.getKeyUserPost()).updateChildren(numberReport);
               likeOrdislike.put(userReport.getKeyUserPost(), 0);
               notifyItemChanged(position, numberReport);

           }

        }
    });

    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {

        return userReportsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder  {

        private ImageView imageView;
        private TextView locationView;
        private TextView sttView;
        private TextView timeStamp;
        private TextView profileName;
        private  Button btnMenu;
        private ImageView report;
        private  TextView numReport;

        private ViewHolder(View itemView) {
            super(itemView);
            profileName =  itemView.findViewById(R.id.tv_name_profile);
            locationView = itemView.findViewById(R.id.tv_location);
            imageView = itemView.findViewById(R.id.iv_imgshow);
            sttView = itemView.findViewById(R.id.tv_statusMsg);
            timeStamp = itemView.findViewById(R.id.tv_timestamp);
            btnMenu = itemView.findViewById(R.id.btn_option_post_menu);
            report = itemView.findViewById(R.id.iv_report);
            btnMenu.setVisibility(View.GONE);
            numReport = itemView.findViewById(R.id.tv_report);

        }


    }
}

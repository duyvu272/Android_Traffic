package capstone2.test5.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import capstone2.test5.R;
import capstone2.test5.model.UserReport;

/**
 * Created by Administrator on 09/16/17.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<capstone2.test5.adapter.RecyclerViewAdapter.ViewHolder> {

    Context context;
    List<UserReport> userReportsList;

    public RecyclerViewAdapter(Context context, List<UserReport> TempList) {

        this.userReportsList = TempList;

        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_acivity, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserReport userReport = userReportsList.get(position);

        holder.emailView.setText(userReport.getEmailPost());
        holder.locationView.setText(userReport.getTxrLocation());
        holder.commentView.setText(userReport.getTxtComment());


        //Loading image from Glide library.
        Glide.with(context).load(userReport.getImageURL()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {

        return userReportsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView locationView;
        public TextView commentView;
        public TextView emailView;

        public ViewHolder(View itemView) {
            super(itemView);
            emailView = (TextView) itemView.findViewById(R.id.tv_emailPost);
            locationView =(TextView) itemView.findViewById(R.id.tv_locationPost);
            imageView =(ImageView) itemView.findViewById(R.id.iv_image);
            commentView = (TextView)itemView.findViewById(R.id.tv_comment);
        }
    }
}

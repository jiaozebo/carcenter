
package com.harbinpointech.carcenter.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.data.Message;
import com.harbinpointech.carcenter.utils.SmileUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageAdapter extends CursorAdapter {

    private final static String TAG = "msg";

    private static final int MESSAGE_TYPE_RECV_TXT = 0;
    private static final int MESSAGE_TYPE_SENT_TXT = 1;
    private static final int MESSAGE_TYPE_SENT_IMAGE = 2;
    private static final int MESSAGE_TYPE_SENT_LOCATION = 3;
    private static final int MESSAGE_TYPE_RECV_LOCATION = 4;
    private static final int MESSAGE_TYPE_RECV_IMAGE = 5;
    private static final int MESSAGE_TYPE_SENT_VOICE = 6;
    private static final int MESSAGE_TYPE_RECV_VOICE = 7;
    private static final int MESSAGE_TYPE_SENT_VIDEO = 8;
    private static final int MESSAGE_TYPE_RECV_VIDEO = 9;
    private static final int MESSAGE_TYPE_SENT_FILE = 10;
    private static final int MESSAGE_TYPE_RECV_FILE = 11;

    public static final String IMAGE_DIR = "chat/image/";
    public static final String VOICE_DIR = "chat/audio/";
    public static final String VIDEO_DIR = "chat/video";
    private final int mOtherSideIndex;

    private String username;
    private LayoutInflater mInflater;
    private Activity activity;

    // reference to conversation object in chatsdk


    private Context context;

    public MessageAdapter(Context context, Cursor c, int otherSideIndex) {
        super(context, c, true);
        mOtherSideIndex = otherSideIndex;

        mInflater = LayoutInflater.from(context);
    }

    public int getItemViewType(int position) {
        Cursor c = (Cursor) getItem(position);
        if (c == null)
            return -1;// invalid
        if (c.getInt(c.getColumnIndex(Message.SENDER)) == mOtherSideIndex) {
            return MESSAGE_TYPE_RECV_TXT;
        } else if (c.getInt(c.getColumnIndex(Message.RECEIVER)) == mOtherSideIndex) {
            return MESSAGE_TYPE_SENT_TXT;
        }
        return -1;
    }

    public int getViewTypeCount() {
        return 2;
    }

    @SuppressLint("NewApi")
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Cursor c = (Cursor) getItem(position);
        final ViewHolder holder;
        int type = getItemViewType(position);
        if (convertView == null) {
            convertView = mInflater.inflate(type == MESSAGE_TYPE_RECV_FILE ? R.layout.row_received_message : R.layout.row_sent_message, parent, false);
            holder = new ViewHolder();
            holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
//            holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
            holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
            // 这里是文字内容
            holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
            holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String msg = c.getString(c.getColumnIndex(Message.CONTENT));
        Spannable span = SmileUtils.getSmiledText(context, msg);
        holder.tv.setText(span, BufferType.SPANNABLE);

        if (type == MESSAGE_TYPE_SENT_TXT) {
            holder.pb.setVisibility(c.getInt(c.getColumnIndex(Message.STATE)) == -1 ? View.VISIBLE : View.GONE);
//            holder.staus_iv.setVisibility(View.GONE);
        } else {
//            holder.tv_userId.setText();
            holder.tv_userId.setText(c.getString(c.getColumnIndex(Message.SENDER)));
        }

        TextView timestamp = (TextView) convertView.findViewById(R.id.timestamp);

        String date_time = c.getString(c.getColumnIndex(Message.DATETIME));
        timestamp.setText(date_time);
        timestamp.setVisibility(View.VISIBLE);

        if (position != 0) {
            //两条消息时间离得如果稍长，显示时间
            Cursor prev = (Cursor) getItem(position - 1);
            String prev_date_time = prev.getString(prev.getColumnIndex(Message.DATETIME));
            try {
                if (closeEnough(date_time, prev_date_time)) {
                    timestamp.setVisibility(View.GONE);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return convertView;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }

    private static boolean closeEnough(String datetime1, String datetime2) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = sdf.parse(datetime1);
        Date d2 = sdf.parse(datetime2);
        return Math.abs(d1.getTime() - d2.getTime()) < 300000;
    }


    public static class ViewHolder {
        ImageView iv;
        TextView tv;
        ProgressBar pb;
        ImageView staus_iv;
        ImageView head_iv;
        TextView tv_userId;
        ImageView playBtn;
        TextView timeLength;
        TextView size;
        LinearLayout container_status_btn;
        LinearLayout ll_container;
        ImageView iv_read_status;
        TextView tv_ack;
        TextView tv_file_name;
        TextView tv_file_size;
        TextView tv_file_download_state;
    }

}
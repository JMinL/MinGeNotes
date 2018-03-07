package com.example.mingenotes;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ShowRecord extends Activity {

	private String audioPath;
	private int isPlaying = 0;
	private AnimationDrawable ad_left,ad_right;
	private Timer mTimer;
	//������������
	private MediaPlayer mPlayer = null;
	private ImageView iv_record_wave_left,iv_record_wave_right,iv_microphone;
	private TextView tv_recordTime;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_show_record);
		//���ñ���
		TextView tv_title = (TextView)findViewById(R.id.tv_title);
		tv_title.setText("�鿴¼��");
		//���ذ�ť
		Button bt_back = (Button)findViewById(R.id.bt_back);
		bt_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (isPlaying == 1) {
					mPlayer.stop();
					mPlayer.release();
				}
				ShowRecord.this.finish();
			}
		});
		Button bt_del = (Button)findViewById(R.id.bt_save);
		bt_del.setVisibility(View.GONE);

		Intent intent = this.getIntent();
		audioPath = intent.getStringExtra("audioPath");
        System.out.print("==================="+audioPath);
		iv_microphone = (ImageView)findViewById(R.id.iv_microphone);
		iv_microphone.setOnClickListener(new ClickEvent());

		iv_record_wave_left = (ImageView)findViewById(R.id.iv_record_wave_left);
		iv_record_wave_right = (ImageView)findViewById(R.id.iv_record_wave_right);
        //��ද��
		ad_left = (AnimationDrawable)iv_record_wave_left.getBackground();
		//�Ҳද��
		ad_right = (AnimationDrawable)iv_record_wave_right.getBackground();
		tv_recordTime = (TextView)findViewById(R.id.tv_recordTime);
	}

	final Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch(msg.what){
				case 1 :
					String time[] = tv_recordTime.getText().toString().split(":");
					int hour = Integer.parseInt(time[0]);
					int minute = Integer.parseInt(time[1]);
					int second = Integer.parseInt(time[2]);

					if(second < 59){
						second++;
					}
					else if(second == 59 && minute < 59){
						minute++;
						second = 0;

					}
					if(second == 59 && minute == 59 && hour < 98){
						hour++;
						minute = 0;
						second = 0;
					}
					time[0] = hour + "";
					time[1] = minute + "";
					time[2] = second + "";
					//������ʽ��ʾ����Ļ��
					if(second < 10)
						time[2] = "0" + second;
					if(minute < 10)
						time[1] = "0" + minute;
					if(hour < 10)
						time[0] = "0" + hour;

					//��ʾ��TextView��
					tv_recordTime.setText(time[0]+":"+time[1]+":"+time[2]);

					break;
			}
		}
	};

	class ClickEvent implements OnClickListener{
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			//����
			if(isPlaying == 0){
				isPlaying = 1;
				mPlayer = new MediaPlayer();
				tv_recordTime.setText("00:00:00");
				mTimer = new Timer();
				mPlayer.setOnCompletionListener(new MediaCompletion());
				try {
					mPlayer.setDataSource(audioPath);
					mPlayer.prepare();
					mPlayer.start();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mTimer.schedule(new TimerTask() {

					@Override
					public void run() {
						Message message = new Message();
						message.what = 1;
						handler.sendMessage(message);
					}
				}, 1000,1000);
				//���Ŷ���
				ad_left.start();
				ad_right.start();
			}
			else{//��������
				isPlaying = 0;
				mPlayer.stop();
				mPlayer.release();
				mPlayer = null;
				mTimer.cancel();
				mTimer = null;
				//ֹͣ����
				ad_left.stop();
				ad_right.stop();
			}
		}
	}

	class MediaCompletion implements OnCompletionListener{
		@Override
		public void onCompletion(MediaPlayer mp) {
			mTimer.cancel();
			mTimer = null;
			isPlaying = 0;
			//ֹͣ����
			ad_left.stop();
			ad_right.stop();
			Toast.makeText(ShowRecord.this, "�������", Toast.LENGTH_SHORT).show();
			tv_recordTime.setText("00:00:00");
		}
	}
}

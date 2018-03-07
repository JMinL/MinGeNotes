package com.example.mingenotes;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
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

@SuppressLint("HandlerLeak") 
public class ActivityRecord extends Activity {
	private Button btn_record;
	private ImageView iv_microphone;
	private TextView tv_recordTime;
	private ImageView iv_record_wave_left, iv_record_wave_right;
	private AnimationDrawable ad_left, ad_right;
	private int isRecording = 0;
	private int isPlaying = 0;
	private Timer mTimer;//��ʱ��
	// ������������
	private MediaPlayer mPlayer = null;
	private MediaRecorder mRecorder = null;
	// ��������·��
	private String FilePath = null;
	private String newtimes="0:0:0";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_record);
		TextView title = (TextView) findViewById(R.id.tv_title);
		title.setText("¼��");

		Button btn_save = (Button) findViewById(R.id.bt_save);
		btn_save.setOnClickListener(new ClickEvent());
		Button btn_back = (Button) findViewById(R.id.bt_back);
		btn_back.setOnClickListener(new ClickEvent());

		btn_record = (Button) findViewById(R.id.btn_record);
		btn_record.setOnClickListener(new ClickEvent());

		iv_microphone = (ImageView) findViewById(R.id.iv_microphone);
		iv_microphone.setOnClickListener(new ClickEvent());

		iv_record_wave_left = (ImageView) findViewById(R.id.iv_record_wave_left);
		iv_record_wave_right = (ImageView) findViewById(R.id.iv_record_wave_right);

		ad_left = (AnimationDrawable) iv_record_wave_left.getBackground();
		ad_right = (AnimationDrawable) iv_record_wave_right.getBackground();

		tv_recordTime = (TextView) findViewById(R.id.tv_recordTime);
	}

	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1:
					String time[] = tv_recordTime.getText().toString().split(":");
					int hour = Integer.parseInt(time[0]);
					int minute = Integer.parseInt(time[1]);
					int second = Integer.parseInt(time[2]);
					if (second < 59) {
						second++;
					} else if (second == 59 && minute < 59) {
						minute++;
						second = 0;
					}
					if (second == 59 && minute == 59 && hour < 98) {
						hour++;
						minute = 0;
						second = 0;
					}
					time[0] = hour + "";
					time[1] = minute + "";
					time[2] = second + "";
					// ������ʽ��ʾ����Ļ��
					if (second < 10)
						time[2] = "0" + second;
					if (minute < 10)
						time[1] = "0" + minute;
					if (hour < 10)
						time[0] = "0" + hour;
					newtimes=time[0] + ":" + time[1] + ":" + time[2];
					// ��ʾ��TextView��
					tv_recordTime.setText(time[0] + ":" + time[1] + ":" + time[2]);
					break;
			}
		}
	};

	class ClickEvent implements OnClickListener {

		@SuppressLint("SimpleDateFormat")
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				// ������ǿ�ʼ¼����ť
				case R.id.btn_record:
					// ��ʼ¼��
					if (isRecording == 0) {
						// ÿһ�ε���¼��������¼����Σ���������Ϊ�������ֻ�����һ�ε�¼���ļ����棬������ɾ��
						if (FilePath != null) {
							File oldFile = new File(FilePath);
							oldFile.delete();
						}
						// ���ϵͳ��ǰʱ�䣬���Ը�ʱ����Ϊ�ļ���
						SimpleDateFormat formatter = new SimpleDateFormat(
								"yyyyMMddHHmmss");
						Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
						String str = formatter.format(curDate);//��String��ʽ��������
						str = str + "record.amr";//�ļ���
						File dir = new File("/sdcard/notes/");//�����ļ���
						File file = new File("/sdcard/notes/", str);//�����ļ�
						if (!dir.exists()) {//�ж��ļ����Ƿ񴴽��ɹ�
							dir.mkdir();//�����ļ���
						} else {
							if (file.exists()) {//�ж��ļ��Ƿ񴴽�
								file.delete();//ɾ���ļ�
							}
						}
						//�ļ�·��
						FilePath = dir.getPath() + "/" + str;
						// ��ʱ��
						mTimer = new Timer();
						// �����ͼ�����óɲ��ɵ����
						iv_microphone.setClickable(false);
						// ����ʾ��ʱ������Ϊ00:00:00
						tv_recordTime.setText("00:00:00");
						// ����ť����ֹͣ¼��
						isRecording = 1;
						btn_record
								.setBackgroundResource(R.drawable.tabbar_record_stop);
						//������Ƶ/��Ƶ��
						mRecorder = new MediaRecorder();
						mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//��������¼��
						//���ñ����ʽ
						mRecorder
								.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
						mRecorder.setOutputFile(FilePath);//���ñ���·��
						mRecorder
								.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);//���ñ����ʽ
						try {
							//׼��¼��
							mRecorder.prepare();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//��ʼ¼��
						mRecorder.start();
						mTimer.schedule(new TimerTask() {
							@Override
							public void run() {
								Message message = new Message();
								message.what = 1;
								handler.sendMessage(message);
							}
						}, 1000, 1000);
						// ���Ŷ���
						ad_left.start();
						ad_right.start();
					}
					// ֹͣ¼��
					else {
						// ����ť���ɿ�ʼ¼��
						isRecording = 0;
						btn_record
								.setBackgroundResource(R.drawable.tabbar_record_start);
						mRecorder.stop();
						mTimer.cancel();
						mTimer = null;
						mRecorder.release();
						mRecorder = null;
						// �����ͼ�����óɿɵ����
						iv_microphone.setClickable(true);
						// ֹͣ����
						ad_left.stop();
						ad_right.stop();
						Toast.makeText(ActivityRecord.this, "�������ͼ���������ٴε����������",
								Toast.LENGTH_LONG).show();
					}
					break;
				// ��������������ͼ�꣬������ǽ�������ģʽ���ٴε����ֹͣ����
				case R.id.iv_microphone:
					if (FilePath == null)
						Toast.makeText(ActivityRecord.this, "û��¼���㲥���Բ��ţ�����¼��",
								Toast.LENGTH_LONG).show();
					else {
						// ����
						if (isPlaying == 0) {
							isPlaying = 1;
							mPlayer = new MediaPlayer();
							tv_recordTime.setText("00:00:00");
							mTimer = new Timer();
							mPlayer.setOnCompletionListener(new MediaCompletion());
							try {
								mPlayer.setDataSource(FilePath);
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
							}, 1000, 1000);
							// ���Ŷ���
							ad_left.start();
							ad_right.start();
						}
						// ��������
						else {
							isPlaying = 0;
							mPlayer.stop();
							mPlayer.release();
							mPlayer = null;
							mTimer.cancel();
							mTimer = null;
							// ֹͣ����
							ad_left.stop();
							ad_right.stop();
						}
					}
					break;

				// ������水ť
				case R.id.bt_save:
					// �����յ�¼���ļ���·�����ص������ռ�ҳ��
					if (FilePath == null) {
						Toast.makeText(ActivityRecord.this, "û��¼�����Ա��棬����¼��",
								Toast.LENGTH_LONG).show();
					} else {
						//������ͼ
						Intent intent = getIntent();
						//��Ϣ�Ĵ���
						Bundle b = new Bundle();
						//�����ļ�·��
						b.putString("audio", FilePath);
						//������Ƶʱ��
						b.putString("time", tv_recordTime.getText().toString());
						//��ʼ����
						intent.putExtras(b);
						//���ݸ���������ǰҳ�����
						setResult(RESULT_OK, intent);
					}
					//�رյ�ǰ��
					ActivityRecord.this.finish();
					break;
				case R.id.bt_back:
					// ����ǰ��¼�����ļ�ɾ��
					if (FilePath != null) {
						File oldFile = new File(FilePath);
						oldFile.delete();
					}
					ActivityRecord.this.finish();
					break;

			}
		}

	}

	class MediaCompletion implements OnCompletionListener {
		@Override
		public void onCompletion(MediaPlayer mp) {
			mTimer.cancel();
			mTimer = null;
			isPlaying = 0;
			// ֹͣ����
			ad_left.stop();
			ad_right.stop();
			Toast.makeText(ActivityRecord.this, "�������", Toast.LENGTH_LONG)
					.show();
			tv_recordTime.setText(newtimes);
		}
	}
}

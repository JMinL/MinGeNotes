package com.example.mingenotes;

import com.example.mingenotes.R;
import com.example.mingenotes.adapter.MainAdapter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.example.mingenotes.bean.SQLBean;
import com.example.mingenotes.data.CallAlarm;
import com.example.mingenotes.db.DatabaseOperation;
import com.example.mingenotes.doubledatepicker.DoubleDatePickerDialog;
import com.example.mingenotes.view.MyGridView;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity{
	private Button bt_add;// ��Ӱ�ť
	private SQLiteDatabase db;//���ݿ����
	private DatabaseOperation dop;//�Զ������ݿ���
	private MyGridView lv_notes;// �Զ�����Ϣ�б�
	private TextView tv_note_id, tv_locktype, tv_lock;
	public static MediaPlayer mediaPlayer;// ���ֲ�����
	public static Vibrator vibrator;//�ֻ�����
	public AlarmManager am;// ��Ϣ������
	public EditText et_keyword;// ������
	public MainAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		bt_add = (Button) findViewById(R.id.bt_add);
		bt_add.setOnClickListener(new ClickEvent());
		et_keyword = (EditText) findViewById(R.id.et_keyword);
		// ���ݿ����
		dop = new DatabaseOperation(this, db);
		lv_notes = (MyGridView) findViewById(R.id.lv_notes);
		if (am == null) {
			am = (AlarmManager) getSystemService(ALARM_SERVICE);
		}
		try {
			Intent intent = new Intent(MainActivity.this, CallAlarm.class);
			PendingIntent sender = PendingIntent.getBroadcast(
					MainActivity.this, 0, intent, 0);
			am.setRepeating(AlarmManager.RTC_WAKEUP, 0, 60 * 1000, sender);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// ��ʾ�����б�
		showNotesList();
		// Ϊ�����б���Ӽ�����
		lv_notes.setOnItemClickListener(new ItemClickEvent());
		// Ϊ�����б���ӳ����¼�
		lv_notes.setOnItemLongClickListener(new ItemLongClickEvent());
	}
	// ��ʾ�����б�
	private void showNotesList() {
		// ����������ݿ� ��ȡ����
		dop.create_db();
		//��ȡ���ݿ�����
		Cursor cursor = dop.query_db();
		if (cursor.getCount() > 0) {
			List<SQLBean> list = new ArrayList<SQLBean>();//�ռ���Ϣ������
			while (cursor.moveToNext()) {// ����ƶ��ɹ�
				// ������ȡ��
				SQLBean bean = new SQLBean();//�������ݿ�ʵ����
				//�����ռ���Ϣid��ʵ����
				bean.set_id("" + cursor.getInt(cursor.getColumnIndex("_id")));
				bean.setContext(cursor.getString(cursor
						.getColumnIndex("context")));//�����ռ����ݵ�ʵ����
				//�����ռǱ��⵽ʵ����
				bean.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				//�����ռǼ�¼ʱ�䵽ʵ����
				bean.setTime(cursor.getString(cursor.getColumnIndex("time")));
				bean.setDatatype(cursor.getString(cursor
						.getColumnIndex("datatype")));//�����ռ��Ƿ���������ʱ�䵽ʵ����
				bean.setDatatime(cursor.getString(cursor
						.getColumnIndex("datatime")));//�����ռ�����ʱ�䵽ʵ����
				bean.setLocktype(cursor.getString(cursor
						.getColumnIndex("locktype")));//�����ռ��Ƿ��������ռ�����ʵ����
				//�����ռ������ܵ�ʵ����
				bean.setLock(cursor.getString(cursor.getColumnIndex("lock")));
				list.add(bean);//�ѱ����ռ���Ϣʵ���ౣ�浽�ռ���Ϣ������
			}
			//������ʾ����
			Collections.reverse(list);
			adapter = new MainAdapter(list, this);//װ���ռ���Ϣ����ҳ
			lv_notes.setAdapter(adapter);//�ռ��б������ռ���Ϣ������
		}
		dop.close_db();//�ر����ݿ�
	}
	// �����б���������
	class ItemLongClickEvent implements OnItemLongClickListener {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
									   int position, long id) {
			//��ʼ���ռ�id����ؼ�
			tv_note_id = (TextView) view.findViewById(R.id.tv_note_id);
			//��ʼ���Ƿ�����ռ�������ؼ�
			tv_locktype = (TextView) view.findViewById(R.id.tv_locktype);
			//��ʼ���ռ������ܱ�����Ϣ
			tv_lock = (TextView) view.findViewById(R.id.tv_lock);
			//��ȡ�ؼ����Ƿ������ռ�����Ϣ
			String locktype = tv_locktype.getText().toString();
			//��ȡ�ؼ����ռ�������Ϣ
			String lock = tv_lock.getText().toString();
			//��ȡ�ؼ���id��Ϣת����int����
			int item_id = Integer.parseInt(tv_note_id.getText().toString());
			//����ѡ������򷽷�
			simpleList(item_id, locktype, lock);
			return true;
		}
	}
	// ���б�Ի�������ѡ�����
	public void simpleList(final int item_id, final String locktype,
						   final String lock) {
		//ʵ����AlertDialog
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,
				R.style.custom_dialog);
		//���õ�������
		alertDialogBuilder.setTitle("ѡ�����");
		//���õ���ͼƬ
		alertDialogBuilder.setIcon(R.drawable.icon);
		//���õ���ѡ������
		alertDialogBuilder.setItems(R.array.itemOperation,
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							// �༭
							case 0:
								if ("0".equals(locktype)) {//�ж��Ƿ������������0û��
									Intent intent = new Intent(MainActivity.this,
											AddActivity.class);//��ת������ռ�ҳ
									intent.putExtra("editModel", "update");//���ݱ༭��Ϣ
									intent.putExtra("noteId", item_id);//����id��Ϣ
									startActivity(intent);//��ʼ��ת
								} else {//��������
									// �������������
									inputTitleDialog(lock, 0, item_id);
								}
								break;
							// ɾ��
							case 1:
								if ("0".equals(locktype)) {// �ж��Ƿ��Ǽ����ռ� 0û��
									dop.create_db();// �����ݿ�
									dop.delete_db(item_id);//ɾ������
									dop.close_db();// �ر����ݿ�
									// ˢ���б���ʾ
									lv_notes.invalidate();
									showNotesList();
								} else {//��������
									// �������������
									inputTitleDialog(lock, 1, item_id);
									// ˢ���б���ʾ
									lv_notes.invalidate();
									//��ʾ�ռ��б���Ϣ
									showNotesList();
								}
								break;
						}
					}
				});
		alertDialogBuilder.create();//���쵯��
		alertDialogBuilder.show();//��ʾ����
	}
	// �����б���������
	class ItemClickEvent implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
								long id) {
			tv_note_id = (TextView) view.findViewById(R.id.tv_note_id);
			tv_locktype = (TextView) view.findViewById(R.id.tv_locktype);
			tv_lock = (TextView) view.findViewById(R.id.tv_lock);
			String locktype = tv_locktype.getText().toString();
			String lock = tv_lock.getText().toString();
			int item_id = Integer.parseInt(tv_note_id.getText().toString());
			if ("0".equals(locktype)) {
				Intent intent = new Intent(MainActivity.this, AddActivity.class);
				intent.putExtra("editModel", "update");
				intent.putExtra("noteId", item_id);
				startActivity(intent);
			} else {
				inputTitleDialog(lock, 0, item_id);
			}
		}
	}
	// �����ռǴ򿪵��������������
	public void inputTitleDialog(final String lock, final int idtype,
								 final int item_id) {// ���������
		final EditText inputServer = new EditText(this);
		inputServer.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		inputServer.setFocusable(true);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("����������").setView(inputServer)
				.setNegativeButton("ȡ��", null);
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				String inputName = inputServer.getText().toString();
				if ("".equals(inputName)) {
					Toast.makeText(MainActivity.this, "���벻��Ϊ�����������룡",
							Toast.LENGTH_LONG).show();
				} else {
					if (inputName.equals(lock)) {
						if (0 == idtype) {
							Intent intent = new Intent(MainActivity.this,
									AddActivity.class);
							intent.putExtra("editModel", "update");
							intent.putExtra("noteId", item_id);
							startActivity(intent);
						} else if (1 == idtype) {
							dop.create_db();
							dop.delete_db(item_id);
							dop.close_db();
							// ˢ���б���ʾ
							lv_notes.invalidate();
							showNotesList();
						}
					} else {
						Toast.makeText(MainActivity.this, "���벻��ȷ��",
								Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		builder.show();
	}
	// ����¼�
	class ClickEvent implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				// ��Ӽ���
				case R.id.bt_add:
					Intent intent = new Intent(MainActivity.this, AddActivity.class);
					intent.putExtra("editModel", "newAdd");
					startActivity(intent);
			}
		}
	}
	// ��������
	public void onSearch(View v){
		//��ȡ�����ؼ���
		String ek = et_keyword.getText().toString();
		if ("".equals(ek)) {//�ж������ؼ����Ƿ�Ϊ��
			Toast.makeText(MainActivity.this, "������ؼ��ʣ�", Toast.LENGTH_LONG)
					.show();
		} else {//������Ϊ��
			//�����������ҳ
			Intent intent = new Intent(MainActivity.this, SearchActivity.class);
			intent.putExtra("keword", ek);//���ݹؼ���
			startActivity(intent);//��ʼ��ת
		}
	}
	// ���ڷ�Χ����
	public void onData(View v) {
		// ���һ��false��ʾ����ʾ���ڣ����Ҫ��ʾ���ڣ�������������true���߲�������
		Calendar c = Calendar.getInstance();
		new DoubleDatePickerDialog(MainActivity.this, 0,
				new DoubleDatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker startDatePicker,
										  int startYear, int startMonthOfYear,
										  int startDayOfMonth, DatePicker endDatePicker,
										  int endYear, int endMonthOfYear, int endDayOfMonth) {
						if (startYear < endYear || startYear == endYear
								&& startMonthOfYear <= endMonthOfYear) {
							int st = startMonthOfYear + 1;
							int et = endMonthOfYear + 1;
							Intent intent = new Intent(MainActivity.this,
									DataSearchActivity.class);
							// sql�ж� ��Ҫ���·�ǰ��0 ����sql����жϲ���ȷ��
							if (st < 10) {
								intent.putExtra("startData", startYear + "-0"
										+ st + "-" + "01");
							} else {
								intent.putExtra("startData", startYear + "-"
										+ st + "-" + "01");
							}
							if (et < 10) {
								intent.putExtra("endData", endYear + "-0" + et
										+ "-" + "30");
							} else {
								intent.putExtra("endData", endYear + "-" + et
										+ "-" + "30");
							}
							startActivity(intent);
						} else {
							Toast.makeText(MainActivity.this, "����ѡ�����������ѡ��",
									Toast.LENGTH_LONG).show();
						}
					}
				}, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
				.get(Calendar.DATE), false).show();
	}
	
}

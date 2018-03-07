package com.example.mingenotes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.mingenotes.adapter.MainAdapter;
import com.example.mingenotes.bean.SQLBean;
import com.example.mingenotes.db.DatabaseOperation;
import com.example.mingenotes.view.MyGridView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends Activity {
	private SQLiteDatabase db;
	private DatabaseOperation dop;
	private MyGridView lv_notes;
	private TextView tv_note_id, tv_locktype, tv_lock;
	public EditText et_keyword;
	public String keword;
	Intent intent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search);
		et_keyword = (EditText) findViewById(R.id.et_keyword);
		intent = getIntent();//��ȡ��ǰ���ݶ���
		keword = intent.getStringExtra("keword");//��ȡ���ݲ���
		et_keyword.setText(keword);
		// ���ݿ����
		dop = new DatabaseOperation(this, db);
		lv_notes = (MyGridView) findViewById(R.id.lv_notes);
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
		// �����ݿ�
		dop.create_db();
		Cursor cursor = dop.query_db(keword);//ģ����ѯ
		if (cursor.getCount() > 0) {
			List<SQLBean> list = new ArrayList<SQLBean>();//ʵ���������б�
			while (cursor.moveToNext()) {// ����ƶ��ɹ�
				SQLBean bean = new SQLBean();//�������ݿ�ʵ����
				//�����ռ���Ϣid��ʵ����
				bean.set_id("" + cursor.getInt(cursor.getColumnIndex("_id")));
				//�����ռ����ݵ�ʵ����
				bean.setContext(cursor.getString(cursor
						.getColumnIndex("context")));
				//�����ռǱ��⵽ʵ����
				bean.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				//�����ռǼ�¼ʱ�䵽ʵ����
				bean.setTime(cursor.getString(cursor.getColumnIndex("time")));
				//�����ռ��Ƿ���������ʱ�䵽ʵ����
				bean.setDatatype(cursor.getString(cursor
						.getColumnIndex("datatype")));
				//�����ռ�����ʱ�䵽ʵ����
				bean.setDatatime(cursor.getString(cursor
						.getColumnIndex("datatime")));
				//�����ռ��Ƿ��������ռ�����ʵ����
				bean.setLocktype(cursor.getString(cursor
						.getColumnIndex("locktype")));
				//�����ռ������ܵ�ʵ����
				bean.setLock(cursor.getString(cursor.getColumnIndex("lock")));
				//�ѱ����ռ���Ϣʵ���ౣ�浽�ռ���Ϣ������
				list.add(bean);
			}
			//������ʾ����
			Collections.reverse(list);
			//װ���ռ���Ϣ����ҳ
			MainAdapter adapter = new MainAdapter(list, this);
			//�ռ��б������ռ���Ϣ������
			lv_notes.setAdapter(adapter);
		}else{
			Toast.makeText(SearchActivity.this, "���޼��£�",
					Toast.LENGTH_LONG).show();
		}
		//�ر����ݿ�
		dop.close_db();
	}

	// �����б���������
	class ItemLongClickEvent implements OnItemLongClickListener {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
									   int position, long id) {
			tv_note_id = (TextView) view.findViewById(R.id.tv_note_id);
			tv_locktype = (TextView) view.findViewById(R.id.tv_locktype);
			tv_lock = (TextView) view.findViewById(R.id.tv_lock);
			String locktype = tv_locktype.getText().toString();
			String lock = tv_lock.getText().toString();
			int item_id = Integer.parseInt(tv_note_id.getText().toString());
			simpleList(item_id, locktype, lock);
			return true;
		}
	}

	// ���б�Ի�������ѡ�����
	public void simpleList(final int item_id, final String locktype,
						   final String lock) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,
				R.style.custom_dialog);
		alertDialogBuilder.setTitle("ѡ�����");
		alertDialogBuilder.setIcon(R.drawable.icon);
		alertDialogBuilder.setItems(R.array.itemOperation,
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						switch (which) {
							// �༭
							case 0:
								if ("0".equals(locktype)) {
									Intent intent = new Intent(SearchActivity.this,
											AddActivity.class);
									intent.putExtra("editModel", "update");
									intent.putExtra("noteId", item_id);
									startActivity(intent);
								} else {
									inputTitleDialog(lock, 0, item_id);
								}
								break;
							// ɾ��
							case 1:
								if ("0".equals(locktype)) {
									dop.create_db();
									dop.delete_db(item_id);
									dop.close_db();
									// ˢ���б���ʾ
									lv_notes.invalidate();
									showNotesList();
								} else {
									inputTitleDialog(lock, 1, item_id);
									// ˢ���б���ʾ
									lv_notes.invalidate();
									showNotesList();
								}
								break;
						}
					}
				});
		alertDialogBuilder.create();
		alertDialogBuilder.show();
	}

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
					Toast.makeText(SearchActivity.this, "���벻��Ϊ�����������룡",
							Toast.LENGTH_LONG).show();
				} else {
					if (inputName.equals(lock)) {
						if (0 == idtype) {
							Intent intent = new Intent(SearchActivity.this,
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
						Toast.makeText(SearchActivity.this, "���벻��ȷ��",
								Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		builder.show();
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
				Intent intent = new Intent(SearchActivity.this,
						AddActivity.class);
				intent.putExtra("editModel", "update");
				intent.putExtra("noteId", item_id);
				startActivity(intent);
			} else {
				inputTitleDialog(lock, 0, item_id);
			}
		}
	}


	// ����
	public void onSearch(View v) {
		String ek = et_keyword.getText().toString();
		if ("".equals(ek)) {
			Toast.makeText(SearchActivity.this, "������ؼ��ʣ�", Toast.LENGTH_LONG)
					.show();
		} else {
			keword=ek;
			showNotesList();
		}
	}

	public void onBack(View v) {
		SearchActivity.this.finish();
	}
}

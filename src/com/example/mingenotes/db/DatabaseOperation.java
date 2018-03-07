package com.example.mingenotes.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class DatabaseOperation {
    private SQLiteDatabase db;
    private Context context;

    public DatabaseOperation(Context context, SQLiteDatabase db) {
        this.db = db;
        this.context = context;
    }

    // ���ݿ�Ĵ򿪻򴴽�
    public void create_db() {
        // ����������ݿ�mynotes.db3
        db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir()
                .toString() + "/mynotes.db3", null);
        if (db == null) {//�ж����ݿ��Ƿ񴴽��ɹ�
            Toast.makeText(context, "���ݿⴴ�����ɹ�", Toast.LENGTH_LONG).show();
        }
        // ������,����Ϊnotes,����Ϊ_id
        db.execSQL("create table if not exists notes(_id integer primary key autoincrement,"
                + "title text,"//����title
                + "context text,"//����context
                + "time varchar(20),"//����ʱ��time
                + "datatype text,"//�Ƿ�����������ʱ��datatype
                + "datatime varchar(20),"//����ʱ��datatype
                + "locktype text,"//�Ƿ��������locktype
                + "lock text" + ")");//������lock
    }

    //�����ռ���Ϣ�����ݿ�
    public void insert_db(String title, String text, String time,
                          String datatype, String datatime, String locktype, String lock) {
        if (text.isEmpty()) {//�ж��Ƿ�������
            Toast.makeText(context, "���ݲ���Ϊ��", Toast.LENGTH_LONG).show();
        } else {
            db.execSQL("insert into notes(title,context,time,datatype,datatime,locktype,lock) values('"
                    + title//�ռǱ���
                    + "','"
                    + text//�ռ�����
                    + "','"
                    + time//�ռ����ʱ��
                    + "','"
                    + datatype//�ռ��Ƿ�������ʱ������
                    + "','"
                    + datatime//�ռ�����ʱ��
                    + "','"
                    + locktype//�ռ��Ƿ��������ռ���
                    + "','"
                    + lock//�ռ�������
                    + "');");
        }
    }
    //����id�������ݿ�������Ϣ
    public void update_db(String title, String text, String time,
                          String datatype, String datatime, String locktype, String lock,
                          int item_ID) {
        if (text.isEmpty()) {
            Toast.makeText(context, "���ֶβ���Ϊ��", Toast.LENGTH_LONG).show();
        } else {
            db.execSQL("update notes set context='" + text + "',title='"
                    + title + "',time='" + time + "',datatype='" + datatype
                    + "',datatime='" + datatime + "',locktype='" + locktype
                    + "',lock='" + lock + "'where _id='" + item_ID + "'");
            Toast.makeText(context, "�޸ĳɹ�", Toast.LENGTH_LONG).show();
        }
    }
    //��ѯ�����ռ�����
    public Cursor query_db() {
        Cursor cursor = db.rawQuery("select * from notes", null);
        return cursor;
    }
    //��������id��ѯ��������
    public Cursor query_db(int item_ID) {
        Cursor cursor = db.rawQuery("select * from notes where _id='" + item_ID
                + "';", null);
        return cursor;

    }

    // select * from ���� where name like '%abc%'//ģ����ѯ
    public Cursor query_db(String keword) {
        Cursor cursor = db.rawQuery("select * from notes where context like '%"
                + keword + "%';", null);
        return cursor;
    }

    // select * from ���� where ʱ�� between '��ʼʱ��' and '����ʱ��'//ʱ��β�ѯ
    public Cursor query_db(String starttime, String endtime) {
        Cursor cursor = db.rawQuery("select * from notes where time >='" + starttime + "'and time<='"
                + endtime + "';", null);
        return cursor;
    }

    // ɾ��ĳһ������
    public void delete_db(int item_ID) {
        db.execSQL("delete from notes where _id='" + item_ID + "'");
    }

    // �ر����ݿ�
    public void close_db() {
        db.close();
    }
}

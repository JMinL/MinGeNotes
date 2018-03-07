package com.example.mingenotes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.graphics.drawable.BitmapDrawable;

import com.example.mingenotes.data.DateTimePickerDialog;
import com.example.mingenotes.data.DateTimePickerDialog.OnDateTimeSetListener;
import com.example.mingenotes.db.DatabaseOperation;
import com.example.mingenotes.view.LineEditText;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class AddActivity extends Activity {
    private Button bt_back;
    private Button bt_save;
    private TextView tv_title;
    private SQLiteDatabase db;//���ݿ������
    private DatabaseOperation dop;//�Զ������ݿ�
    private LineEditText et_Notes;
    private GridView bottomMenu;
    // �ײ���ť�˵���ťͼƬ����
    private int[] bottomItems = {
          R.drawable.tabbar_microphone,
            R.drawable.tabbar_photo, R.drawable.tabbar_camera,
            R.drawable.tabbar_appendix};
    InputMethodManager imm;//�����ֻ�����
    Intent intent;
    String editModel = null;
    int item_Id;
    String title;
    String time;
    String context;
    public String datatype = "0";// �ж��Ƿ�����¼���������ѹ���
    public String datatime = "0";// ����ʱ��
    public String locktype = "0";// �ж��Ƿ��������
    public String lock = "0";// ����
    private RelativeLayout datarl;
    private TextView datatv;
    private ScrollView sclv;
    // ��¼editText�е�ͼƬ�����ڵ���ʱ�жϵ���������һ��ͼƬ
    private List<Map<String, String>> imgList = new ArrayList<Map<String, String>>();
    private ImageButton ib_lk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        bt_back = (Button) findViewById(R.id.bt_back);
        bt_back.setOnClickListener(new ClickEvent());
        bt_save = (Button) findViewById(R.id.bt_save);
        bt_save.setOnClickListener(new ClickEvent());
        tv_title = (TextView) findViewById(R.id.tv_title);
        et_Notes = (LineEditText) findViewById(R.id.et_note);
        bottomMenu = (GridView) findViewById(R.id.bottomMenu);
        datarl = (RelativeLayout) findViewById(R.id.datarl);
        datatv = (TextView) findViewById(R.id.datatv);
        sclv = (ScrollView) findViewById(R.id.sclv);
        ib_lk = (ImageButton) findViewById(R.id.ib_lk);
        // ���ò˵�
        initBottomMenu();
        // Ϊ�˵����ü�����
        bottomMenu.setOnItemClickListener(new MenuClickEvent());
        // Ĭ�Ϲر������,����ͨ��ʧȥ��������
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_Notes.getWindowToken(), 0);
        dop = new DatabaseOperation(this, db);
        intent = getIntent();
        editModel = intent.getStringExtra("editModel");
        item_Id = intent.getIntExtra("noteId", 0);
        // ��������
        loadData();
        // ��editText��ӵ����¼�
        et_Notes.setOnClickListener(new TextClickEvent());
    }

    // ��������
    private void loadData() {
        // �������������ģʽ����editText���
        if (editModel.equals("newAdd")) {
            et_Notes.setText("");
        }
        // ����༭�����Ѵ��ڵļ��£������ݿ�ı��������ȡ��������ʾ��EditText��
        else if (editModel.equals("update")) {
            tv_title.setText("�༭����");
            dop.create_db();
            Cursor cursor = dop.query_db(item_Id);
            cursor.moveToFirst();
            // ȡ�����ݿ�����Ӧ���ֶ�����
            context = cursor.getString(cursor.getColumnIndex("context"));
            datatype = cursor.getString(cursor.getColumnIndex("datatype"));
            datatime = cursor.getString(cursor.getColumnIndex("datatime"));
            locktype = cursor.getString(cursor.getColumnIndex("locktype"));
            lock = cursor.getString(cursor.getColumnIndex("lock"));
            if ("0".equals(locktype)) {
                ib_lk.setBackgroundResource(R.drawable.un_locky);
            } else {
                ib_lk.setBackgroundResource(R.drawable.locky);
            }
            if ("0".equals(datatype)) {
                datarl.setVisibility(View.GONE);
            } else {
                datarl.setVisibility(View.VISIBLE);
                datatv.setText("����ʱ�䣺" + datatime);
            }
            // ����������ʽ������ƥ��·��
            Pattern p = Pattern.compile("/([^\\.]*)\\.\\w{3}");
            Matcher m = p.matcher(context);
            int startIndex = 0;
            while (m.find()) {
                // ȡ��·��ǰ������
                if (m.start() > 0) {
                    et_Notes.append(context.substring(startIndex, m.start()));
                }
                SpannableString ss = new SpannableString(m.group().toString());
                // ȡ��·��
                String path = m.group().toString();
                // ȡ��·���ĺ�׺
                String type = path.substring(path.length() - 3, path.length());
                Bitmap bm = null;
                Bitmap rbm = null;
                // �жϸ��������ͣ������¼���ļ��������Դ�ļ��м���ͼƬ
                if (type.equals("amr")) {
                    bm = BitmapFactory.decodeResource(getResources(),
                            R.drawable.record_icon);
                    // ����ͼƬ
                    rbm = resize(bm, 400);
                } else {
                    // ȡ��ͼƬ
                    bm = BitmapFactory.decodeFile(m.group());
                    // ����ͼƬ
                    rbm = resize(bm, 480);
                    // ΪͼƬ��ӱ߿�Ч��
                    rbm = getBitmapHuaSeBianKuang(rbm);
                }
                ImageSpan span = new ImageSpan(this, rbm);
                ss.setSpan(span, 0, m.end() - m.start(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                System.out.println(m.start() + "-------" + m.end());
                et_Notes.append(ss);
                startIndex = m.end();
                // ��List��¼��¼����λ�ü�����·�������ڵ����¼�
                Map<String, String> map = new HashMap<String, String>();
                map.put("location", m.start() + "-" + m.end());
                map.put("path", path);
                imgList.add(map);
            }
            // �����һ��ͼƬ֮������������TextView��
            et_Notes.append(context.substring(startIndex, context.length()));
            dop.close_db();
        }
    }

    // ΪEidtText���ü�����
    class TextClickEvent implements OnClickListener {
        @Override
        public void onClick(View v) {
            Spanned s = et_Notes.getText();
            ImageSpan[] imageSpans;
            imageSpans = s.getSpans(0, s.length(), ImageSpan.class);
            int selectionStart = et_Notes.getSelectionStart();
            for (ImageSpan span : imageSpans) {
                int start = s.getSpanStart(span);
                int end = s.getSpanEnd(span);
                // �ҵ�ͼƬ
                if (selectionStart >= start && selectionStart < end) {
                    // ���ҵ�ǰ������ͼƬ����һ��ͼƬ
                    String path = null;
                    for (int i = 0; i < imgList.size(); i++) {
                        Map<String, String> map = imgList.get(i);
                        // �ҵ���
                        if (map.get("location").equals(start + "-" + end)) {
                            path = imgList.get(i).get("path");
                            break;
                        }
                    }
                    // �����жϵ�ǰͼƬ�Ƿ���¼�������Ϊ¼��������ת������¼����Activity��������ǣ�����ת���鿴ͼƬ�Ľ���
                    // ¼��������ת������¼����Activity
                    if (path.substring(path.length() - 3, path.length())
                            .equals("amr")) {
                        Intent intent = new Intent(AddActivity.this,
                                ShowRecord.class);
                        intent.putExtra("audioPath", path);
                        startActivity(intent);
                    }
                    // ͼƬ������ת���鿴ͼƬ�Ľ���
                    else {
                        // �����ַ������鿴ͼƬ����һ�־���ֱ�ӵ���ϵͳ��ͼ��鿴ͼƬ���ڶ������Զ���Activity
                        // ����ϵͳͼ��鿴ͼƬ
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        File file = new File(path);
                        Uri uri = Uri.fromFile(file);
                        intent.setDataAndType(uri, "image/*");
                        startActivity(intent);
                    }
                } else
                    // ����������ǿհ׳������֣����ý��㣬���������
                    imm.showSoftInput(et_Notes, 0);
            }
        }
    }

    // ���༭�������ô���������
    class TextTouchEvent implements OnTouchListener{
        @SuppressLint("ClickableViewAccessibility")
		@Override
        public boolean onTouch(View v, MotionEvent event) {
            Spanned s = et_Notes.getText();
            ImageSpan[] imageSpans;
            imageSpans = s.getSpans(0, s.length(), ImageSpan.class);
            int selectionStart = et_Notes.getSelectionStart();
            for (ImageSpan span : imageSpans) {
                int start = s.getSpanStart(span);
                int end = s.getSpanEnd(span);
                int inType = et_Notes.getInputType(); // backup the input type
                // �ҵ�ͼƬ
                if (selectionStart >= start && selectionStart < end) {
                    Bitmap bitmap = ((BitmapDrawable) span.getDrawable())
                            .getBitmap();
                    et_Notes.setInputType(InputType.TYPE_NULL); // disable soft
                    et_Notes.onTouchEvent(event); // call native handler
                    et_Notes.setInputType(inType); // restore input type
                    AddActivity.this.finish();
                } else {
                    // ����������ǿհ׳������֣����ý��㣬���������
                    imm.showSoftInput(et_Notes, 0);
                    et_Notes.setInputType(inType);
                }
            }
            return true;
        }
    }

    // ���ð�ť������
    class ClickEvent implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bt_back:
                    // ��ǰActivity�������򷵻���һ��Activity
                    AddActivity.this.finish();
                    break;
                // ��������ӵ����ݿ���
                case R.id.bt_save:
                    // ȡ��EditText�е�����
                    context = et_Notes.getText().toString();
                    if (context.isEmpty()) {
                        Toast.makeText(AddActivity.this, "����Ϊ��!", Toast.LENGTH_LONG)
                                .show();
                    } else {
                        // ȡ�õ�ǰʱ��
                        SimpleDateFormat formatter = new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm");
                        Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
                        time = formatter.format(curDate);
                        // ��ȡEditText�е�ǰһ������Ϊ���⣬������ʾ����ҳ�б���
                        title = getTitle(context);
                        // �����ݿ�
                        dop.create_db();
                        // �ж��Ǹ��»�����������
                        if (editModel.equals("newAdd")) {
                            // �����²��뵽���ݿ���
                            dop.insert_db(title, context, time, datatype, datatime,
                                    locktype, lock);
                        }
                        // ����Ǳ༭����¼��¼���
                        else if (editModel.equals("update")) {
                            dop.update_db(title, context, time, datatype, datatime,
                                    locktype, lock, item_Id);
                        }
                        dop.close_db();
                        // ������ǰactivity
                        AddActivity.this.finish();
                    }
                    break;
            }
        }
    }

    // ��ȡEditText�е�ǰһ������Ϊ���⣬������ʾ����ҳ�б���
    private String getTitle(String context) {
        // ����������ʽ������ƥ��·��
        Pattern p = Pattern.compile("/([^\\.]*)\\.\\w{3}");
        Matcher m = p.matcher(context);
        StringBuffer strBuff = new StringBuffer();
        String title = "";
        int startIndex = 0;
        while (m.find()) {
            // ȡ��·��ǰ������
            if (m.start() > 0) {
                strBuff.append(context.substring(startIndex, m.start()));
            }
            // ȡ��·��
            String path = m.group().toString();
            // ȡ��·���ĺ�׺
            String type = path.substring(path.length() - 3, path.length());
            // �жϸ���������
            if (type.equals("amr")) {
                strBuff.append("[¼��]");
            } else {
                // strBuff.append("");
            }
            startIndex = m.end();
            // ֻȡ��ǰ15������Ϊ����
            if (strBuff.length() > 15) {
                // ͳһ���س�,�������ַ����ɿո�
                title = strBuff.toString().replaceAll("\r|\n|\t", " ");
                return title;
            }
        }
        strBuff.append(context.substring(startIndex, context.length()));
        // ͳһ���س�,�������ַ����ɿո�
        title = strBuff.toString().replaceAll("\r|\n|\t", " ");
        return title;
    }

    // ���ò˵�
    private void initBottomMenu() {
        //�˵�����
        ArrayList<Map<String, Object>> menus = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < bottomItems.length; i++) {//ѭ���˵�����
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("image", bottomItems[i]);//ѭ��ͼƬ������ӵ��˵���
            menus.add(item);//���ͼƬ�˵����ײ��˵�
        }
        //�˵�����
        bottomMenu.setNumColumns(bottomItems.length);
        //�ײ��˵�
        bottomMenu.setSelector(R.drawable.bottom_item);
        //ʵ�����ײ��˵�������
        SimpleAdapter mAdapter = new SimpleAdapter(AddActivity.this, menus,
                R.layout.item_button, new String[]{"image"},
                new int[]{R.id.item_image});
        bottomMenu.setAdapter(mAdapter);//Ϊ�ײ��˵����������
    }

    // ���ò˵��������
    class MenuClickEvent implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Intent intent;
            switch (position) {
                    
                // ����
                case 0:
                    intent = new Intent(AddActivity.this, ActivityRecord.class);
                    startActivityForResult(intent, 4);
                    break;
                // ��Ƭ
                case 1:
                    // ���ͼƬ����Ҫ����
                    intent = new Intent();
                    // �趨����Ϊimage
                    intent.setType("image/*");
                    // ����action
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    // ѡ����Ƭ�󷵻ر�Activity
                    startActivityForResult(intent, 1);
                    break;
                // ����
                case 2:
                    //if (Build.VERSION.SDK_INT >= 23) {

                        //if(ContextCompat.checkSelfPermission(AddActivity.this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
                            //����Ȩ��
                          // ActivityCompat.requestPermissions(AddActivity.this,
                                 //new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                  // 1);
                       // }else {
                            // ����ϵͳ���ս���
                            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                            // ����ѡ����Ƭ
                            startActivityForResult(intent, 2);
                       // }//����ע��
                   // } 
                    break;
                // ��������
                case 3:
                    setReminder();
                    break;

            }
        }
    }
    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // ����ϵͳ���ս���
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                // ����ѡ����Ƭ
                startActivityForResult(intent, 2);
            } else {
                // Permission Denied
            }
        }
    }  */ 
    // ��������
    private void setReminder() {
        DateTimePickerDialog d;
        if ("0".equals(datatime)) {//�ж��Ƿ����ù��¼���û�����ù�����ʱ��
            d = new DateTimePickerDialog(this, System.currentTimeMillis());//�����Զ���ʱ�䵯����ʾϵͳʱ��
        } else {
            d = new DateTimePickerDialog(this, getdaytime(datatime));//�����Զ���ʱ�䵯����ʾ���ù���ʱ��
        }
        d.setOnDateTimeSetListener(new OnDateTimeSetListener() {
            public void OnDateTimeSet(AlertDialog dialog, long date) {
                // ȡ�õ�ǰʱ��
                SimpleDateFormat formatter = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm");//��Ƹ�ʽ
                datatime = formatter.format(date);//���Լ����õ�ʱ���ʽ��ʾʱ�� dateΪ��ǰѡ���ʱ��
                datatype = "1";
                datarl.setVisibility(View.VISIBLE);
                datatv.setText("����ʱ�䣺" + datatime);
            }
        });
        d.show();
    }

    @SuppressLint("SimpleDateFormat")
	public static long getdaytime(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date dt2 = null;
        try {
            dt2 = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt2.getTime();
    }
    //���ݻص�����
    @SuppressLint("SimpleDateFormat")
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // ȡ������
            Uri uri = data.getData();
            //ʵ�������ݱ�
            ContentResolver cr =getContentResolver();
            //ͼƬ���ڴ���ѡ���ת����Bitmap����
            Bitmap bitmap = null;
            //���շ�����Ϣ
            Bundle extras = null;
            // �����ѡ����Ƭ
            if (requestCode == 1) {
                // ȡ��ѡ����Ƭ��·��
                String[] proj = {MediaStore.Images.Media.DATA};
                //���ڲ�ѯָ��ͼƬλ��
                Cursor actualimagecursor = managedQuery(uri, proj, null, null,
                        null);
                //��ȡý�����ݿ�
                int actual_image_column_index = actualimagecursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                //�ƶ����ݵ���һ��
                actualimagecursor.moveToFirst();
                //ͼƬ·��
                String path = actualimagecursor
                        .getString(actual_image_column_index);
                try {
                    // ���������Bitmap��
                    bitmap = BitmapFactory
                            .decodeStream(cr.openInputStream(uri));
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // ����ͼƬ
                InsertBitmap(bitmap, 480, path);
            }
            // ѡ���������
            else
            if (requestCode == 2) {
                try {
                    if (uri != null){
                        // ��������Ǹ���Uri��ȡBitmapͼƬ�ľ�̬����
                        bitmap = MediaStore.Images.Media.getBitmap(cr, uri);
                        // ��������Щ���պ��ͼƬ��ֱ�Ӵ�ŵ�Bundle�е��������ǿ��Դ��������ȡBitmapͼƬ
                    }else {
                        //�õ���������
                        extras = data.getExtras();
                        //��ȡͼƬ
                        bitmap = extras.getParcelable("data");
                    }
                    // ���ĵ���Ƭ����ָ�����ļ�����
                    // ���ϵͳ��ǰʱ�䣬���Ը�ʱ����Ϊ�ļ���
                    SimpleDateFormat formatter = new SimpleDateFormat(
                            "yyyyMMddHHmmss");
                    // ��ȡ��ǰʱ��
                    Date curDate = new Date(System.currentTimeMillis());
                    // ��ǰʱ�䱣���String����
                    String str = formatter.format(curDate);
                    //���ڼ�¼ͼƬ·��
                    String paintPath = "";
                    //ͼƬ·��
                    str = str + "paint.png";
                    //�½��ļ���
                    File dir = new File("/sdcard/notes/");
                    //�½��ļ�
                    File file = new File("/sdcard/notes/", str);
                    if (!dir.exists()) {// �ж��ļ��д����Ƿ�ɹ�
                        dir.mkdir();// �����ļ���
                    } else {
                        if (file.exists()) {// �ж��ļ��Ƿ񴴽�
                            file.delete();// ɾ���ļ�
                        }
                    }
                    //�½��ļ���
                    FileOutputStream fos = new FileOutputStream(file);
                    // �� bitmap ѹ����������ʽ��ͼƬ����
                    bitmap.compress(CompressFormat.PNG, 100, fos);
                    fos.flush();//����������
                    fos.close();//�ر���
                    //ͼƬ·��
                    String path = "/sdcard/notes/" + str;
                    //����ͼƬ
                    InsertBitmap(bitmap, 480, path);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            // ���ص��ǻ�ͼ��Ľ��
            else if (requestCode == 3) {
                //����������
                extras = data.getExtras();
                //���շ��ص���Ϣ
                String path = extras.getString("paintPath");
                // ͨ��·��ȡ��ͼƬ������bitmap��
                bitmap = BitmapFactory.decodeFile(path);
                // �����ͼ�ļ�
                InsertBitmap(bitmap, 480, path);
            }
            // ���ص���¼���ļ�
            else if (requestCode == 4) {
                //����������
                extras = data.getExtras();
                //���շ��ص���Ϣ
                String path = extras.getString("audio");
                //ת��ͼƬ��bitmap��ʽ
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.record_icon);
                // ����¼��ͼ��
                InsertBitmap(bitmap, 200, path);
            }
           
           
        }
    }

    // ��ͼƬ�ȱ������ŵ����ʵĴ�С�������EditText��
    void InsertBitmap(Bitmap bitmap, int S, String imgPath) {
        bitmap = resize(bitmap, S);
        // ��ӱ߿�Ч��
        // bitmap = getBitmapHuaSeBianKuang(bitmap);
        // bitmap = addBigFrame(bitmap,R.drawable.line_age);
        final ImageSpan imageSpan = new ImageSpan(this, bitmap);
        SpannableString spannableString = new SpannableString(imgPath);
        spannableString.setSpan(imageSpan, 0, spannableString.length(),
                SpannableString.SPAN_MARK_MARK);
        // ����Ƶ���һ��
        // et_Notes.append("\n");
        Editable editable = et_Notes.getEditableText();
        int selectionIndex = et_Notes.getSelectionStart();
        spannableString.getSpans(0, spannableString.length(), ImageSpan.class);
        // ��ͼƬ��ӽ�EditText��
        editable.insert(selectionIndex, spannableString);
        // ���ͼƬ���Զ��ճ�����
        et_Notes.append("\n");
        // ��List��¼��¼����λ�ü�����·�������ڵ����¼�
        Map<String, String> map = new HashMap<String, String>();
        map.put("location", selectionIndex + "-"
                + (selectionIndex + spannableString.length()));
        map.put("path", imgPath);
        imgList.add(map);
    }

    // �ȱ�������ͼƬ
    private Bitmap resize(Bitmap bitmap, int S) {
        int imgWidth = bitmap.getWidth();
        int imgHeight = bitmap.getHeight();
        double partion = imgWidth * 1.0 / imgHeight;
        double sqrtLength = Math.sqrt(partion * partion + 1);
        // �µ�����ͼ��С
        double newImgW = S * (partion / sqrtLength);
        double newImgH = S * (1 / sqrtLength);
        float scaleW = (float) (newImgW / imgWidth);
        float scaleH = (float) (newImgH / imgHeight);
        Matrix mx = new Matrix();
        // ��ԭͼƬ��������
        mx.postScale(scaleW, scaleH);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, mx,
                true);
        return bitmap;
    }

    // ��ͼƬ�ӱ߿򣬲����ر߿���ͼƬ
    public Bitmap getBitmapHuaSeBianKuang(Bitmap bitmap) {
        float frameSize = 0.2f;
        Matrix matrix = new Matrix();

        // ��������ͼ
        Bitmap bitmapbg = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        // ���õ�ͼΪ����
        Canvas canvas = new Canvas(bitmapbg);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));

        float scale_x = (bitmap.getWidth() - 2 * frameSize - 2) * 1f
                / (bitmap.getWidth());
        float scale_y = (bitmap.getHeight() - 2 * frameSize - 2) * 1f
                / (bitmap.getHeight());
        matrix.reset();
        matrix.postScale(scale_x, scale_y);
        // ����Ƭ��С����(��ȥ�߿�Ĵ�С)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(1);
        paint.setStyle(Style.FILL);

        // ���Ƶ�ͼ�߿�
        canvas.drawRect(
                new Rect(0, 0, bitmapbg.getWidth(), bitmapbg.getHeight()),
                paint);

        // ���ƻ�ɫ�߿�
        paint.setColor(Color.GRAY);
        canvas.drawRect(
                new Rect((int) (frameSize), (int) (frameSize), bitmapbg
                        .getWidth() - (int) (frameSize), bitmapbg.getHeight()
                        - (int) (frameSize)), paint);

        canvas.drawBitmap(bitmap, frameSize + 1, frameSize + 1, paint);
        return bitmapbg;
    }

    // ȡ������
    public void onDataCancel(View v) {
        datarl.setVisibility(View.GONE);
        datatype = "0";// �ж��Ƿ�����¼���������ѹ���
        datatime = "0";
    }

    // �޸���������ʱ��
    public void onDataChange(View v) {
        setReminder();
    }

    // ����ռ��� ȡ���ռ���
    public void onLOCK(View v) {
        if ("0".equals(locktype)) {//�ж��Ƿ�����������
            inputlockDialog();//�����������뵯��
        } else {
            inputunlockDialog();//����ȡ�����뵯��
        }
    }

    //ȡ������
    private void inputunlockDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//����������
        builder.setTitle("�Ƿ�ȡ������")
                .setNegativeButton("ȡ��", null);//�ڵ��������ñ�������ȡ����ť
        builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {//����ȷ�ϰ�ť
                locktype = "0";//����û����������
                lock = "0";//��������
                ib_lk.setBackgroundResource(R.drawable.un_locky);
                Toast.makeText(AddActivity.this, "������ȡ��",
                        Toast.LENGTH_LONG).show();
            }
        });
        builder.show();//����ȡ�����뵯��
    }

    //�������뵯��
    private void inputlockDialog() {
        final EditText inputServer = new EditText(this);//����EditText�����
        inputServer.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);//�������������
        inputServer.setFocusable(true);//��ȡ����
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//����������
        builder.setTitle("��������").setView(inputServer)
                .setNegativeButton("ȡ��", null);//�ڵ��������ñ�����������
        builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {//����ȷ�ϰ�ť
                String inputName = inputServer.getText().toString();
                if ("".equals(inputName)) {//�ж�����������Ƿ�Ϊ��
                    Toast.makeText(AddActivity.this, "���벻��Ϊ�� ���������룡",
                            Toast.LENGTH_LONG).show();
                } else {//��������ݲ�Ϊ�ա�
                    lock = inputName;//����
                    locktype = "1";//�����������
                    ib_lk.setBackgroundResource(R.drawable.locky);//���������ͼ��
                    Toast.makeText(AddActivity.this, "�������óɹ���",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.show();//�����������뵯��
    }

    // ������
    public void onFX(View v) {
        Bitmap c = getBitmapByView(sclv);//��ȡ��ͼƬ
        try {
            saveMyBitmap("notesimge", c);//����ͼƬ
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String imagePath = Environment.getExternalStorageDirectory()
                + File.separator + "notesimge.jpg";//ͼƬ·��
        // ���ļ��õ�uri
        Uri imageUri = Uri.fromFile(new File(imagePath));
        Log.d("share", "uri:" + imageUri);//��ӡͼƬ·��
        Intent shareIntent = new Intent();//������ͼ
        shareIntent.setAction(Intent.ACTION_SEND);//����������������
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);//����ͼƬ
        shareIntent.setType("image/*");//��������
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//Ĭ����ת����
        startActivity(Intent.createChooser(shareIntent, "������"));
    }

    //����ͼƬ
    public void saveMyBitmap(String bitName, Bitmap mBitmap) throws IOException {
        File f = new File(Environment.getExternalStorageDirectory()
                + File.separator + "notesimge.jpg");//��ʼ���ļ�
        f.createNewFile();//����ͼƬ�ļ�
        FileOutputStream fOut = null;//�����ļ���
        try {
            fOut = new FileOutputStream(f);//ʵ�����ļ���
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);//ͼƬ���浽�ļ���
        try {
            fOut.flush();//�ļ�дд���������
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();//�ر��ļ���
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //��ȡscrollview����Ļ
    public static Bitmap getBitmapByView(ScrollView scrollView) {
        int h = 0;//���ø߶�0
        Bitmap bitmap = null;//���ÿյ�ͼƬ
        // ��ȡscrollviewʵ�ʸ߶�
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            h += scrollView.getChildAt(i).getHeight();//����scrollViewʵ�ʸ߶�
            scrollView.getChildAt(i).setBackgroundColor(
                    Color.parseColor("#FFFFFF"));//����scrollView������ɫ
        }
        // ����scrollView��С��bitmap
        bitmap = Bitmap.createBitmap(scrollView.getWidth(), h,
                Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);//����ͼƬ
        scrollView.draw(canvas);//����scrollView
        return bitmap;//����ͼƬ
    }
}

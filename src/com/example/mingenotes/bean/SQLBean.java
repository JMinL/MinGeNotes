package com.example.mingenotes.bean;

public class SQLBean {
	private String _id; //��������id
	private String title; //�ռǱ���
	private String context; //�ռ�����
	private String time; //�ռǼ�¼ʱ��
	private String datatype; //�Ƿ������˵�ʱ����0����δ����
	private String datatime; //����ʱ��
	private String locktype; //�Ƿ�������������0����δ����
	private String lock; //����������
	public String get_id() {//��ȡ�ռ�id
		return _id;
	}
	public void set_id(String _id) {//�����ռ�id
		this._id = _id;
	}
	public String getTitle() {//��ȡ�ռǱ���
		return title;
	}
	public void setTitle(String title) {//�����ռǱ���
		this.title = title;
	}
	public String getContext() {//��ȡ�ռ�����
		return context;
	}
	public void setContext(String context) {//�����ռ�����
		this.context = context;
	}
	public String getTime() {//��ȡ�ռ�����ʱ��
		return time;
	}
	public void setTime(String time) {//�����ռ�����ʱ��
		this.time = time;
	}
	public String getDatatype() {//��ȡ�Ƿ������˵�ʱ����
		return datatype;
	}
	public void setDatatype(String datatype) {//���������ڵ�ʱ����
		this.datatype = datatype;
	}
	public String getDatatime() {//��ȡ����ʱ��
		return datatime;
	}
	public void setDatatime(String datatime) {//��������ʱ��
		this.datatime = datatime;
	}
	public String getLocktype() {//��ȡ�Ƿ��������ռ���
		return locktype;
	}
	public void setLocktype(String locktype) {//�����ռ���
		this.locktype = locktype;
	}
	public String getLock() {//��ȡ�ռ�������
		return lock;
	}
	public void setLock(String lock) {//�����ռ�������
		this.lock = lock;
	}
}
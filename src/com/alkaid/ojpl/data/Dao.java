package com.alkaid.ojpl.data;

import java.util.List;

import android.content.Context;

import com.alkaid.ojpl.common.AlkaidException;
import com.alkaid.ojpl.model.Model;

public abstract class Dao<T extends Model> {
	protected Context ctx;
	public Dao(Context ctx) {
		this.ctx=ctx;
	}

	public boolean insert(T model) throws AlkaidException {
		throw new AlkaidException("unsupport this method!");
	}

	public boolean insert(List<T> models) throws AlkaidException {
		throw new AlkaidException("unsupport this method!");
	}

	public T getById(String id) throws AlkaidException {
		throw new AlkaidException("unsupport this method!");
	}

	public T getById(int id) throws AlkaidException {
		throw new AlkaidException("unsupport this method!");
	}

	public List<T> getAll() throws AlkaidException {
		throw new AlkaidException("unsupport this method!");
	}
	
	public List<T> getAll(int parentId) throws AlkaidException {
		throw new AlkaidException("unsupport this method!");
	}

	public boolean remove(String id) throws AlkaidException {
		throw new AlkaidException("unsupport this method!");
	}

	public boolean remove(int id) throws AlkaidException {
		throw new AlkaidException("unsupport this method!");
	}

	public boolean remove(T model) throws AlkaidException {
		throw new AlkaidException("unsupport this method!");
	}

	public boolean remove(List<T> models) throws AlkaidException {
		throw new AlkaidException("unsupport this method!");
	}

	public boolean update(T model) throws AlkaidException {
		throw new AlkaidException("unsupport this method!");
	}

	public boolean update(List<T> models) throws AlkaidException {
		throw new AlkaidException("unsupport this method!");
	}

}

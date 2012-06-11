package com.alkaid.ojpl.data;

import java.util.List;

import com.alkaid.ojpl.common.AlkaidException;
import com.alkaid.ojpl.model.Model;
public interface DaoInterface<T> {
	boolean insert(T model) throws AlkaidException;
	boolean insert(List<T> models) throws AlkaidException;
	T getById(String id) throws AlkaidException;
	T getById(int id) throws AlkaidException;
	List<T> getAll() throws AlkaidException;
	boolean remove(String id) throws AlkaidException;
	boolean remove(int id) throws AlkaidException;
	boolean remove(T model) throws AlkaidException;
	boolean remove(List<T> models) throws AlkaidException;
	boolean update(T model) throws AlkaidException;
	boolean update(List<T> models) throws AlkaidException;
}

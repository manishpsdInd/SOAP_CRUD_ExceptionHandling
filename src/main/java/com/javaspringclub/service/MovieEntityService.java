package com.javaspringclub.service;

import java.util.List;
import com.javaspringclub.entity.MovieEntity;

public interface MovieEntityService {

	public MovieEntity getEntityById(long id);
	public MovieEntity getEntityByTitle(String title);
	public List<MovieEntity> getAllEntities();
	public MovieEntity addEntity(MovieEntity entity);
	public boolean updateEntity(MovieEntity entity);
	public boolean deleteEntityById(long id);
}
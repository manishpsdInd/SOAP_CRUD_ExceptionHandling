package com.javaspringclub.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.javaspringclub.entity.MovieEntity;

@Repository
public interface MovieEntityRepository extends CrudRepository<MovieEntity, Long> {

	public MovieEntity findByTitle(String title);
}

package com.javaspringclub.service;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.javaspringclub.entity.MovieEntity;
import com.javaspringclub.exception.ServiceFaultException;
import com.javaspringclub.repository.MovieEntityRepository;

@Service
@Transactional
public class MovieEntityServiceImpl implements MovieEntityService {

    private MovieEntityRepository repository;

    public MovieEntityServiceImpl() {

    }

    @Autowired
    public MovieEntityServiceImpl(MovieEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public MovieEntity getEntityById(long id) {
        return this.repository.findById(id).get();
    }

    @Override
    public MovieEntity getEntityByTitle(String title) {
        return this.repository.findByTitle(title);
    }

    @Override
    public List < MovieEntity > getAllEntities() {
        List <MovieEntity> list = new ArrayList <MovieEntity> ();
        repository.findAll().forEach(e -> list.add(e));
        return list;
    }

    @Override
    public MovieEntity addEntity(MovieEntity entity) {
        try {
            return this.repository.save(entity);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public boolean updateEntity(MovieEntity entity) {
        try {
            this.repository.save(entity);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional(dontRollbackOn = ServiceFaultException.class)
    public boolean deleteEntityById(long id) {
        try {
            this.repository.deleteById(id);
            return true;
        } catch (ServiceFaultException e) {
            return false;
        }

    }

}
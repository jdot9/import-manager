package com.dotwavesoftware.importscheduler.repository;

import org.springframework.stereotype.Repository;

import com.dotwavesoftware.importscheduler.entity.BaseEntity;

import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Optional;

@Repository
public abstract class BaseRepository<T extends BaseEntity> {
    protected final JdbcTemplate jdbcTemplate;

    public BaseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public abstract int save(T entity);

    public abstract List<T> findAll();

    public abstract Optional<T> findById(Integer id);

    public abstract int update(T entity, Integer id);

    public abstract int deleteById(Integer id);
}

package com.example.lab6.repository;

import com.example.lab6.domain.Entity;
import com.example.lab6.domain.paging.Page;
import com.example.lab6.domain.paging.Pageable;

public interface PagingRepository<ID, E extends Entity<ID>> extends Repository<ID, E> {
    Page<E> findAllOnPage(Pageable pageable);
}

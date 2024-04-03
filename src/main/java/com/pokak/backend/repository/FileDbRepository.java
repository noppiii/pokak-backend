package com.pokak.backend.repository;

import com.pokak.backend.entity.common.FileDb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileDbRepository extends JpaRepository<FileDb, Long> {

}

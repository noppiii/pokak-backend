package com.pokak.backend.service;

import com.pokak.backend.entity.common.FileDb;
import com.pokak.backend.entity.common.FileType;
import com.pokak.backend.repository.FileDbRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FileDbService {

    private final FileDbRepository fileDBRepository;

    public FileDbService(FileDbRepository fileDBRepository) {
        this.fileDBRepository = fileDBRepository;
    }

    public FileDb save(String name, FileType type, byte[] data) {
        FileDb FileDB = new FileDb(name, type, data);
        return fileDBRepository.save(FileDB);
    }

    @Cacheable(cacheNames = "file", key = "#id")
    public Optional<FileDb> findById(Long id) {
        return fileDBRepository.findById(id);
    }
}

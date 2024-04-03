package com.pokak.backend.dto.mapper.file;

import com.pokak.backend.entity.common.FileDb;
import com.pokak.backend.exception.BadRequestException;
import com.pokak.backend.service.FileDbService;
import org.mapstruct.ObjectFactory;
import org.springframework.stereotype.Component;

@Component
public class FileDbMapperResolver {

    private final FileDbService fileDbService;

    public FileDbMapperResolver(FileDbService fileDbService) {
        this.fileDbService = fileDbService;
    }

    @ObjectFactory
    public FileDb resolve(Long id){
        return fileDbService.findById(id).orElseThrow(()->new BadRequestException("fileNotExist"));
    }
}

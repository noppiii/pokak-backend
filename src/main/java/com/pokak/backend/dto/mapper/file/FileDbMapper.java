package com.pokak.backend.dto.mapper.file;

import com.pokak.backend.dto.file.FileDbDto;
import com.pokak.backend.dto.mapper.CustomMapper;
import com.pokak.backend.entity.common.FileDb;
import com.pokak.backend.entity.common.FileType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = FileDbMapperResolver.class)
public interface FileDbMapper extends CustomMapper<FileDbDto, FileDb> {

    default String fileTypeToString(FileType fileType) {
        return fileType.getMimeType();
    }

    default FileType stringToFileType(String mimeType) {
        return FileType.fromMimeType(mimeType).orElse(null);
    }
}

package com.mapvendor.module.file.repository;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileResourceMapper {
    int insert(FileResourceRow row);
    FileResourceRow selectActiveByStorageKey(String storageKey);
}

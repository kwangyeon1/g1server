package org.yht.exps.express.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.com.fileshare.FileShare;
import org.yht.exps.express.model.com.fileshare.FileShareFile;

@Mapper
public interface FileMapper {
	Map<String, Object> selectFileShare(FileShare fileShareInfo);
	void deleteFileShareFile(FileShareFile fileShareFile);
	void deleteFileShare(FileShare fileShare);
	void insertFileShare(FileShare fileShare);
	void insertFileShareFile(FileShare fileShare);
	void updateFileShare(FileShare fileShare);
}

package org.yht.exps.express.mapper.bms;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsAllocateCar.BmsAllocateCar;
import org.yht.exps.express.model.bms.bmsAllocateCar.search.SearchBmsAllocateCarList;

@Mapper
public interface BmsAllocateCarMapper {

	/**
	 * 배차예정정보 조회
	 * @param search
	 * @return
	 */
	List<BmsAllocateCar> selectBmsAllocateCarList(SearchBmsAllocateCarList search);
	BmsAllocateCar selectBmsAllocateCarDetail(SearchBmsAllocateCarList search);
	int updateBmsAllocateCar(BmsAllocateCar bmsAllocateCar);
	
}

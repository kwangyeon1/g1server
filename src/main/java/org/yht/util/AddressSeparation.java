package org.yht.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class AddressSeparation {
	public AddressSeparation() {
		//AddressSeparation
    }
	
    /**
     * Address Separation Program
     * 한글주소 단위별로 나누기
     * receiver -> 받는 사람 주소
     * contents -> 메일 내용
     * subject-> 메일 제목
     * @param receiver String, contents String, subject String
     * @return
     * @exception MessagingException, Exception
     */
    public static Map<String, Object> separateAddress(Map<String, Object> map) throws Exception {
        try{
        	if(!"".equals(Util.null2str(map.get("Address")))){
    			String[] tempAddr = map.get("Address").toString().split(" ");
    			String[] tempBuildingNo = null;
    			Pattern pattern = Pattern.compile("[1-9][0-9]*-?[0-9]*");
    			Matcher matcher = null;
    			String addrType = "A";
    			//A : SiDo + SiGunGu(EupMyeon) + Road + Detail					(ex : 서울특별시 강서구 마곡중앙8로1길 50), (ex : 세종특별자치시 소정면 넉바위길 82)
    			//B : SiDo + SiGunGu + SiGunGu(EupMyeon) + Road + Detail  		(ex : 충청북도 청주시 흥덕구 1순환로512번길 32)
    			//C : SiDo + SiGunGu + SiGunGu + EupMyeon + Road + Detail		(ex : 충청북도 청주시 상당구 미원면 용곡구방로 817)
    			//D : SiDo + Road + Detail										(ex : 세종특별자치시 고운북1길 9)
    			
    			if(tempAddr.length > 3){
    				if(tempAddr[0].startsWith("강원")){
    					map.put("SiDo", "강원도");
    				}else if(tempAddr[0].startsWith("경기")){
    					map.put("SiDo", "경기도");
    				}else if(tempAddr[0].equals("경남")){
    					map.put("SiDo", "경상남도");
    				}else if(tempAddr[0].equals("경북")){
    					map.put("SiDo", "경상북도");
    				}else if(tempAddr[0].equals("전남")){
    					map.put("SiDo", "전라남도");
    				}else if(tempAddr[0].equals("전북")){
    					map.put("SiDo", "전라북도");
    				}else if(tempAddr[0].startsWith("제주")){
    					map.put("SiDo", "제주특별자치도");
    				}else if(tempAddr[0].equals("충남")){
    					map.put("SiDo", "충청남도");
    				}else if(tempAddr[0].equals("충북")){
    					map.put("SiDo", "충청북도");
    				}else if(tempAddr[0].lastIndexOf("도") > 0){
    					map.put("SiDo", tempAddr[0]);
    				}else if(tempAddr[0].startsWith("광주")){
    					map.put("SiDo", "광주광역시");
    				}else if(tempAddr[0].startsWith("대구")){
    					map.put("SiDo", "대구광역시");
    				}else if(tempAddr[0].startsWith("대전")){
    					map.put("SiDo", "대전광역시");
    				}else if(tempAddr[0].startsWith("부산")){
    					map.put("SiDo", "부산광역시");
    				}else if(tempAddr[0].startsWith("서울")){
    					map.put("SiDo", "서울특별시");
    				}else if(tempAddr[0].startsWith("세종")){
    					map.put("SiDo", "세종특별자치시");
    				}else if(tempAddr[0].startsWith("울산")){
    					map.put("SiDo", "울산광역시");
    				}else if(tempAddr[0].startsWith("인천")){
    					map.put("SiDo", "인천광역시");
    				}
    				
    				if(tempAddr[1].lastIndexOf("시") == (tempAddr[1].length()-1) || tempAddr[1].lastIndexOf("군") == (tempAddr[1].length()-1) || tempAddr[1].lastIndexOf("구") == (tempAddr[1].length()-1)){
    					map.put("SiGunGu", tempAddr[1]);
    				}else if(tempAddr[1].lastIndexOf("읍") == (tempAddr[1].length()-1) || tempAddr[1].lastIndexOf("면") == (tempAddr[1].length()-1)){
    					map.put("EupMyeon", tempAddr[1]);
    					if(tempAddr[2].lastIndexOf("길") == (tempAddr[2].length()-1) || tempAddr[2].lastIndexOf("로") == (tempAddr[2].length()-1) || tempAddr[2].lastIndexOf("리") == (tempAddr[2].length()-1) || tempAddr[2].lastIndexOf("개") == (tempAddr[2].length()-1)){
    						map.put("Road", tempAddr[2]);
    					}
    				}else if(tempAddr[1].lastIndexOf("길") == (tempAddr[1].length()-1) || tempAddr[1].lastIndexOf("로") == (tempAddr[1].length()-1) || tempAddr[1].lastIndexOf("리") == (tempAddr[1].length()-1) || tempAddr[1].lastIndexOf("개") == (tempAddr[1].length()-1)){
    					addrType = "D";
    					map.put("Road", tempAddr[1]);
    				} else if(tempAddr[1].lastIndexOf("동") == (tempAddr[1].length()-1) || tempAddr[1].lastIndexOf("가") == (tempAddr[1].length()-1) || tempAddr[1].lastIndexOf("로") == (tempAddr[1].length()-1)){
    					addrType = "D";
    					map.put("CourtDong", tempAddr[1]);
    				}
    				
    				if(tempAddr[2].lastIndexOf("길") == (tempAddr[2].length()-1) || tempAddr[2].lastIndexOf("로") == (tempAddr[2].length()-1) || tempAddr[2].lastIndexOf("리") == (tempAddr[2].length()-1) || tempAddr[2].lastIndexOf("개") == (tempAddr[2].length()-1)){
    					map.put("Road", tempAddr[2]);
    				} else if(tempAddr[2].lastIndexOf("동") == (tempAddr[2].length()-1) || tempAddr[2].lastIndexOf("가")  == (tempAddr[2].length()-1) || tempAddr[2].lastIndexOf("로")  == (tempAddr[2].length()-1)){
    					map.put("CourtDong", tempAddr[2]);
    				} else if(tempAddr[2].lastIndexOf("읍") == (tempAddr[2].length()-1) || tempAddr[2].lastIndexOf("면") == (tempAddr[2].length()-1)){
    					addrType = "B";
    					map.put("EupMyeon", tempAddr[2]);
    					if(tempAddr[3].lastIndexOf("길") == (tempAddr[3].length()-1) || tempAddr[3].lastIndexOf("로") == (tempAddr[3].length()-1) || tempAddr[3].lastIndexOf("리") == (tempAddr[3].length()-1) || tempAddr[3].lastIndexOf("개") == (tempAddr[3].length()-1)){
    						map.put("Road", tempAddr[3]);
    					}
    				} else if(tempAddr[2].lastIndexOf("구") == (tempAddr[2].length()-1)){
    					addrType = "B";
    					map.put("SiGunGu", Util.null2str(map.get("SiGunGu")) + " " + tempAddr[2]);
    					if(tempAddr[3].lastIndexOf("길") == (tempAddr[3].length()-1) || tempAddr[3].lastIndexOf("로") == (tempAddr[3].length()-1) || tempAddr[3].lastIndexOf("리") == (tempAddr[3].length()-1) || tempAddr[3].lastIndexOf("개") == (tempAddr[3].length()-1)){
    						map.put("Road", tempAddr[3]);
    					} else if(tempAddr[3].lastIndexOf("동") == (tempAddr[3].length()-1) || tempAddr[3].lastIndexOf("가") == (tempAddr[3].length()-1) || tempAddr[3].lastIndexOf("로") == (tempAddr[3].length()-1)){
    						map.put("CourtDong", tempAddr[3]);
    					} else if(tempAddr[3].lastIndexOf("읍") == (tempAddr[3].length()-1) || tempAddr[3].lastIndexOf("면") == (tempAddr[3].length()-1)){
    						addrType = "C";
    						map.put("EupMyeon", tempAddr[3]);
    						if(tempAddr.length > 4){
    							if(tempAddr[4].lastIndexOf("길") == (tempAddr[4].length()-1) || tempAddr[4].lastIndexOf("로") == (tempAddr[4].length()-1) || tempAddr[4].lastIndexOf("리") == (tempAddr[4].length()-1) || tempAddr[4].lastIndexOf("개") == (tempAddr[4].length()-1)){
    								map.put("Road", tempAddr[4]);
    							}
    						}else{
    							map.put("Road", "");
    						}
    					}
    				}
    				
    				if("A".equals(addrType)){
    					matcher = pattern.matcher(tempAddr[3]);
    					
    					if(matcher.find()){
    						tempBuildingNo = matcher.group().split("-");
    						if(!"".equals(Util.null2str(map.get("Road")))){
    							if(tempBuildingNo.length == 2){
    								map.put("BuildingMainNo", tempBuildingNo[0]);
    								map.put("BuildingSubNo", tempBuildingNo[1]);
    							}else{
    								map.put("BuildingMainNo", tempBuildingNo[0]);
    								map.put("BuildingSubNo", "0");
    							}
    						}else if(!"".equals(Util.null2str(map.get("CourtDong")))){
    							if(tempBuildingNo.length == 2){
    								map.put("AddrMainNo", tempBuildingNo[0]);
    								map.put("AddrSubNo", tempBuildingNo[1]);
    							}else{
    								map.put("AddrMainNo", tempBuildingNo[0]);
    								map.put("AddrSubNo", "0");
    							}
    						}
    					} 
    				} else if("B".equals(addrType)){
    					if(tempAddr.length > 4){
    						matcher = pattern.matcher(tempAddr[4]);
        					
        					if(matcher.find()){
        						tempBuildingNo = matcher.group().split("-");
        						if(!"".equals(Util.null2str(map.get("EupMyeon")))){
        							if(tempBuildingNo.length == 2){
        								map.put("BuildingMainNo", tempBuildingNo[0]);
        								map.put("BuildingSubNo", tempBuildingNo[1]);
        							}else{
        								map.put("BuildingMainNo", tempBuildingNo[0]);
        								map.put("BuildingSubNo", "0");
        							}
        						}else if(!"".equals(Util.null2str(map.get("Road")))){
        							if(tempBuildingNo.length == 2){
        								map.put("BuildingMainNo", tempBuildingNo[0]);
        								map.put("BuildingSubNo", tempBuildingNo[1]);
        							}else{
        								map.put("BuildingMainNo", tempBuildingNo[0]);
        								map.put("BuildingSubNo", "0");
        							}
        						}else if(!"".equals(Util.null2str(map.get("CourtDong")))){
        							if(tempBuildingNo.length == 2){
        								map.put("AddrMainNo", tempBuildingNo[0]);
        								map.put("AddrSubNo", tempBuildingNo[1]);
        							}else{
        								map.put("AddrMainNo", tempBuildingNo[0]);
        								map.put("AddrSubNo", "0");
        							}
        						}
        					}
    					}else{
    						map.put("BuildingMainNo", "");
    						map.put("BuildingSubNo", "");
    						map.put("AddrMainNo", "");
    						map.put("AddrSubNo", "");
    					}
    				} else if("C".equals(addrType)){
    					if(tempAddr.length > 5){
	    					matcher = pattern.matcher(tempAddr[5]);
	    					
	    					if(matcher.find()){
	    						tempBuildingNo = matcher.group().split("-");
	    						if(!"".equals(Util.null2str(map.get("EupMyeon")))){
	    							if(tempBuildingNo.length == 2){
	    								map.put("BuildingMainNo", tempBuildingNo[0]);
	    								map.put("BuildingSubNo", tempBuildingNo[1]);
	    							}else{
	    								map.put("BuildingMainNo", tempBuildingNo[0]);
	    								map.put("BuildingSubNo", "0");
	    							}
	    						}
	    					}
    					}else{
    						map.put("BuildingMainNo", "");
    						map.put("BuildingSubNo", "");
    					}
    				} else if("D".equals(addrType)){
    					matcher = pattern.matcher(tempAddr[2]);
    					
    					if(matcher.find()){
    						tempBuildingNo = matcher.group().split("-");
    						if(!"".equals(Util.null2str(map.get("Road")))){
    							if(tempBuildingNo.length == 2){
    								map.put("BuildingMainNo", tempBuildingNo[0]);
    								map.put("BuildingSubNo", tempBuildingNo[1]);
    							}else{
    								map.put("BuildingMainNo", tempBuildingNo[0]);
    								map.put("BuildingSubNo", "0");
    							}
    						}else if(!"".equals(Util.null2str(map.get("CourtDong")))){
    							if(tempBuildingNo.length == 2){
    								map.put("AddrMainNo", tempBuildingNo[0]);
    								map.put("AddrSubNo", tempBuildingNo[1]);
    							}else{
    								map.put("AddrMainNo", tempBuildingNo[0]);
    								map.put("AddrSubNo", "0");
    							}
    						}
    					}
    				}
    			}
    		}
            return map;
        } catch(Exception e){
            e.printStackTrace(System.out);
            map.put("Error", e.getMessage());
            return map;
        }
    }
}
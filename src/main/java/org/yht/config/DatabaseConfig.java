package org.yht.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.yht.config.routerdatasource.MyRoutingDataSource;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.util.Util;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DatabaseConfig {
	
	@Autowired
	CustomProperties customProperties;
	
	@Bean 
	public DataSource createRouterDatasource() {
		AbstractRoutingDataSource routingDataSource = new MyRoutingDataSource();
		Map<Object, Object> targetDataSources = new HashMap<>();

		//주소 검증 dev 와 prod을 분리 하기 위해서 추가..2022-06-14(hyuk)
		String profile = System.getProperty("spring.profiles.active");
		
		targetDataSources.put("current:SEJONG", 
				createDataSource(customProperties.getDbUrlSejong() + "****", "****", "****", 3));		


		if(profile != null && !profile.contains("bms") && !profile.contains("ups") && !profile.contains("pantos")){
		targetDataSources.put("current:KOREX", 
				createDataSourceOracle(customProperties.getDbUrlKorex() , "****", "****", 1));
		
		if(Util.null2str(customProperties.getDbUrlKorexadd2()).equals("test")){
			targetDataSources.put("current:KOREXADD", 									  
					createDataSourceOracle(customProperties.getDbUrlKorexadd1() , "****", "****", 1));
		}else if(!Util.null2str(customProperties.getDbUrlKorexadd2()).equals("notUse")){
			int korexaddPoolSize = 1;
			if("prod5".equals(profile)){
				korexaddPoolSize = 2;
			}
			targetDataSources.put("current:KOREXADD", 
					createDataSourceOracle("****"
											+ "****"
											+ "****"+customProperties.getDbUrlKorexadd1()+"****"+customProperties.getDbUrlKorexadd2()+"****"
											+ "****", "****", "****", korexaddPoolSize));
		}
		}	
		
//		/** KOREX 테스트중 리얼DB접속안되도록 임시처리 */
//		targetDataSources.put("current:KOREX", 
//				createDataSourceOracle("****" , "****", "****"));
//		targetDataSources.put("current:KOREXADD", 									  
//				createDataSourceOracle("****" , "****", "****"));
		
		
		/** HANJIN 테스트중 리얼DB접속안되도록 임시처리 */ /** bms는 HANJIN 개발DB 연결안함 */
		if(profile != null && !profile.contains("bms") && !profile.contains("ups") && !profile.contains("pantos")){
			targetDataSources.put("current:HANJIN", 
					createDataSourceOracle("****", "****", "****", 1));
//			targetDataSources.put("current:HANJIN", 
//			createDataSourceOracle(customProperties.getDbUrlHanjin(), "****", "****"));	 // 운영 : ebbeip.hjt.co.kr	 , 개발 : ebbeid.hjt.co.kr
		}
		
		/** bms, ups XRAY이미지조회 db설정 */
		if(!Util.null2str(customProperties.getDbUrlXray()).equals("notUse")){
			String username = "exps";
			if(profile.contains("ups")) {
				username = "yuhantecwms";
			}
			targetDataSources.put("current:XRAY",
					createDataSource(customProperties.getDbUrlXray() + "****", "****", "****"));
		}

        /** bms, hubnet CWHP db 설정 */
        if(profile != null && !profile.contains("bms") && !profile.contains("ups") && !profile.contains("pantos")) {
            if (!Util.null2str(customProperties.getDbUrlCwhp()).equals("notUse")) {
                targetDataSources.put("current:CWHP",
                        createDataSource(customProperties.getDbUrlCwhp() + "****", "****", "****"));
                targetDataSources.put("current:CWHPS",
                        createDataSource(customProperties.getDbUrlCwhp() + "****", "****", "****"));
            }
        }

//		/** UPS 뉴빌링 */
//		if("ups1".equals(profile) || "local".equals(profile)){
//			targetDataSources.put("current:NEWBILLING",
//					createDataSource(customProperties.getDbUrlNewbilling() + "****", "****", customProperties.getDbPwdNewbilling()));
//		}
		
		
		targetDataSources.put("current:MASTER",
				createDataSource(customProperties.getDbUrlMaster() + "****", "****", "****"));
		if("notUse".equals(customProperties.getDbUrlBase2())){
			targetDataSources.put("current:MASTER_DB2",
					createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
		}
		for(String ent : customProperties.getEntDbList()) {
			if(ent.equals("YHTXX")){
				targetDataSources.put("current:YHTXX",
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("ENHAK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("DGCLK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("HANPK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("RUDDK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("GEMGK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("ACGAK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("MLLOK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("MACXK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("DSKRK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("MIDMK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("TOASK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("WLEXK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("ACEIK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("PLSAK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("HARFK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("PATKK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("TYGCK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("TPCNK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("HDLCK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("DW24K")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("TDGLK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("GCSTK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("MEXGK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("IVYLK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("SKYAK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("LSOLK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("HSASK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("WINLK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("DATAK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("SRLCK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("ABELK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("BOKLK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("KYKYK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("GLBXK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("YSTTK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("BONLK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("DSTCK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("ZHCLK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("ARAXK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("HKLXK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("ABCXK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("MENTK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("VCSZK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("GSSLK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("HJICK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("KSPLK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("GNKLK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("TBINK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("TFMTK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("GEM1K")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("BROSK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("UPSXK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("DHLLK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("PANTK")){
				// bms test 와 pantos 운영에 따라서 db정보가 다름.
				if(profile != null && profile.contains("pantos")) {
					targetDataSources.put("current:" + ent, 
							createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
				}else {
					targetDataSources.put("current:" + ent, 
							createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
				}
			}else if(ent.equals("PANTK_BACK")){
				// bms test 와 pantos 운영에 따라서 db정보가 다름.
				if(profile != null && profile.contains("pantos")) {
					targetDataSources.put("current:" + ent, 
							createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
				}else {
					targetDataSources.put("current:" + ent, 
							createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
				}
			}else if(ent.equals("HUBNK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("UNIKK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("WCBTK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("SNTLK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("DTLPK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("BAROM")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("KRLIK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("ULKCK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("TINXK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("HWSMK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("ALGTK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("SELSK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("JJSHK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("PWLTK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("ENODK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("KMTCK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("AEGSK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("SLLSK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("JDKRK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("DEOUK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("DTLCK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("BRCHK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("AGETK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("JSCGK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("ENGLK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("GTCAK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("DSGSK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("SNKGK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("KEMOK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("PAT1K")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("CRLXK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("KKTHK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("TNDLK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("YMLSK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("ZNTLK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("AYEXK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("BBARK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("CNKAK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("GEXSK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("ANSGK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("STTDK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("OCSKK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
			}else if(ent.equals("RDSUN")){
                targetDataSources.put("current:" + ent,
                        createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
            }else if(ent.equals("YEKRK")){
                targetDataSources.put("current:" + ent,
                        createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
            }else if(ent.equals("COSGK")){
                targetDataSources.put("current:" + ent,
                        createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
            }else if(ent.equals("UNUNK")){
                targetDataSources.put("current:" + ent,
                        createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
            }else if(ent.equals("KYOPK")){
                targetDataSources.put("current:" + ent,
                        createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
            }else if(ent.equals("TPC1K")){
                targetDataSources.put("current:" + ent,
                        createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
            }else if(ent.equals("GLOBK")){
                targetDataSources.put("current:" + ent,
                        createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
            }else if(ent.equals("ONXSK")){
                targetDataSources.put("current:" + ent,
                        createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));
            }
		
			
		/********************* 미사용업체 *********************
			else if(ent.equals("PWLTK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("RDSUN")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));
			}else if(ent.equals("GDSPK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));	// 2023.10.06
			}else if(ent.equals("REDFK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));	// 2023.10.06
			}else if(ent.equals("SMATK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));	// 2024.01.19
			}else if(ent.equals("NFLLK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));	// 2024.01.19
			}else if(ent.equals("USQEK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));	// 2024.03.15
			}else if(ent.equals("KCMLK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));	// 2024.03.15
			}else if(ent.equals("CIONK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));	// 2024.12.24
			}else if(ent.equals("BDGEK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));	// 2024.12.24
			}else if(ent.equals("JYLIK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****"));	// 2024.12.24
			}else if(ent.equals("SAYEK")){
				targetDataSources.put("current:" + ent,
						createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****"));	// 2024.12.24
			}
			else if(ent.equals("TAPIK")){
				targetDataSources.put("current:" + ent, 
						createDataSource(customProperties.getDbUrlBase() + "****", "****", "****")); // 2026.04.28
			}
			
			else if(ent.equals("MIKEK")){
                targetDataSources.put("current:" + ent,
                        createDataSource(customProperties.getDbUrlBase2() + "****", "****", "****")); // 2026.04.28
            }
		 *************************************************/
		}
		
		
		routingDataSource.setTargetDataSources(targetDataSources);
		
		return routingDataSource;
	}
	
	private HikariDataSource createDataSource(String url, String user, String password) {
		return createDataSource(url, "****", "****", 20);
	}
	private HikariDataSource createDataSource(String url, String user, String password, int maximumPoolSize) {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setDriverClassName("net.sf.log4jdbc.sql.jdbcapi.DriverSpy");
		dataSource.setJdbcUrl("jdbc:log4jdbc:sqlserver://" + url);
		
		dataSource.setUsername(user);
		dataSource.setPassword(password);
		
		dataSource.setPoolName("pool-g1logis");
		dataSource.setMaximumPoolSize(maximumPoolSize);
		dataSource.setMaxLifetime(1800000);

		Properties dsProperties = new Properties();
		dsProperties.setProperty("cachePrepStmts", "true");
		dsProperties.setProperty("prepStmtCacheSize", "250");
		dsProperties.setProperty("prepStmtCacheSqlLimit", "2048");
		dsProperties.setProperty("useServerPrepStmts", "true");
		dataSource.setDataSourceProperties(dsProperties);

		return dataSource;
	}
	
	private HikariDataSource createDataSourceOracle(String url, String user, String password, int maximumPoolSize) { 
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setDriverClassName("net.sf.log4jdbc.sql.jdbcapi.DriverSpy");
		dataSource.setJdbcUrl("jdbc:log4jdbc:oracle:thin:@" + url); 
 
		dataSource.setUsername(user);
		dataSource.setPassword(password);
		
		
		//dataSource.setIdleTimeout(10000);
		//dataSource.setPoolName("pool-g1logis");
		dataSource.setMaximumPoolSize(maximumPoolSize);
		dataSource.setMinimumIdle(3);
		dataSource.setMaxLifetime(100000); 
		//dataSource.setValidationTimeout(3000); // 커넥션이 유효한지 검사할 때 대기 시간(5000 = 5초)
		//dataSource.setConnectionTestQuery("SELECT 1 FROM DUAL"); // 오라클 연결시 커넥션 끊어지는 문제로 인해 커넥션이 유효한지 검사할 때 사용할 쿼리를 지정한다. 
		//dataSource.setConnectionTimeout(10000);
		
		
		Properties dsProperties = new Properties();
		dsProperties.setProperty("cachePrepStmts", "true");
		dsProperties.setProperty("prepStmtCacheSize", "250");
		dsProperties.setProperty("prepStmtCacheSqlLimit", "2048");
		dsProperties.setProperty("useServerPrepStmts", "true");
		dataSource.setDataSourceProperties(dsProperties);
		
		return dataSource;
	}	
}

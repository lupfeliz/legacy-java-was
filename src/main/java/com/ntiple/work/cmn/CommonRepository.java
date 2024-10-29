/**
 * @File        : CommonRepository.java
 * @Author      : 정재백
 * @Since       : 2024-10-29
 * @Description : 공통 sqlmap
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.work.cmn;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ntiple.work.cmn.CommonEntity.CmmnFile;
import com.ntiple.work.cmn.CommonEntity.Code;

@Mapper
public interface CommonRepository {

  /** cl_cd, cd, sn, cd_nm 4개 칼럼만 조회 */
  List<Code> findCode(@Param("prm") Object prm);

  /** 전체 칼럼 조회 */
  List<Code> findCodeDetail(@Param("prm") Object prm);

  /** 코드 저장 */
  Integer saveCode(@Param("prm") Object prm);

  /** 코드 수정 */
  Integer updateCode(@Param("prm") Object prm, @Param("clCd") String clCd, @Param("cd") String cd);

  /** 공통파일조회 */
  CmmnFile getCmmnFile(@Param("prm") Object prm);

  /** 공통파일검색 */
  List<CmmnFile> findCmmnFile(@Param("prm") CmmnFile prm);

  /** 공통파일저장 */
  Integer saveCmmnFile(@Param("prm") Object prm);

  Integer incCmmnFileSn();

  /** 공통파일수정 */
  Integer updateCmmnFile(@Param("prm") Object prm);

  /** 공통파일삭제 */
  Integer deleteCmmnFile(@Param("prm") Object prm);

  /** DB암호화 */
  String dbEncrypt(@Param("value") String value, @Param("cipher") String cipher, @Param("secret") String secret, @Param("encode") String encode, @Param("charset") String charset);

  /** DB복호화 */
  String dbDecrypt(@Param("value") String value, @Param("cipher") String cipher, @Param("secret") String secret, @Param("encode") String encode, @Param("charset") String charset);

  /** DB현재시간 */
  Date dbCurrent();
}

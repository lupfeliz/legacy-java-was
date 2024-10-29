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

import com.ntiple.work.cmn.CommonEntity.Code;

@Mapper
public interface CommonRepository {

  /** cl_cd, cd, sn, cd_nm 4개 칼럼만 조회 */
  List<Code> findCode(@Param("prm") Object prm);

  /** 전체 칼럼 조회 */
  List<Code> findCodeDetail(@Param("prm") Object prm);

  /** DB암호화 */
  String dbEncrypt(@Param("value") String value, @Param("cipher") String cipher, @Param("secret") String secret, @Param("encode") String encode, @Param("charset") String charset);

  /** DB복호화 */
  String dbDecrypt(@Param("value") String value, @Param("cipher") String cipher, @Param("secret") String secret, @Param("encode") String encode, @Param("charset") String charset);

  /** DB현재시간 */
  Date dbCurrent();
}

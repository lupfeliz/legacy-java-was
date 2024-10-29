/**
 * @File        : SystemRepository.java
 * @Author      : 정재백
 * @Since       : 2024-10-29
 * @Description : 관리자 공통 sqlmap
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.work.sys;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ntiple.work.cmn.CommonEntity.Group;
import com.ntiple.work.cmn.CommonEntity.GroupUser;
import com.ntiple.work.cmn.CommonEntity.Menu;
import com.ntiple.work.cmn.CommonEntity.MenuAuthor;
import com.ntiple.work.cmn.CommonEntity.Mngr;

@Mapper
public interface SystemRepository {

  Integer countMngr(@Param("prm") Object prm);
  List<Mngr> findMngr(@Param("prm") Object prm);
  Mngr getMngr(@Param("prm") Object prm);
  Integer saveMngr(@Param("prm") Object prm);
  Integer updateMngr(@Param("prm") Object prm, @Param("mngrId") String mngrId);
  Integer deleteMngr(@Param("prm") Object prm);

  Integer countMenu(@Param("prm") Object prm);
  List<Menu> findMenu(@Param("prm") Object prm);
  Menu getMenu(@Param("prm") Object prm);
  Integer saveMenu(@Param("prm") Object prm);
  Integer updateMenu(@Param("prm") Object prm, @Param("menuSn") Integer menuSn);
  Integer deleteMenu(@Param("prm") Object prm);

  List<MenuAuthor> findMenuAuthor(@Param("prm") Object prm);
  MenuAuthor getMenuAuthor(@Param("prm") Object prm);
  Integer saveMenuAuthor(@Param("prm") Object prm);
  Integer updateMenuAuthor(@Param("prm") Object prm, @Param("menuSn") Integer menuSn, @Param("groupSn") Integer groupSn);
  Integer deleteMenuAuthor(@Param("prm") Object prm);

  Integer countGroup(@Param("prm") Object prm);
  List<Group> findGroup(@Param("prm") Object prm);
  Group getGroup(@Param("prm") Object prm);
  Integer saveGroup(@Param("prm") Object prm);
  Integer updateGroup(@Param("prm") Object prm, @Param("groupSn") Integer groupSn);
  Integer deleteGroup(@Param("prm") Object prm);

  Integer countGroupUser(@Param("prm") Object prm);
  List<GroupUser> findGroupUser(@Param("prm") Object prm);
  GroupUser getGroupUser(@Param("prm") Object prm);
  Integer saveGroupUser(@Param("prm") Object prm);
  Integer updateGroupUser(@Param("prm") Object prm, @Param("groupSn") Integer groupSn, @Param("mngrId") String mngrId);
  Integer deleteGroupUser(@Param("prm") Object prm);

  public Integer saveMngrLoginHist(@Param("prm") Object prm);

  Integer curMenuSn();
  Integer incMenuSn();

  Integer curGroupSn();
  Integer incGroupSn();

  Integer curSeqMngrLoginSn();
  Integer incSeqMngrLoginSn();
}

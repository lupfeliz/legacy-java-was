<%!
/**
 * @File        : dialog-container.jsp
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 단순대화창 컨테이너
 * @Site        : https://devlog.ntiple.com
 **/
%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<div
  ref="modal"
  class="modal fade no-tran com-dialog"
  data-bs-backdrop="static"
  data-bs-keyboard="false"
  tabindex="-1"
  aria-labelledby="staticBackdropLabel"
  aria-hidden="true"
  >
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-body">
        <p v-html="ctx.modal.current?.msg"></p>
        <div class="text-center">
          <template v-if="ctx.modal.current?.type === C.ALERT">
            <Button
              class="btn btn-primary"
              @click="click(1)"
              >
              확인
            </Button>
          </template>
          <template v-if="ctx.modal.current?.type === C.CONFIRM">
            <Button
              class="btn btn-primary mx-1"
              @click="click(1)"
              >
              확인
            </Button>
            <Button
              class="btn btn-secondary mx-1"
              @click="click(2)"
              >
              취소
            </Button>
          </template>
        </div>
      </div>
    </div>
  </div>
</div>
<div
  ref="progress"
  class="modal fade no-tran com-progress"
  data-bs-backdrop="static"
  data-bs-keyboard="false"
  tabindex="-1"
  aria-hidden="true"
  >
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="spinner-border" role="status">
        <span class="visually-hidden"></span>
      </div>
      <div>
        잠시만 기다려 주세요...
      </div>
    </div>
  </div>
</div>
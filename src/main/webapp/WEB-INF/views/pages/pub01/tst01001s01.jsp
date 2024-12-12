<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<page:ex>
<div>
OK
</div>
</page:ex>
<script:ex>
let cache = LRUCache({
  max: 500,
  maxAge: 1000 * 60 * 60,
  length(n, key) {
    return 1;
  },
  dispose(key, n) {
  }
});
cache.set("key1", "value1", 1000 * 30);
log.debug("CACHE:", cache.keys());
</script:ex>